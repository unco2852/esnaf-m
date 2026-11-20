const admin = require("firebase-admin");
const firebaseConfig = require("../../.firebaserc");

const projectId = process.env.GCLOUD_PROJECT
  || process.env.GOOGLE_CLOUD_PROJECT
  || firebaseConfig.projects?.default;
admin.initializeApp({ projectId });

const db = admin.firestore();
const PAGE_SIZE = 400;

function isMerchant(data) {
  const role = String(data.role || data.userType || data.accountType || "").trim().toUpperCase();
  return role === "MERCHANT" || role === "ESNAF" || Boolean(data.businessName || data.category || data.businessAddress);
}

function publicFields(uid, data) {
  return {
    uid,
    role: "MERCHANT",
    businessName: String(data.businessName || data.name || "İşletme").slice(0, 120),
    category: String(data.category || "").slice(0, 100),
    businessDescription: String(data.businessDescription || "").slice(0, 2000),
    businessPhone: String(data.businessPhone || "").slice(0, 40),
    businessAddress: String(data.businessAddress || data.address || "").slice(0, 500),
    workingHours: String(data.workingHours || "09:00 - 18:00").slice(0, 100),
    businessLogoUrl: String(data.businessLogoUrl || "").slice(0, 2000),
    businessLatitude: typeof data.businessLatitude === "number" ? data.businessLatitude : null,
    businessLongitude: typeof data.businessLongitude === "number" ? data.businessLongitude : null,
    city: String(data.city || "").slice(0, 100),
    district: String(data.district || "").slice(0, 100),
    isOpen: typeof data.isBusinessOpen === "boolean" ? data.isBusinessOpen : true
  };
}

async function main() {
  let cursor = null;
  let migrated = 0;

  while (true) {
    let query = db.collection("users")
      .orderBy(admin.firestore.FieldPath.documentId())
      .limit(PAGE_SIZE);
    if (cursor) query = query.startAfter(cursor);

    const page = await query.get();
    if (page.empty) break;

    const batch = db.batch();
    let pageMigrations = 0;
    for (const userDocument of page.docs) {
      const data = userDocument.data();
      if (!isMerchant(data)) continue;
      const currentRole = String(data.role || "").trim().toUpperCase();
      if (currentRole !== "MERCHANT") batch.update(userDocument.ref, { role: "MERCHANT" });
      batch.set(
        db.collection("public_merchants").doc(userDocument.id),
        publicFields(userDocument.id, data)
      );
      migrated += 1;
      pageMigrations += 1;
    }
    if (pageMigrations > 0) await batch.commit();
    cursor = page.docs[page.docs.length - 1];
    if (page.size < PAGE_SIZE) break;
  }

  console.log(`Migrated ${migrated} public merchant profiles.`);
}

main().catch((error) => {
  console.error("Public merchant migration failed:", error.message);
  process.exitCode = 1;
});
