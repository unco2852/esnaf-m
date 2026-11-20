package com.example.esnafm.ui.tradesman

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esnafm.data.model.AppointmentModel
import com.example.esnafm.data.model.ItemModel
import com.example.esnafm.data.model.MerchantMessageModel
import com.example.esnafm.data.model.OrderModel
import com.example.esnafm.data.model.ServiceModel
import com.example.esnafm.data.repository.AppointmentRepository
import com.example.esnafm.data.repository.AuthRepository
import com.example.esnafm.data.repository.ItemRepository
import com.example.esnafm.data.repository.MerchantMessageRepository
import com.example.esnafm.data.repository.OrderRepository
import com.example.esnafm.data.repository.ServiceRepository
import com.example.esnafm.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

private val previousMerchantOrderStatuses = setOf("cancelled", "delivered", "completed")
private val previousMerchantAppointmentStatuses = setOf("cancelled", "rejected", "service_completed", "completed")

private fun String.isInMerchantHistory(statuses: Set<String>): Boolean =
    trim().lowercase(Locale.ROOT) in statuses

data class TradesmanUiState(
    val businessName: String = "",
    val isOpen: Boolean = true,
    val products: List<ItemModel> = emptyList(),
    val services: List<ServiceModel> = emptyList(),
    val appointments: List<AppointmentModel> = emptyList(),
    val orders: List<OrderModel> = emptyList(),
    val messages: List<MerchantMessageModel> = emptyList(),
    val category: String = "",
    val businessDescription: String = "",
    val businessCity: String = "",
    val businessDistrict: String = "",
    val businessAddress: String = "",
    val businessPhone: String = "",
    val workingHours: String = "",
    val isAddProductDialogOpen: Boolean = false,
    val isAddServiceDialogOpen: Boolean = false,
    val editingProduct: ItemModel? = null,
    val editingService: ServiceModel? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val statusMessage: String? = null
) {
    val activeOrders: List<OrderModel>
        get() = orders.filterNot { it.status.isInMerchantHistory(previousMerchantOrderStatuses) }

    val previousOrders: List<OrderModel>
        get() = orders.filter { it.status.isInMerchantHistory(previousMerchantOrderStatuses) }

    val activeAppointments: List<AppointmentModel>
        get() = appointments.filterNot { it.status.isInMerchantHistory(previousMerchantAppointmentStatuses) }

    val previousAppointments: List<AppointmentModel>
        get() = appointments.filter { it.status.isInMerchantHistory(previousMerchantAppointmentStatuses) }
}

/**
 * Esnaf Yönetim Paneli ViewModel (Ürün, Hizmet, Görsel ve Randevu Yönetimi)
 */
