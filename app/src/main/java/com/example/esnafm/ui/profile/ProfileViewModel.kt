package com.example.esnafm.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esnafm.data.model.AddressModel
import com.example.esnafm.data.model.SupportTicketModel
import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.repository.AddressRepository
import com.example.esnafm.data.repository.AuthRepository
import com.example.esnafm.data.repository.SupportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val addresses: List<AddressModel> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSignedOut: Boolean = false,
    val isEditProfileDialogOpen: Boolean = false,
    val isAddressDialogOpen: Boolean = false,
    val isSecurityDialogOpen: Boolean = false,
    val isSupportTicketDialogOpen: Boolean = false,
    val isFaqDialogOpen: Boolean = false,
    val isLanguageDialogOpen: Boolean = false,
    val isLegalTermsDialogOpen: Boolean = false,
    val isSignOutConfirmDialogOpen: Boolean = false,
    val isDeleteAccountDialogOpen: Boolean = false,
    val statusMessage: String? = null,
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false
)

/**
 * Kapsamlı Hesap ve Profil Yönetim ViewModel Katmanı
 */
class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val addressRepository: AddressRepository = AddressRepository(),
    private val supportRepository: SupportRepository = SupportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        _uiState.update { it.copy(isLoading = true, statusMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = authRepository.currentUser
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false, isSignedOut = true) }
                return@launch
            }

            val result = authRepository.getUserProfile(currentUser.uid)
            result.fold(
                onSuccess = { profile ->
                    _uiState.update { it.copy(
                        isLoading = false, 
                        userProfile = profile,
                        isEmailVerified = currentUser.isEmailVerified,
                        isPhoneVerified = !currentUser.phoneNumber.isNullOrBlank() || profile.isPhoneVerified
                    ) }
                    observeAddresses(currentUser.uid)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false, 
                        userProfile = null,
                        statusMessage = "❌ Profil bilgileri yüklenemedi: ${error.localizedMessage}",
                        isEmailVerified = currentUser.isEmailVerified,
                        isPhoneVerified = !currentUser.phoneNumber.isNullOrBlank()
                    ) }
                }
            )
        }
    }

    private fun observeAddresses(uid: String) {
        viewModelScope.launch {
            addressRepository.getUserAddressesFlow(uid)
                .catch { /* Sessizce yut */ }
                .collect { list ->
                    _uiState.update { it.copy(addresses = list) }
                }
        }
    }

    fun openEditProfileDialog() {
        _uiState.update { it.copy(isEditProfileDialogOpen = true, statusMessage = null) }
    }

    fun closeEditProfileDialog() {
        _uiState.update { it.copy(isEditProfileDialogOpen = false) }
    }

    fun openAddressDialog() {
        _uiState.update { it.copy(isAddressDialogOpen = true, statusMessage = null) }
    }

    fun closeAddressDialog() {
        _uiState.update { it.copy(isAddressDialogOpen = false) }
    }

    fun openSecurityDialog() {
        _uiState.update { it.copy(isSecurityDialogOpen = true, statusMessage = null) }
    }

    fun closeSecurityDialog() {
        _uiState.update { it.copy(isSecurityDialogOpen = false) }
    }

    fun openSupportTicketDialog() {
        _uiState.update { it.copy(isSupportTicketDialogOpen = true, statusMessage = null) }
    }

    fun closeSupportTicketDialog() {
        _uiState.update { it.copy(isSupportTicketDialogOpen = false) }
    }

    fun openFaqDialog() {
        _uiState.update { it.copy(isFaqDialogOpen = true) }
    }

    fun closeFaqDialog() {
        _uiState.update { it.copy(isFaqDialogOpen = false) }
    }

    fun openLanguageDialog() {
        _uiState.update { it.copy(isLanguageDialogOpen = true) }
    }

    fun closeLanguageDialog() {
        _uiState.update { it.copy(isLanguageDialogOpen = false) }
    }

    fun openLegalTermsDialog() {
        _uiState.update { it.copy(isLegalTermsDialogOpen = true) }
    }

    fun closeLegalTermsDialog() {
        _uiState.update { it.copy(isLegalTermsDialogOpen = false) }
    }

    fun openSignOutDialog() {
        _uiState.update { it.copy(isSignOutConfirmDialogOpen = true) }
    }

    fun closeSignOutDialog() {
        _uiState.update { it.copy(isSignOutConfirmDialogOpen = false) }
    }

    fun openDeleteAccountDialog() {
        _uiState.update { it.copy(isDeleteAccountDialogOpen = true) }
    }

    fun closeDeleteAccountDialog() {
        _uiState.update { it.copy(isDeleteAccountDialogOpen = false) }
    }

    fun updateUserProfile(
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
        photoUri: android.net.Uri? = null
    ) {
        val currentProfile = _uiState.value.userProfile ?: return
        _uiState.update { it.copy(isSaving = true, statusMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val updatedProfile = currentProfile.copy(
                name = name,
                phone = phone,
                birthDate = birthDate,
                gender = gender,
                bio = bio,
                city = city,
                district = district,
                neighborhood = neighborhood,
                preferredContactMethod = preferredContactMethod,
                businessName = businessName,
                category = category,
                businessPhone = businessPhone,
                businessAddress = businessAddress,
                address = address,
                businessLatitude = businessLatitude,
                businessLongitude = businessLongitude,
                photoUrl = photoUri?.toString() ?: currentProfile.photoUrl
            )

            val result = authRepository.updateUserProfile(updatedProfile)
            result.fold(
                onSuccess = {
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            isEditProfileDialogOpen = false,
                            userProfile = updatedProfile,
                            statusMessage = "✅ Profil bilgileriniz başarıyla güncellendi!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            statusMessage = "❌ Profil güncellenemedi: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun addNewAddress(title: String, fullAddress: String, description: String) {
        val currentUser = authRepository.currentUser ?: return
        if (fullAddress.isBlank()) return

        _uiState.update { it.copy(isSaving = true, statusMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val newAddress = AddressModel(
                uid = currentUser.uid,
                title = title.ifBlank { "Ev" },
                fullAddress = fullAddress,
                description = description
            )

            val result = addressRepository.saveAddress(newAddress)
            result.fold(
                onSuccess = {
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            statusMessage = "✅ Yeni adresiniz eklendi!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            statusMessage = "❌ Adres kaydedilemedi: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun deleteAddress(addressId: String) {
        val currentUser = authRepository.currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            addressRepository.deleteAddress(currentUser.uid, addressId)
        }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.userProfile?.email ?: authRepository.currentUser?.email ?: return
        _uiState.update { it.copy(isSaving = true, statusMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            statusMessage = "📧 Şifre sıfırlama bağlantısı $email adresinize gönderildi!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            statusMessage = "❌ Şifre e-postası gönderilemedi: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun sendSupportTicket(subject: String, message: String) {
        val currentUser = authRepository.currentUser ?: return
        if (message.isBlank()) return

        _uiState.update { it.copy(isSaving = true, statusMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val ticket = SupportTicketModel(
                uid = currentUser.uid,
                userEmail = currentUser.email ?: "",
                subject = subject,
                message = message
            )

            val result = supportRepository.createTicket(ticket)
            result.fold(
                onSuccess = {
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            isSupportTicketDialogOpen = false,
                            statusMessage = "✅ Destek talebiniz başarıyla iletildi!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            statusMessage = "❌ Talep iletilemedi: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun deleteAccount(onDeleteComplete: () -> Unit) {
        _uiState.update { it.copy(isSaving = true, statusMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.deleteAccount()
            result.fold(
                onSuccess = {
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            isSignedOut = true,
                            isDeleteAccountDialogOpen = false,
                            userProfile = null
                        )
                    }
                    launch(Dispatchers.Main) {
                        onDeleteComplete()
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            isDeleteAccountDialogOpen = false,
                            statusMessage = "❌ Hesap silinemedi: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun signOut(onSignOutComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.signOut()
            _uiState.update {
                it.copy(
                    isSignedOut = true,
                    userProfile = null,
                    isSignOutConfirmDialogOpen = false
                )
            }
            launch(Dispatchers.Main) {
                onSignOutComplete()
            }
        }
    }
}
