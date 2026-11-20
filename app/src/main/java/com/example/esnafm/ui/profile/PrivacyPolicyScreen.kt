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
        LegalSection(1, "Veri Sorumlusunun Kimliği ve İletişim Bilgileri", "Esnafım Platformu ('Esnafım'), 6698 sayılı Kişisel Verilerin Korunması Kanunu ('KVKK') uyarınca veri sorumlusu sıfatıyla hareket etmektedir. İletişim: destek@esnafm.com"),
        LegalSection(2, "Aydınlatma Metninin Amacı ve Kapsamı", "Bu metin, platformumuzu kullanan müşteri ve esnafların kişisel verilerinin hangi amaçlarla işlendiğini ve haklarını açıklamak amacıyla hazırlanmıştır."),
        LegalSection(3, "İşlenen Kişisel Veri Kategorileri", "İşlenen veriler: Kimlik Bilgileri (Ad, Soyad), İletişim Bilgileri (E-posta, Telefon, Adres), İşlem Bilgileri (Randevular, Siparişler) ve Kullanım Bilgileridir."),
        LegalSection(4, "Kişisel Verilerin İşlenme Amaçları", "Verileriniz; üyelik süreçlerinin yürütülmesi, müşteri-esnaf iletişiminin sağlanması, randevu ve sipariş taleplerinin iletilmesi ve platform güvenliğinin sağlanması amaçlarıyla işlenir."),
        LegalSection(5, "Kişisel Verilerin İşlenmesinin Hukuki Sebepleri", "KVKK Madde 5 uyarınca; sözleşmenin kurulması ve ifası, hukuki yükümlülüklerin yerine getirilmesi ve meşru menfaatlerimiz hukuki sebeplerine dayanılmaktadır."),
        LegalSection(6, "Kişisel Verilerin Toplanma Yöntemleri", "Kişisel verileriniz, mobil uygulama üzerinden doldurduğunuz kayıt, profil, randevu ve sipariş formları aracılığıyla elektronik ortamda toplanmaktadır."),
        LegalSection(7, "Veri Alıcıları ve Aktarım Kategorileri", "Verileriniz yalnızca hizmetin ifası için gerekli olan ilgili mahalle esnafı veya müşterisi ile ve yasal zorunluluk halinde yetkili kamu kurumlarıyla paylaşılır."),
        LegalSection(8, "Firebase ve Bulut Hizmet Sağlayıcıları", "Esnafım, veri depolama ve kimlik doğrulama altyapısı olarak Google Firebase servislerini (Firebase Auth ve Cloud Firestore) kullanmaktadır."),
        LegalSection(9, "Yurt Dışına Veri Aktarımı", "Firebase bulut sunucularının yurt dışında bulunması sebebiyle verileriniz güvenli sunucu standartlarında muhafaza edilmektedir."),
        LegalSection(10, "Saklama Süreleri ve Veri Silme Politikası", "Kişisel verileriniz üyeliğiniz devam ettiği sürece saklanır. Hesabınızı sildiğinizde verileriniz mevzuattaki yasal süreler sonunda kalıcı olarak silinir."),
        LegalSection(11, "Veri Güvenliği ve Teknik Tedbirler", "Verilerinizin yetkisiz erişimlere karşı korunması amacıyla e-posta doğrulaması, şifreli veritabanı iletişimi ve yetkilendirme kuralları uygulanmaktadır."),
        LegalSection(12, "6698 Sayılı KVKK Kapsamındaki Haklarınız", "KVKK Madde 11 uyarınca; verilerinizin işlenip işlenmediğini öğrenme, düzeltilmesini veya silinmesini talep etme haklarına sahipsiniz."),
        LegalSection(13, "Veri Sorumlusuna Başvuru Yöntemleri", "Haklarınıza ilişkin taleplerinizi destek@esnafm.com e-posta adresine iletebilirsiniz."),
        LegalSection(14, "İlgili Kişi Başvurularının Değerlendirilmesi", "Başvurularınız en geç 30 gün içerisinde ücretsiz olarak değerlendirilip tarafınıza yazılı veya elektronik ortamda yanıt verilecektir."),
        LegalSection(15, "Çocuklara Ait Kişisel Veriler", "Esnafım platformu 18 yaşın altındaki bireylere yönelik değildir ve çocuklara ait verileri kasten işlemez."),
        LegalSection(16, "Çerezler ve İzleme Teknolojileri", "Mobil uygulamamızda oturum güvenliği için yalnızca teknik çerezler kullanılmaktadır."),
        LegalSection(17, "Politika Değişiklikleri ve Yürürlük Tarihi", "Bu politika 1 Ocak 2026 tarihinde yürürlüğe girmiştir. Güncellemeler uygulama üzerinden yayınlanır.")
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
                            Text("Aydınlatma Metni Son Güncelleme: 01.01.2026", fontWeight = FontWeight.Bold, color = DarkInk)
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