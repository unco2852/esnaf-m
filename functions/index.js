const admin = require("firebase-admin");
const functionsV1 = require("firebase-functions/v1");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");

admin.initializeApp();

const db = admin.firestore();
const APPROVAL_WINDOW_MS = 30 * 60 * 1000;
const CHAT_RETENTION_MS = 30 * 24 * 60 * 60 * 1000;

async function deleteQueryInPages(query, onPage = async () => {}) {
  while (true) {
    const page = await query.limit(400).get();
    if (page.empty) return;
    await onPage(page.docs);
    const batch = db.batch();
    page.docs.forEach((document) => batch.delete(document.ref));
    await batch.commit();
  }
}

function toMillis(value) {
  if (typeof value === "number" && Number.isFinite(value)) return value;
  if (value && typeof value.toMillis === "function") return value.toMillis();
  return 0;
}

function messageCustomerId(message) {
  return String(message.customerId || message.senderId || "");
}

async function findLatestConversationMessage(merchantId, customerId) {
  const messages = db.collection("merchant_messages");
  const indexedPage = await messages
    .where("merchantId", "==", merchantId)
    .where("customerId", "==", customerId)
    .orderBy("createdAt", "desc")
    .limit(1)
    .get();
  if (!indexedPage.empty) return indexedPage.docs[0];

  // Older messages did not always contain customerId; for those, senderId is the customer UID.
  let cursor = null;
  while (true) {
    let query = messages
      .where("merchantId", "==", merchantId)
      .orderBy("createdAt", "desc")
      .limit(400);
    if (cursor) query = query.startAfter(cursor);
    const page = await query.get();
    if (page.empty) return null;
    const match = page.docs.find((document) => messageCustomerId(document.data()) === customerId);
    if (match) return match;
    cursor = page.docs[page.docs.length - 1];
  }
}

async function deleteMerchantConversationData(merchantId, customerId, conversationRef) {
  const bucket = admin.storage().bucket();
  const attachmentPrefix = `merchant_chats/${merchantId}/${customerId}/`;
  const messageCollection = db.collection("merchant_messages");
  let cursor = null;

  while (true) {
    let query = messageCollection
      .where("merchantId", "==", merchantId)
      .orderBy(admin.firestore.FieldPath.documentId())
      .limit(400)
    if (cursor) query = query.startAfter(cursor);
    const page = await query.get();
    if (page.empty) break;
    cursor = page.docs[page.docs.length - 1];
    const conversationMessages = page.docs.filter((document) =>
      messageCustomerId(document.data()) === customerId
    );
    if (conversationMessages.length === 0) continue;

    const attachmentPaths = [...new Set(conversationMessages
      .map((document) => String(document.get("attachmentPath") || ""))
      .filter((path) => path.startsWith(attachmentPrefix)))];
    await Promise.all(attachmentPaths.map((path) =>
      bucket.file(path).delete({ ignoreNotFound: true })
    ));

    const batch = db.batch();
    conversationMessages.forEach((document) => batch.delete(document.ref));
    await batch.commit();
  }

  await conversationRef.delete();
}

