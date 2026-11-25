# Esnafım — Sunum Öncesi Teknik Denetim Raporu

**Denetim tarihi:** 23 Kasım 2026  
**Kapsam:** Android kaynak kodu, Firebase kuralları/yapılandırması, Cloud Functions, testler, derleme ve statik analiz  
**Genel karar:** **Koşullu sunum adayı.** Debug ve Release derlemeleri başarılı; ancak telefonda/emülatörde kullanım akışları çalıştırılamadı ve canlı Firebase dağıtımı bu ortamdan doğrulanamadı. Bu rapor kod incelemesi ile yerel test sonuçlarını ayırır; üretim onayı veya canlı sistem garantisi değildir.

## 1. Yönetici özeti

Esnafım, Kotlin ve Jetpack Compose/Material 3 ile hazırlanmış, MVVM ve StateFlow kullanan bir Android uygulamasıdır. Firebase Authentication, Cloud Firestore, Firebase Storage, Firebase Cloud Messaging ve Firebase AI Logic kullanır. Müşteri ile işletme akışları, ürün siparişi, hizmet randevusu, sohbet, profil ve Çırak işletme bulma asistanı aynı uygulamada yer alır.

Sunum öncesinde giderilen belirgin bir Çırak API yapılandırma hatası vardı: `gemini-3.8-flash` isteğinde desteklenmeyen `candidateCount` gönderiliyordu. Firebase belgelerine göre bu modelde bu parametre 400 hatası üretir; `temperature` da yeni Gemini 3.x modellerinde desteklenmediğinden istekten çıkarıldı. Düzeltilmiş Android kaynakları, birim testleri ve APK derlemeleri geçti. Model çağrısının canlı Firebase projesine ulaştığı ise cihaz ve proje ayarları doğrulanamadığından henüz kanıtlanmış değil.

En büyük sunum/işletim sınırlamaları şunlardır:

