# Yerel güvenlik değişikliklerini Firebase'e aktarma

Bu değişiklikler uygulama ve Firebase kaynak dosyalarındadır; canlı Firebase projesine kendiliğinden gönderilmez.

## İşletme profili geçişi

Müşteri uygulaması artık işletme listesini `public_merchants` koleksiyonundan okur. Eski işletme belgelerini yeni kuralları dağıtmadan önce kopyalamak gerekir:

1. Firebase projesi için Admin SDK kimlik bilgilerini `GOOGLE_APPLICATION_CREDENTIALS` ile ayarlayın veya Firebase/Google Cloud CLI oturumuyla giriş yapın.
2. `functions` klasöründe `npm install` çalıştırın.
3. `npm run migrate:public-merchants` çalıştırın. Script `.firebaserc` içindeki varsayılan projeyi kullanır; `GCLOUD_PROJECT` veya `GOOGLE_CLOUD_PROJECT` ayarlanmışsa onu tercih eder.
4. Firestore'daki `public_merchants` belgelerinde yalnızca işletme adına açık alanların bulunduğunu kontrol edin.
5. Sonra Firebase CLI ile Firestore ve Storage kurallarını dağıtın.

## Hesap silme

Hesap silme ekranı Firebase Authentication hesabını önce siler. İlişkili kullanıcı belgesi, ilanlar, randevular, mesaj ekleri ve diğer uygulama kayıtları `cleanupDeletedAccount` Cloud Function tetikleyicisiyle temizlenir. Bu nedenle Cloud Function'ı da dağıtın ve çalıştığını Firebase Functions günlüklerinden kontrol edin.

Storage kuralları sohbet dosyası erişimini Firestore'daki konuşma kaydıyla doğrular. Firebase CLI bu kural için Firestore erişim iznini etkinleştirmeyi isteyebilir.

## Bildirimler

Android bildirimleri için Firebase Cloud Messaging'in projede etkin olması gerekir. `POST_NOTIFICATIONS` izni Android 13 ve sonrasında uygulama içindeki Bildirim Tercihleri ekranından istenir. Yeni sipariş, randevu ve sohbet bildirimlerini gönderen Cloud Functions'ı da dağıtın. Bildirim tercihleri kullanıcının özel Firestore alt koleksiyonunda tutulur.

Bu çalışma ödeme/PayTR entegrasyonunu değiştirmez.
