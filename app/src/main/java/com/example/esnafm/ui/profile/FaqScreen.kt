package com.example.esnafm.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.esnafm.R
import com.example.esnafm.ui.theme.DarkInk
import com.example.esnafm.ui.theme.LightBeige
import com.example.esnafm.ui.theme.Turquoise
import com.example.esnafm.ui.theme.WarmCream

data class FaqItem(
    val id: Int,
    val question: String,
    val answer: String,
    val category: String
)

/**
 * Sıkça Sorulan Sorular Ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tümü") }

    val categories = listOf("Tümü", "Hesap & Profil", "Randevular", "Siparişler", "Esnaf İşlemleri", "Güvenlik & Gizlilik")

    val faqList = remember {
        listOf(
            FaqItem(1, "Esnafım nedir ve nasıl çalışır?", "Esnafım, mahalle esnafları ile müşterileri buluşturan dijital bir rehberdir. Müşteriler ürün inceleyip randevu alabilir; esnaflar ise dükkanlarını dijital ortamda yönetebilir.", "Hesap & Profil"),
            FaqItem(2, "Nasıl hesap oluşturabilirim?", "Açılış ekranında Müşteri veya Esnaf seçeneğini seçtikten sonra 'Kayıt Ol' formunu doldurarak anında hesap oluşturabilirsiniz.", "Hesap & Profil"),
            FaqItem(3, "Şifremi unuttum, ne yapmalıyım?", "Giriş ekranında veya Profil > Güvenlik sekmesinde 'Şifremi Unuttum' butonuna basarak e-posta adresinize sıfırlama bağlantısı isteyebilirsiniz.", "Güvenlik & Gizlilik"),
            FaqItem(4, "E-posta adresimi nasıl değiştiririm?", "Profil > Güvenlik sekmesinden mevcut şifrenizi doğrulayarak güvenli şekilde yeni e-posta adresinizi belirleyebilirsiniz.", "Hesap & Profil"),
            FaqItem(5, "Telefon numaramı nasıl güncellerim?", "Profilim > Profili Düzenle ekranından iletişim telefon numaranızı güncelleyebilirsiniz.", "Hesap & Profil"),
            FaqItem(6, "Profil bilgilerimi nasıl düzenlerim?", "Profilim ekranındaki 'Profili Düzenle' butonuna basarak ad soyad, adres ve dükkan bilgilerinizi güncelleyebilirsiniz.", "Hesap & Profil"),
            FaqItem(7, "Profil doluluk oranı ne anlama gelir?", "Profilinizin ne kadar eksiksiz doldurulduğunu gösterir. %100 doluluk oranı müşteri ve esnafların süreçlerini hızlandırır.", "Hesap & Profil"),
            FaqItem(8, "Mahallemdeki esnafları nasıl bulabilirim?", "Keşfet sekmesinden arama çubuğunu veya kategori çiplerini kullanarak çevrenizdeki tüm esnafları listeleyebilirsiniz.", "Esnaf İşlemleri"),
            FaqItem(9, "Bir işletmeyi favorilerime nasıl eklerim?", "Esnaf kartlarının üzerindeki kalp ikonuna basarak işletmeyi Favorilerim sekmesine ekleyebilirsiniz.", "Esnaf İşlemleri"),
            FaqItem(10, "Randevu nasıl oluşturulur?", "İlgili esnafın detay sayfasındaki hizmetlerden 'Randevu Al' butonuna basıp tarih ve saat seçerek talep oluşturabilirsiniz.", "Randevular"),
            FaqItem(11, "Randevumu nasıl görüntülerim?", "Müşteri portalındaki 'Randevularım' sekmesinden onaylı ve beklemedeki tüm randevularınızı görebilirsiniz.", "Randevular"),
            FaqItem(12, "Randevumu nasıl iptal eder veya değiştiririm?", "Randevularım sekmesindeki ilgili randevu kartından iptal talebinizi iletebilirsiniz.", "Randevular"),
            FaqItem(13, "Siparişlerimi nereden takip edebilirim?", "Müşteri ana sayfasındaki 'Siparişlerim' sekmesinden geçmiş ve aktif sipariş durumlarınızı canlı izleyebilirsiniz.", "Siparişler"),
            FaqItem(14, "Siparişimi nasıl iptal edebilirim?", "Hazırlanmaya başlamamış siparişleriniz için Siparişlerim sekmesinden iptal butonunu kullanabilirsiniz.", "Siparişler"),
            FaqItem(15, "Esnaf olarak işletme bilgilerimi nasıl düzenlerim?", "Esnaf panelindeki 'İşletme Ayarları' veya Profilim sayfasından dükkan adı, kategori ve adresi değiştirebilirsiniz.", "Esnaf İşlemleri"),
            FaqItem(16, "Çalışma saatlerimi nasıl güncellerim?", "Esnaf paneli İşletme Ayarları bölümünden dükkanınızın açılış ve kapanış saatlerini güncelleyebilirsiniz.", "Esnaf İşlemleri"),
            FaqItem(17, "Bildirimleri nasıl açıp kapatabilirim?", "Profil > Bildirim Tercihleri menüsünden randevu ve sipariş bildirim ayarlarını değiştirebilirsiniz.", "Güvenlik & Gizlilik"),
            FaqItem(18, "Hesabımın güvenliğini nasıl koruyabilirim?", "Güçlü şifreler kullanarak ve e-posta adresinizi doğrulanmış tutarak hesabınızı güvenceye alabilirsiniz.", "Güvenlik & Gizlilik"),
            FaqItem(19, "Kişisel verilerim nasıl işleniyor?", "Verileriniz KVKK 6698 uyarınca yalnızca hizmet sunumu amacıyla işlenir; dilediğinizde Profil > Gizlilik Politikası ekranından detayları okuyabilirsiniz.", "Güvenlik & Gizlilik"),
            FaqItem(20, "Hesabımı nasıl silebilir veya destek isteyebilirim?", "Profil > Güvenlik menüsünün altındaki 'Hesabımı Kalıcı Olarak Sil' butonundan veya Sorun Bildir ekranından destek alabilirsiniz.", "Güvenlik & Gizlilik")
        )
    }

    val filteredFaq = faqList.filter { item ->
        val matchesCategory = selectedCategory == "Tümü" || item.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() || item.question.contains(searchQuery, ignoreCase = true) || item.answer.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        containerColor = WarmCream,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.faq_screen_title), fontWeight = FontWeight.Bold)
                },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Arama Çubuğu
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Sorularda ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Turquoise) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Kategori Çipleri
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Soru-Cevap Akordeon Listesi
            if (filteredFaq.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, modifier = Modifier.size(56.dp), tint = Turquoise)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aradığınız kriterlere uygun soru bulunamadı.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        color = DarkInk
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredFaq) { faq ->
                        FaqAccordionCard(faq = faq)
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqAccordionCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightBeige),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${faq.id}. ${faq.question}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Turquoise
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkInk.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}