package com.example.esnafm.ui.customer

import com.example.esnafm.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.esnafm.data.model.ItemModel
import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.repository.AddressRepository
import com.example.esnafm.data.repository.AuthRepository
import com.example.esnafm.data.repository.ItemRepository
import com.example.esnafm.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Ürün Detay Ekranı
 * Ürün bilgilerini, stok durumunu ve ilgili esnafa yönlendirmeyi gösterir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMerchantDetail: (String) -> Unit
) {
    var itemModel by remember { mutableStateOf<ItemModel?>(null) }
    var merchantProfile by remember { mutableStateOf<UserProfile?>(null) }
    var customerProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var orderMessage by remember { mutableStateOf<String?>(null) }
    var orderError by remember { mutableStateOf<String?>(null) }
    var isOrdering by remember { mutableStateOf(false) }
    var isCheckoutOpen by remember { mutableStateOf(false) }
    var orderQuantity by remember(productId) { mutableIntStateOf(1) }
    val orderRepository = remember { OrderRepository() }
    val addressRepository = remember { AddressRepository() }
    val authRepository = remember { AuthRepository() }
    val itemRepository = remember { ItemRepository() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val savedAddresses by remember(currentUserId) { addressRepository.getUserAddressesFlow(currentUserId) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentUserId) {
        customerProfile = null
        if (currentUserId.isNotBlank()) {
            customerProfile = authRepository.getUserProfile(currentUserId).getOrNull()
        }
    }

    LaunchedEffect(productId) {
        if (productId.isBlank()) {
            errorMessage = "Geçersiz ürün kimliği."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            isLoading = true
            errorMessage = null
            val itemResult = itemRepository.getItemById(productId)
            if (itemResult.isSuccess) {
                val item = itemResult.getOrThrow()
                itemModel = item

                // 2. İlgili Esnaf Profilini Çek
                if (item.merchantId.isNotBlank()) {
                    merchantProfile = authRepository.getPublicMerchantProfile(item.merchantId).getOrNull()
                }
            } else {
                errorMessage = itemResult.exceptionOrNull()?.localizedMessage ?: "Ürün bulunamadı."
            }

            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Ürün detayları yüklenirken hata oluştu: ${e.localizedMessage}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = itemModel?.title ?: "Ürün Detayı",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val item = itemModel!!
                val profile = merchantProfile

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Ürün Görseli / Yer Tutucu
                    if (!item.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Ürün Başlık & Fiyat
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            if (item.category.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.price} TL",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )

                                val stockText = if (item.stock > 0) "🟢 Stokta Var (${item.stock} Adet)" else "🔴 Stok Tükendi"
                                Text(
                                    text = stockText,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (item.stock > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }

                            if (!item.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Açıklama:",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (orderMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(orderMessage!!, color = MaterialTheme.colorScheme.primary)
                            }
                            if (orderError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(orderError!!, color = MaterialTheme.colorScheme.error)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { if (orderQuantity > 1) orderQuantity-- },
                                    enabled = !isOrdering && orderQuantity > 1
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Bir azalt")
                                }
                                Text(
                                    text = orderQuantity.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                IconButton(
                                    onClick = { if (orderQuantity < item.stock) orderQuantity++ },
                                    enabled = !isOrdering && orderQuantity < item.stock
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Bir artır")
                                }
                                Button(
                                    onClick = {
                                        orderError = null
                                        if (FirebaseAuth.getInstance().currentUser == null) {
                                            orderError = "Sipariş vermek için önce giriş yapmalısınız."
                                        } else {
                                            isCheckoutOpen = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isOrdering && item.stock > 0 && orderQuantity <= item.stock
                                ) {
                                    if (isOrdering) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (item.stock > 0) "Sipariş ver" else "Stokta yok")
                                    }
                                }
                            }
                        }
                    }

                    // Esnaf Kartı
                    if (profile != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Store,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = profile.businessName.ifBlank { profile.name },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (profile.category.isNotBlank()) {
                                            Text(
                                                text = profile.category,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }

                                if (profile.address.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = profile.address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedButton(
                                    onClick = { onNavigateToMerchantDetail(profile.uid) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Esnaf Sayfasına Git")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isCheckoutOpen && itemModel != null) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val profile = customerProfile
        val profileAddress = profile?.address?.takeIf { it.isNotBlank() }
            ?: listOf(profile?.city, profile?.district, profile?.neighborhood, profile?.street, profile?.buildingNo)
                .filterNotNull()
                .filter { it.isNotBlank() }
                .joinToString(", ")
        val preferredAddress = savedAddresses.firstOrNull { it.isDefault }?.fullAddress?.takeIf { it.isNotBlank() }
            ?: savedAddresses.firstOrNull()?.fullAddress?.takeIf { it.isNotBlank() }
            ?: profileAddress
        ProductCheckoutDialog(
            item = itemModel!!,
            quantity = orderQuantity,
            savedAddresses = savedAddresses,
            initialAddress = preferredAddress,
            isSubmitting = isOrdering,
            errorMessage = orderError,
            onDismiss = { if (!isOrdering) isCheckoutOpen = false },
            onConfirm = { address, fulfillmentType, paymentMethod ->
                if (currentUser == null) {
                    orderError = "Sipariş vermek için tekrar giriş yapmalısınız."
                    return@ProductCheckoutDialog
                }
                isOrdering = true
                orderError = null
                coroutineScope.launch {
                    val result = orderRepository.createOrder(
                        itemId = itemModel!!.id,
                        customerId = currentUser.uid,
                        quantity = orderQuantity,
                        deliveryAddress = if (fulfillmentType == "delivery") address else "",
                        fulfillmentType = fulfillmentType,
                        paymentMethod = paymentMethod
                    )
                    isOrdering = false
                    result.fold(
                        onSuccess = {
                            itemModel = itemModel!!.copy(stock = (itemModel!!.stock - orderQuantity).coerceAtLeast(0))
                            isCheckoutOpen = false
                            orderMessage = if (paymentMethod == "mock_card") {
                                "Ödeme kaydınız oluşturuldu, sipariş esnaf onayına gönderildi. 30 dakika içinde onaylanmazsa sipariş otomatik iptal edilir."
                            } else {
                                "Siparişiniz oluşturuldu. Esnaf onayını ve teslimat aşamalarını Siparişlerim bölümünden takip edin."
                            }
                            orderQuantity = 1
                        },
                        onFailure = { orderError = it.localizedMessage ?: "Sipariş oluşturulamadı." }
                    )
                }
            }
        )
    }

}
