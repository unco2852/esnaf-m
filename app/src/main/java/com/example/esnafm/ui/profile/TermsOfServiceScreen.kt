package com.example.esnafm.ui.profile

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

/**
 * Kullanım Koşulları ve Sözleşme Ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    onNavigateBack: () -> Unit
) {
    val sections = listOf(
        LegalSection(1, "Taraflar ve Amaç", "Bu kullanım koşulları, Esnafım mobil platformu ('Esnafım') ile platforma üye olan müşteri ve esnaf kullanıcılar arasındaki şartları düzenler."),
        LegalSection(2, "Hizmetin Kapsamı", "Esnafım; mahalle esnaflarının ürün ve hizmetlerini sergilemelerine, müşterilerin randevu alıp sipariş oluşturmalarına olanak sağlayan aracı bir rehber platformdur."),
        LegalSection(3, "Üyelik ve Hesap Güvenliği", "Kullanıcılar verdikleri bilgilerin doğruluğundan ve hesap şifrelerinin güvenliğinden sorumludur."),
        LegalSection(4, "Müşteri Yükümlülükleri", "Müşteriler, oluşturdukları randevulara ve sipariş taleplerine dürüstlük kuralları çerçevesinde uymayı kabul eder."),
        LegalSection(5, "Esnaf Yükümlülükleri", "Esnaflar, sergiledikleri ürün ve hizmetlerin fiyat ve açıklama bilgilerinin doğru olduğunu kabul eder."),
        LegalSection(6, "Hizmetin Kesintisizliği ve Sorumluluk Sınırı", "Esnafım, teknik altyapı güncellemeleri veya kesintiler sebebiyle oluşabilecek aksaklıklardan sorumlu tutulamaz."),
        LegalSection(7, "Sözleşme Değişiklikleri ve Yürürlük", "Esnafım kullanım koşullarını güncelleme hakkını saklı tutar. Güncel koşullar uygulama üzerinden yayınlandığı tarihte yürürlüğe girer.")
    )

    Scaffold(
        containerColor = WarmCream,
        topBar = {
            TopAppBar(
                title = { Text("Kullanım Koşulları 📜", fontWeight = FontWeight.Bold) },
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
                            Text("Kullanım Koşulları Yürürlük Tarihi: 01.01.2026", fontWeight = FontWeight.Bold, color = DarkInk)
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