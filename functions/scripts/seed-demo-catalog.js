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
const dryRun = process.argv.includes("--dry-run");

function item(title, category, price, stock, description) {
  return { title, category, price, stock, description };
}

function service(title, category, price, duration, description) {
  return { title, category, price, depositAmount: 0, duration, description };
}

const catalogs = {
  paint: {
    items: [
      item("İç Cephe Silikonlu Boya 15 L", "Boya", 1690, 12, "Mat beyaz, silinebilir iç cephe boyası."),
      item("Boya Rulo ve Fırça Seti", "Boya Malzemesi", 320, 20, "Duvar boyama için farklı boylarda rulo ve fırça seti.")
    ],
    service: service("İç Cephe Boya Uygulaması", "Boya & Badana", 4500, "1 gün", "Bir oda için yüzey hazırlığı ve iç cephe boya uygulaması.")
  },
  electrician: {
    items: [
      item("LED Ampul 9 W", "Aydınlatma", 85, 30, "Ev içi kullanım için tasarruflu LED ampul."),
      item("Topraklı Priz", "Elektrik Malzemesi", 165, 20, "Standart duvar tipi topraklı priz.")
    ],
    service: service("Priz ve Aydınlatma Kontrolü", "Elektrik Servisi", 650, "45 dk", "Bir odadaki priz veya aydınlatma bağlantısının kontrolü.")
  },
  plumbing: {
    items: [
      item("Krom Lavabo Bataryası", "Tesisat", 1250, 10, "Tek kumandalı standart lavabo bataryası."),
      item("Esnek Bağlantı Hortumu", "Tesisat", 180, 25, "Lavabo ve batarya bağlantıları için örgülü hortum.")
    ],
    service: service("Musluk ve Batarya Değişimi", "Tesisat", 750, "60 dk", "Mevcut musluk veya bataryanın sökülüp yenisinin takılması.")
  },
  climate: {
    items: [
      item("Klima Temizlik Filtresi", "Klima", 350, 15, "Uyumlu ölçüye göre seçilen yıkanabilir filtre."),
      item("Klima Bakım Temizlik Seti", "Klima", 420, 10, "Dış yüzey ve filtre temizliği için bakım seti.")
    ],
    service: service("Klima Bakım ve Filtre Temizliği", "Klima Servisi", 950, "75 dk", "İç ünite filtre temizliği ve genel çalışma kontrolü.")
  },
  locksmith: {
    items: [
      item("Standart Kapı Kilit Göbeği", "Kilit ve Anahtar", 600, 12, "Yaygın ölçüler için örnek kapı kilit göbeği."),
      item("Yedek Anahtar Kopyası", "Kilit ve Anahtar", 120, 20, "Standart ev anahtarı için kopyalama ürünü.")
    ],
    service: service("Kapı Kilidi Değişimi", "Çilingir", 800, "45 dk", "Standart kapı kilidinin sökülüp yenisinin takılması.")
  },
  roof: {
    items: [
      item("Çatı İzolasyon Bandı", "Çatı Malzemesi", 260, 15, "Küçük yüzey onarımlarında kullanılan su yalıtım bandı."),
      item("Oluk Bağlantı Kelepçesi", "Çatı Malzemesi", 95, 30, "Yağmur oluğu sabitleme ve bağlantı parçası.")
    ],
    service: service("Çatı ve Oluk Kontrolü", "Çatı Onarımı", 1200, "90 dk", "Çatı ve yağmur oluğunda gözle kontrol ve küçük bakım.")
  },
  glass: {
    items: [
      item("Ölçüye Göre Ayna Kesimi", "Cam ve Ayna", 650, 10, "Belirtilen ölçüye göre hazırlanan standart ayna."),
      item("Şeffaf Duşakabin Silikonu", "Cam Malzemesi", 190, 20, "Islak alanlarda kullanılan suya dayanıklı silikon.")
    ],
    service: service("Cam Ölçüsü ve Montajı", "Cam Servisi", 1500, "90 dk", "Yerinde ölçü alma ve standart cam montajı için hizmet talebi.")
  },
  flooring: {
    items: [
      item("Laminat Parke Paketi", "Parke", 780, 18, "Örnek katalog ürünü; paket içeriği seçilen modele göre değişir."),
      item("Parke Süpürgeliği 2,4 m", "Parke", 150, 25, "Duvar ve laminat parke birleşimi için süpürgelik.")
    ],
    service: service("Oda Parke Döşeme", "Parke Döşeme", 2500, "1 gün", "Bir oda için zemin hazırlığı ve laminat parke döşeme hizmeti.")
  },
  carpentry: {
    items: [
      item("Ahşap Duvar Rafı", "Ahşap Ürün", 450, 12, "Ev içi kullanım için sade ahşap raf."),
      item("Mobilya Menteşe Seti", "Mobilya Malzemesi", 140, 20, "Dolap kapakları için standart menteşe seti.")
    ],
    service: service("Dolap Kapağı Tamiri", "Marangozluk", 950, "60 dk", "Dolap kapağı ayarı, menteşe kontrolü ve küçük ahşap tamiri.")
  },
  appliance: {
    items: [
      item("Çamaşır Makinesi Giriş Hortumu", "Beyaz Eşya", 280, 15, "Standart bağlantılı su giriş hortumu."),
      item("Buzdolabı Su Filtresi", "Beyaz Eşya", 460, 10, "Uyumlu buzdolapları için değişim filtresi.")
    ],
    service: service("Beyaz Eşya Arıza Tespiti", "Teknik Servis", 550, "45 dk", "Cihazın ilk kontrolü ve olası arıza hakkında bilgilendirme.")
  },
  carpet: {
    items: [
      item("Halı Bakım Spreyi", "Halı Bakım", 180, 18, "Ev tipi halı yüzeyleri için bakım spreyi."),
      item("Koltuk ve Halı Leke Bezi", "Temizlik", 95, 25, "Hassas yüzeylerde kullanılabilen mikrofiber bez.")
    ],
    service: service("Standart Halı Yıkama", "Halı Yıkama", 420, "3 gün", "Standart boy halının teslim alınması, yıkanması ve geri teslimi.")
  },
  upholstery: {
    items: [
      item("Döşemelik Kumaş Numunesi", "Döşeme", 80, 30, "Renk ve doku seçimi için örnek kumaş parçası."),
      item("Mobilya Keçe Koruyucu Seti", "Mobilya", 120, 20, "Sandalye ve masa ayakları için zemin koruyucu keçe.")
    ],
    service: service("Sandalye Döşeme Yenileme", "Koltuk Döşeme", 1800, "1 gün", "Bir sandalye oturağının kumaş ve dolgu yenileme hizmeti.")
  },
  cleaning: {
    items: [
      item("Mikrofiber Temizlik Bezi Seti", "Temizlik", 120, 24, "Ev ve ofis yüzeyleri için çok amaçlı bez seti."),
      item("Yüzey Temizleyici 1 L", "Temizlik", 150, 18, "Günlük kullanıma uygun genel yüzey temizleyici.")
    ],
    service: service("Ev Temizliği", "Temizlik", 1800, "4 saat", "Standart büyüklükte ev için temel temizlik hizmeti.")
  },
  boiler: {
    items: [
      item("Petek Purjör Anahtarı", "Kombi ve Petek", 60, 30, "Standart petek hava alma anahtarı."),
      item("Kombi Manyetik Filtre", "Kombi ve Petek", 750, 8, "Tesisat hattındaki tortular için örnek manyetik filtre.")
    ],
    service: service("Kombi Yıllık Bakımı", "Kombi Servisi", 1150, "60 dk", "Kombi çalışma kontrolü ve temel yıllık bakım hizmeti.")
  },
  water: {
    items: [
      item("Su Arıtma Ön Filtre Seti", "Su Arıtma", 650, 12, "Standart ölçülerde ön filtre seti; cihaz uyumu kontrol edilmelidir."),
      item("Su Arıtma Membranı", "Su Arıtma", 1200, 6, "Ters ozmoz cihazları için örnek membran filtresi.")
    ],
    service: service("Filtre Değişimi ve Cihaz Kontrolü", "Su Arıtma", 450, "45 dk", "Filtrelerin kontrol edilmesi ve uygun filtrelerin değiştirilmesi.")
  },
  garden: {
    items: [
      item("Mevsimlik Balkon Çiçeği", "Bahçe", 160, 20, "Balkon ve pencere önü için mevsimlik saksı bitkisi."),
      item("Orta Boy Saksı", "Bahçe", 240, 15, "Balkon ve ev bitkileri için drenaj delikli saksı.")
    ],
    service: service("Balkon ve Bahçe Bakımı", "Bahçe Bakımı", 700, "90 dk", "Bitki budama, saksı düzenleme ve temel bakım hizmeti.")
  },
  pest: {
    items: [
      item("Haşere İzleme Yapışkan Tuzağı", "Ev Bakımı", 90, 20, "Ev içinde haşere yoğunluğunu izlemek için yapışkan tuzak."),
      item("Kapı Altı Koruyucu Fitil", "Ev Bakımı", 180, 15, "Kapı altındaki boşlukları kapatmaya yardımcı koruyucu fitil.")
    ],
    service: service("Ev İçi Haşere Kontrolü", "İlaçlama", 950, "60 dk", "Alan incelemesi ve uygun haşere kontrolü için hizmet talebi.")
  },
  autoElectric: {
    items: [
      item("Otomotiv Sigorta Seti", "Oto Elektrik", 180, 18, "Araç elektrik sistemleri için farklı amperlerde sigorta seti."),
      item("H7 Far Ampulü", "Oto Elektrik", 240, 12, "Uyumlu araçlar için standart H7 far ampulü.")
    ],
    service: service("Oto Elektrik Arıza Kontrolü", "Oto Elektrik", 600, "45 dk", "Akü, aydınlatma ve temel elektrik bağlantılarının kontrolü.")
  },
  bicycle: {
    items: [
      item("Bisiklet İç Lastiği", "Bisiklet", 250, 16, "Yaygın ölçüler için örnek iç lastik."),
      item("Bisiklet Fren Balatası", "Bisiklet", 320, 12, "Uyumlu fren sistemleri için değişim balatası.")
    ],
    service: service("Bisiklet Genel Bakımı", "Bisiklet Servisi", 500, "60 dk", "Fren, vites ve lastiklerin temel bakım ve ayarı.")
  },
  phone: {
    items: [
      item("Şeffaf Telefon Kılıfı", "Telefon Aksesuarı", 300, 20, "Farklı cihaz modellerine göre seçilebilen şeffaf kılıf."),
      item("Temperli Ekran Koruyucu", "Telefon Aksesuarı", 180, 25, "Cihaz modeline uygun ekran koruyucu.")
    ],
    service: service("Telefon Arıza Tespiti", "Telefon Teknik Servisi", 400, "30 dk", "Telefonun ilk kontrolü ve onarım seçenekleri hakkında bilgilendirme.")
  },
  computer: {
    items: [
      item("Kablosuz Ofis Mouse", "Bilgisayar", 450, 14, "Günlük bilgisayar kullanımı için kablosuz mouse."),
      item("USB-C Şarj Adaptörü", "Bilgisayar Aksesuarı", 800, 10, "Uyumlu dizüstü bilgisayarlar için örnek adaptör.")
    ],
    service: service("Bilgisayar Kurulum ve Bakımı", "Bilgisayar Servisi", 650, "60 dk", "Temel kurulum, güncelleme ve performans kontrolü.")
  },
  shoe: {
    items: [
      item("Dayanıklı Ayakkabı Bağı", "Ayakkabı Bakımı", 90, 25, "Günlük ayakkabılar için farklı uzunluklarda bağcık."),
      item("Deri Ayakkabı Bakım Kremi", "Ayakkabı Bakımı", 180, 14, "Deri ayakkabılar için bakım ve parlaklık kremi.")
    ],
    service: service("Ayakkabı Taban ve Dikiş Onarımı", "Ayakkabı Tamiri", 350, "1 gün", "Ayakkabı tabanı ve dikişleri için küçük onarım hizmeti.")
  },
  florist: {
    items: [
      item("Mevsim Buketi", "Çiçek", 1200, 8, "Mevsim çiçeklerinden hazırlanan örnek buket."),
      item("Salon Bitkisi", "Çiçek", 480, 10, "Ev ve ofis ortamlarına uygun saksı bitkisi.")
    ],
    service: service("Özel Gün Buketi Tasarımı", "Çiçek Tasarımı", 250, "30 dk", "İstenen renk ve bütçeye göre buket hazırlama hizmeti.")
  },
  pet: {
    items: [
      item("Evcil Hayvan Şampuanı", "Pet Bakım", 280, 12, "Düzenli evcil hayvan bakımı için nazik şampuan."),
      item("Kedi ve Küçük Köpek Tarağı", "Pet Bakım", 250, 10, "Tüy bakımında kullanılabilen günlük tarak.")
    ],
    service: service("Yıkama ve Tüy Bakımı", "Pet Kuaför", 650, "60 dk", "Evcil hayvanlar için yıkama ve temel tüy bakım hizmeti.")
  },
  moving: {
    items: [
      item("Taşınma Kolisi", "Taşınma", 80, 50, "Ev eşyalarının paketlenmesi için dayanıklı koli."),
      item("Streç Film 300 m", "Taşınma", 240, 12, "Taşınma sırasında eşya sabitlemek için paketleme filmi.")
    ],
    service: service("Mahalle İçi Küçük Eşya Taşıma", "Nakliye", 3000, "3 saat", "Mahalle içinde sınırlı miktarda eşya taşıma hizmeti.")
  },
  grocery: {
    items: [
      item("Günlük Süt 1 L", "Gıda", 55, 30, "Günlük temel alışveriş için süt."),
      item("Yumurta 10'lu", "Gıda", 90, 24, "Günlük tüketim için 10'lu yumurta paketi.")
    ],
    service: service("Özel Alışveriş Sepeti Hazırlama", "Mahalle Marketi", 50, "20 dk", "Müşterinin seçtiği ürünlerden alışveriş sepeti hazırlama.")
  },
  deli: {
    items: [
      item("Ezine Tipi Beyaz Peynir 500 g", "Şarküteri", 220, 16, "Kahvaltılık beyaz peynir; gramajlı ürün."),
      item("Siyah Zeytin 500 g", "Şarküteri", 160, 18, "Kahvaltı için sele tipi siyah zeytin.")
    ],
    service: service("Kahvaltılık Sunum Tabağı", "Şarküteri", 250, "30 dk", "Kişi sayısına göre kahvaltılık ürün tabağı hazırlama.")
  },
  borek: {
    items: [
      item("Peynirli Börek Porsiyonu", "Börek", 85, 25, "Günlük hazırlanan peynirli börek porsiyonu."),
      item("Ispanaklı Börek Porsiyonu", "Börek", 80, 20, "Günlük hazırlanan ıspanaklı börek porsiyonu.")
    ],
    service: service("Tepsi Börek Siparişi", "Börek", 500, "1 gün", "Önceden planlanan tepsi börek hazırlama talebi.")
  },
  bakery: {
    items: [
      item("Günlük Ekmek", "Fırın", 15, 50, "Gün içinde taze hazırlanan ekmek."),
      item("Poğaça", "Fırın", 25, 30, "Günlük hazırlanan sade poğaça.")
    ],
    service: service("Toplu Fırın Ürünü Hazırlama", "Fırın", 250, "1 gün", "Toplantı ve etkinlikler için önceden ürün hazırlama talebi.")
  },
  pastry: {
    items: [
      item("Çikolatalı Pasta Dilimi", "Pastane", 120, 18, "Günlük vitrinde sunulan çikolatalı pasta dilimi."),
      item("Karışık Kurabiye Kutusu", "Pastane", 260, 12, "Özel gün ve ikramlar için kurabiye kutusu.")
    ],
    service: service("Özel Gün Pastası Hazırlama", "Pastane", 350, "2 gün", "İstenen kişi sayısı ve tasarıma göre pasta hazırlama talebi.")
  },
  simit: {
    items: [
      item("Susamlı Simit", "Fırın", 20, 40, "Günlük fırın ürünü susamlı simit."),
      item("Açma", "Fırın", 25, 30, "Günlük hazırlanan yumuşak açma.")
    ],
    service: service("Kahvaltı Paketi Hazırlama", "Fırın", 120, "20 dk", "Simit ve açma seçenekleriyle kahvaltı paketi hazırlama.")
  },
  cafe: {
    items: [
      item("Sütlü Latte", "Kafe", 110, 30, "Espresso ve süt ile hazırlanan latte."),
      item("Ev Yapımı Brownie", "Kafe", 95, 18, "Kahve yanında sunulan brownie dilimi.")
    ],
    service: service("Toplantı Kahve Sunum Paketi", "Kafe", 280, "45 dk", "Küçük grup toplantıları için kahve ve ikram hazırlama talebi.")
  },
  meals: {
    items: [
      item("Günün Ev Yemeği Menüsü", "Ev Yemekleri", 180, 20, "Günün menüsünden örnek ana yemek ve yardımcı ürün."),
      item("Mercimek Çorbası", "Ev Yemekleri", 90, 24, "Günlük hazırlanan mercimek çorbası porsiyonu.")
    ],
    service: service("Toplu Ev Yemeği Siparişi", "Ev Yemekleri", 300, "1 gün", "Küçük gruplar için önceden yemek hazırlama talebi.")
  },
  salad: {
    items: [
      item("Mevsim Salata Kasesi", "Salata", 160, 20, "Mevsim yeşillikleriyle hazırlanan salata."),
      item("Sebzeli Sandviç", "Sandviç", 140, 18, "Hafif öğün için sebzeli sandviç.")
    ],
    service: service("Ofis Öğle Yemeği Paketi", "Hafif Öğün", 100, "30 dk", "Önceden seçilen salata ve sandviçlerden öğle paketi hazırlama.")
  },
  pizza: {
    items: [
      item("Margherita Pizza", "Pizza", 320, 15, "Domates sosu, mozzarella ve fesleğen ile hazırlanır."),
      item("Sebzeli Pizza", "Pizza", 350, 15, "Mevsim sebzeleri ve peynir ile hazırlanan pizza.")
    ],
    service: service("Grup Pizza Siparişi Hazırlama", "Pizza", 250, "60 dk", "Grup siparişleri için önceden pizza hazırlama talebi.")
  },
  tailor: {
    items: [
      item("Dikiş İğnesi ve İplik Seti", "Dikiş Malzemesi", 180, 18, "Ev tipi küçük onarımlar için iğne ve iplik seti."),
      item("Fermuar 30 cm", "Dikiş Malzemesi", 90, 24, "Kıyafet tamiri için standart fermuar.")
    ],
    service: service("Pantolon Paça ve Boy Kısaltma", "Terzi", 220, "1 gün", "Pantolon paça boyu ölçülerek kısaltılır.")
  },
  sewing: {
    items: [
      item("Perde Korniş Bandı", "Dikiş Malzemesi", 80, 20, "Perde dikimi ve yenileme için korniş bandı."),
      item("Dikiş İpliği Seti", "Dikiş Malzemesi", 160, 18, "Günlük dikiş ve onarım işleri için renkli iplik seti.")
    ],
    service: service("Perde Boyu Kısaltma", "Terzi", 350, "1 gün", "Perde ölçüsüne göre boy kısaltma ve kenar dikimi.")
  },
  fabric: {
    items: [
      item("Pamuklu Kumaş 1 m", "Kumaş", 150, 30, "Dikiş ve ev tekstili işleri için pamuklu kumaş."),
      item("Renkli Düğme Seti", "Dikiş Malzemesi", 45, 30, "Kıyafet onarımları için farklı renklerde düğme seti.")
    ],
    service: service("Ölçüye Göre Basit Dikiş", "Dikiş", 500, "3 gün", "Müşterinin ölçülerine göre basit tekstil ürünü dikimi talebi.")
  },
  dryCleaning: {
    items: [
      item("Giysi Koruma Kılıfı", "Giysi Bakımı", 55, 25, "Temizlenen kıyafetleri saklamak için koruyucu kılıf."),
      item("Yünlü Giysi Bakım Tarağı", "Giysi Bakımı", 130, 14, "Yünlü ve örme giysiler için bakım tarağı.")
    ],
    service: service("Takım Elbise Kuru Temizleme", "Kuru Temizleme", 300, "2 gün", "Takım elbise için standart kuru temizleme hizmeti.")
  },
  barber: {
    items: [
      item("Sakal Bakım Yağı", "Erkek Bakımı", 220, 12, "Sakal ve cilt bakımı için günlük bakım yağı."),
      item("Saç Şekillendirici Wax", "Erkek Bakımı", 180, 16, "Saç şekillendirme için orta tutuşlu wax.")
    ],
    service: service("Saç ve Sakal Kesimi", "Berber", 500, "45 dk", "Saç kesimi ve sakal düzenleme hizmeti.")
  },
  hair: {
    items: [
      item("Isı Koruyucu Saç Spreyi", "Saç Bakımı", 350, 12, "Isı ile şekillendirme öncesi kullanılan saç bakım spreyi."),
      item("Nemlendirici Saç Maskesi", "Saç Bakımı", 450, 10, "Yıpranmış saçlar için nemlendirici bakım maskesi.")
    ],
    service: service("Saç Kesimi ve Fön", "Kuaför", 800, "90 dk", "Saç kesimi ve fön uygulaması için randevu hizmeti.")
  },
  childHair: {
    items: [
      item("Çocuk Saç Bakım Şampuanı", "Çocuk Bakımı", 190, 12, "Çocuklar için nazik saç bakım şampuanı."),
      item("Renkli Saç Tokası Seti", "Saç Aksesuarı", 100, 20, "Günlük kullanım için renkli toka seti.")
    ],
    service: service("Çocuk Saç Kesimi", "Çocuk Kuaförü", 350, "30 dk", "Çocuklar için kısa saç kesimi randevusu.")
  },
  tire: {
    items: [
      item("Oto Lastik Tamir Kiti", "Oto Bakım", 280, 12, "Acil durumlar için örnek lastik tamir kiti."),
      item("Lastik Sibop Seti", "Oto Bakım", 150, 20, "Araç lastikleri için standart sibop seti.")
    ],
    service: service("Lastik Değişimi ve Balans", "Lastik Servisi", 850, "60 dk", "Araç lastiği değişimi ve balans ayarı hizmeti.")
  }
};

