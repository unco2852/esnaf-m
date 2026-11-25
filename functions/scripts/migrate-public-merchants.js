const admin = require("firebase-admin");
const fs = require("node:fs");
const path = require("node:path");
const firebaseConfig = JSON.parse(
  fs.readFileSync(path.resolve(__dirname, "../../.firebaserc"), "utf8")
);

const projectId = process.env.GCLOUD_PROJECT
  || process.env.GOOGLE_CLOUD_PROJECT
  || firebaseConfig.projects?.default;
if (!projectId) throw new Error("Firebase proje kimliği bulunamadı. GCLOUD_PROJECT ayarlayın veya .firebaserc dosyasını kontrol edin.");
admin.initializeApp({ projectId });

const db = admin.firestore();
const PAGE_SIZE = 400;
const originalSampleMerchantIds = new Set([
  "merchant_dogan_tesisat",
  "merchant_bastug_yapi",
  "merchant_nas_terzi",
  "merchant_yokyok_anahtar",
  "merchant_engin_kuafor"
]);

function isMerchant(data) {
  const role = String(data.role || data.userType || data.accountType || "").trim().toUpperCase();
  return role === "MERCHANT" || role === "ESNAF" || Boolean(data.businessName || data.category || data.businessAddress);
}

function publicFields(uid, data) {
  const fields = {
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
  if (data.isDemo === true || originalSampleMerchantIds.has(uid)) fields.isDemo = true;
  return fields;
}

async function createPublicMerchantIfMissing(uid, data) {
  const publicRef = db.collection("public_merchants").doc(uid);
  return db.runTransaction(async (transaction) => {
    const current = await transaction.get(publicRef);
    if (current.exists) return false;
    transaction.create(publicRef, publicFields(uid, data));
    return true;
  });
}

async function main() {
  let cursor = null;
  let migrated = 0;
  let keptExisting = 0;

  while (true) {
    let query = db.collection("users")
      .orderBy(admin.firestore.FieldPath.documentId())
      .limit(PAGE_SIZE);
    if (cursor) query = query.startAfter(cursor);

    const page = await query.get();
    if (page.empty) break;

    for (const userDocument of page.docs) {
      const data = userDocument.data();
      if (!isMerchant(data)) continue;
      const created = await createPublicMerchantIfMissing(userDocument.id, data);
      if (created) migrated += 1;
      else keptExisting += 1;
    }
    cursor = page.docs[page.docs.length - 1];
    if (page.size < PAGE_SIZE) break;
  }

  console.log(`Added ${migrated} missing public merchant profiles; preserved ${keptExisting} existing profiles and all private user documents.`);
}

main().catch((error) => {
  console.error("Public merchant migration failed:", error.message);
  process.exitCode = 1;
});