async function removeDeletedAccountData(uid) {
  const bucket = admin.storage().bucket();
  const userRef = db.collection("users").doc(uid);

  await deleteQueryInPages(userRef.collection("addresses"));
  await deleteQueryInPages(userRef.collection("favorites"));
  await deleteQueryInPages(userRef.collection("notification_tokens"));
  await userRef.collection("settings").doc("notifications").delete().catch(() => undefined);

  const attachmentPaths = new Set();
  const messageQueries = [
    db.collection("merchant_messages").where("merchantId", "==", uid),
    db.collection("merchant_messages").where("customerId", "==", uid),
    db.collection("merchant_messages").where("senderId", "==", uid)
  ];
  for (const query of messageQueries) {
    await deleteQueryInPages(query, async (documents) => {
      for (const document of documents) {
        const attachmentPath = document.get("attachmentPath");
        if (typeof attachmentPath === "string" && attachmentPath.startsWith("merchant_chats/")) {
          attachmentPaths.add(attachmentPath);
        }
      }
    });
  }
  await Promise.all([...attachmentPaths].map((path) => bucket.file(path).delete({ ignoreNotFound: true }).catch(() => undefined)));

  const conversationQueries = [
    db.collection("merchant_conversations").where("merchantId", "==", uid),
    db.collection("merchant_conversations").where("customerId", "==", uid)
  ];
  for (const query of conversationQueries) await deleteQueryInPages(query);

  for (const collectionName of ["items", "services", "support_tickets"]) {
    const ownerField = collectionName === "support_tickets" ? "uid" : "merchantId";
    await deleteQueryInPages(db.collection(collectionName).where(ownerField, "==", uid));
  }

  const appointmentQueries = [
    db.collection("appointments").where("customerId", "==", uid),
    db.collection("appointments").where("merchantId", "==", uid)
  ];
  for (const query of appointmentQueries) {
    await deleteQueryInPages(query, async (documents) => {
      const slotRefs = documents.map((appointment) => {
        const merchantId = appointment.get("merchantId");
        const dateKey = appointment.get("slotDateKey");
        const timeKey = appointment.get("slotTimeKey");
        if (![merchantId, dateKey, timeKey].every((value) => typeof value === "string" && value.length > 0)) return null;
        return db.collection("appointment_slots").doc(merchantId)
          .collection("days").doc(dateKey).collection("times").doc(timeKey);
      }).filter(Boolean);
      const batch = db.batch();
      slotRefs.forEach((ref) => batch.delete(ref));
      if (slotRefs.length > 0) await batch.commit();
    });
  }

  const customerOrders = db.collection("orders").where("customerId", "==", uid);
  await deleteQueryInPages(customerOrders, async (documents) => {
    for (const orderDocument of documents) {
      const order = orderDocument.data();
      if (order.status !== "pending") continue;
      const itemId = String(order.itemId || "");
      const quantity = Number(order.quantity || 0);
      if (!itemId || !Number.isInteger(quantity) || quantity <= 0) continue;
      const itemRef = db.collection("items").doc(itemId);
      await db.runTransaction(async (transaction) => {
        const latestOrder = await transaction.get(orderDocument.ref);
        if (!latestOrder.exists || latestOrder.get("status") !== "pending") return;
        const itemSnapshot = await transaction.get(itemRef);
        if (itemSnapshot.exists) {
          transaction.update(itemRef, "stock", Number(itemSnapshot.get("stock") || 0) + quantity);
        }
      });
    }
  });

  await deleteQueryInPages(db.collection("orders").where("merchantId", "==", uid));
  await Promise.all([
    bucket.deleteFiles({ prefix: `product_images/${uid}/` }).catch(() => undefined),
    bucket.deleteFiles({ prefix: `service_images/${uid}/` }).catch(() => undefined),
    bucket.deleteFiles({ prefix: `profile_images/${uid}/` }).catch(() => undefined),
    bucket.deleteFiles({ prefix: `business_logos/${uid}/` }).catch(() => undefined),
    bucket.deleteFiles({ prefix: `merchant_chats/${uid}/` }).catch(() => undefined)
  ]);

  await db.collection("public_merchants").doc(uid).delete().catch(() => undefined);
  await userRef.delete().catch(() => undefined);
}

/** Deletes profile, listings, private messages and related app data after Auth deletion. */
exports.cleanupDeletedAccount = functionsV1
  .runWith({ timeoutSeconds: 540, memory: "1GB" })
  .auth.user()
  .onDelete(async (user) => {
  if (!user.uid) return;
  await removeDeletedAccountData(user.uid);
  });

