# Yerel güvenlik değişikliklerini Firebase'e aktarma

Bu değişiklikler uygulama ve Firebase kaynak dosyalarındadır; canlı Firebase projesine kendiliğinden gönderilmez.

## İşletme profili geçişi

Müşteri uygulaması artık işletme listesini `public_merchants` koleksiyonundan okur. Eski işletme belgelerini yeni kuralları dağıtmadan önce kopyalamak gerekir:

1. Firebase projesine Admin yetkili Google Cloud kimlik bilgileriyle giriş yapın (`gcloud auth application-default login`) veya servis hesabı kimlik bilgilerini `GOOGLE_APPLICATION_CREDENTIALS` ile ayarlayın. Bu proje klasöründe bu kimlik bilgileri bulunmaz.
2. `functions` klasöründe `npm install` çalıştırın.
3. `npm run migrate:public-merchants` çalıştırın. Script `.firebaserc` içindeki varsayılan projeyi kullanır; `GCLOUD_PROJECT` veya `GOOGLE_CLOUD_PROJECT` ayarlanmışsa onu tercih eder. Yalnızca eksik herkese açık profilleri ekler; var olan public profilleri veya `/users` kayıtlarını değiştirmez.
4. `npm run seed:demo-merchants` çalıştırın. Beş eski örnek işletmeyi yalnızca eksikse geri ekler ve ayrıca farklı alanlardan 50 örnek işletme profili ekler. Sabit örnek kimlikleri zaten varsa olduğu gibi bırakır.
5. İsterseniz `npm run seed:demo-catalog` ile yalnızca `isDemo=true` profillere uygun örnek katalog ekleyin. Mevcut ürün ve hizmet belgeleri korunur.
6. Firestore'daki `public_merchants` belgelerini gözden geçirin. Örnek profiller uygulamada “Örnek işletme” olarak işaretlenir.
7. Sonra Firebase CLI ile Firestore kurallarını, bileşik dizinleri ve Storage kurallarını dağıtın.

## Sipariş ve sohbet sorguları

Sipariş, randevu ve sohbet listeleri istemcide en yeni kayda göre sıralanır; bu ekranların açılması için ek bileşik indeks dağıtımı gerekmez. `firestore.indexes.json` dosyasındaki bileşik indeksler yalnızca zaman aşımı Functions sorguları içindir.

## Cloud Functions çalışma koşulu

Functions çalışma zamanı Node.js 22 olarak ayarlanmıştır. Firebase Functions bu projede sipariş zaman aşımı, hesap temizliği ve bildirim gibi sunucu görevlerini yürütür. Firebase'in güncel plan koşulları Functions dağıtımı için Blaze planı ve etkin faturalandırma hesabı gerektirir; proje Spark planındaysa bu görevler çalışmaz. Bu not kaynak kodunu değiştirmez ve Firebase planını yükseltmez. Node.js 20, 24 Mart 2026'da destek sonuna ulaştığı için Functions için 22 kullanılır.

Sipariş kartı ve hizmet depozitosu kodda demo durumundadır. Bu akışlar gerçek para tahsil etmez ve bu değişiklikler PayTR entegrasyonu eklemez.

## Hesap silme

Hesap silme ekranı Firebase Authentication hesabını önce siler. İlişkili kullanıcı belgesi, ilanlar, randevular, mesaj ekleri ve diğer uygulama kayıtları `cleanupDeletedAccount` Cloud Function tetikleyicisiyle temizlenir. Bu nedenle Cloud Function'ı da dağıtın ve çalıştığını Firebase Functions günlüklerinden kontrol edin.

Storage kuralları sohbet dosyası erişimini Firestore'daki konuşma kaydıyla doğrular. Firebase CLI bu kural için Firestore erişim iznini etkinleştirmeyi isteyebilir.

## Bildirimler

Android bildirimleri için Firebase Cloud Messaging'in projede etkin olması gerekir. `POST_NOTIFICATIONS` izni Android 13 ve sonrasında uygulama içindeki Bildirim Tercihleri ekranından istenir. Yeni sipariş, randevu ve sohbet bildirimlerini gönderen Cloud Functions'ı da dağıtın. Bildirim tercihleri kullanıcının özel Firestore alt koleksiyonunda tutulur.

Bu çalışma ödeme/PayTR entegrasyonunu değiştirmez.
