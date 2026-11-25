package com.example.esnafm.data.legal

import com.example.esnafm.data.model.UserRole

object LegalDocumentVersions {
    const val PRE_INFORMATION = "1.0.0"
    const val CUSTOMER_AGREEMENT = "1.0.0"
    const val MERCHANT_AGREEMENT = "1.0.0"

    fun agreementFor(role: UserRole): String =
        if (role == UserRole.MERCHANT) MERCHANT_AGREEMENT else CUSTOMER_AGREEMENT
}

data class LegalDocumentSection(
    val title: String,
    val content: String
)

data class LegalDocument(
    val id: String,
    val title: String,
    val version: String,
    val updatedAt: String = "24 Kasım 2026",
    val sections: List<LegalDocumentSection>
)

object LegalDocuments {
    const val PRE_INFORMATION_ID = "pre_information"
    const val CUSTOMER_AGREEMENT_ID = "customer_agreement"
    const val MERCHANT_AGREEMENT_ID = "merchant_agreement"

    val preInformation = LegalDocument(
        id = PRE_INFORMATION_ID,
        title = "Üyelik Ön Bilgilendirme Metni",
        version = LegalDocumentVersions.PRE_INFORMATION,
        sections = listOf(
            LegalDocumentSection(
                "1. Platform ve proje sürümü",
                "Esnafım; müşterilerin yerel işletmeleri bulmasına, işletme profillerini ve kataloglarını incelemesine, uygulama içi mesaj, sipariş ve randevu akışlarını kullanmasına yönelik bir Android proje uygulamasıdır. Bu sürüm eğitim ve proje sunumu amacı taşır. Uygulamanın gerçek hizmet sağlayıcısı veya işletmeci tüzel kişisi bu proje sürümünde tanımlanmamıştır."
            ),
            LegalDocumentSection(
                "2. Kullanıcı rolleri ve hizmet",
                "Kayıt sırasında müşteri veya işletme rolü seçilir. Müşteriler işletme araması yapabilir, işletmelere mesaj gönderebilir ve uygulamada sunulan sipariş/randevu akışlarını deneyebilir. İşletme hesapları profil ve katalog bilgilerini yönetmek ve gelen talepleri takip etmek için tasarlanmıştır. Kayıt, her işletmenin doğrulandığı veya her talebin kabul edileceği anlamına gelmez."
            ),
            LegalDocumentSection(
                "3. Örnek kayıtlar ve bilgi doğruluğu",
                "Uygulamada bazı işletme, ürün ve hizmet kayıtları örnek amaçlıdır; gerçek satıcı, gerçek stok, kesin fiyat, çalışma saati veya hizmet uygunluğu olarak kabul edilmemelidir. Gerçek bir işlem yapmadan önce ilgili işletmenin bilgilerini ayrıca doğrulayın."
            ),
            LegalDocumentSection(
                "4. Ücret ve ödeme",
                "Bu proje sürümünde üyelik oluşturmak için uygulama içinde ücret alınmaz. Kart alanı ve ödeme durumu ekranları ödeme akışını göstermek için hazırlanmış simülasyondur; gerçek kart tahsilatı, ödeme sağlayıcısına aktarım veya gerçek para iadesi yapılmaz. Gerçek kart bilgilerinizi bu sürüme girmeyin."
            ),
            LegalDocumentSection(
                "5. Sipariş ve randevular hakkında",
                "Uygulamadaki kayıt onayı, tek başına sipariş veya randevunun kabul edildiği anlamına gelmez. İlgili işletmenin onay durumu uygulamada ayrıca gösterilir. Bu üyelik metni belirli bir sipariş veya randevu için satıcı/sağlayıcı bilgisi, ürün/hizmet özellikleri, toplam fiyat, teslimat, iptal veya ödeme ayrıntılarını içermez ve işlem öncesi sunulması gereken işlem bazlı bilgilendirmenin yerine geçmez."
            ),
            LegalDocumentSection(
                "6. Çırak ve işletme önerileri",
                "Çırak, kullanıcının yazdığı ihtiyacı uygun işletme alanıyla eşleştirmeye ve uygulamada kayıtlı işletmeleri bulmaya çalışır. Öneriler otomatik üretilir; bulunabilirlik, uzmanlık, fiyat veya sonuç garantisi vermez. Acil veya güvenlik açısından kritik konularda yalnızca uygulama önerisine dayanmayın."
            ),
            LegalDocumentSection(
                "7. Hesap ve veriler",
                "Kayıt ve kullanım sırasında hesap, profil, işletme, mesaj, sipariş, randevu ve izin verilirse konum bilgileri uygulamanın çalışması için işlenebilir. Kişisel veriler hakkındaki amaçlar, hukuki sebepler, alıcılar, saklama ve haklar için kayıt ekranındaki veya profildeki KVKK Aydınlatma Metnini okuyun. Bu ön bilgilendirme metnini okuma bildirimi, tek başına kişisel verilerin işlenmesine açık rıza değildir."
            ),
            LegalDocumentSection(
                "8. Destek ve hesabı kapatma",
                "Uygulamayla ilgili soru veya sorunlar için uygulama içindeki destek talebi alanı kullanılabilir. Hesap silme akışı profilde bulunur; ilişkili bulut verilerinin temizlenmesi sunucu tarafı yapılandırmasına bağlı olabileceğinden bu proje sürümünde silme işleminin tüm sistemlerde tamamlandığı garanti edilmez."
            ),
            LegalDocumentSection(
                "9. Onayların anlamı",
                "Kayıt ekranındaki ilk kutu, bu üyelik ön bilgilendirme metnini okuduğunuzu bildirir. İkinci kutu, seçtiğiniz role ait Kullanıcı Sözleşmesini kabul ettiğinizi bildirir. KVKK Aydınlatma Metni ayrı olarak okunabilir; pazarlama izni veya kişisel veri işleme açık rızası bu iki kutunun içine dahil değildir."
            )
        )
    )

