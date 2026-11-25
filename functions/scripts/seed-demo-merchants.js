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

// These five IDs match the original sample businesses. Existing records are preserved.
const restoredSamples = [
  {
    uid: "merchant_dogan_tesisat",
    businessName: "Doğan Tesisat",
    category: "Tesisat & Bakım",
    businessDescription: "Örnek profil: sıhhi tesisat ve bakım servisi.",
    city: "Aksaray",
    district: "Merkez",
    businessAddress: "Aksaray Merkez"
  },
  {
    uid: "merchant_bastug_yapi",
    businessName: "Baştuğ Yapı Malzemeleri",
    category: "Tesisat & Bakım",
    businessDescription: "Örnek profil: boya, hırdavat ve yapı malzemeleri.",
    city: "Aksaray",
    district: "Merkez",
    businessAddress: "Aksaray Merkez"
  },
  {
    uid: "merchant_nas_terzi",
    businessName: "Nas Terzi",
    category: "Terzi & Tekstil",
    businessDescription: "Örnek profil: terzilik ve tekstil işleri.",
    city: "Aksaray",
    district: "Merkez",
    businessAddress: "Aksaray Merkez"
  },
  {
    uid: "merchant_yokyok_anahtar",
    businessName: "Yok-Yok Anahtar",
    category: "Tesisat & Bakım",
    businessDescription: "Örnek profil: kilit, anahtar ve çilingir servisi.",
    city: "Aksaray",
    district: "Merkez",
    businessAddress: "Aksaray Merkez"
  },
  {
    uid: "merchant_engin_kuafor",
    businessName: "Engin Kuaför",
    category: "Berber & Kuaför",
    businessDescription: "Örnek profil: saç ve sakal bakım salonu.",
    city: "Aksaray",
    district: "Merkez",
    businessAddress: "Aksaray Merkez"
  }
];

