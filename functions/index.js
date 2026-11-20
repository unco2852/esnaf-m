const admin = require("firebase-admin");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");

admin.initializeApp();

const db = admin.firestore();
const APPROVAL_WINDOW_MS = 30 * 60 * 1000;

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
    const pendingSnapshot = await db.collection("orders")
      .where("status", "==", "pending")
      .get();

    const expired = pendingSnapshot.docs.filter((doc) => {
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
          transaction.update(orderRef, updates);

          const quantity = Number(order.quantity || 0);
          if (itemRef && itemSnapshot?.exists && Number.isInteger(quantity) && quantity > 0) {
            const currentStock = Number(itemSnapshot.get("stock") || 0);
            transaction.update(itemRef, "stock", currentStock + quantity);
          }
        });
      }));
    }

    console.log(`Checked ${pendingSnapshot.size} pending orders; cancelled ${expired.length} expired requests.`);
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