const matchingRules = [
  ["autoElectric", ["oto elektrik"]],
  ["paint", ["boya", "badana", "usta firca"]],
  ["electrician", ["elektrik servisi", "elektrik"]],
  ["plumbing", ["tesisat", "musluk", "su kacagi"]],
  ["climate", ["klima", "iklim"]],
  ["locksmith", ["anahtar", "kilit", "cilingir"]],
  ["roof", ["cati", "oluk"]],
  ["glass", ["camli dunya", "cam montaj", "ayna"]],
  ["flooring", ["parke"]],
  ["carpentry", ["marangoz", "mobilya", "ahsap"]],
  ["appliance", ["beyaz esya", "bulasik makinesi", "camasir makinesi"]],
  ["carpet", ["hali yikama", "hali ve kilim"]],
  ["upholstery", ["koltuk doseme", "doseme", "koltuk kumasi"]],
  ["cleaning", ["ev temizligi", "temizlik"]],
  ["boiler", ["kombi", "petek"]],
  ["water", ["aritma", "su aritma"]],
  ["garden", ["bahce", "peyzaj"]],
  ["pest", ["ilaclama", "hasere"]],
  ["bicycle", ["bisiklet"]],
  ["phone", ["telefon", "cep telefonu"]],
  ["computer", ["bilgisayar", "laptop"]],
  ["shoe", ["ayakkabi", "deri esya"]],
  ["florist", ["cicek", "buket"]],
  ["pet", ["pet kuafor", "pati", "patiler"]],
  ["moving", ["tasima", "nakliye", "evden eve"]],
  ["deli", ["sarkuteri", "kahvaltilik"]],
  ["grocery", ["bakkal", "market", "kiler", "sepet"]],
  ["borek", ["borek"]],
  ["pizza", ["pizza"]],
  ["simit", ["simit"]],
  ["pastry", ["pastane", "pasta", "kurabiye"]],
  ["bakery", ["firin", "ekmek"]],
  ["cafe", ["kahve", "kafe", "cafe"]],
  ["meals", ["ev yemek", "ogle menusu"]],
  ["salad", ["salata", "sandvic"]],
  ["dryCleaning", ["kuru temizleme"]],
  ["fabric", ["kumas", "dugme"]],
  ["sewing", ["dikis evi", "perde kisaltma"]],
  ["tailor", ["terzi", "paca", "kiyafet tadilat"]],
  ["childHair", ["cocuk kuafor", "minik makas"]],
  ["barber", ["berber"]],
  ["hair", ["kuafor", "sac studyo", "makas sac"]],
  ["tire", ["lastik", "jant"]]
];

