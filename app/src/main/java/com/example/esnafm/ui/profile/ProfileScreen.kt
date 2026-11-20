package com.example.esnafm.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import android.net.Uri
import android.Manifest
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esnafm.R
import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.model.UserRole
import com.example.esnafm.ui.theme.DarkInk
import com.example.esnafm.ui.theme.LightBeige
import com.example.esnafm.ui.theme.SkyBlue
import com.example.esnafm.ui.theme.Terracotta
import com.example.esnafm.ui.theme.Turquoise
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import com.example.esnafm.ui.theme.WarmCream
import com.example.esnafm.ui.theme.WarmWhite
import com.example.esnafm.ui.theme.customTextFieldColors

import androidx.compose.ui.platform.LocalContext
import com.example.esnafm.util.LocaleHelper
import android.app.Activity
import com.example.esnafm.util.hasLocationPermission
import com.example.esnafm.util.requestDeviceLocation

/**
 * Kapsamlı Hesap ve Profil Yönetim Merkezi Ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToAddressBook: () -> Unit = {},
    onNavigateToNotificationPrefs: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isAboutAppOpen by remember { mutableStateOf(false) }
    val currentLang = LocaleHelper.getLanguage()
    val langName = if (currentLang == "en") "English" else "Türkçe"

    Scaffold(
        containerColor = WarmCream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Dön"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBeige,
                    titleContentColor = DarkInk
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Turquoise)
            }
        } else {
            val profile = uiState.userProfile
            val completionResult = ProfileCompletionUtil.calculateCompletion(profile, uiState.addresses.size)

            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Durum / Bildirim Mesajı
                if (uiState.statusMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.statusMessage!!.contains("✅") || uiState.statusMessage!!.contains("📧")) LightBeige else MaterialTheme.colorScheme.errorContainer
                            )
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

                // 1. ÜST BÖLÜM: Profil Kartı & Dinamik Doluluk Oranı
                item {
                    ProfileHeaderCard(
                        profile = profile,
                        completionResult = completionResult,
                        onEditClick = { viewModel.openEditProfileDialog() }
                    )
                }

                // 2. HIZLI ERİŞİM KISAYOL KARTLARI
                item {
                    Text(
                        text = stringResource(R.string.quick_actions),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickAccessCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.my_addresses),
                            subtitle = "${uiState.addresses.size} Kayıtlı Adres",
                            icon = Icons.Default.LocationOn,
                            onClick = onNavigateToAddressBook
                        )

                        QuickAccessCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.security),
                            subtitle = stringResource(R.string.password_email),
                            icon = Icons.Default.Security,
                            onClick = onNavigateToSecurity
                        )
                    }
                }

                // 3. HESABIM SEÇENEKLERİ
                item {
                    Text(
                        text = stringResource(R.string.account_contact),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileOptionRow(
                            icon = Icons.Default.Person,
                            title = stringResource(R.string.personal_info),
                            subtitle = stringResource(R.string.personal_info_desc),
                            onClick = { viewModel.openEditProfileDialog() }
                        )

                        ProfileOptionRow(
                            icon = Icons.Default.LocationOn,
                            title = "Adres Defterim (${uiState.addresses.size})",
                            subtitle = stringResource(R.string.address_book_desc),
                            onClick = onNavigateToAddressBook
                        )

                        if (profile?.role == UserRole.MERCHANT) {
                            ProfileOptionRow(
                                icon = Icons.Default.Business,
                                title = stringResource(R.string.business_info),
                                subtitle = "${profile.businessName.ifBlank { "Dükkan Adı" }} • ${profile.category.ifBlank { "Kategori" }}",
                                onClick = { viewModel.openEditProfileDialog() }
                            )
                        }
                    }
                }

                // 4. TERCIHLER VE DESTEK
                item {
                    Text(
                        text = stringResource(R.string.prefs_support),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileOptionRow(
                            icon = Icons.Default.Notifications,
                            title = stringResource(R.string.notification_prefs),
                            subtitle = stringResource(R.string.notification_prefs_desc),
                            onClick = onNavigateToNotificationPrefs
                        )

                        ProfileOptionRow(
                            icon = Icons.Default.Language,
                            title = stringResource(R.string.app_language),
                            subtitle = langName,
                            onClick = { viewModel.openLanguageDialog() }
                        )

                        ProfileOptionRow(
                            icon = Icons.AutoMirrored.Filled.Help,
                            title = stringResource(R.string.faq_screen_title),
                            subtitle = stringResource(R.string.faq_subtitle),
                            onClick = onNavigateToFaq
                        )

                        ProfileOptionRow(
                            icon = Icons.AutoMirrored.Filled.Subject,
                            title = stringResource(R.string.report_issue),
                            subtitle = stringResource(R.string.report_issue_desc),
                            onClick = { viewModel.openSupportTicketDialog() }
                        )

                        ProfileOptionRow(
                            icon = Icons.Default.Description,
                            title = stringResource(R.string.privacy_policy),
                            subtitle = "17 maddelik aydınlatma metni",
                            onClick = onNavigateToPrivacyPolicy
                        )

                        ProfileOptionRow(
                            icon = Icons.Default.Description,
                            title = stringResource(R.string.terms_of_service),
                            subtitle = stringResource(R.string.terms_of_service_desc),
                            onClick = onNavigateToTermsOfService
                        )

                        ProfileOptionRow(
                            icon = Icons.Default.Info,
                            title = stringResource(R.string.about_app),
                            subtitle = stringResource(R.string.about_app_desc),
                            onClick = { isAboutAppOpen = true }
                        )
                    }
                }

                // 5. GÜVENLİK VE HESAP İŞLEMLERİ
                item {
                    Text(
                        text = stringResource(R.string.security_session),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileOptionRow(
                            icon = Icons.Default.LockReset,
                            title = stringResource(R.string.security_center),
                            subtitle = stringResource(R.string.security_center_desc),
                            onClick = onNavigateToSecurity
                        )

                        // Çıkış Yap Satırı
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openSignOutDialog() },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Terracotta),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = stringResource(R.string.sign_out),
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.sign_out),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Terracotta
                                    )
                                    Text(
                                        text = stringResource(R.string.sign_out_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkInk.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Hesabımı Sil Satırı
                        TextButton(
                            onClick = { viewModel.openDeleteAccountDialog() },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = stringResource(R.string.delete_account),
                                style = MaterialTheme.typography.labelMedium,
                                color = Terracotta
                            )
                        }
                    }
                }
            }

            // MODAL DIYALOGLAR:

            // 1. Profili Düzenleme Diyaloğu
            if (uiState.isEditProfileDialogOpen && profile != null) {
                EditProfileDialog(
                    profile = profile,
                    isEmailVerified = uiState.isEmailVerified,
                    isPhoneVerified = uiState.isPhoneVerified,
                    onDismiss = { viewModel.closeEditProfileDialog() },
                    onSave = { name, phone, birthDate, gender, bio, city, district, neighborhood, prefContact, bName, bCategory, bPhone, bBusinessAddress, address, businessLatitude, businessLongitude, photoUri ->
                        viewModel.updateUserProfile(
                            name, phone, birthDate, gender, bio, city, district, neighborhood, prefContact,
                            bName, bCategory, bPhone, bBusinessAddress, address, businessLatitude, businessLongitude, photoUri
                        )
                    },
                    isSaving = uiState.isSaving
                )
            }

            // 2. Sorun Bildir & Destek Diyaloğu
            if (uiState.isSupportTicketDialogOpen) {
                SupportTicketDialog(
                    onDismiss = { viewModel.closeSupportTicketDialog() },
                    onSend = { subject, message ->
                        viewModel.sendSupportTicket(subject, message)
                    },
                    isSaving = uiState.isSaving
                )
            }

            // 3. Çıkış Yap Onay Diyaloğu
            if (uiState.isSignOutConfirmDialogOpen) {
                AlertDialog(
                    onDismissRequest = { viewModel.closeSignOutDialog() },
                    title = { Text("Çıkış Yapılsın mı?", fontWeight = FontWeight.Bold) },
                    text = { Text("Mevcut oturumunuz kapatılacaktır. Devam etmek istiyor musunuz?") },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.signOut(onSignOutComplete = onSignOut) },
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                        ) {
                            Text("Evet, Çıkış Yap")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.closeSignOutDialog() }) {
                            Text("Vazgeç", color = DarkInk)
                        }
                    }
                )
            }

            // 4. Hesabımı Sil Onay Diyaloğu
            if (uiState.isDeleteAccountDialogOpen) {
                AlertDialog(
                    onDismissRequest = { viewModel.closeDeleteAccountDialog() },
                    title = { Text("Hesabınızı Silmek İstediğinize Emin Misiniz?", fontWeight = FontWeight.Bold, color = Terracotta) },
                    text = { Text("Bu işlem geri alınamaz. Hesabınız ve kayıtlı verileriniz kalıcı olarak silinecektir.") },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.deleteAccount(onDeleteComplete = onSignOut) },
                            colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onError)
                            } else {
                                Text("Evet, Hesabımı Sil")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.closeDeleteAccountDialog() }) {
                            Text("Vazgeç", color = DarkInk)
                        }
                    }
                )
            }

            // 5. Uygulama Hakkında Diyaloğu
            if (isAboutAppOpen) {
                AlertDialog(
                    onDismissRequest = { isAboutAppOpen = false },
                    title = { Text("Esnafım v1.0.0 🏪", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("Esnafım, Türkiye'deki mahalle esnaflarını ve müşterileri dijital ortamda buluşturan yerel rehber platformudur.\n\n© 2026 Esnafım - Tüm hakları saklıdır.")
                    },
                    confirmButton = {
                        TextButton(onClick = { isAboutAppOpen = false }) {
                            Text("Kapat", color = Turquoise)
                        }
                    }
                )
            }

            // 6. Dil Seçim Diyaloğu
            if (uiState.isLanguageDialogOpen) {
                AlertDialog(
                    onDismissRequest = { viewModel.closeLanguageDialog() },
                    title = { Text(stringResource(R.string.app_language), fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.closeLanguageDialog()
                                        LocaleHelper.setLocale("tr")
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Türkçe", style = MaterialTheme.typography.bodyLarge, color = DarkInk)
                                if (currentLang == "tr") {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("✓", color = Turquoise, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.closeLanguageDialog()
                                        LocaleHelper.setLocale("en")
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("English", style = MaterialTheme.typography.bodyLarge, color = DarkInk)
                                if (currentLang == "en") {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("✓", color = Turquoise, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.closeLanguageDialog() }) {
                            Text("Kapat", color = DarkInk)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Profil Üst Bilgi Kartı (Dinamik Doluluk Oranı İle)
 */