    val customerAgreement = LegalDocument(
        id = CUSTOMER_AGREEMENT_ID,
        title = "Müşteri Kullanıcı Sözleşmesi",
        version = LegalDocumentVersions.CUSTOMER_AGREEMENT,
        sections = listOf(
            LegalDocumentSection(
                "1. Taraflar ve kapsam",
                "Bu sözleşme, Esnafım proje uygulamasını müşteri rolüyle kullanan kişi ile Esnafım platformu arasındaki kullanım şartlarını açıklar. Bu proje sürümünde gerçek hizmet sağlayıcının ticari unvanı ve iletişim adresi tanımlanmamıştır; metin eğitim/prototip kullanımı için hazırlanmıştır."
            ),
            LegalDocumentSection(
                "2. Hesap ve kullanıcı bilgileri",
                "Kullanıcı, kayıt sırasında verdiği bilgilerin kendisine ait ve mümkün olduğunca doğru olmasına dikkat eder; şifresini başkalarıyla paylaşmaz ve hesabındaki işlemleri takip eder. Hesap güvenliğinin tehlikeye girdiğini düşünürse uygulama içi destek alanına başvurur."
            ),
            LegalDocumentSection(
                "3. Platformun işlevi",
                "Esnafım işletme keşfi ve taraflar arasındaki dijital iletişim için tasarlanmıştır. Çırak, işletme aramasına yardımcı olur ancak işletmenin kimliğini, yeterliliğini, fiyatını, stokunu veya hizmet sonucunu garanti etmez. İşletme profilindeki bilgi ve katalog içeriği ilgili işletme tarafından sağlanabilir."
            ),
            LegalDocumentSection(
                "4. Sipariş ve randevu talepleri",
                "Sipariş veya randevu talebi oluşturmak, ilgili işletme uygulamada kabul ettiğini bildirene kadar kesin kabul anlamına gelmez. Kullanıcı ürün, hizmet, miktar, tarih, saat, adres ve ücret bilgilerini işlem ekranında kontrol eder. İptal ve durum adımları, uygulamada ilgili talep için sunulan seçeneklere göre yürütülür. İşlem bazlı fiyat, teslimat/ifa ve cayma/iptal bilgileri gerekiyorsa kullanıcıdan işlem kesinleştirilmeden önce ayrıca gösterilmelidir."
            ),
            LegalDocumentSection(
                "5. Ödeme simülasyonu",
                "Bu proje sürümündeki kart girişi ve ödeme durumları gerçek ödeme altyapısına bağlı değildir. Gerçek para tahsilatı veya iadesi yapılmaz. Kullanıcı bu sürümde gerçek kart numarası, güvenlik kodu veya banka bilgisi girmemelidir."
            ),
            LegalDocumentSection(
                "6. Mesajlaşma ve kişisel bilgi",
                "Kullanıcı, mesaj ve dosya paylaşırken kendi veya başkasına ait telefon, adres, kimlik, kart bilgisi gibi gereksiz özel bilgileri göndermemelidir. Sohbette kullanıcı kimliğinin gösterilmesi, mesajın içine yazılan veya dosyada bulunan bilgilerin gizli kalacağını garanti etmez. Sipariş ve randevu için gerekli bilgiler yalnızca ilgili işlem amacıyla kullanılmalıdır."
            ),
            LegalDocumentSection(
                "7. Uygun kullanım",
                "Kullanıcı uygulamayı hukuka uygun ve başkalarının haklarına saygılı biçimde kullanır. Yanıltıcı talep oluşturmaz, taciz edici içerik göndermez, sisteme zarar vermeye veya başka bir kullanıcının hesabına erişmeye çalışmaz; yüklediği görsel ve dosyaları paylaşma hakkına sahip olur."
            ),
            LegalDocumentSection(
                "8. İçerik ve hizmet kesintileri",
                "Örnek işletme ve katalog bilgileri değiştirilebilir veya kaldırılabilir. İnternet, Firebase servisleri, yapay zekâ veya sunucu işlerinin geçici olarak çalışmaması mümkündür. Bu eğitim sürümünde kesintisiz çalışma taahhüdü verilmez."
            ),
            LegalDocumentSection(
                "9. Sözleşmenin sürümü",
                "Bu müşteri sözleşmesi 1.0.0 sürümüdür ve 24 Kasım 2026 tarihinde hazırlanmıştır. Değişiklik yapılırsa yeni sürüm ve yürürlük bilgisi kullanıcıya gösterilmelidir. Gerçek kullanıma açılmadan önce işletmeci kimliği, başvuru kanalı ve işlem bazlı tüketici metinleri tamamlanmalıdır."
            )
        )
    )