function normalize(value) {
  return String(value || "")
    .toLocaleLowerCase("tr-TR")
    .normalize("NFD")
    .replace(/\p{M}+/gu, "")
    .replace(/ı/g, "i")
    .replace(/[^a-z0-9]+/g, " ")
    .trim()
    .replace(/\s+/g, " ");
}

function catalogFor(profile) {
  const searchable = normalize(`${profile.businessName} ${profile.category} ${profile.businessDescription}`);
  const match = matchingRules.find(([, phrases]) => phrases.some((phrase) => searchable.includes(phrase)));
  return match ? catalogs[match[0]] : null;
}

async function existingMerchantIds(collectionName, merchantIds) {
  const found = new Set();
  for (let offset = 0; offset < merchantIds.length; offset += 30) {
    const page = merchantIds.slice(offset, offset + 30);
    if (page.length === 0) continue;
    const snapshot = await db.collection(collectionName).where("merchantId", "in", page).get();
    snapshot.docs.forEach((doc) => found.add(doc.get("merchantId")));
  }
  return found;
}

function itemDocument(merchantId, itemData) {
  return {
    merchantId,
    title: itemData.title,
    price: itemData.price,
    stock: itemData.stock,
    category: itemData.category,
    description: itemData.description,
    imageUrl: "",
    isActive: true,
    isDemo: true,
    createdAt: Date.now()
  };
}