@Composable
private fun ProfileHeaderCard(
    profile: UserProfile?,
    completionResult: ProfileCompletionResult,
    onEditClick: () -> Unit
) {
    val name = profile?.name?.ifBlank { "Kullanıcı" } ?: "Kullanıcı"
    val initials = remember(name) {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "ES"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBeige),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = WarmWhite,
            border = BorderStroke(2.dp, Turquoise),
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (!profile?.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profile!!.photoUrl,
                        contentDescription = "Profil Fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Esnafım Logo",
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = DarkInk
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (profile?.role == UserRole.MERCHANT) "🏬 Esnaf Hesabı" else "👤 Müşteri Hesabı",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Turquoise
            )

            if (!profile?.email.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile!!.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkInk.copy(alpha = 0.7f)
                )
            }
            if (!profile?.phone.isNullOrBlank()) {
                Text(
                    text = "Telefon: ${profile!!.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkInk.copy(alpha = 0.7f)
                )
            }
            if (!profile?.address.isNullOrBlank()) {
                Text(
                    text = "Adres: ${profile!!.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkInk.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dinamik Profil Tamamlama Kartı
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = completionResult.statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DarkInk
                    )
                    Text(
                        text = "%${completionResult.percentage}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Turquoise
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { completionResult.percentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Turquoise,
                    trackColor = SkyBlue.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = completionResult.explanationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkInk.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Düzenle",
                    modifier = Modifier.size(16.dp),
                    tint = DarkInk
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Profili Düzenle", color = DarkInk)
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightBeige),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SkyBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = DarkInk, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DarkInk)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Turquoise)
        }
    }
}