class TradesmanPanelViewModel(
    private val itemRepository: ItemRepository = ItemRepository(),
    private val serviceRepository: ServiceRepository = ServiceRepository(),
    private val appointmentRepository: AppointmentRepository = AppointmentRepository(),
    private val storageRepository: StorageRepository = StorageRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val merchantMessageRepository: MerchantMessageRepository = MerchantMessageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradesmanUiState())
    val uiState: StateFlow<TradesmanUiState> = _uiState.asStateFlow()

    init {
        observeBusinessProfile()
        observeProducts()
        observeServices()
        observeMerchantAppointments()
        observeMerchantOrders()
        observeMerchantMessages()
    }

    private fun observeBusinessProfile() {
        val merchantId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            authRepository.getUserProfile(merchantId).onSuccess { profile ->
                _uiState.update { current ->
                    current.copy(
                        businessName = profile.businessName.ifBlank { profile.name },
                        category = profile.category,
                        businessDescription = profile.businessDescription,
                        businessCity = profile.city,
                        businessDistrict = profile.district,
                        businessAddress = profile.businessAddress.ifBlank { profile.address },
                        businessPhone = profile.businessPhone.ifBlank { profile.phone },
                        workingHours = profile.workingHours
                    )
                }
            }
        }
    }

    fun observeProducts() {
        viewModelScope.launch {
            itemRepository.getItemsFlow()
                .catch { error ->
                    _uiState.update { current ->
                        current.copy(statusMessage = "Ürünler yüklenemedi: ${error.localizedMessage}")
                    }
                }
                .collect { allItems ->
                    val currentMerchantId = authRepository.currentUser?.uid
                    val myProducts = if (!currentMerchantId.isNullOrBlank()) {
                        allItems.filter { it.merchantId == currentMerchantId || it.merchantId.isBlank() }
                    } else {
                        allItems
                    }
                    _uiState.update { current -> current.copy(products = myProducts) }
                }
        }
    }

    fun observeServices() {
        viewModelScope.launch {
            serviceRepository.getServicesFlow()
                .catch { error ->
                    _uiState.update { current ->
                        current.copy(statusMessage = "Hizmetler yüklenemedi: ${error.localizedMessage}")
                    }
                }
                .collect { allServices ->
                    val currentMerchantId = authRepository.currentUser?.uid
                    val myServices = if (!currentMerchantId.isNullOrBlank()) {
                        allServices.filter { it.merchantId == currentMerchantId || it.merchantId.isBlank() }
                    } else {
                        allServices
                    }
                    _uiState.update { current -> current.copy(services = myServices) }
                }
        }
    }

    /**
     * Esnafa gelen randevu taleplerini canlı dinler.
     */
    fun observeMerchantAppointments() {
        val currentMerchantId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            appointmentRepository.getMerchantAppointmentsFlow(currentMerchantId)
                .catch { /* Sessizce yut */ }
                .collect { list ->
                    _uiState.update { current -> current.copy(appointments = list) }
                }
        }
    }

    fun observeMerchantOrders() {
        val merchantId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            orderRepository.getMerchantOrdersFlow(merchantId)
                .catch { error ->
                    _uiState.update { it.copy(statusMessage = "Siparişler yüklenemedi: ${error.localizedMessage}") }
                }
                .collect { orders ->
                    _uiState.update { it.copy(orders = orders) }
                }
        }
    }

    fun observeMerchantMessages() {
        val merchantId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            merchantMessageRepository.getMerchantMessagesFlow(merchantId)
                .catch { error ->
                    _uiState.update { it.copy(statusMessage = "Mesajlar yüklenemedi: ${error.localizedMessage}") }
                }
                .collect { messages ->
                    _uiState.update { it.copy(messages = messages) }
                }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, newStatus)
            result.fold(
                onSuccess = {
                    val message = when (newStatus) {
                        "confirmed" -> "✅ Sipariş onaylandı."
                        "cancelled" -> "✅ Sipariş iptal edildi."
                        "ready" -> "✅ Sipariş teslime hazır olarak işaretlendi."
                        "out_for_delivery" -> "✅ Sipariş yola çıktı olarak güncellendi."
                        "delivered" -> "✅ Sipariş teslim edildi olarak işaretlendi."
                        else -> "✅ Sipariş hazırlanmaya alındı."
                    }
                    _uiState.update { it.copy(statusMessage = message) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(statusMessage = "Sipariş durumu güncellenemedi: ${error.localizedMessage}") }
                }
            )
        }
    }

    fun toggleBusinessStatus() {
        _uiState.update { it.copy(isOpen = !it.isOpen) }
    }

    fun openAddProductDialog(itemToEdit: ItemModel? = null) {
        _uiState.update { it.copy(isAddProductDialogOpen = true, editingProduct = itemToEdit, statusMessage = null) }
    }

    fun closeAddProductDialog() {
        _uiState.update { it.copy(isAddProductDialogOpen = false, editingProduct = null) }
    }

    fun openAddServiceDialog(serviceToEdit: ServiceModel? = null) {
        _uiState.update { it.copy(isAddServiceDialogOpen = true, editingService = serviceToEdit, statusMessage = null) }
    }

    fun closeAddServiceDialog() {
        _uiState.update { it.copy(isAddServiceDialogOpen = false, editingService = null) }
    }

    fun saveProduct(
        title: String,
        price: Double,
        stock: Int,
        category: String,
        description: String?,
        imageUri: Uri?
    ) {
        val currentMerchantId = authRepository.currentUser?.uid ?: ""
        _uiState.update { it.copy(isSaving = true, statusMessage = null) }

        viewModelScope.launch {
            var imageUrl = _uiState.value.editingProduct?.imageUrl

            if (imageUri != null) {
                val uploadResult = storageRepository.uploadImage(imageUri, "product_images", currentMerchantId)
                uploadResult.fold(
                    onSuccess = { url -> imageUrl = url },
                    onFailure = { error ->
                        _uiState.update { current ->
                            current.copy(
                                isSaving = false,
                                statusMessage = "❌ Görsel yükleme hatası: ${error.localizedMessage}"
                            )
                        }
                        return@launch
                    }
                )
            }

            val existing = _uiState.value.editingProduct
            if (existing != null) {
                val updatedItem = existing.copy(
                    title = title,
                    price = price,
                    stock = stock,
                    category = category,
                    description = description?.takeIf { it.isNotBlank() },
                    imageUrl = imageUrl
                )
                val result = itemRepository.updateItem(updatedItem)
                result.fold(
                    onSuccess = {
                        _uiState.update { current ->
                            current.copy(
                                isSaving = false,
                                isAddProductDialogOpen = false,
                                editingProduct = null,
                                statusMessage = "✅ Ürün başarıyla güncellendi!"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { current ->
                            current.copy(isSaving = false, statusMessage = "❌ Hata: ${error.localizedMessage}")
                        }
                    }
                )
            } else {
                val newItem = ItemModel(
                    merchantId = currentMerchantId,
                    title = title,
                    price = price,
                    stock = stock,
                    category = category,
                    description = description?.takeIf { it.isNotBlank() },
                    imageUrl = imageUrl
                )
                val result = itemRepository.addItem(newItem)
                result.fold(
                    onSuccess = {
                        _uiState.update { current ->
                            current.copy(
                                isSaving = false,
                                isAddProductDialogOpen = false,
                                statusMessage = "✅ Ürün başarıyla eklendi!"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { current ->
                            current.copy(isSaving = false, statusMessage = "❌ Hata: ${error.localizedMessage}")
                        }
                    }
                )
            }
        }
    }

    fun saveService(
        title: String,
        price: Double,
        depositAmount: Double,
        duration: String?,
        category: String,
        description: String?,
        imageUri: Uri?
    ) {
        if (depositAmount < 0.0 || depositAmount > price * 0.5) {
            _uiState.update { it.copy(isSaving = false, statusMessage = "❌ Depozito toplam hizmet ücretinin yarısını geçemez.") }
            return
        }
        val currentMerchantId = authRepository.currentUser?.uid ?: ""
        _uiState.update { it.copy(isSaving = true, statusMessage = null) }

        viewModelScope.launch {
            var imageUrl = _uiState.value.editingService?.imageUrl

            if (imageUri != null) {
                val uploadResult = storageRepository.uploadImage(imageUri, "service_images", currentMerchantId)
                uploadResult.fold(
                    onSuccess = { url -> imageUrl = url },
                    onFailure = { error ->
                        _uiState.update { current ->
                            current.copy(
                                isSaving = false,
                                statusMessage = "❌ Görsel yükleme hatası: ${error.localizedMessage}"
                            )
                        }
                        return@launch
                    }
                )
            }

            val existing = _uiState.value.editingService
            if (existing != null) {
                val updatedService = existing.copy(
                    title = title,
                    price = price,
                    depositAmount = depositAmount,
                    duration = duration?.takeIf { it.isNotBlank() },
                    category = category,
                    description = description?.takeIf { it.isNotBlank() },
                    imageUrl = imageUrl
                )
                val result = serviceRepository.updateService(updatedService)
                result.fold(
                    onSuccess = {
                        _uiState.update { current ->
                            current.copy(
                                isSaving = false,
                                isAddServiceDialogOpen = false,
                                editingService = null,
                                statusMessage = "✅ Hizmet başarıyla güncellendi!"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { current ->
                            current.copy(isSaving = false, statusMessage = "❌ Hata: ${error.localizedMessage}")
                        }
                    }
                )
            } else {
                val newService = ServiceModel(
                    merchantId = currentMerchantId,
                    title = title,
                    price = price,
                    depositAmount = depositAmount,
                    duration = duration?.takeIf { it.isNotBlank() },
                    category = category,
                    description = description?.takeIf { it.isNotBlank() },
                    imageUrl = imageUrl
                )
                val result = serviceRepository.addService(newService)
                result.fold(
                    onSuccess = {
                        _uiState.update { current ->
                            current.copy(
                                isSaving = false,
                                isAddServiceDialogOpen = false,
                                statusMessage = "✅ Hizmet başarıyla eklendi!"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { current ->
                            current.copy(isSaving = false, statusMessage = "❌ Hata: ${error.localizedMessage}")
                        }
                    }
                )
            }
        }
    }

    /**
     * Randevu Talebini Onayla veya Reddet
     */
    fun updateAppointmentStatus(appointmentId: String, newStatus: String) {
        _uiState.update { it.copy(isLoading = true, statusMessage = null) }
        viewModelScope.launch {
            val result = when (newStatus) {
                "request_balance" -> appointmentRepository.completeServiceAndRequestBalance(appointmentId)
                "cash_paid" -> appointmentRepository.completeServiceWithCash(appointmentId)
                else -> appointmentRepository.updateAppointmentStatus(appointmentId, newStatus)
            }
            result.fold(
                onSuccess = {
                    val msg = when (newStatus) {
                        "approved" -> "✅ Randevu onaylandı!"
                        "request_balance" -> "✅ Hizmet tamamlandı; müşteriye demo bakiye ödeme talebi gönderildi."
                        "cash_paid" -> "✅ Elden ödeme alındı olarak tamamlandı."
                        else -> "❌ Randevu reddedildi."
                    }
                    _uiState.update { current ->
                        current.copy(isLoading = false, statusMessage = msg)
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(isLoading = false, statusMessage = "❌ İşlem hatası: ${error.localizedMessage}")
                    }
                }
            )
        }
    }

    fun deleteProduct(itemId: String) {
        val currentMerchantId = authRepository.currentUser?.uid ?: ""
        _uiState.update { it.copy(isLoading = true, statusMessage = null) }
        viewModelScope.launch {
            val result = itemRepository.deleteItem(itemId, currentMerchantId)
            result.fold(
                onSuccess = {
                    _uiState.update { current ->
                        current.copy(isLoading = false, statusMessage = "🗑️ Ürün silindi.")
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(isLoading = false, statusMessage = "❌ Silme hatası: ${error.localizedMessage}")
                    }
                }
            )
        }
    }

    fun deleteService(serviceId: String) {
        val currentMerchantId = authRepository.currentUser?.uid ?: ""
        _uiState.update { it.copy(isLoading = true, statusMessage = null) }
        viewModelScope.launch {
            val result = serviceRepository.deleteService(serviceId, currentMerchantId)
            result.fold(
                onSuccess = {
                    _uiState.update { current ->
                        current.copy(isLoading = false, statusMessage = "🗑️ Hizmet silindi.")
                    }
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        current.copy(isLoading = false, statusMessage = "❌ Silme hatası: ${error.localizedMessage}")
                    }
                }
            )
        }
    }
}