    val merchantAgreement = LegalDocument(
        id = MERCHANT_AGREEMENT_ID,
        title = "İşletme Kullanıcı Sözleşmesi",
        version = LegalDocumentVersions.MERCHANT_AGREEMENT,
        sections = listOf(
            LegalDocumentSection(
                "1. Taraflar ve kapsam",
                "Bu sözleşme, Esnafım proje uygulamasını işletme rolüyle kullanan hesap ile platform arasındaki kullanım şartlarını açıklar. Bu proje sürümünde platformu işleten gerçek/tüzel kişi bilgileri tanımlanmamıştır; metin eğitim/prototip kullanımı için hazırlanmıştır."
            ),
            LegalDocumentSection(
                "2. İşletme hesabı ve yetki",
                "İşletme hesabı açan kişi, işletme adına işlem yapmaya yetkili olduğunu ve kayıt sırasında verdiği işletme/iletişim bilgilerinin doğru olmasını sağlamalıdır. Yetki veya işletme bilgisi değişirse profil güncellenmelidir. Hesap şifresi ve erişimi korunmalıdır."
            ),
            LegalDocumentSection(
                "3. Profil, ürün ve hizmet bilgileri",
                "İşletme; adı, kategorisi, açıklaması, adresi, çalışma saati, fiyatı, ürün/hizmet özellikleri, stok ve uygunluk bilgisini doğru ve güncel tutmaktan sorumludur. Müşterilere gösterilmek üzere girilen işletme adı, iş adresi, işletme telefonu, çalışma saati ve katalog içeriği herkese açık işletme profilinde gösterilebilir. Kişisel yetkili kişi bilgileri işletme profiline eklenmemelidir."
            ),
            LegalDocumentSection(
                "4. Taleplere yanıt verme",
                "İşletme, gelen sipariş ve randevu taleplerini panelinden takip eder; kabul, ret, hazırlık, tamamlama veya iptal durumlarını doğru biçimde günceller. Talep oluşturulması tek başına işletme tarafından kabul edildiği anlamına gelmez. Sunulan ürün veya hizmeti yerine getiremeyecekse kullanıcıyı zamanında bilgilendirir."
            ),
            LegalDocumentSection(
                "5. Müşteri verileri ve sohbet",
                "İşletme, müşteriye ait işlem ve mesaj bilgilerini yalnızca ilgili talebi yanıtlamak ve tamamlamak için kullanır; uygulama dışı reklam, paylaşım veya ilgisiz amaçlarla kullanmaz. Sohbet ekranında kullanıcı kimliği görünse bile mesaj veya eklerde kişisel bilgiler bulunabilir. İşletme bu bilgileri korumalı ve gereksiz kopya oluşturmamalıdır."
            ),
            LegalDocumentSection(
                "6. Ödeme ve platformun rolü",
                "Bu proje sürümünde kart ekranı gerçek tahsilat/iade yapmaz; ödeme durumları simülasyondur. İşletme, uygulamada ödeme yapılmış gibi görünen bir durumu gerçek para transferi kanıtı olarak kabul etmemelidir. Gerçek ödeme, komisyon veya tahsilat koşulları bu prototipte tanımlanmamıştır."
            ),
            LegalDocumentSection(
                "7. Uygun kullanım ve içerik hakları",
                "İşletme hukuka uygun ürün ve hizmet sunar; yanıltıcı fiyat, stok veya uzmanlık beyanında bulunmaz. Profil ve katalog görselleri dahil yüklediği içerikleri paylaşma hakkına sahip olmalıdır. Başka işletme veya kullanıcıları taklit etmek, taciz etmek, sistemi kötüye kullanmak veya hukuka aykırı içerik yayımlamak yasaktır."
            ),
            LegalDocumentSection(
                "8. Kullanıcı hesabı ve hizmetin sınırları",
                "İşletme hesabı bu proje sürümünde doğrulanmış ticari kayıt veya ruhsat anlamına gelmez. Örnek profil kayıtları gerçek bir işletmenin çalıştığını, fiyatını veya hizmet kapasitesini kanıtlamaz. Uygulama ve bulut servisleri geçici olarak kullanılamayabilir; zamanlanmış işlemler sunucu yapılandırmasına bağlı olabilir."
            ),
            LegalDocumentSection(
                "9. Sözleşmenin sürümü",
                "Bu işletme sözleşmesi 1.0.0 sürümüdür ve 24 Kasım 2026 tarihinde hazırlanmıştır. Gerçek kullanımdan önce platform işletmecisinin kimliği, destek/başvuru bilgileri, varsa ücret ve komisyonlar, doğrulama şartları ve veri işleme süreçleri tamamlanmalı; metin uzman hukuk incelemesinden geçirilmelidir."
            )
        )
    )

    val all = listOf(preInformation, customerAgreement, merchantAgreement)

    fun agreementFor(role: UserRole): LegalDocument =
        if (role == UserRole.MERCHANT) merchantAgreement else customerAgreement

    fun byId(id: String): LegalDocument? = all.firstOrNull { it.id == id }
}