1. Kart ekranları gerçek ödeme yapmaz. Kart bilgileri arayüzde girilir; uygulama bunları bir ödeme sağlayıcısına göndermez, ödeme/iade durumlarını Firestore'da örnek durum olarak işaretler.
2. Otomatik sipariş iptali, 30 günlük sohbet temizliği, hesap verisi temizliği ve bildirimler Cloud Functions dağıtılmışsa çalışır. Fonksiyon dağıtım durumu bu oturumda doğrulanmadı. Güncel Firebase koşullarında Cloud Functions dağıtımı Blaze faturalandırma planı gerektirir; kullanım ücretsiz kotaları aşarsa ücret doğabilir. [Firebase fiyatlandırma planları](https://firebase.google.com/docs/projects/billing/firebase-pricing-plans), [Cloud Functions SSS](https://firebase.google.com/docs/functions/faq-and-troubleshooting)
3. ADB'de bağlı telefon veya emülatör görünmedi. Gerçek ekran, Türkçe klavye, izinler, giriş/çıkış ve uçtan uca Firestore senaryoları denenemedi.
4. Release APK'sı derlendi ancak **imzasız**. Telefonda sunum için Debug APK'sı kullanılabilir; mağazaya veya dağıtıma verilecek release paketi ayrıca imzalanmalı.

## 2. Teknoloji ve mimari

| Alan | Projedeki uygulama |
|---|---|
| Platform / dil | Native Android, Kotlin |
| Arayüz | Jetpack Compose, Material 3 |
| Mimari | MVVM; ViewModel, StateFlow, repository katmanı |
| Gezinme | Jetpack Navigation Compose |
| Kimlik | Firebase Authentication |
| Veri | Cloud Firestore ve SnapshotListener akışları |
| Medya | Firebase Storage; ürün/hizmet görseli ve sohbet eki |
| Bildirim | Firebase Cloud Messaging ve Android bildirim kanalı |
| Yapay zekâ | Firebase AI Logic, Gemini `gemini-3.8-flash` |
| Sunucu işleri | Firebase Cloud Functions: sipariş zaman aşımı, sohbet temizliği, hesap temizliği ve bildirimler |
| Ayrı REST API | Yok; Android Firebase SDK'ları üzerinden doğrudan Firebase servisleri kullanılıyor |

Firebase App Check başlatması Debug varyantında Debug sağlayıcısını, Release varyantında Play Integrity sağlayıcısını seçiyor. Gerçek Firebase projesinde App Check zorlaması, Play Integrity kaydı ve Debug token ayarları bu denetim sırasında sınanamadı.

## 3. Ekran ve özellik envanteri

### Müşteri tarafı

- Açılış, rol seçimi, giriş ve kayıt.
- Keşfet/işletme listesi, arama ve kategoriler; Çırak asistanı.
- İşletme profili; işletmeye ulaş/uygulama içi sohbet.
- İşletme ürün ve hizmet detayları.
- Ürün siparişi: teslimat veya işletmeden alma, adres ve ödeme adımı, durum takibi ve iptal.
- Hizmet randevusu: zaman ve hizmet yeri seçimi, depozito, onay ve hizmet sonrası kalan ödeme akışı.
- Siparişlerim, randevularım ve önceki işlem kayıtları; favoriler ve ödeme tamamlama istekleri.
- Profil, adres defteri, bildirim tercihleri, güvenlik, gizlilik, kullanım şartları ve SSS.

### İşletme tarafı

- İşletme kayıt/profil yönetimi.
- Ürün ve stok yönetimi; hizmet ve depozito yönetimi.
- Sipariş ve randevu durumlarını yönetme.
- Hizmet tamamlandıktan sonra çevrimiçi bakiye talebi veya elden ödendi olarak tamamlama.
- Sohbet listesi, medya eki ve sohbeti bitirme.
- Bildirim tercihleri ve işletme ayarları.

## 4. İş mantığı incelemesi

### Siparişler

- Sipariş oluşturma işlemi Firestore transaction içinde ürünün güncel fiyat/stok bilgisini okur, stoku ayırır ve siparişi `pending` olarak oluşturur.
- Müşteri yalnızca esnaf onaylamadan önce iptal edebilir. İptalde stok transaction ile geri eklenir.
- Siparişe 30 dakikalık onay son tarihi yazılır. Gerçek otomatik iptal ve örnek iade kaydı, `expireUnapprovedOrders` Cloud Function'ı çalışırsa gerçekleşir.
- Kodda ödeme yöntemleri `mock_card`, `demo_paid` ve `demo_refunded` olarak modellenmiştir. Bu alan adları gerçek ödeme hareketi değildir.

### Randevu ve hizmet ödemeleri

- Randevu aynı işletmenin aynı tarih/saat slotuna transaction ile tek rezervasyon koyar; aynı saate eşzamanlı çift rezervasyonu veri katmanında engeller.
- Depozito üst sınırı arayüz/repository/rules tarafında toplam hizmet ücretinin yarısıdır.
- İşletme talebi onaylayıp reddedebilir; müşteri bekleyen veya onaylanan randevuyu iptal edebilir. İşletme hizmeti tamamlayıp kalan tutarı isteyebilir ya da nakit tahsil edildi olarak tamamlayabilir.
- Depozito kart alanı ile girilmiş görünse de kart bilgileri `AppointmentModel`'e gönderilmez; akış yalnızca yerel örnek ödeme durumunu kaydeder.

### Sohbet ve gizlilik

- Firestore mesajlarında profil adı yerine müşteri/işletme UID'si bulunur; erişim kuralları sohbet katılımcılarını sınırlar.
- Fotoğraf, video ve dosya ekleri Storage'a yüklenebilir. İşletmenin sohbeti kapatması mesaj ve ek temizleme akışını başlatır; 30 gün etkin olmayan sohbetleri temizleme ayrıca Cloud Function'a bağlıdır.
- UID göstermek, kullanıcının mesaj metnine veya ekine kendi telefonunu/adresini yazmasını teknik olarak engellemez. Arayüz kişisel bilgilerin paylaşılmamasını söyleyebilir; bu, içerik filtresi veya kesin gizlilik garantisi değildir.

### Çırak

- Çırak mesajı uygulama içi niyet/etiketlerle ve Gemini ile sınıflandırıp işletme önerisi üretmek üzere hazırlanmıştır; ürün/hizmet satırı tavsiye etmez.
- Gemini modeli ve yapılandırılmış JSON şeması derleniyor. Ancak gerçek cihazda doğru uzmanı seçtiği, fiyat sınırını uyguladığı ve Firebase AI Logic projesinin çağrıyı kabul ettiği uçtan uca test edilmedi.
- Güncel kodda Gemini yapılandırma hatası düzeltildi: `candidateCount` ve desteklenmeyen `temperature` kaldırıldı. Firebase dokümanları `gemini-3.8-flash` modelini destekliyor ve `candidateCount` kullanımının bu modelde 400 hatası verdiğini belirtiyor. [Desteklenen modeller](https://firebase.google.com/docs/ai-logic/models), [Model parametreleri](https://firebase.google.com/docs/ai-logic/model-parameters)

## 5. Bulunan riskler ve öncelik sırası

| Öncelik | Bulgu | Etki / önerilen işlem | Durum |
|---|---|---|---|
| **P1** | Gemini isteğinde desteklenmeyen parametre | Çırak yanıtı 400 hatası alabilir. `candidateCount` ve `temperature` kaldırıldı; canlı çağrı cihazda tekrar denenmeli. | **Kod düzeltildi; canlı doğrulama bekliyor** |
| **P1** | Kart ödemesi ve iade gerçek değil | Kart numarası, son kullanma ve CVV ödeme sağlayıcısına gitmiyor. Gerçek para tahsilatı/iadesi yok; Firestore yalnızca örnek durum kaydediyor. Sunumda bu akışın simülasyon olduğu açık anlatılmalı. | Tasarım gereği simülasyon |
| **P1** | Sunucu görevlerinin dağıtımı bilinmiyor | Functions dağıtılmadıysa 30 dakika sonunda otomatik iptal, 30 günlük sohbet temizliği, silinen hesap verisinin temizliği ve push bildirimleri kendiliğinden çalışmaz. Firebase CLI bu makinede yok; canlı Firebase durumu okunmadı. Dağıtım Blaze planı ve faturalandırma hesabı gerektirebilir. | Doğrulanamadı |
| **P1** | Release paketi imzasız | `app-release-unsigned.apk` çıktı; telefona/dağıtıma verilecek release için imzalama yapılandırması gerekir. Sınıf sunumunda Debug APK kullanılabilir. | İmzalama yapılmadı |
| **P1** | Gerçek cihaz testi yok | ADB'de bağlı cihaz/emülatör listelenmedi. Görsel taşma, izin diyaloğu, klavye, arka/çıkış onayı, Firebase giriş ve uçtan uca ekran akışları çalıştırılamadı. | Kullanıcı cihazında kontrol edilmeli |
| **P1** | Otomatik test kapsamı çok sınırlı | Mevcut 4 birim testi profil tamamlama (3) ve örnek toplama (1) ile sınırlı. Sipariş, iptal/stok iadesi, randevu slotu/depozito, sohbet erişimi, Çırak sınıflandırması, Security Rules ve Functions mantığı için otomatik senaryo yok. | İyileştirme gerekli |
| **P2** | Randevu ekranı dolu saatleri önceden gizlemiyor | Slot transaction'ı son anda çakışmayı engelliyor; ancak arayüz seçilebilir saatleri statik üretiyor. Dolu saat seçilirse kullanıcı form sonunda hata alabilir. Slotları ekranda canlı göstermek gerekir. | Kod incelemesinde bulundu |
| **P2** | Bekleyen randevular için zaman aşımı yok | İşletme yanıt vermez ve müşteri iptal etmezse slot rezervasyonu takvimde kalır. Ürün siparişindeki gibi açık bir randevu talebi süresi ve slot serbest bırakma politikası tanımlanabilir. | İş kuralı kararı gerekli |
| **P2** | Firestore ve Storage kuralları canlı emülatörde doğrulanmadı | Kaynakta kurallar bulunuyor; Firebase CLI/emülatör olmadığı için kuralların derlenmesi ve gerçek kullanıcı yetki testleri çalışmadı. `firebase.json`, indeksler ve JavaScript dosyaları biçim/söz dizimi kontrolünden geçti; bu, kuralların sunucuda etkin olduğunu kanıtlamaz. | Doğrulanamadı |
| **P2** | Örnek işletme kataloğunun canlı görünümü doğrulanmadı | Yerel seed scriptleri var, fakat canlı `public_merchants`, ürün ve hizmet kayıtları okunmadı. Sunum öncesi gerçek kayıtların ve Aksaray konumlarının Firebase Console'da doğrulanması gerekir. | Doğrulanamadı |
| **P2** | Listeler 200 kayıtla sınırlı | Müşteri/esnaf sipariş ve randevu akışlarında son 200 belge dinleniyor. Büyük hesaplarda daha eski geçmiş kayıtları görünmeyebilir; cursor tabanlı sayfalama eklenebilir. | Ölçek büyüyünce ele alınmalı |
| **P3** | Eski audit belgesinin gövdesi güncel değil | `APP_AUDIT_REPORT.md` içinde “sipariş yok/favoriler yerel” gibi güncel kaynakla uyuşmayan bölümler var. Bu yeni rapor güncel denetim için esas alınmalı; eski belge düzeltilmeli veya arşivlenmeli. | Dokümantasyon borcu |
| **P3** | Android Lint uyarıları | Lint başarılı, fakat 105 uyarı var. Büyük kısmı kullanılmayan kaynaklar (59), launcher ikon şekli/konumu (14), sürüm güncelleme önerileri ve eski API kullanımları. Derlemeyi durdurmuyor; sunum öncesi en görünür ikon ve manifest uyarıları temizlenebilir. | Temizlik önerisi |

## 6. Yapılan doğrulamalar

| Kontrol | Sonuç |
|---|---|
| `testDebugUnitTest` | **Başarılı:** 4 test, 0 başarısızlık, 0 hata, 0 atlanan |
| `assembleDebug` | **Başarılı:** Debug APK 31,088,020 bayt |
| `assembleRelease` | **Başarılı:** imzasız Release APK 10,836,431 bayt |
| `lintDebug` | **Başarılı:** 105 uyarı; hata yok |
| Functions JavaScript sözdizimi | **Başarılı:** `functions/index.js` ve `functions/scripts` altındaki 4 JavaScript dosyası kontrol edildi |
| Firebase/Firestore emülatör testi | Çalıştırılamadı: Firebase CLI kurulu değil |
| Telefon/emülatör UI testi | Çalıştırılamadı: ADB'de bağlı cihaz yok |
| Canlı Firebase/AI isteği | Bu oturumda çalıştırılmadı; canlı veriye yazılmadı |

APK yolları:

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

## 7. Sunumdan önce telefonda uygulanacak kısa kontrol listesi

1. Debug APK'yı telefona kur; müşteri ve işletme hesabıyla ayrı ayrı giriş yap.
2. Müşteri ekranında Çırak'a “boyacı arıyorum”, “kapıda kaldım”, “musluk su akıtıyor” ve “1000 TL'yi aşmasın” yaz; çıkan işletme türünü ve bütçe filtresini kontrol et.
3. İşletme sayfasından sohbet başlat; metin, fotoğraf ve uygun boyutta bir dosya gönder; işletme sohbeti kapattıktan sonra yeniden gönderimin engellendiğini doğrula.
4. Teslimat ve işletmeden alma siparişlerini dene; adresin yalnızca gerekli teslimat akışında istendiğini, müşteri telefon/adının işletmeye gitmediğini ve stokun azaldığını kontrol et.
5. Ürün kart ekranındaki “ödeme”nin gerçek tahsilat olmadığını sunumda açıkça belirt; sahte kart bilgisi kullan.
6. Depozitosuz ve depozitolu randevu akışını dene; %50 üzeri depozitonun engellendiğini, dolu saat çakışmasının hata verdiğini ve iptalin slotu açtığını kontrol et.
7. Firebase Console'da kullanılan projenin doğru olduğunu, işletme/katalog belgelerinin sunum verisi içerdiğini ve gerekli Functions/rules/index dağıtımlarının yapıldığını kontrol et.

## 8. Son karar

Kod şu an **Debug sunum APK'sı üretme açısından hazır**: birim testleri, Debug/Release derlemeleri ve Android Lint tamamlandı. Ancak **canlı uygulama akışının bütünü doğrulanmış değil**. Sunumdan önce gerçek telefonda kısa smoke test yapılmalı; otomatik sipariş iptali, hesap/sohbet temizliği ve bildirim gerekiyorsa Firebase Functions dağıtımı kontrol edilmeli. Uygulama gerçek ödeme ürünü olarak sunulmamalı; mevcut kart akışı simülasyondur.