@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightBeige),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SkyBlue.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkInk,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkInk.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Detay",
                tint = Turquoise,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Profili Düzenleme Form Diyaloğu
 */
@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    isEmailVerified: Boolean,
    isPhoneVerified: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        phone: String,
        birthDate: String,
        gender: String,
        bio: String,
        city: String,
        district: String,
        neighborhood: String,
        preferredContactMethod: String,
        businessName: String,
        category: String,
        businessPhone: String,
        businessAddress: String,
        address: String,
        businessLatitude: Double?,
        businessLongitude: Double?,
        photoUri: Uri?
    ) -> Unit,
    isSaving: Boolean
) {
    var name by remember { mutableStateOf(profile.name) }
    var phone by remember { mutableStateOf(profile.phone) }
    var birthDate by remember { mutableStateOf(profile.birthDate) }
    var gender by remember { mutableStateOf(profile.gender) }
    var bio by remember { mutableStateOf(profile.bio) }
    var city by remember { mutableStateOf(profile.city) }
    var district by remember { mutableStateOf(profile.district) }
    var neighborhood by remember { mutableStateOf(profile.neighborhood) }
    var preferredContactMethod by remember { mutableStateOf(profile.preferredContactMethod) }

    var businessName by remember { mutableStateOf(profile.businessName) }
    var category by remember { mutableStateOf(profile.category) }
    var businessPhone by remember { mutableStateOf(profile.businessPhone) }
    var businessAddress by remember { mutableStateOf(profile.businessAddress.ifBlank { profile.address }) }
    var businessLatitude by remember { mutableStateOf(profile.businessLatitude) }
    var businessLongitude by remember { mutableStateOf(profile.businessLongitude) }
    var businessLocationMessage by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf(profile.address) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(if (profile.photoUrl.isNotBlank()) Uri.parse(profile.photoUrl) else null) }
    val context = LocalContext.current

    fun captureBusinessLocation() {
        requestDeviceLocation(context) { coordinates ->
            if (coordinates == null) {
                businessLocationMessage = "Konum alınamadı. Cihazın konum ayarını açıp tekrar dene."
            } else {
                businessLatitude = coordinates.latitude
                businessLongitude = coordinates.longitude
                businessLocationMessage = "Konum alındı. Saklamak için profil değişikliklerini kaydet."
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionResults ->
            if (permissionResults.values.any { it }) captureBusinessLocation()
            else businessLocationMessage = "Konum izni verilmedi; işletme konumu kaydedilmedi."
        }
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.edit_profile_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = DarkInk
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp) // Dialog boyutunu sınırla
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Profil Fotoğrafı (Sadece Görsel)
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = WarmWhite,
                            border = BorderStroke(2.dp, Turquoise),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (selectedImageUri != null) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Profil Fotoğrafı",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.app_logo),
                                        contentDescription = "Esnafım Logo",
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                        TextButton(onClick = { 
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Text(stringResource(R.string.edit_photo), color = Turquoise)
                        }
                    }
                }

                // Doğrulama Durumları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.email_verification), style = MaterialTheme.typography.bodySmall, color = DarkInk)
                    Text(
                        if (isEmailVerified) stringResource(R.string.verified_yes) else stringResource(R.string.verified_no),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isEmailVerified) Turquoise else MaterialTheme.colorScheme.error
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.phone_verification), style = MaterialTheme.typography.bodySmall, color = DarkInk)
                    Text(
                        if (isPhoneVerified) stringResource(R.string.verified_yes) else stringResource(R.string.verified_no),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPhoneVerified) Turquoise else MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.label_name_surname)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Turquoise) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.take(24) },
                    label = { Text("Telefon numarası") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text(stringResource(R.string.label_birthdate)) },
                    placeholder = { Text(stringResource(R.string.placeholder_date)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text(stringResource(R.string.label_gender)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(stringResource(R.string.label_bio)) },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customTextFieldColors()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(stringResource(R.string.label_city)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text(stringResource(R.string.label_district)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )
                }

                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    label = { Text(stringResource(R.string.label_neighborhood)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = preferredContactMethod,
                    onValueChange = { preferredContactMethod = it },
                    label = { Text(stringResource(R.string.label_preferred_contact)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it.take(400) },
                    label = { Text("Kişisel / teslimat adresi") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Turquoise) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customTextFieldColors()
                )

                if (profile.role == UserRole.MERCHANT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.label_business_info), fontWeight = FontWeight.Bold, color = DarkInk)

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text(stringResource(R.string.label_business_name)) },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = Turquoise) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(stringResource(R.string.label_category)) },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Turquoise) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )
                    
                    OutlinedTextField(
                        value = businessPhone,
                        onValueChange = { businessPhone = it },
                        label = { Text(stringResource(R.string.label_alt_phone)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )

                    OutlinedTextField(
                        value = businessAddress,
                        onValueChange = { businessAddress = it.take(400) },
                        label = { Text(stringResource(R.string.label_business_address)) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Turquoise) },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )

                    OutlinedButton(
                        onClick = {
                            if (hasLocationPermission(context)) {
                                captureBusinessLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (businessLatitude != null && businessLongitude != null) "İşletme konumunu yenile" else "İşletme konumumu kaydet")
                    }
                    Text(
                        text = businessLocationMessage
                            ?: if (businessLatitude != null && businessLongitude != null) "İşletme konumu kaydedildi." else "Yakındaki müşterilere doğru sıralama için işletmedeyken konumunu kaydet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (businessLocationMessage?.startsWith("Konum alınamadı") == true) MaterialTheme.colorScheme.error else DarkInk.copy(alpha = 0.7f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name, phone, birthDate, gender, bio, city, district, neighborhood, preferredContactMethod,
                        businessName, category, businessPhone, businessAddress, address, businessLatitude, businessLongitude, selectedImageUri
                    )
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue,
                    contentColor = DarkInk
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DarkInk, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.btn_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.btn_cancel), color = DarkInk)
            }
        }
    )
}

/**
 * Sorun Bildir & Destek Talebi Diyaloğu
 */
@Composable
private fun SupportTicketDialog(
    onDismiss: () -> Unit,
    onSend: (subject: String, message: String) -> Unit,
    isSaving: Boolean
) {
    var subject by remember { mutableStateOf("Sorun Bildir") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sorun Bildir & Özellik Öner 🤝", fontWeight = FontWeight.Bold, color = DarkInk) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Konu / Başlık") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Açıklama / Mesajınız") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(subject, message) },
                enabled = !isSaving && message.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = DarkInk)
            ) {
                Text("Talebi Gönder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = DarkInk)
            }
        }
    )
}