async function sendUserNotification(uid, category, title, body) {
  if (!uid) return;
  const userRef = db.collection("users").doc(uid);
  const [preferencesSnapshot, tokenSnapshot] = await Promise.all([
    userRef.collection("settings").doc("notifications").get(),
    userRef.collection("notification_tokens").get()
  ]);
  const preferences = preferencesSnapshot.data() || {};
  if (preferences.allNotifications === false || preferences[category] === false) return;

  const tokenDocuments = tokenSnapshot.docs.filter((document) => typeof document.get("token") === "string");
  if (tokenDocuments.length === 0) return;
  const staleTokenRefs = [];
  // Firebase accepts at most 500 registration tokens per multicast request.
  for (let offset = 0; offset < tokenDocuments.length; offset += 500) {
    const tokenBatch = tokenDocuments.slice(offset, offset + 500);
    const response = await admin.messaging().sendEachForMulticast({
      tokens: tokenBatch.map((document) => document.get("token")),
      notification: { title, body },
      data: { category }
    });
    response.responses.forEach((result, index) => {
      const code = result.error?.code;
      if (code === "messaging/registration-token-not-registered" || code === "messaging/invalid-registration-token") {
        staleTokenRefs.push(tokenBatch[index].ref);
      }
    });
  }
  for (let offset = 0; offset < staleTokenRefs.length; offset += 400) {
    const batch = db.batch();
    staleTokenRefs.slice(offset, offset + 400).forEach((ref) => batch.delete(ref));
    await batch.commit();
  }
}

exports.notifyNewAppointment = onDocumentCreated(
  { document: "appointments/{appointmentId}", region: "europe-west1" },
  async (event) => {
    const appointment = event.data?.data();
    if (!appointment) return;
    await sendUserNotification(
      String(appointment.merchantId || ""),
      "appointments",
      "Yeni randevu talebi",
      `${String(appointment.date || "")} ${String(appointment.time || "")} için yeni bir talep var.`
    );
  }
);

exports.notifyAppointmentUpdate = onDocumentUpdated(
  { document: "appointments/{appointmentId}", region: "europe-west1" },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after) return;
    if (before.status !== after.status || before.balancePaymentStatus !== after.balancePaymentStatus) {
      await sendUserNotification(
        String(after.customerId || ""),
        "appointments",
        "Randevu güncellendi",
        "Randevunuzun durumu değişti. Ayrıntılar için uygulamayı açın."
      );
    }
  }
);

exports.notifyNewOrder = onDocumentCreated(
  { document: "orders/{orderId}", region: "europe-west1" },
  async (event) => {
    const order = event.data?.data();
    if (!order) return;
    await sendUserNotification(
      String(order.merchantId || ""),
      "orders",
      "Yeni sipariş talebi",
      "İşletmenize yeni bir sipariş talebi geldi."
    );
  }
);

exports.notifyOrderUpdate = onDocumentUpdated(
  { document: "orders/{orderId}", region: "europe-west1" },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after || before.status === after.status) return;
    await sendUserNotification(
      String(after.customerId || ""),
      "orders",
      "Sipariş güncellendi",
      "Siparişinizin durumu değişti. Ayrıntılar için uygulamayı açın."
    );
  }
);

exports.notifyNewMessage = onDocumentCreated(
  { document: "merchant_messages/{messageId}", region: "europe-west1" },
  async (event) => {
    const message = event.data?.data();
    if (!message) return;
    const merchantId = String(message.merchantId || "");
    const customerId = String(message.customerId || "");
    const messageAt = toMillis(message.createdAt) || Date.now();
    if (merchantId && customerId) {
      const conversationRef = db.collection("merchant_conversations").doc(`${merchantId}--${customerId}`);
      await db.runTransaction(async (transaction) => {
        const conversation = await transaction.get(conversationRef);
        if (!conversation.exists || conversation.get("status") === "closed") return;
        if (messageAt > toMillis(conversation.get("lastMessageAt"))) {
          transaction.update(conversationRef, {
            lastMessageAt: messageAt,
            lastMessageId: event.params.messageId
          });
        }
      });
    }
    const recipientUid = message.senderId === message.customerId ? message.merchantId : message.customerId;
    await sendUserNotification(
      String(recipientUid || ""),
      "messages",
      "Yeni mesaj",
      "Esnaf sohbetinizde yeni bir mesajınız var."
    );
  }
);

