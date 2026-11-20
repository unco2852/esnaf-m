package com.example.esnafm.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esnafm.ui.theme.DarkInk
import com.example.esnafm.ui.theme.LightBeige
import com.example.esnafm.ui.theme.Turquoise
import com.example.esnafm.ui.theme.WarmCream

data class LegalSection(
    val number: Int,
    val title: String,
    val content: String
)

/**
 * 6698 Sayılı KVKK ve Gizlilik Politikası Kapsamlı Bilgilendirme Ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    val sections = listOf(
        LegalSection(1, "Proje Sürümü Hakkında", "Esnafım bu sürümde bir eğitim/prototip uygulamasıdır. Gerçek bir veri sorumlusu, destek adresi ve yayıma hazır hukuki aydınlatma metni tanımlanmamıştır."),
        LegalSection(2, "Uygulamanın Kapsamı", "Uygulama; müşteri ve işletme hesapları, işletme listeleri, ürün ve hizmet ilanları, sipariş/randevu talepleri, uygulama içi mesajlar ve Çırak asistanı özelliklerini içerir."),
        LegalSection(3, "İşlenebilecek Veri Türleri", "Kayıt ve profil bilgileri (ad, e-posta, telefon ve adres), işletme bilgileri, sipariş/randevu ayrıntıları, mesaj metinleri ve ekleri, destek talepleri ve izin verilirse yaklaşık cihaz konumu işlenebilir."),
        LegalSection(4, "Kullanım Amaçları", "Bilgiler hesap oturumunu yönetmek, işletme ve ilanları göstermek, sipariş/randevu taleplerini ilgili taraflara iletmek, sohbeti ve destek akışını sağlamak ve kullanıcının isteğiyle yakın işletmeleri bulmak için kullanılır."),
        LegalSection(5, "Çırak Asistanı", "Çırak'a yazılan ihtiyaç metni, uygun işletme kategorisini belirlemek ve işletme araması yapmak amacıyla Firebase AI Logic üzerinden işlenebilir. Konum izni ayrıca istenir; izin verilmezse konum olmadan arama yapılır."),
        LegalSection(6, "Verilerin Toplanması", "Veriler uygulamadaki kayıt, profil, adres, mesaj, sipariş ve randevu alanları ile kullanıcı izin verirse cihaz konum servisi üzerinden alınır."),
        LegalSection(7, "İşletmelerle Paylaşım", "Bir sipariş veya randevu talebi gönderildiğinde, talebi karşılayacak işletmeye işlem için gereken bilgiler iletilir. 'Esnafa ulaş' sohbetinde kullanıcı hesabı yerine UID gösterilir; mesaj ve ekler yalnızca konuşmanın taraflarına açılır."),
        LegalSection(8, "Kullanılan Bulut Servisleri", "Proje kodu Firebase Authentication, Cloud Firestore ve Cloud Storage kullanır. Çırak için Firebase AI Logic entegrasyonu bulunur. Gerçek projedeki bölge, saklama ayarı ve erişim yapılandırması Firebase Console'dan ayrıca doğrulanmalıdır."),
        LegalSection(9, "Bulut İşleme", "Firebase servisleri verileri proje yapılandırmasına göre bulut altyapısında işleyebilir. Bu eğitim sürümü, gerçek proje bölgesini veya yurt dışı aktarım koşullarını doğrulayan bir beyan değildir."),
        LegalSection(10, "Saklama ve Hesap Silme", "Hesap silme akışı Auth hesabını siler; ilişkili verilerin temizlenmesi Cloud Function dağıtımına bağlıdır. Bu işlev canlı projeye dağıtılmadan silmenin tüm verileri temizlediği varsayılmamalıdır."),
        LegalSection(11, "Erişim Kontrolleri", "Kaynakta kullanıcı profilleri ve sohbet verileri için Firebase Security Rules bulunur. Kuralların Firebase Console'a dağıtılmış sürümü yerel dosyalarla aynı olmalıdır; App Check ve gerçek proje ayarları ayrıca kontrol edilmelidir."),
        LegalSection(12, "Konum İzni", "Yakın işletme araması için konum izni isteğe bağlıdır. İzin verilmezse uygulamanın konumsuz kullanım akışları devam eder."),
        LegalSection(13, "Kullanıcı Kontrolü", "Kullanıcı profil ve adres bilgilerini uygulamada güncelleyebilir, mesaj gönderebilir veya silebilir ve hesabını silme akışını başlatabilir. Bulut tarafındaki işlemlerin tamamlanması backend dağıtımına bağlı olabilir."),
        LegalSection(14, "Destek İletişimi", "Bu ödev sürümünde gerçek destek e-posta adresi tanımlı değildir. Uygulamadaki destek formu, oturum açmış kullanıcının talebini Firebase'e kaydeder."),
        LegalSection(15, "Çocuklara Ait Veriler", "Bu prototip çocuklara yönelik tasarlanmamıştır. Gerçek yayından önce yaş sınırı ve çocuk verilerine ilişkin süreçler belirlenmelidir."),
        LegalSection(16, "Çerezler ve İzleme", "Android uygulaması web çerezi kullanmaz. Konum servisi yalnızca ilgili izin verildiğinde çağrılır."),
        LegalSection(17, "Güncelleme Tarihi", "Bu prototip bilgilendirmesi 20 Kasım 2026 tarihinde güncellenmiştir. Gerçek kullanım öncesinde veri sorumlusu bilgileri ve hukuki metin uygulamanın gerçek çalışma biçimine göre hazırlanmalıdır.")
    )

    Scaffold(
        containerColor = WarmCream,
        topBar = {
            TopAppBar(
                title = { Text("Gizlilik Politikası & KVKK 📄", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBeige,
                    titleContentColor = DarkInk
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBeige)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Turquoise)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Prototip bilgilendirmesi · 20.11.2026", fontWeight = FontWeight.Bold, color = DarkInk)
                        }
                    }
                }
            }

            items(sections) { sec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBeige)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${sec.number}. ${sec.title}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkInk
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sec.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkInk.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}
