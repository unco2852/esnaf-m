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
import com.example.esnafm.data.legal.LegalDocuments
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
    Scaffold(
        containerColor = WarmCream,
        topBar = {
            TopAppBar(
                title = { Text("Ön Bilgilendirme ve Sözleşmeler", fontWeight = FontWeight.Bold) },
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
                            Text("Esnafım proje sürümü · 24.11.2026", fontWeight = FontWeight.Bold, color = DarkInk)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Bu metinler müşteri ve işletme kayıtlarında okunabilir. Eğitim/prototip sürümü için hazırlanmıştır; gerçek kullanıma geçmeden önce işletmeci bilgileri ve işlem koşulları tamamlanıp hukuki incelemeden geçirilmelidir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkInk.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            items(LegalDocuments.all, key = { it.id }) { document ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DarkInk
                    )
                    Text(
                        text = "Sürüm ${document.version} · ${document.updatedAt}",
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkInk.copy(alpha = 0.7f)
                    )
                    document.sections.forEach { section ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = LightBeige)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DarkInk
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = section.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkInk.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