// Fictional profiles for the school-project showcase; no phone numbers or private data.
const newBusinesses = [
  ["Usta Fırça Boya & Badana", "Tesisat & Bakım", "Ev ve iş yeri iç cephe boya, badana ve renk yenileme işleri.", "Ankara", "Çankaya"],
  ["Akım Elektrik Servisi", "Tesisat & Bakım", "Aydınlatma, priz, sigorta ve küçük elektrik bakım işleri.", "Ankara", "Yenimahalle"],
  ["Çınar Tesisat", "Tesisat & Bakım", "Musluk, batarya, gider ve su kaçağı bakım hizmetleri.", "İzmir", "Bornova"],
  ["Serin İklim Klima", "Tesisat & Bakım", "Klima temizliği, montajı ve dönemsel bakım hizmetleri.", "Antalya", "Muratpaşa"],
  ["Anahtar Ustası", "Tesisat & Bakım", "Kilit değişimi, anahtar kopyalama ve kapı açma hizmetleri.", "Bursa", "Nilüfer"],
  ["Güven Çatı Onarım", "Tesisat & Bakım", "Çatı, oluk ve yağmur suyu gideri bakım işleri.", "Trabzon", "Ortahisar"],
  ["Camlı Dünya", "Tesisat & Bakım", "Cam değişimi, ayna kesimi ve duşakabin montajı.", "İstanbul", "Bakırköy"],
  ["Parke Dünyası", "Tesisat & Bakım", "Laminat parke döşeme ve küçük zemin yenileme işleri.", "Kocaeli", "İzmit"],
  ["Usta Marangoz Atölyesi", "Tesisat & Bakım", "Raf, kapı, dolap ve ahşap tamir işleri.", "Eskişehir", "Tepebaşı"],
  ["Beyaz Eşya Usta", "Tesisat & Bakım", "Buzdolabı, çamaşır makinesi ve bulaşık makinesi bakım servisi.", "İzmir", "Karşıyaka"],
  ["Pırıl Halı Yıkama", "Tesisat & Bakım", "Halı ve kilim temizliği için mahalle teslimat hizmeti.", "Konya", "Selçuklu"],
  ["Yeni Görünüm Koltuk Döşeme", "Tesisat & Bakım", "Koltuk kumaşı yenileme ve sandalye döşeme işleri.", "İstanbul", "Ataşehir"],
  ["Mis Ev Temizliği", "Tesisat & Bakım", "Ev ve küçük ofisler için düzenli temizlik hizmeti.", "Ankara", "Çankaya"],
  ["Kombi Bakım Noktası", "Tesisat & Bakım", "Kombi yıllık bakım ve petek kontrol hizmetleri.", "Gaziantep", "Şahinbey"],
  ["Temiz Su Arıtma", "Tesisat & Bakım", "Su arıtma cihazı filtre değişimi ve bakım desteği.", "Adana", "Çukurova"],
  ["Yeşil Bahçe Bakım", "Tesisat & Bakım", "Balkon, bahçe ve küçük peyzaj düzenleme hizmetleri.", "Antalya", "Konyaaltı"],
  ["Doğa İlaçlama", "Tesisat & Bakım", "Ev ve iş yerleri için haşere kontrol hizmeti.", "İstanbul", "Pendik"],
  ["Oto Elektrik Garajı", "Tesisat & Bakım", "Araç aydınlatma, akü ve elektrik arıza kontrolü.", "Bursa", "Osmangazi"],
  ["Pedal Bisiklet Servisi", "Diğer", "Bisiklet bakım, fren ayarı ve lastik değişimi.", "İzmir", "Konak"],
  ["Cep Telefonu Teknik", "Diğer", "Telefon ekranı ve aksesuarları için örnek teknik servis profili.", "İstanbul", "Maltepe"],
  ["Bilgisayar Ustası", "Diğer", "Bilgisayar kurulumu, bakım ve temel yazılım desteği.", "Ankara", "Etimesgut"],
  ["Usta Ayakkabı Tamiri", "Terzi & Tekstil", "Ayakkabı tabanı, dikiş ve küçük deri eşya tamiri.", "İstanbul", "Fatih"],
  ["Çiçek Bahçesi", "Diğer", "Buket, saksı bitkisi ve özel gün çiçek tasarımı.", "İstanbul", "Üsküdar"],
  ["Pati Makas Pet Kuaför", "Diğer", "Evcil hayvanlar için yıkama ve tüy bakım profili.", "İzmir", "Buca"],
  ["Mahalle Taşıma Ekibi", "Diğer", "Küçük ev eşyası taşıma ve yerleştirme desteği.", "Ankara", "Keçiören"],
  ["Köşe Bakkalı", "Bakkal & Market", "Günlük temel ihtiyaçlar ve mahalle alışverişi.", "İstanbul", "Kadıköy"],
  ["Taze Sepet Market", "Bakkal & Market", "Paketli gıda, içecek ve günlük mutfak ürünleri.", "Bursa", "Yıldırım"],
  ["Mahalle Kileri", "Bakkal & Market", "Temel gıda ve ev ihtiyaçları için örnek market profili.", "Samsun", "Atakum"],
  ["Bereket Şarküteri", "Bakkal & Market", "Peynir, zeytin ve kahvaltılık ürünler.", "İstanbul", "Bakırköy"],
  ["Usta Börek Evi", "Fırın & Pastane", "Günlük börek ve tuzlu hamur işi çeşitleri.", "İzmir", "Karşıyaka"],
  ["Mahalle Fırını", "Fırın & Pastane", "Ekmek, poğaça ve günlük fırın ürünleri.", "Ankara", "Mamak"],
  ["Mimoza Pastanesi", "Fırın & Pastane", "Pasta, kurabiye ve kutlama tatlıları.", "Antalya", "Kepez"],
  ["Simit Durağı", "Fırın & Pastane", "Simit, açma ve hızlı kahvaltı seçenekleri.", "Kocaeli", "İzmit"],
  ["Kıyı Kahve", "Restoran & Kafe", "Kahve ve hafif atıştırmalıklar sunan örnek kafe.", "İstanbul", "Beşiktaş"],
  ["Fincan Kahve Evi", "Restoran & Kafe", "Sıcak içecek ve tatlı molası için mahalle kafesi.", "Eskişehir", "Tepebaşı"],
  ["Sofra Ev Yemekleri", "Restoran & Kafe", "Günlük ev yemekleri ve öğle menüsü.", "Ankara", "Çankaya"],
  ["Yeşil Masa", "Restoran & Kafe", "Salata, sandviç ve hafif öğün seçenekleri.", "İzmir", "Bornova"],
  ["Taş Fırın Pizza", "Restoran & Kafe", "Fırın pizza ve paket servis için örnek işletme profili.", "Gaziantep", "Şahinbey"],
  ["Moda Terzi Atölyesi", "Terzi & Tekstil", "Paça, daraltma ve kıyafet tadilat işleri.", "İstanbul", "Şişli"],
  ["İğne İplik Terzi", "Terzi & Tekstil", "Günlük kıyafet onarımı ve ölçüye göre dikim.", "Bursa", "Nilüfer"],
  ["Dikiş Evi", "Terzi & Tekstil", "Perde kısaltma, fermuar değişimi ve dikiş işleri.", "Konya", "Selçuklu"],
  ["Renkli Kumaşlar", "Terzi & Tekstil", "Kumaş, düğme ve temel dikiş malzemeleri.", "İstanbul", "Fatih"],
  ["Temiz Hat Kuru Temizleme", "Terzi & Tekstil", "Kıyafet bakım ve kuru temizleme için örnek profil.", "Antalya", "Muratpaşa"],
  ["Şehir Berberi", "Berber & Kuaför", "Saç kesimi ve sakal bakım hizmetleri.", "Ankara", "Çankaya"],
  ["Yeni Stil Kuaför", "Berber & Kuaför", "Saç kesimi, fön ve temel saç bakım hizmetleri.", "İzmir", "Konak"],
  ["Makas Saç Stüdyosu", "Berber & Kuaför", "Kadın ve erkek saç bakımına yönelik örnek salon.", "İstanbul", "Ataşehir"],
  ["Minik Makas Çocuk Kuaförü", "Berber & Kuaför", "Çocuklar için saç kesimi ve bakım hizmeti.", "Samsun", "Atakum"],
  ["Klasik Berber", "Berber & Kuaför", "Saç ve sakal kesimi için mahalle berberi.", "Adana", "Çukurova"],
  ["Lastik ve Jant Servisi", "Diğer", "Araç lastik kontrolü ve küçük bakım işleri.", "İstanbul", "Pendik"],
  ["Dost Patiler Bakım", "Diğer", "Evcil hayvanlar için günlük bakım ve aksesuar profili.", "İzmir", "Buca"]
].map(([businessName, category, businessDescription], index) => ({
  uid: `demo_merchant_${String(index + 1).padStart(3, "0")}`,
  businessName,
  category,
  businessDescription,
  city: "Aksaray",
  district: "Merkez",
  businessAddress: "Aksaray Merkez"
}));