/** Removes chat history and uploaded attachments after 30 days without a message. */
exports.purgeInactiveMerchantConversations = onSchedule(
  {
    schedule: "every day 04:15",
    timeZone: "Europe/Istanbul",
    region: "europe-west1",
    timeoutSeconds: 540,
    maxInstances: 1
  },
  async () => {
    const cutoff = Date.now() - CHAT_RETENTION_MS;
    const conversations = db.collection("merchant_conversations");
    let cursor = null;
    let scanned = 0;
    let deleted = 0;

    while (true) {
      let pageQuery = conversations
        .orderBy(admin.firestore.FieldPath.documentId())
        .limit(100);
      if (cursor) pageQuery = pageQuery.startAfter(cursor);
      const page = await pageQuery.get();
      if (page.empty) break;
      cursor = page.docs[page.docs.length - 1];

      for (const conversation of page.docs) {
        scanned += 1;
        const data = conversation.data();
        const merchantId = String(data.merchantId || "");
        const customerId = String(data.customerId || "");
        if (!merchantId || !customerId || conversation.id !== `${merchantId}--${customerId}`) continue;

        const latestMessage = await findLatestConversationMessage(merchantId, customerId);
        const latestMessageAt = latestMessage ? toMillis(latestMessage.get("createdAt")) : 0;
        const storedActivityAt = toMillis(data.lastMessageAt);
        const createdAt = toMillis(data.createdAt);
        const lastActivityAt = Math.max(latestMessageAt, storedActivityAt, createdAt);
        const isClosed = data.status === "closed";

        // Backfill older conversations and repair activity metadata before the retention check.
        if (!isClosed && latestMessageAt > storedActivityAt) {
          await conversation.ref.set({
            lastMessageAt: latestMessageAt,
            lastMessageId: latestMessage.id
          }, { merge: true });
        }
        if (!isClosed && lastActivityAt > cutoff) continue;

        const shouldDelete = await db.runTransaction(async (transaction) => {
          const latestConversation = await transaction.get(conversation.ref);
          if (!latestConversation.exists) return false;
          if (latestConversation.get("status") === "closed") return true;
          if (toMillis(latestConversation.get("lastMessageAt")) > cutoff) return false;
          transaction.update(conversation.ref, {
            status: "closed",
            closedAt: Date.now()
          });
          return true;
        });

        if (shouldDelete) {
          await deleteMerchantConversationData(merchantId, customerId, conversation.ref);
          deleted += 1;
        }
      }
    }

    console.log(`Scanned ${scanned} conversations and removed ${deleted} inactive chats.`);
  }
);

/**
 * Cancels unpaid/unaccepted order requests after 30 minutes. The card payment
 * state in this app is a demo only, so this records a demo refund and never
 * contacts PayTR or moves money.
 */
