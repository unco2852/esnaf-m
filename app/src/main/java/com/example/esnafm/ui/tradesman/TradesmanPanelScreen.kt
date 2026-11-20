package com.example.esnafm.ui.tradesman

import com.example.esnafm.R
import androidx.compose.ui.res.stringResource
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.esnafm.ui.customer.MerchantChatDialog
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.compose.BackHandler
import com.example.esnafm.data.model.AppointmentModel
import com.example.esnafm.data.model.ItemModel
import com.example.esnafm.data.model.MerchantMessageModel
import com.example.esnafm.data.model.OrderModel
import com.example.esnafm.data.model.ServiceModel

private enum class MerchantHistoryPage { ORDERS, SERVICES }

/**
 * Esnaf Yönetim Paneli Ekranı
 * Ürün, hizmet, sipariş, randevu, mesaj ve işletme ayarları alanlarını içerir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradesmanPanelScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: TradesmanPanelViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var historyPage by remember { mutableStateOf<MerchantHistoryPage?>(null) }
    var itemToDelete by remember { mutableStateOf<ItemModel?>(null) }
    var serviceToDelete by remember { mutableStateOf<ServiceModel?>(null) }
    var activeCustomerChatId by remember { mutableStateOf<String?>(null) }
    val currentMerchantId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    BackHandler(enabled = historyPage != null) {
        historyPage = null
    }

    val tabs = listOf(
        "Ürünlerim" to Icons.Default.ShoppingBag,
        "Hizmetlerim" to Icons.Default.Build,
        stringResource(R.string.tab_my_orders_count, uiState.activeOrders.size) to Icons.Default.ShoppingCart,
        stringResource(R.string.tab_my_appointments_count, uiState.activeAppointments.size) to Icons.Default.DateRange,
        "Mesajlar" to Icons.Default.Email,
        "İşletme Ayarları" to Icons.Default.Settings
    )

    Scaffold(
        containerColor = com.example.esnafm.ui.theme.WarmCream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (historyPage) {
                            MerchantHistoryPage.ORDERS -> "Önceki siparişler"
                            MerchantHistoryPage.SERVICES -> "Önceki hizmetler"
                            null -> stringResource(R.string.tradesman_panel_title)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (historyPage != null) historyPage = null else onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                },
                actions = {
                    if (historyPage == null) {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.btn_my_profile)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.esnafm.ui.theme.LightBeige,
                    titleContentColor = com.example.esnafm.ui.theme.DarkInk
                )
            )
        },
        floatingActionButton = {
            if (historyPage == null && selectedTabIndex == 0) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddProductDialog() },
                    icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product), tint = com.example.esnafm.ui.theme.DarkInk) },
                    text = { Text(stringResource(R.string.add_new_product), color = com.example.esnafm.ui.theme.DarkInk) },
                    containerColor = com.example.esnafm.ui.theme.SkyBlue,
                    contentColor = com.example.esnafm.ui.theme.DarkInk
                )
            } else if (historyPage == null && selectedTabIndex == 1) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddServiceDialog() },
                    icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_service), tint = com.example.esnafm.ui.theme.DarkInk) },
                    text = { Text(stringResource(R.string.add_new_service), color = com.example.esnafm.ui.theme.DarkInk) },
                    containerColor = com.example.esnafm.ui.theme.SkyBlue,
                    contentColor = com.example.esnafm.ui.theme.DarkInk
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (historyPage != null) {
                when (historyPage) {
                    MerchantHistoryPage.ORDERS -> TradesmanOrdersSection(
                        orders = uiState.previousOrders,
                        onUpdateStatus = { _, _ -> },
                        isHistoryPage = true
                    )
                    MerchantHistoryPage.SERVICES -> AppointmentRequestsSection(
                        appointments = uiState.previousAppointments,
                        onUpdateStatus = { app, status -> viewModel.updateAppointmentStatus(app.id, status) },
                        isHistoryPage = true
                    )
                    null -> Unit
                }
            } else {
            // İşletme Durumu Özet Kartı
            BusinessHeaderCard(
                uiState = uiState,
                onToggleStatus = { viewModel.toggleBusinessStatus() }
            )

            // Ana alan sekme başlıkları
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = { Icon(imageVector = icon, contentDescription = null) }
                    )
                }
            }

            // Durum / Bildirim Mesajı
            if (uiState.statusMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.statusMessage!!.contains("✅") || uiState.statusMessage!!.contains("🗑️")) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Text(
                        text = uiState.statusMessage!!,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Yükleme Durumu
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Seçili Sekmenin İçeriği
                when (selectedTabIndex) {
                    0 -> ProductListSection(
                        products = uiState.products,
                        onEditClick = { viewModel.openAddProductDialog(it) },
                        onDeleteClick = { itemToDelete = it }
                    )
                    1 -> ServiceListSection(
                        services = uiState.services,
                        onEditClick = { viewModel.openAddServiceDialog(it) },
                        onDeleteClick = { serviceToDelete = it }
                    )
                    2 -> TradesmanOrdersSection(
                        orders = uiState.activeOrders,
                        previousCount = uiState.previousOrders.size,
                        onOpenPrevious = { historyPage = MerchantHistoryPage.ORDERS },
                        onUpdateStatus = { order, status -> viewModel.updateOrderStatus(order.id, status) }
                    )
                    3 -> AppointmentRequestsSection(
                        appointments = uiState.activeAppointments,
                        previousCount = uiState.previousAppointments.size,
                        onOpenPrevious = { historyPage = MerchantHistoryPage.SERVICES },
                        onUpdateStatus = { app, status -> viewModel.updateAppointmentStatus(app.id, status) }
                    )
                    4 -> TradesmanMessagesSection(
                        messages = uiState.messages,
                        onOpenConversation = { activeCustomerChatId = it }
                    )
                    5 -> BusinessSettingsSection(uiState = uiState)
                }
            }
            }
        }

        // Ürün Ekleme / Düzenleme Diyaloğu
        if (uiState.isAddProductDialogOpen) {
            AddEditProductDialog(
                editingItem = uiState.editingProduct,
                onDismiss = { viewModel.closeAddProductDialog() },
                onSave = { title, price, stock, category, desc, imageUri ->
                    viewModel.saveProduct(title, price, stock, category, desc, imageUri)
                },
                isSaving = uiState.isSaving
            )
        }

        // Hizmet Ekleme / Düzenleme Diyaloğu
        if (uiState.isAddServiceDialogOpen) {
            AddEditServiceDialog(
                editingService = uiState.editingService,
                onDismiss = { viewModel.closeAddServiceDialog() },
                onSave = { title, price, deposit, duration, category, desc, imageUri ->
                    viewModel.saveService(title, price, deposit, duration, category, desc, imageUri)
                },
                isSaving = uiState.isSaving
            )
        }

        // Ürün Silme Onay Diyaloğu
        if (itemToDelete != null) {
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text(stringResource(R.string.delete_product_title)) },
                text = { Text("'${itemToDelete?.title}' başlıklı ürünü silmek istediğinize emin misiniz?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = itemToDelete?.id
                            itemToDelete = null
                            if (id != null) {
                                viewModel.deleteProduct(id)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.btn_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }

        // Hizmet Silme Onay Diyaloğu
        if (serviceToDelete != null) {
            AlertDialog(
                onDismissRequest = { serviceToDelete = null },
                title = { Text(stringResource(R.string.delete_service_title)) },
                text = { Text("'${serviceToDelete?.title}' başlıklı hizmeti silmek istediğinize emin misiniz?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = serviceToDelete?.id
                            serviceToDelete = null
                            if (id != null) {
                                viewModel.deleteService(id)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.btn_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { serviceToDelete = null }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }
    }

    activeCustomerChatId?.let { customerId ->
        MerchantChatDialog(
            merchantId = currentMerchantId,
            customerId = customerId,
            currentUserId = currentMerchantId,
            title = "Müşteri sohbeti",
            isMerchant = true,
            onDismiss = { activeCustomerChatId = null }
        )
    }
}

@Composable
private fun BusinessHeaderCard(
    uiState: TradesmanUiState,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                        text = uiState.businessName.ifBlank { "İşletme hesabı" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (uiState.category.isNotBlank()) {
                        Text(uiState.category, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = if (uiState.isOpen) "🟢 İşletme Açık" else "🔴 İşletme Kapalı",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.isOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Switch(
                checked = uiState.isOpen,
                onCheckedChange = { onToggleStatus() }
            )
        }
    }
}

/**
 * Ürün Listesi Bölümü
 */
