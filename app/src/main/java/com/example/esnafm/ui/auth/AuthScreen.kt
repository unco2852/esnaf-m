package com.example.esnafm.ui.auth

import com.example.esnafm.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.model.UserRole
import com.example.esnafm.ui.components.EsnafımLogo
import com.example.esnafm.ui.theme.DarkInk
import com.example.esnafm.ui.theme.LightBeige
import com.example.esnafm.ui.theme.SkyBlue
import com.example.esnafm.ui.theme.Turquoise
import com.example.esnafm.ui.theme.WarmCream
import com.example.esnafm.ui.theme.customTextFieldColors

/**
 * Giriş ve Kayıt Ekranı (Mahalle Sıcaklığında Dinamik Formlar)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    role: UserRole,
    onNavigateBack: () -> Unit,
    onAuthSuccess: (UserProfile) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(role) {
        viewModel.setInitialRole(role)
    }

    val roleTitle = if (role == UserRole.MERCHANT) stringResource(R.string.role_merchant) else stringResource(R.string.role_customer)
    val modeTitle = if (uiState.isLoginMode) stringResource(R.string.btn_login) else stringResource(R.string.btn_register)

    Scaffold(
        containerColor = WarmCream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$roleTitle Kapısı - $modeTitle",
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EsnafımLogo(iconSize = 56.dp, showSubtitle = false)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (uiState.isLoginMode) "$roleTitle Hesabınıza Giriş Yapın" else "Yeni $roleTitle Hesabı Oluşturun",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = DarkInk
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hata Mesajı
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (uiState.isLoginMode) {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = { Text(stringResource(R.string.email_address)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Turquoise) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(14.dp),
                    colors = customTextFieldColors()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = { Text(stringResource(R.string.password)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Turquoise) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(14.dp),
                    colors = customTextFieldColors()
                )
            } else {
                RegistrationSection(
                    title = "Hesap ve güvenlik",
                    subtitle = "Uygulamaya giriş yapmak için kullanacağınız bilgiler"
                ) {
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = { Text(stringResource(R.string.email_address)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Turquoise) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        colors = customTextFieldColors()
                    )
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        label = { Text(if (role == UserRole.MERCHANT) "Şifre (en az 8 karakter)" else stringResource(R.string.password)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Turquoise) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(14.dp),
                        colors = customTextFieldColors()
                    )
                    if (role == UserRole.MERCHANT) {
                        OutlinedTextField(
                            value = uiState.passwordConfirmation,
                            onValueChange = { viewModel.onPasswordConfirmationChanged(it) },
                            label = { Text("Şifreyi tekrar girin") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                    }
                }

                if (role == UserRole.MERCHANT) {
                    RegistrationSection(
                        title = "Yetkili kişi",
                        subtitle = "İşletme hesabından sorumlu kişinin bilgileri · * zorunlu"
                    ) {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChanged(it) },
                            label = { Text("Ad soyad *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.onPhoneChanged(it.take(20)) },
                            label = { Text("Yetkili telefon numarası *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                    }

                    RegistrationSection(
                        title = "İşletme profili",
                        subtitle = "Müşterilerin işletmenizi tanımasına yardımcı olacak bilgiler"
                    ) {
                        OutlinedTextField(
                            value = uiState.businessName,
                            onValueChange = { viewModel.onBusinessNameChanged(it.take(100)) },
                            label = { Text("İşletme adı *") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = uiState.category,
                            onValueChange = { viewModel.onCategoryChanged(it.take(80)) },
                            label = { Text("İşletme kategorisi *") },
                            placeholder = { Text("Örn. Tesisatçı, Kuaför, Fırın") },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = uiState.businessDescription,
                            onValueChange = { viewModel.onBusinessDescriptionChanged(it.take(500)) },
                            label = { Text("İşletme açıklaması (isteğe bağlı)") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                        Text(
                            text = "${uiState.businessDescription.length}/500",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkInk.copy(alpha = 0.65f)
                        )
                    }

                    RegistrationSection(
                        title = "Konum ve çalışma bilgileri",
                        subtitle = "Yakındaki müşteriler işletmenizi bu bilgilere göre bulabilir · * zorunlu"
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.city,
                                onValueChange = { viewModel.onCityChanged(it.take(50)) },
                                label = { Text("Şehir *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = customTextFieldColors()
                            )
                            OutlinedTextField(
                                value = uiState.district,
                                onValueChange = { viewModel.onDistrictChanged(it.take(50)) },
                                label = { Text("İlçe *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = customTextFieldColors()
                            )
                        }
                        OutlinedTextField(
                            value = uiState.address,
                            onValueChange = { viewModel.onAddressChanged(it.take(300)) },
                            label = { Text("Açık işletme adresi *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = uiState.businessPhone,
                            onValueChange = { viewModel.onBusinessPhoneChanged(it.take(20)) },
                            label = { Text("İşletme telefonu (isteğe bağlı)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = uiState.workingHours,
                            onValueChange = { viewModel.onWorkingHoursChanged(it.take(80)) },
                            label = { Text("Çalışma saatleri") },
                            placeholder = { Text("Örn. Hafta içi 09:00–18:00") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                    }
                } else {
                    RegistrationSection(title = "Kişisel bilgiler") {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChanged(it) },
                            label = { Text(stringResource(R.string.full_name)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Turquoise) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Gönder Butonu
            Button(
                onClick = { viewModel.submitForm(onSuccess = onAuthSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue,
                    contentColor = DarkInk
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DarkInk
                    )
                } else {
                    Text(
                        text = if (uiState.isLoginMode) stringResource(R.string.btn_login) else "Hemen Kayıt Ol",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mod Değiştirme Butonu (Giriş Yap / Kayıt Ol Değişimi)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isLoginMode) "Hesabınız yok mu?" else "Zaten hesabınız var mı?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkInk
                )
                TextButton(onClick = { viewModel.toggleMode() }) {
                    Text(
                        text = if (uiState.isLoginMode) "Hemen Kayıt Olun" else "Giriş Yapın",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Turquoise
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistrationSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LightBeige)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DarkInk)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DarkInk.copy(alpha = 0.7f))
                }
                content()
            }
        )
    }
}