const aksarayNeighborhoods = [
  { name: "Taşpazar", latitude: 38.366576, longitude: 34.017268 },
  { name: "Ereğlikapı", latitude: 38.3643, longitude: 34.0257 },
  { name: "Büyük Bölcek", latitude: 38.370274, longitude: 34.011079 },
  { name: "Hacılar Harmanı", latitude: 38.3574545, longitude: 33.9959218 }
];

function locationForDemo(index) {
  const neighborhood = aksarayNeighborhoods[index % aksarayNeighborhoods.length];
  // Small deterministic offsets create distinct demo map pins near each neighborhood center.
  const offset = Math.floor(index / aksarayNeighborhoods.length);
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

const profilesWithLocations = [...restoredSamples, ...newBusinesses].map((profile, index) => ({
  ...profile,
  ...locationForDemo(index)
}));

function toPublicProfile(profile) {
  return {
    uid: profile.uid,
    role: "MERCHANT",
    businessName: profile.businessName,
    category: profile.category,
    businessDescription: profile.businessDescription,
    businessPhone: "",
    businessAddress: profile.businessAddress,
    workingHours: "09:00 - 18:00",
    businessLogoUrl: "",
    businessLatitude: profile.businessLatitude,
    businessLongitude: profile.businessLongitude,
    city: profile.city,
    district: profile.district,
    isOpen: true,
    isDemo: true
  };
}

async function createOnlyIfMissing(profile) {
  const reference = db.collection("public_merchants").doc(profile.uid);
  return db.runTransaction(async (transaction) => {
    const existing = await transaction.get(reference);
    if (existing.exists) return false;
    transaction.create(reference, toPublicProfile(profile));
    return true;
  });
}

async function main() {
  if (!projectId) throw new Error("Firebase projesi seçilmedi.");
  let added = 0;
  let preserved = 0;
  for (const profile of profilesWithLocations) {
    if (await createOnlyIfMissing(profile)) added += 1;
    else preserved += 1;
  }
  console.log(`Added ${added} sample merchant profiles; preserved ${preserved} profiles already in Firestore.`);
}

main().catch((error) => {
  console.error("Demo merchant seeding failed:", error.message);
  process.exitCode = 1;
});