function serviceDocument(merchantId, serviceData) {
  return {
    merchantId,
    title: serviceData.title,
    description: serviceData.description,
    price: serviceData.price,
    depositAmount: serviceData.depositAmount,
    duration: serviceData.duration,
    category: serviceData.category,
    imageUrl: "",
    isActive: true,
    isDemo: true,
    createdAt: Date.now()
  };
}

async function createMissingDocuments(collectionName, merchantId, entries, mapDocument) {
  const refs = entries.map((entry, index) => ({
    ref: db.collection(collectionName).doc(`demo_${merchantId}_${String(index + 1).padStart(2, "0")}`),
    data: mapDocument(merchantId, entry, index + 1)
  }));
  return db.runTransaction(async (transaction) => {
    const snapshots = await Promise.all(refs.map(({ ref }) => transaction.get(ref)));
    let created = 0;
    snapshots.forEach((snapshot, index) => {
      if (snapshot.exists) return;
      transaction.create(refs[index].ref, refs[index].data);
      created += 1;
    });
    return created;
  });
}

async function main() {
  const demoSnapshot = await db.collection("public_merchants").where("isDemo", "==", true).get();
  const demoProfiles = demoSnapshot.docs.map((doc) => ({ uid: doc.id, ...doc.data() }));
  if (demoProfiles.length === 0) throw new Error("isDemo=true olan örnek işletme bulunamadı; hiçbir kayıt yazılmadı.");

  const merchantIds = demoProfiles.map((profile) => profile.uid);
  const [merchantsWithItems, merchantsWithServices] = await Promise.all([
    existingMerchantIds("items", merchantIds),
    existingMerchantIds("services", merchantIds)
  ]);

  let addedItems = 0;
  let addedServices = 0;
  let skippedItems = 0;
  let skippedServices = 0;
  let missingCatalog = 0;

  for (const profile of demoProfiles) {
    const catalog = catalogFor(profile);
    if (!catalog) {
      missingCatalog += 1;
      console.warn(`No sample catalog rule matched ${profile.uid}; leaving it unchanged.`);
      continue;
    }

    if (merchantsWithItems.has(profile.uid)) {
      skippedItems += 1;
    } else if (dryRun) {
      addedItems += catalog.items.length;
    } else {
      addedItems += await createMissingDocuments("items", profile.uid, catalog.items, itemDocument);
    }

    if (merchantsWithServices.has(profile.uid)) {
      skippedServices += 1;
    } else if (dryRun) {
      addedServices += 1;
    } else {
      addedServices += await createMissingDocuments("services", profile.uid, [catalog.service], serviceDocument);
    }
  }

  console.log(JSON.stringify({
    projectId,
    dryRun,
    demoMerchants: demoProfiles.length,
    addedItems,
    addedServices,
    merchantsWithExistingItemsPreserved: skippedItems,
    merchantsWithExistingServicesPreserved: skippedServices,
    unmatchedProfiles: missingCatalog
  }, null, 2));
}

main().catch((error) => {
  console.error("Demo catalog seeding failed:", error.code || error.message);
  process.exitCode = 1;
}).finally(async () => {
  await db.terminate().catch(() => undefined);
  process.exit(process.exitCode || 0);
});
