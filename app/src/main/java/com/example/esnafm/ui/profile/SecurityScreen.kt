package com.example.esnafm.ui.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esnafm.ui.theme.DarkInk
import com.example.esnafm.ui.theme.LightBeige
import com.example.esnafm.ui.theme.SkyBlue
import com.example.esnafm.ui.theme.Terracotta
import com.example.esnafm.ui.theme.Turquoise
import com.example.esnafm.ui.theme.WarmCream

/**
 * Gelişmiş Güvenlik ve Şifre Yönetim Ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isPasswordResetDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = WarmCream,
        topBar = {
            TopAppBar(
                title = { Text("Hesap Güvenliği 🔒", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Durum Mesajı
            if (uiState.statusMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LightBeige)
                    ) {
                        Text(
                            text = uiState.statusMessage!!,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkInk,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // 1. E-POSTA DOĞRULAMA DURUMU
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBeige)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Turquoise)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("E-posta Adresiniz", fontWeight = FontWeight.Bold, color = DarkInk)
                                Text(uiState.userProfile?.email ?: "-", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = "🟢 Kayıtlı",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Turquoise
                            )
                        }
                    }
                }
            }

            // 2. ŞİFRE SIFIRLAMA E-POSTASI GÖNDER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPasswordResetDialogOpen = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBeige)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = Turquoise)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Şifre Sıfırlama Bağlantısı Gönder", fontWeight = FontWeight.Bold, color = DarkInk)
                            Text("Kayıtlı e-posta adresinize sıfırlama e-postası iletilir.", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Turquoise)
                    }
                }
            }

            // 3. TELEFON DOĞRULAMA DURUMU
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBeige)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Turquoise)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Telefon Doğrulama Durumu", fontWeight = FontWeight.Bold, color = DarkInk)
                            Text(uiState.userProfile?.phone?.ifBlank { "Henüz telefon eklenmedi" } ?: "Henüz telefon eklenmedi", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Şifre Sıfırlama Onay Diyaloğu
        if (isPasswordResetDialogOpen) {
            AlertDialog(
                onDismissRequest = { isPasswordResetDialogOpen = false },
                title = { Text("Şifre Sıfırlama E-postası", fontWeight = FontWeight.Bold) },
                text = { Text("${uiState.userProfile?.email} adresinize şifre sıfırlama bağlantısı gönderilsin mi?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.sendPasswordResetEmail()
                            isPasswordResetDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = DarkInk)
                    ) {
                        Text("Gönder")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isPasswordResetDialogOpen = false }) {
                        Text("Vazgeç", color = DarkInk)
                    }
                }
            )
        }
    }
}