exports.expireUnapprovedOrders = onSchedule(
  {
    schedule: "every 1 minutes",
    timeZone: "Europe/Istanbul",
    region: "europe-west1",
    timeoutSeconds: 120,
    maxInstances: 1
  },
  async () => {
    const now = Date.now();
    const orders = db.collection("orders");
    const [deadlineSnapshot, legacySnapshot] = await Promise.all([
      orders
        .where("status", "==", "pending")
        .where("approvalDeadlineAt", "<=", now)
        .orderBy("approvalDeadlineAt", "asc")
        .limit(100)
        .get(),
      orders
        .where("status", "==", "pending")
        .where("createdAt", "<=", now - APPROVAL_WINDOW_MS)
        .orderBy("createdAt", "asc")
        .limit(100)
        .get()
    ]);
    const candidates = new Map();
    [...deadlineSnapshot.docs, ...legacySnapshot.docs].forEach((document) => {
      candidates.set(document.id, document);
    });
    const expired = [...candidates.values()].filter((doc) => {
      const order = doc.data();
      const createdAt = Number(order.createdAt || 0);
      const deadline = Number(order.approvalDeadlineAt || (createdAt + APPROVAL_WINDOW_MS));
      return deadline > 0 && deadline <= now;
    });

    for (let offset = 0; offset < expired.length; offset += 25) {
      const batch = expired.slice(offset, offset + 25);
      await Promise.all(batch.map(async (orderDocument) => {
        const orderRef = orderDocument.ref;
        await db.runTransaction(async (transaction) => {
          const latestSnapshot = await transaction.get(orderRef);
          if (!latestSnapshot.exists) return;

          const order = latestSnapshot.data();
          if (order.status !== "pending") return;
          const createdAt = Number(order.createdAt || 0);
          const deadline = Number(order.approvalDeadlineAt || (createdAt + APPROVAL_WINDOW_MS));
          if (deadline <= 0 || deadline > Date.now()) return;

          const itemRef = order.itemId
            ? db.collection("items").doc(String(order.itemId))
            : null;
          const itemSnapshot = itemRef ? await transaction.get(itemRef) : null;

          const updates = {
            status: "cancelled",
            cancelledAt: Date.now(),
            cancellationReason: "merchant_approval_timeout"
          };
          if (order.paymentStatus === "demo_paid") {
            updates.paymentStatus = "demo_refunded";
            updates.refundAt = Date.now();
          }
          const quantity = Number(order.quantity || 0);
          if (itemRef && itemSnapshot?.exists && Number.isInteger(quantity) && quantity > 0) {
            const currentStock = Number(itemSnapshot.get("stock") || 0);
            transaction.update(itemRef, "stock", currentStock + quantity);
            updates.stockRestoredAt = Date.now();
          }
          // Admin SDK writes bypass Firestore rules; this marker keeps retries and
          // client-side cancellation handling from restoring the same stock twice.
          transaction.update(orderRef, updates);
        });
      }));
    }

    console.log(`Found ${expired.length} overdue requests from bounded queries; cancelled expired orders.`);
  }
);

/** Restore the reserved item stock once when a customer cancels an order. */
exports.restoreStockAfterCustomerCancellation = onDocumentUpdated(
  {
    document: "orders/{orderId}",
    region: "europe-west1",
    retry: true
  },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after
        || before.status === "cancelled"
        || after.status !== "cancelled"
        || after.cancellationReason !== "customer_cancelled"
        || after.stockRestoredAt) {
      return;
    }

    const itemId = String(after.itemId || "");
    const quantity = Number(after.quantity || 0);
    if (!itemId || !Number.isInteger(quantity) || quantity <= 0) {
      console.error(`Cannot restore stock for cancelled order ${event.params.orderId}: invalid item or quantity.`);
      return;
    }

    const orderRef = db.collection("orders").doc(event.params.orderId);
    const itemRef = db.collection("items").doc(itemId);
    await db.runTransaction(async (transaction) => {
      const orderSnapshot = await transaction.get(orderRef);
      if (!orderSnapshot.exists) return;
      const order = orderSnapshot.data();
      if (order.status !== "cancelled"
          || order.cancellationReason !== "customer_cancelled"
          || order.stockRestoredAt) {
        return;
      }

      const itemSnapshot = await transaction.get(itemRef);
      if (itemSnapshot.exists) {
        const currentStock = Number(itemSnapshot.get("stock") || 0);
        transaction.update(itemRef, "stock", currentStock + quantity);
      } else {
        console.error(`Product ${itemId} is missing while restoring order ${event.params.orderId}.`);
      }
      transaction.update(orderRef, "stockRestoredAt", Date.now());
    });
  }
);
