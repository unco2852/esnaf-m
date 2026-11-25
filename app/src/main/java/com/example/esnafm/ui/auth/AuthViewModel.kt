package com.example.esnafm.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.model.UserRole
import com.example.esnafm.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val hasReadPreInformation: Boolean = false,
    val hasAcceptedUserAgreement: Boolean = false,
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val name: String = "",
    val phone: String = "",
    val businessName: String = "",
    val category: String = "",
    val address: String = "",
    val city: String = "",
    val district: String = "",
    val businessPhone: String = "",
    val businessDescription: String = "",
    val workingHours: String = "09:00 - 18:00",
    val role: UserRole = UserRole.CUSTOMER,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authenticatedUser: UserProfile? = null
)

/**
 * Giriş ve Kayıt İşlemlerinin Yönetildiği ViewModel
 */
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setInitialRole(role: UserRole) {
        _uiState.update {
            it.copy(
                role = role,
                hasReadPreInformation = false,
                hasAcceptedUserAgreement = false,
                email = "",
                password = "",
                passwordConfirmation = "",
                name = "",
                phone = "",
                businessName = "",
                category = "",
                address = "",
                city = "",
                district = "",
                businessPhone = "",
                businessDescription = "",
                workingHours = "09:00 - 18:00",
                isLoginMode = true,
                isLoading = false,
                errorMessage = null,
                authenticatedUser = null
            )
        }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                hasReadPreInformation = false,
                hasAcceptedUserAgreement = false,
                errorMessage = null
            )
        }
    }

    fun onPreInformationReadChanged(checked: Boolean) {
        _uiState.update { it.copy(hasReadPreInformation = checked, errorMessage = null) }
    }

    fun onUserAgreementAcceptedChanged(checked: Boolean) {
        _uiState.update { it.copy(hasAcceptedUserAgreement = checked, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onPasswordConfirmationChanged(password: String) {
        _uiState.update { it.copy(passwordConfirmation = password) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun onBusinessNameChanged(bName: String) {
        _uiState.update { it.copy(businessName = bName) }
    }

    fun onCategoryChanged(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onAddressChanged(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onCityChanged(city: String) {
        _uiState.update { it.copy(city = city) }
    }

    fun onDistrictChanged(district: String) {
        _uiState.update { it.copy(district = district) }
    }

    fun onBusinessPhoneChanged(phone: String) {
        _uiState.update { it.copy(businessPhone = phone) }
    }

    fun onBusinessDescriptionChanged(description: String) {
        _uiState.update { it.copy(businessDescription = description) }
    }

    fun onWorkingHoursChanged(workingHours: String) {
        _uiState.update { it.copy(workingHours = workingHours) }
    }

    fun submitForm(onSuccess: (UserProfile) -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Lütfen e-posta ve şifrenizi giriniz.") }
            return
        }

        if (!state.isLoginMode && !state.hasReadPreInformation) {
            _uiState.update { it.copy(errorMessage = "Devam etmek için ön bilgilendirme metnini okuduğunuzu onaylayın.") }
            return
        }

        if (!state.isLoginMode && !state.hasAcceptedUserAgreement) {
            _uiState.update { it.copy(errorMessage = "Kayıt olmak için kullanıcı sözleşmesini kabul etmeniz gerekiyor.") }
            return
        }

        if (!state.isLoginMode && state.role == UserRole.MERCHANT && state.password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Şifreniz en az 8 karakter olmalıdır.") }
            return
        }

        if (!state.isLoginMode && state.role == UserRole.MERCHANT && state.password != state.passwordConfirmation) {
            _uiState.update { it.copy(errorMessage = "Şifreler eşleşmiyor. Lütfen tekrar kontrol edin.") }
            return
        }

        if (!state.isLoginMode && state.role == UserRole.MERCHANT) {
            val missingBusinessField = when {
                state.name.isBlank() -> "yetkili kişinin adını ve soyadını"
                state.businessName.isBlank() -> "işletme adını"
                state.category.isBlank() -> "işletme kategorisini"
                state.phone.isBlank() -> "yetkili telefon numarasını"
                state.city.isBlank() -> "şehri"
                state.district.isBlank() -> "ilçeyi"
                state.address.isBlank() -> "işletme adresini"
                else -> null
            }
            if (missingBusinessField != null) {
                _uiState.update { it.copy(errorMessage = "Lütfen $missingBusinessField giriniz.") }
                return
            }
            if (state.phone.filter { it.isDigit() }.length < 10) {
                _uiState.update { it.copy(errorMessage = "Geçerli bir yetkili telefon numarası giriniz.") }
                return
            }
            if (state.businessPhone.isNotBlank() && state.businessPhone.filter { it.isDigit() }.length < 10) {
                _uiState.update { it.copy(errorMessage = "İşletme telefon numarasını kontrol edin.") }
                return
            }
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            if (state.isLoginMode) {
                // Beklenen rol bilgisi (expectedRole) gönderilerek veritabanından doğrulama yapılır
                val result = authRepository.signIn(state.email.trim(), state.password, expectedRole = state.role)
                result.fold(
                    onSuccess = { profile ->
                        _uiState.update { it.copy(isLoading = false, authenticatedUser = profile) }
                        withContext(Dispatchers.Main) {
                            onSuccess(profile)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.localizedMessage ?: "Giriş yapılamadı."
                            )
                        }
                    }
                )
            } else {
                val result = authRepository.signUp(
                    email = state.email.trim(),
                    pass = state.password,
                    name = state.name.ifBlank { "Kullanıcı" },
                    role = state.role,
                    preInformationAcknowledged = state.hasReadPreInformation,
                    userAgreementAccepted = state.hasAcceptedUserAgreement,
                    businessName = state.businessName.trim(),
                    category = state.category.trim(),
                    address = state.address.trim(),
                    phone = state.phone.trim(),
                    city = state.city.trim(),
                    district = state.district.trim(),
                    businessPhone = state.businessPhone.trim(),
                    businessDescription = state.businessDescription.trim(),
                    workingHours = state.workingHours.trim()
                )
                result.fold(
                    onSuccess = { profile ->
                        _uiState.update { it.copy(isLoading = false, authenticatedUser = profile) }
                        withContext(Dispatchers.Main) {
                            onSuccess(profile)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.localizedMessage ?: "Kayıt olunamadı."
                            )
                        }
                    }
                )
            }
        }
    }
}
