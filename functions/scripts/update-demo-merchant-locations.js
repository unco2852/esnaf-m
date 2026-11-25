const admin = require("firebase-admin");
const fs = require("node:fs");
const path = require("node:path");

const firebaseConfig = JSON.parse(
  fs.readFileSync(path.resolve(__dirname, "../../.firebaserc"), "utf8")
);
const projectId = process.env.GCLOUD_PROJECT
  || process.env.GOOGLE_CLOUD_PROJECT
  || firebaseConfig.projects?.default;
if (!projectId) throw new Error("Firebase proje kimliği bulunamadı.");

admin.initializeApp({ projectId });
const db = admin.firestore();

const legacyDemoIds = [
  "merchant_dogan_tesisat",
  "merchant_bastug_yapi",
  "merchant_nas_terzi",
  "merchant_yokyok_anahtar",
  "merchant_engin_kuafor"
];
const numberedDemoIds = Array.from({ length: 50 }, (_, index) =>
  `demo_merchant_${String(index + 1).padStart(3, "0")}`
);
const allowedIds = [...legacyDemoIds, ...numberedDemoIds];
const neighborhoods = [
  { name: "Taşpazar", latitude: 38.366576, longitude: 34.017268 },
  { name: "Ereğlikapı", latitude: 38.3643, longitude: 34.0257 },
  { name: "Büyük Bölcek", latitude: 38.370274, longitude: 34.011079 },
  { name: "Hacılar Harmanı", latitude: 38.3574545, longitude: 33.9959218 }
];

function locationForIndex(index) {
  const neighborhood = neighborhoods[index % neighborhoods.length];
  const offset = Math.floor(index / neighborhoods.length);
  const latitudeOffset = (((offset * 7) % 11) - 5) * 0.00008;
  const longitudeOffset = (((offset * 5) % 13) - 6) * 0.00008;
  return {
    city: "Aksaray",
    district: "Merkez",
    businessAddress: `${neighborhood.name}, Aksaray Merkez`,
    businessLatitude: Number((neighborhood.latitude + latitudeOffset).toFixed(6)),
    businessLongitude: Number((neighborhood.longitude + longitudeOffset).toFixed(6))
  };
}

async function main() {
  let updated = 0;
  let missing = 0;
  let skipped = 0;
  const batch = db.batch();

  for (const [index, uid] of allowedIds.entries()) {
    const reference = db.collection("public_merchants").doc(uid);
    const snapshot = await reference.get();
    if (!snapshot.exists) {
      missing += 1;
      continue;
    }

    // Legacy IDs are fixed demo records; numbered records must still be marked as demos.
    const isLegacyDemo = legacyDemoIds.includes(uid);
    if (!isLegacyDemo && snapshot.get("isDemo") !== true) {
      skipped += 1;
      continue;
    }

    batch.update(reference, locationForIndex(index));
    updated += 1;
  }

  if (updated > 0) await batch.commit();
  console.log(`Updated ${updated} demo locations in ${projectId}; missing ${missing}; skipped non-demo ${skipped}.`);
}

main().catch((error) => {
  console.error("Demo merchant location update failed:", error.message);
  process.exitCode = 1;
});