@Composable
private fun ProductListSection(
    products: List<ItemModel>,
    onEditClick: (ItemModel) -> Unit,
    onDeleteClick: (ItemModel) -> Unit
) {
    if (products.isEmpty()) {
        EmptyStateSection(
            icon = Icons.Default.ShoppingBag,
            title = stringResource(R.string.empty_products_title),
            subtitle = stringResource(R.string.empty_products_desc)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "📦 Dükkan Ürünleri (${products.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(products) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!item.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (!item.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Stok: ${item.stock} Adet",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${item.price} TL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Row {
                                IconButton(onClick = { onEditClick(item) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.btn_edit),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { onDeleteClick(item) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.btn_delete),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hizmet Listesi Bölümü
 */
@Composable
private fun ServiceListSection(
    services: List<ServiceModel>,
    onEditClick: (ServiceModel) -> Unit,
    onDeleteClick: (ServiceModel) -> Unit
) {
    if (services.isEmpty()) {
        EmptyStateSection(
            icon = Icons.Default.Build,
            title = stringResource(R.string.empty_services_title),
            subtitle = stringResource(R.string.empty_services_desc)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "🛠️ Hizmetleriniz (${services.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(services) { service ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!service.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = service.imageUrl,
                                contentDescription = service.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = service.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (!service.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = service.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!service.duration.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Süre: ${service.duration}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                text = "Depozito: ${service.depositAmount} TL (en fazla %50)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${service.price} TL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Row {
                                IconButton(onClick = { onEditClick(service) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.btn_edit),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { onDeleteClick(service) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.btn_delete),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Esnafa Gelen Siparişler ve Durum Takibi Bölümü
 */
@Composable
private fun TradesmanOrdersSection(
    orders: List<OrderModel>,
    onUpdateStatus: (OrderModel, String) -> Unit,
    previousCount: Int = 0,
    onOpenPrevious: () -> Unit = {},
    isHistoryPage: Boolean = false
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (!isHistoryPage) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 16.dp, top = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpenPrevious, modifier = Modifier.height(38.dp)) {
                    Text("Önceki siparişler · $previousCount", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                EmptyStateSection(
                    icon = Icons.Default.ShoppingCart,
                    title = if (isHistoryPage) "Önceki sipariş yok" else "Henüz aktif sipariş yok",
                    subtitle = if (isHistoryPage) "Tamamlanan veya iptal edilen siparişler burada görünecek." else "Yeni sipariş talepleri bu bölümde görünecek."
                )
            }
        } else {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (isHistoryPage) "Önceki siparişler (${orders.size})" else "Gelen siparişler (${orders.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            items(orders, key = { it.id }) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(order.itemTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = when (order.status) {
                                "confirmed" -> "Esnaf onayladı"
                                "preparing" -> "Hazırlanıyor"
                                "ready" -> "Teslime hazır"
                                "out_for_delivery" -> "Yolda"
                                "delivered" -> "Tamamlandı"
                                "cancelled" -> "İptal edildi"
                                else -> "Esnaf onayı bekleniyor"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text("Adet: ${order.quantity}  •  Toplam: ${order.price * order.quantity} TL")
                    Text(
                        text = "Ödeme: ${when {
                            order.paymentStatus == "demo_paid" -> "Kart ödemesi demo olarak kaydedildi · gerçek tahsilat yok"
                            order.paymentStatus == "demo_refunded" -> "Demo iadesi işlendi · gerçek para hareketi yok"
                            order.paymentMethod == "cash_on_delivery" -> "Kapıda nakit"
                            order.paymentMethod == "pay_at_store" -> "İşletmede ödeme"
                            else -> "Ödeme bekleniyor"
                        }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (order.recipientName.isNotBlank()) Text("Teslim alacak: ${order.recipientName} · ${order.customerPhone}")
                    if (order.fulfillmentType == "pickup") {
                        Text("Teslim alma: İşletmeden")
                    } else if (order.deliveryAddress.isNotBlank()) {
                        Text("Teslimat adresi: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (order.customerId.isNotBlank()) {
                        Text(
                            text = "Müşteri ID: ${order.customerId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (order.status == "pending") {
                        val deadline = java.text.SimpleDateFormat("HH:mm", java.util.Locale("tr", "TR"))
                            .format(java.util.Date(order.approvalDeadlineAt))
                        Text("Onay süresi sonu: $deadline · süre dolarsa sipariş otomatik iptal edilir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                    if (order.status == "cancelled" && order.cancellationReason == "merchant_approval_timeout") {
                        Text("30 dakika içinde esnaf onayı gelmediği için otomatik iptal edildi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    if (order.status == "cancelled" && order.cancellationReason == "customer_cancelled") {
                        Text("Müşteri siparişi iptal etti. Varsa demo ödeme iadesi işlendi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    if (order.status == "cancelled" && order.cancellationReason == "merchant_cancelled") {
                        Text("Sipariş işletme tarafından iptal edildi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    when (order.status) {
                        "pending" -> Button(
                            onClick = { onUpdateStatus(order, "preparing") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Siparişi onayla ve hazırlamaya al") }
                        "confirmed" -> Button(onClick = { onUpdateStatus(order, "preparing") }, modifier = Modifier.fillMaxWidth()) {
                            Text("Hazırlamaya al")
                        }
                        "preparing" -> OutlinedButton(
                            onClick = { onUpdateStatus(order, "ready") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Hazır olarak işaretle") }
                        "ready" -> Button(
                            onClick = { onUpdateStatus(order, if (order.fulfillmentType == "delivery") "out_for_delivery" else "delivered") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (order.fulfillmentType == "delivery") "Kuryeye verildi · yola çıktı" else "Müşteriye teslim edildi") }
                        "out_for_delivery" -> OutlinedButton(
                            onClick = { onUpdateStatus(order, "delivered") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Teslimatı tamamla") }
                    }
                    if (order.status == "pending" ||
                        (order.status == "confirmed" && order.paymentStatus != "paid" && order.paymentStatus != "processing")) {
                        OutlinedButton(
                            onClick = { onUpdateStatus(order, "cancelled") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Sipariş talebini iptal et") }
                    }
                }
            }
        }
    }
    }
}
}

@Composable
private fun TradesmanMessagesSection(
    messages: List<MerchantMessageModel>,
    onOpenConversation: (String) -> Unit
) {
    if (messages.isEmpty()) {
        EmptyStateSection(
            icon = Icons.Default.Email,
            title = "Henüz mesaj yok",
            subtitle = "Esnaf sayfandan gelen mesajlar burada görünür."
        )
        return
    }

    val conversations = remember(messages) {
        messages.groupBy { it.customerId.ifBlank { it.senderId } }
            .mapNotNull { (customerId, thread) -> thread.maxByOrNull { it.createdAt }?.let { customerId to it } }
            .sortedByDescending { it.second.createdAt }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Sohbetler (${conversations.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        items(conversations, key = { it.first }) { (customerId, message) ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenConversation(customerId) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Kullanıcı ID: $customerId",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = message.message.ifBlank { when (message.attachmentType) { "image" -> "📷 Fotoğraf"; "video" -> "🎥 Video"; else -> "📎 ${message.attachmentName.ifBlank { "Dosya" }}" } },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = java.text.SimpleDateFormat("HH:mm", java.util.Locale("tr", "TR")).format(java.util.Date(message.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/** Esnafa gelen hizmet ve randevu taleplerini, ödeme adımlarıyla birlikte gösterir. */
@Composable
private fun AppointmentRequestsSection(
    appointments: List<AppointmentModel>,
    onUpdateStatus: (AppointmentModel, String) -> Unit,
    previousCount: Int = 0,
    onOpenPrevious: () -> Unit = {},
    isHistoryPage: Boolean = false
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (!isHistoryPage) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 16.dp, top = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpenPrevious, modifier = Modifier.height(38.dp)) {
                    Text("Önceki hizmetler · $previousCount", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (appointments.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                EmptyStateSection(
                    icon = Icons.Default.DateRange,
                    title = if (isHistoryPage) "Önceki hizmet yok" else "Henüz aktif hizmet yok",
                    subtitle = if (isHistoryPage) "Tamamlanan, reddedilen veya iptal edilen hizmetler burada görünecek." else "Yeni randevu talepleri bu bölümde görünecek."
                )
            }
        } else {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (isHistoryPage) "📅 Önceki hizmetler (${appointments.size})" else "📅 Gelen hizmet talepleri (${appointments.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(appointments) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.serviceTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            val (statusText, statusColor) = when (app.status) {
                                "approved" -> "🟢 Onaylandı" to MaterialTheme.colorScheme.primary
                                "service_completed" -> "💳 Kalan ödeme bekleniyor" to MaterialTheme.colorScheme.tertiary
                                "rejected" -> "🔴 Reddedildi" to MaterialTheme.colorScheme.error
                                "completed" -> "✅ Tamamlandı" to MaterialTheme.colorScheme.primary
                                "cancelled" -> "🔴 Müşteri iptal etti" to MaterialTheme.colorScheme.error
                                else -> "🟡 Beklemede" to MaterialTheme.colorScheme.tertiary
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (app.customerEmail.isNotBlank()) {
                            Text(
                                text = "Müşteri: ${app.customerEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (app.recipientName.isNotBlank() || app.customerPhone.isNotBlank()) {
                            Text("İletişim: ${app.recipientName} · ${app.customerPhone}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = "Depozito: ${app.depositAmount} TL · ${when (app.depositPaymentStatus) {
                                "not_required" -> "yok"
                                "demo_paid" -> "demo olarak ödendi"
                                "demo_refunded" -> "demo iadesi işlendi"
                                "cash_paid" -> "elden alındı"
                                else -> "demo ödemesi bekleniyor"
                            }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Kalan ${ (app.price - app.depositAmount).coerceAtLeast(0.0) } TL · ${when (app.balancePaymentStatus) {
                                "requested" -> "müşteriden demo ödeme istendi"
                                "demo_paid" -> "demo olarak tamamlandı"
                                "cash_paid" -> "elden alındı"
                                else -> "hizmet sonunda alınacak"
                            }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (app.serviceLocation == "at_customer" && app.serviceAddress.isNotBlank()) {
                            Text("Hizmet adresi: ${app.serviceAddress}", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("Hizmet yeri: İşletme", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${app.date} - ${app.time}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${app.price} TL",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (!app.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Müşteri Notu: ${app.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (app.status == "pending") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onUpdateStatus(app, "approved") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.btn_approve))
                                }

                                OutlinedButton(
                                    onClick = { onUpdateStatus(app, "rejected") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.btn_reject), color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        if (app.status == "approved") {
                            Spacer(modifier = Modifier.height(12.dp))
                            if (app.depositAmount > 0.0 && app.depositPaymentStatus == "unpaid") {
                                Text("Müşterinin depozito demo ödemesi bekleniyor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                            } else {
                                Button(onClick = { onUpdateStatus(app, "request_balance") }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Hizmet tamamlandı · online demo bakiye iste")
                                }
                                OutlinedButton(onClick = { onUpdateStatus(app, "cash_paid") }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Elden ödeme alındı · tamamla")
                                }
                            }
                        }
                        if (app.status == "cancelled" && app.cancellationReason == "customer_cancelled") {
                            Text("Müşteri randevuyu iptal etti.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                        if (app.status == "service_completed" && app.balancePaymentStatus == "requested") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Müşteriye kalan tutar için demo online ödeme talebi gönderildi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                            OutlinedButton(onClick = { onUpdateStatus(app, "cash_paid") }, modifier = Modifier.fillMaxWidth()) {
                                Text("Elden ödeme alındı · tamamla")
                            }
                        }
                        if (app.depositAmount > 0.0 && app.depositPaymentStatus == "demo_paid") {
                            Text("Depozito gerçek tahsilat değildir; demo kaydıdır.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    }
}

/**
 * Boş Durum (Empty State) Bileşeni
 */
@Composable
private fun EmptyStateSection(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * İşletme Ayarları Section
 */
@Composable
private fun BusinessSettingsSection(uiState: TradesmanUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_business_info),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            SettingDetailCard(
                icon = Icons.Default.Info,
                title = stringResource(R.string.business_name),
                value = uiState.businessName.ifBlank { "Henüz belirtilmedi" }
            )
        }

        item {
            SettingDetailCard(
                icon = Icons.Default.Category,
                title = "Kategori",
                value = uiState.category.ifBlank { "Henüz belirtilmedi" }
            )
        }

        item {
            SettingDetailCard(
                icon = Icons.Default.Description,
                title = "İşletme açıklaması",
                value = uiState.businessDescription.ifBlank { "Henüz eklenmedi" }
            )
        }

        item {
            SettingDetailCard(
                icon = Icons.Default.LocationOn,
                title = "Şehir / İlçe",
                value = listOf(uiState.businessCity, uiState.businessDistrict)
                    .filter { it.isNotBlank() }
                    .joinToString(" / ")
                    .ifBlank { "Henüz belirtilmedi" }
            )
        }

        item {
            SettingDetailCard(
                icon = Icons.Default.Store,
                title = stringResource(R.string.business_address),
                value = uiState.businessAddress.ifBlank { "Henüz belirtilmedi" }
            )
        }

        item {
            SettingDetailCard(
                icon = Icons.Default.Phone,
                title = stringResource(R.string.contact_phone),
                value = uiState.businessPhone.ifBlank { "Henüz belirtilmedi" }
            )
        }

        item {
            SettingDetailCard(
                icon = Icons.Default.Schedule,
                title = stringResource(R.string.working_hours),
                value = uiState.workingHours.ifBlank { "Henüz belirtilmedi" }
            )
        }
    }
}

@Composable
private fun SettingDetailCard(
    icon: ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

/**
 * Ürün Ekleme / Düzenleme Form Diyaloğu
 */
@Composable
private fun AddEditProductDialog(
    editingItem: ItemModel?,
    onDismiss: () -> Unit,
    onSave: (title: String, price: Double, stock: Int, category: String, description: String?, imageUri: Uri?) -> Unit,
    isSaving: Boolean
) {
    val categoryList = remember {
        listOf("Berber & Kuaför", "Bakkal & Market", "Terzi & Tekstil", "Tesisat & Bakım", "Fırın & Pastane", "Restoran & Kafe", "Diğer")
    }

    var title by remember { mutableStateOf(editingItem?.title ?: "") }
    var priceText by remember { mutableStateOf(editingItem?.price?.toString() ?: "") }
    var stockText by remember { mutableStateOf(editingItem?.stock?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(editingItem?.category?.ifBlank { categoryList.first() } ?: categoryList.first()) }
    var description by remember { mutableStateOf(editingItem?.description ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editingItem != null) stringResource(R.string.edit_product) else stringResource(R.string.add_new_product),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Görsel Seçme & Önizleme Alanı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = stringResource(R.string.photo_selected_product),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (editingItem != null && !editingItem.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = editingItem.imageUrl,
                            contentDescription = stringResource(R.string.photo_current_product),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = stringResource(R.string.btn_select_photo),
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.add_product_photo_optional),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.product_title_required)) },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Kategori Seçim Alanı
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.select_category),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categoryList) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(stringResource(R.string.price_required)) },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text(stringResource(R.string.stock_required)) },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.desc_optional)) },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = priceText.toDoubleOrNull()
                    val parsedStock = stockText.toIntOrNull()

                    if (title.isBlank()) {
                        errorMessage = "Lütfen ürün başlığı giriniz."
                    } else if (parsedPrice == null || parsedPrice <= 0) {
                        errorMessage = "Geçerli bir fiyat giriniz."
                    } else if (parsedStock == null || parsedStock < 0) {
                        errorMessage = "Geçerli bir stok adedi giriniz."
                    } else {
                        errorMessage = null
                        onSave(title, parsedPrice, parsedStock, selectedCategory, description, selectedImageUri)
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (editingItem != null) stringResource(R.string.btn_update) else "Kaydet")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.btn_cancel_request))
            }
        }
    )
}

/**
 * Hizmet Ekleme / Düzenleme Form Diyaloğu
 */
@Composable
private fun AddEditServiceDialog(
    editingService: ServiceModel?,
    onDismiss: () -> Unit,
    onSave: (title: String, price: Double, depositAmount: Double, duration: String?, category: String, description: String?, imageUri: Uri?) -> Unit,
    isSaving: Boolean
) {
    val categoryList = remember {
        listOf("Berber & Kuaför", "Bakkal & Market", "Terzi & Tekstil", "Tesisat & Bakım", "Fırın & Pastane", "Restoran & Kafe", "Diğer")
    }

    var title by remember { mutableStateOf(editingService?.title ?: "") }
    var priceText by remember { mutableStateOf(editingService?.price?.toString() ?: "") }
    var depositText by remember { mutableStateOf(editingService?.depositAmount?.toString() ?: "0") }
    var durationText by remember { mutableStateOf(editingService?.duration ?: "") }
    var selectedCategory by remember { mutableStateOf(editingService?.category?.ifBlank { categoryList.first() } ?: categoryList.first()) }
    var description by remember { mutableStateOf(editingService?.description ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editingService != null) stringResource(R.string.edit_service) else stringResource(R.string.add_new_service),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Görsel Seçme & Önizleme Alanı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = stringResource(R.string.photo_selected_service),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (editingService != null && !editingService.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = editingService.imageUrl,
                            contentDescription = stringResource(R.string.photo_current_service),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = stringResource(R.string.btn_select_photo),
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.add_service_photo_optional),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.service_name_required)) },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Kategori Seçim Alanı
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.select_category),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categoryList) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(stringResource(R.string.price_required)) },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = depositText,
                    onValueChange = { depositText = it },
                    label = { Text("Randevu depozitosu (TL)") },
                    supportingText = { Text("En fazla toplam hizmet ücretinin yarısı olabilir.") },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text(stringResource(R.string.estimated_duration)) },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.desc_optional)) },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = priceText.toDoubleOrNull()
                    val parsedDeposit = depositText.toDoubleOrNull()

                    if (title.isBlank()) {
                        errorMessage = "Lütfen hizmet adını giriniz."
                    } else if (parsedPrice == null || parsedPrice <= 0) {
                        errorMessage = "Geçerli bir fiyat giriniz."
                    } else if (parsedDeposit == null || parsedDeposit < 0 || parsedDeposit > parsedPrice * 0.5) {
                        errorMessage = "Depozito toplam hizmet ücretinin yarısını geçemez."
                    } else {
                        errorMessage = null
                        onSave(title, parsedPrice, parsedDeposit, durationText, selectedCategory, description, selectedImageUri)
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (editingService != null) stringResource(R.string.btn_update) else "Kaydet")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.btn_cancel_request))
            }
        }
    )
}
