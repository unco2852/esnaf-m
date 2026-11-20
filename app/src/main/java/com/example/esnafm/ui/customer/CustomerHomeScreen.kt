package com.example.esnafm.ui.customer

import android.Manifest
import com.example.esnafm.R
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.esnafm.data.model.AppointmentModel
import com.example.esnafm.data.model.ItemModel
import com.example.esnafm.data.model.OrderModel
import com.example.esnafm.data.model.ServiceModel
import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.ui.theme.DarkInk
import com.example.esnafm.ui.theme.LightBeige
import com.example.esnafm.ui.theme.SkyBlue
import com.example.esnafm.ui.theme.Terracotta
import com.example.esnafm.ui.theme.Turquoise
import com.example.esnafm.ui.theme.WarmCream
import com.example.esnafm.util.hasLocationPermission
import com.example.esnafm.util.requestDeviceLocation
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class CustomerPreviousHistoryPage {
    ORDERS,
    SERVICES
}

/**
 * Müşteri Ana Ekranı (Keşfet, Kategoriler, Randevularım, Siparişlerim, Favoriler)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMerchantDetail: (String) -> Unit = {},
    onNavigateToProductDetail: (String) -> Unit = {},
    onNavigateToServiceDetail: (String) -> Unit = {},
    viewModel: CustomerHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var previousHistoryPage by remember { mutableStateOf<CustomerPreviousHistoryPage?>(null) }
    var demoPaymentRequest by remember { mutableStateOf<Pair<AppointmentModel, Boolean>?>(null) }
    var appointmentToCancel by remember { mutableStateOf<AppointmentModel?>(null) }
    var orderToCancel by remember { mutableStateOf<OrderModel?>(null) }
    var pendingAssistantQuery by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    BackHandler(enabled = previousHistoryPage != null || selectedTabIndex != 0) {
        if (previousHistoryPage != null) previousHistoryPage = null
        else selectedTabIndex = 0
    }

    fun submitAssistantQueryWithLocation(query: String) {
        requestDeviceLocation(context) { coordinates ->
            viewModel.askCirak(query, coordinates?.latitude, coordinates?.longitude)
        }
    }

    val assistantLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val query = pendingAssistantQuery
            pendingAssistantQuery = null
            if (query != null) {
                if (permissions.values.any { it }) submitAssistantQueryWithLocation(query)
                else viewModel.askCirak(query)
            }
        }
    )

    fun submitAssistantQuery(query: String) {
        viewModel.beginAssistantLocationLookup()
        if (hasLocationPermission(context)) {
            submitAssistantQueryWithLocation(query)
        } else {
            pendingAssistantQuery = query
            assistantLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        containerColor = WarmCream,
        topBar = {
            TopAppBar(
                title = {
                    if (previousHistoryPage == null) {
                        Column {
                            Text(
                                text = stringResource(R.string.home_welcome),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(R.string.home_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = Turquoise
                            )
                        }
                    } else {
                        Text(
                            text = when (previousHistoryPage) {
                                CustomerPreviousHistoryPage.ORDERS -> "Önceki siparişler"
                                CustomerPreviousHistoryPage.SERVICES -> "Önceki hizmetler"
                                null -> ""
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (previousHistoryPage != null) previousHistoryPage = null
                        else onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                },
                actions = {
                    if (previousHistoryPage == null) {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.btn_my_profile)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBeige,
                    titleContentColor = DarkInk
                )
            )
        },
        bottomBar = {
            if (previousHistoryPage == null) {
                NavigationBar(
                    containerColor = LightBeige,
                    contentColor = DarkInk,
                    tonalElevation = 8.dp
                ) {
                    CustomerNavigationItem(
                        selected = selectedTabIndex == 0,
                        label = stringResource(R.string.tab_explore),
                        icon = Icons.Default.Explore,
                        onClick = { selectedTabIndex = 0 }
                    )
                    CustomerNavigationItem(
                        selected = selectedTabIndex == 1,
                        label = stringResource(R.string.tab_categories),
                        icon = Icons.Default.Category,
                        onClick = { selectedTabIndex = 1 }
                    )
                    CustomerNavigationItem(
                        selected = selectedTabIndex == 2,
                        label = stringResource(R.string.tab_my_appointments),
                        icon = Icons.Default.DateRange,
                        badgeCount = uiState.activeAppointments.size,
                        onClick = { selectedTabIndex = 2 }
                    )
                    CustomerNavigationItem(
                        selected = selectedTabIndex == 3,
                        label = stringResource(R.string.tab_my_orders),
                        icon = Icons.Default.ShoppingCart,
                        badgeCount = uiState.activeOrders.size,
                        onClick = { selectedTabIndex = 3 }
                    )
                    CustomerNavigationItem(
                        selected = selectedTabIndex == 4,
                        label = stringResource(R.string.tab_favorites),
                        icon = Icons.Default.Favorite,
                        badgeCount = uiState.favoriteMerchants.size,
                        onClick = { selectedTabIndex = 4 }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (previousHistoryPage != null) {
                when (previousHistoryPage) {
                    CustomerPreviousHistoryPage.ORDERS -> CustomerOrdersSection(
                        orders = uiState.previousOrders,
                        isHistoryPage = true,
                        onCancelOrder = {}
                    )
                    CustomerPreviousHistoryPage.SERVICES -> CustomerAppointmentsSection(
                        appointments = uiState.previousAppointments,
                        isHistoryPage = true,
                        onPayDeposit = {},
                        onPayBalance = { demoPaymentRequest = it to false },
                        onCancelAppointment = {}
                    )
                    null -> Unit
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                // Arama Çubuğu
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    placeholder = { Text(stringResource(R.string.search_placeholder_explore)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.btn_search), tint = Turquoise)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = stringResource(R.string.btn_clear_icon))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Aktif Kategori Filtresi Bilgi Çubuğu
                if (uiState.selectedCategory != "Tümü") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Seçili Kategori: ${uiState.selectedCategory}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkInk
                            )
                        )
                        TextButton(onClick = { viewModel.clearCategoryFilter() }) {
                            Text(stringResource(R.string.filter_clear), color = Terracotta)
                        }
                    }
                }

                // Durum / Başarı Bildirimi Mesajı
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Terracotta.copy(alpha = 0.12f))
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkInk,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                if (uiState.bookingSuccessMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = LightBeige
                        )
                    ) {
                        Text(
                            text = uiState.bookingSuccessMessage!!,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkInk,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        (fadeIn() + slideInVertically(initialOffsetY = { it / 14 })) togetherWith
                            (fadeOut() + slideOutVertically(targetOffsetY = { -it / 18 }))
                    },
                    label = "customer_navigation_transition"
                ) { activeTab ->
                when (activeTab) {
                    0 -> ExploreSection(
                        uiState = uiState,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onNavigateToMerchantDetail = onNavigateToMerchantDetail,
                        onOpenAssistant = { viewModel.openAssistant() }
                    )
                    1 -> CategoriesSection(
                        uiState = uiState,
                        onCategorySelected = {
                            viewModel.onCategorySelected(it)
                            selectedTabIndex = 0 // Filtrelenen sonuçları Keşfet sekmesinde göster
                        }
                    )
                    2 -> CustomerAppointmentsSection(
                        appointments = uiState.activeAppointments,
                        previousCount = uiState.previousAppointments.size,
                        onOpenPrevious = { previousHistoryPage = CustomerPreviousHistoryPage.SERVICES },
                        onPayDeposit = { demoPaymentRequest = it to true },
                        onPayBalance = { demoPaymentRequest = it to false },
                        onCancelAppointment = { appointmentToCancel = it }
                    )
                    3 -> CustomerOrdersSection(
                        orders = uiState.activeOrders,
                        previousCount = uiState.previousOrders.size,
                        onOpenPrevious = { previousHistoryPage = CustomerPreviousHistoryPage.ORDERS },
                        onCancelOrder = { orderToCancel = it }
                    )
                    4 -> FavoritesSection(
                        favoriteMerchants = uiState.favoriteMerchants,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onNavigateToMerchantDetail = onNavigateToMerchantDetail
                    )
                }
                }
            }
            }

            // Çırak Düğmesi - Yalnızca Keşfet sekmesinde (selectedTabIndex == 0) sol alt köşede
            if (selectedTabIndex == 0 && previousHistoryPage == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 24.dp)
                ) {
                    FloatingActionButton(
                        onClick = { viewModel.openAssistant() },
                        containerColor = Turquoise,
                        contentColor = WarmCream,
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Çırak Asistan",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Çırak",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Randevu Talebi Oluşturma Diyaloğu
        if (uiState.selectedServiceForBooking != null) {
            BookAppointmentDialog(
                service = uiState.selectedServiceForBooking!!,
                onDismiss = { viewModel.closeBookingDialog() },
                initialName = FirebaseAuth.getInstance().currentUser?.displayName.orEmpty(),
                initialPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber.orEmpty(),
                errorMessage = uiState.errorMessage,
                onConfirm = { date, time, note, name, phone, location, address ->
                    viewModel.bookAppointment(
                        uiState.selectedServiceForBooking!!,
                        date,
                        time,
                        note,
                        name,
                        phone,
                        location,
                        address
                    )
                },
                isSaving = uiState.isBookingLoading
            )
        }

        // Çırak Sohbet Diyaloğu
        if (uiState.isAssistantOpen) {
            CirakChatDialog(
                uiState = uiState,
                onDismiss = {
                    pendingAssistantQuery = null
                    viewModel.closeAssistant()
                },
                onSubmitMessage = { submitAssistantQuery(it) },
                onNavigateToMerchantDetail = onNavigateToMerchantDetail,
                onToggleFavorite = { viewModel.toggleFavorite(it) }
            )
        }

        demoPaymentRequest?.let { (appointment, isDeposit) ->
            DemoPaymentConfirmationDialog(
                title = if (isDeposit) "Depozito demo ödemesi" else "Kalan tutar demo ödemesi",
                amount = if (isDeposit) appointment.depositAmount else (appointment.price - appointment.depositAmount).coerceAtLeast(0.0),
                onDismiss = { demoPaymentRequest = null },
                onConfirm = {
                    if (isDeposit) viewModel.payAppointmentDepositDemo(appointment)
                    else viewModel.payAppointmentBalanceDemo(appointment)
                    demoPaymentRequest = null
                }
            )
        }

        appointmentToCancel?.let { appointment ->
            AlertDialog(
                onDismissRequest = { appointmentToCancel = null },
                title = { Text("Randevu iptal edilsin mi?") },
                text = {
                    Text(
                        if (appointment.depositPaymentStatus == "demo_paid") {
                            "${appointment.serviceTitle} randevusu iptal edilecek. Demo depozito demo iadesi olarak işaretlenecek."
                        } else {
                            "${appointment.serviceTitle} randevusunu iptal etmek istiyor musunuz?"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.cancelCustomerAppointment(appointment.id)
                            appointmentToCancel = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                    ) { Text("Randevuyu iptal et") }
                },
                dismissButton = {
                    TextButton(onClick = { appointmentToCancel = null }) { Text("Vazgeç") }
                }
            )
        }

        orderToCancel?.let { order ->
            AlertDialog(
                onDismissRequest = { orderToCancel = null },
                title = { Text("Sipariş iptal edilsin mi?") },
                text = {
                    Text(
                        if (order.paymentStatus == "demo_paid") {
                            "${order.itemTitle} siparişi iptal edilecek. Demo ödeme iade edildi olarak işaretlenecek."
                        } else {
                            "${order.itemTitle} siparişini iptal etmek istiyor musunuz?"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.cancelCustomerOrder(order.id)
                            orderToCancel = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                    ) { Text("Siparişi iptal et") }
                },
                dismissButton = {
                    TextButton(onClick = { orderToCancel = null }) { Text("Vazgeç") }
                }
            )
        }
    }
}

@Composable
private fun CustomerNavigationItem(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 480f),
        label = "customer_navigation_icon_scale"
    )

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(containerColor = Terracotta, contentColor = WarmCream) {
                            Text(if (badgeCount > 99) "99+" else badgeCount.toString())
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }
        },
        label = { Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Turquoise,
            selectedTextColor = DarkInk,
            indicatorColor = Turquoise.copy(alpha = 0.16f),
            unselectedIconColor = DarkInk.copy(alpha = 0.68f),
            unselectedTextColor = DarkInk.copy(alpha = 0.68f)
        )
    )
}

/**
 * 1. ALAN: Sadeleştirilmiş Keşfet Bölümü (AI Asistan ve Esnaf Odaklı - Ürün/Hizmet Kartları Olmadan)
 */
@Composable
private fun ExploreSection(
    uiState: CustomerUiState,
    onToggleFavorite: (String) -> Unit,
    onNavigateToMerchantDetail: (String) -> Unit,
    onOpenAssistant: () -> Unit
) {
    val merchants = uiState.filteredMerchants
    val isSearchOrFilterActive = uiState.searchQuery.isNotBlank() || uiState.selectedCategory != "Tümü"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Turquoise)
                }
            }
        } else if (merchants.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Turquoise
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isSearchOrFilterActive) stringResource(R.string.empty_search_results) else "Henüz kayıtlı esnaf bulunmuyor.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = DarkInk
                        )
                    }
                }
            }
        } else {
            item {
                ExploreWelcomeCard(onOpenAssistant = onOpenAssistant)
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🏬 Çevrenizdeki Mahalle Esnafları (${merchants.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(merchants) { merchant ->
                RealMerchantCard(
                    merchant = merchant,
                    isFavorite = uiState.favoriteMerchantIds.contains(merchant.uid),
                    onToggleFavorite = { onToggleFavorite(merchant.uid) },
                    onClick = { onNavigateToMerchantDetail(merchant.uid) }
                )
            }
        }
    }
}

@Composable
private fun ExploreWelcomeCard(onOpenAssistant: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Turquoise, Color(0xFF377B86))))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mahallende çözüm var ✨",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "İhtiyacını anlat, Çırak sana yardımcı olsun.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onOpenAssistant,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = WarmCream, contentColor = DarkInk),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Çırak'a sor", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.92f),
            modifier = Modifier.size(48.dp).padding(start = 8.dp)
        )
    }
}

/**
 * Çırak Asistan Sohbet Diyaloğu
 */
@Composable
private fun CirakChatDialog(
    uiState: CustomerUiState,
    onDismiss: () -> Unit,
    onSubmitMessage: (String) -> Unit,
    onNavigateToMerchantDetail: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Turquoise),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = WarmCream,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Çırak - Mahalle Asistanı",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkInk
                    )
                    Text(
                        text = "İzin verirsen yakındakileri önce listeler; konumun kaydedilmez.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Turquoise
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.chatMessages) { message ->
                        ChatMessageBubble(
                            message = message,
                            favoriteMerchantIds = uiState.favoriteMerchantIds,
                            onToggleFavorite = onToggleFavorite,
                            onNavigateToMerchantDetail = { merchantId ->
                                onDismiss()
                                onNavigateToMerchantDetail(merchantId)
                            }
                        )
                    }

                    if (uiState.isAssistantLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = LightBeige)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Turquoise,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Çırak düşünüyor...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DarkInk.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("İhtiyacınızı yazın (örn. Musluğum akıtıyor)...") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSubmitMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = !uiState.isAssistantLoading && inputText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isBlank()) LightBeige else Turquoise)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gönder",
                            tint = if (inputText.isBlank()) DarkInk.copy(alpha = 0.4f) else WarmCream
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = DarkInk)
            }
        }
    )
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    favoriteMerchantIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onNavigateToMerchantDetail: (String) -> Unit
) {
    val isUser = message.isFromUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 12.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) SkyBlue else LightBeige
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = DarkInk
            )
        }

        if (!isUser && message.merchants.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🏬 Önerilen Esnaflar (${message.merchants.size}):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )
                message.merchants.forEach { merchant ->
                    RealMerchantCard(
                        merchant = merchant,
                        isFavorite = favoriteMerchantIds.contains(merchant.uid),
                        onToggleFavorite = { onToggleFavorite(merchant.uid) },
                        onClick = { onNavigateToMerchantDetail(merchant.uid) }
                    )
                }
            }
        }
    }
}

/**
 * Gerçek Esnaf Kartı
 */
@Composable
private fun RealMerchantCard(
    merchant: UserProfile,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f),
        label = "merchant_card_press_scale"
    )
    val publicAddress = merchant.businessAddress.ifBlank { merchant.address }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = cardScale; scaleY = cardScale }
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightBeige
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(SkyBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = DarkInk,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = merchant.businessName.ifBlank { merchant.name },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkInk
                    )
                    Text(
                        text = if (merchant.isBusinessOpen) "Açık" else "Kapalı",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (merchant.isBusinessOpen) Turquoise else Terracotta
                    )
                }
                if (merchant.category.isNotBlank()) {
                    Text(
                        text = merchant.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = Turquoise
                    )
                }
                if (publicAddress.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.lbl_address),
                            tint = Terracotta,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = publicAddress,
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkInk.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.lbl_favorite),
                    tint = if (isFavorite) Terracotta else DarkInk.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Müşterinin Randevularım Sekmesi
 */
@Composable
private fun CustomerAppointmentsSection(
    appointments: List<AppointmentModel>,
    onPayDeposit: (AppointmentModel) -> Unit,
    onPayBalance: (AppointmentModel) -> Unit,
    onCancelAppointment: (AppointmentModel) -> Unit,
    previousCount: Int = 0,
    onOpenPrevious: () -> Unit = {},
    isHistoryPage: Boolean = false
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (!isHistoryPage) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 16.dp, top = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpenPrevious, modifier = Modifier.height(38.dp)) {
                    Text("Önceki hizmetler · $previousCount", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

    if (appointments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Turquoise
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isHistoryPage) "Önceki hizmet bulunmuyor" else stringResource(R.string.empty_appointments_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHistoryPage) "Tamamlanan veya iptal edilen hizmetler burada görünecek." else stringResource(R.string.empty_appointments_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkInk.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (isHistoryPage) "📅 Önceki hizmetler (${appointments.size})" else "📅 Randevu Taleplerim (${appointments.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )
            }

            items(appointments) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightBeige
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
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkInk
                            )

                            val (statusText, statusColor) = when (app.status) {
                                "service_completed" -> "💳 Kalan ödeme bekleniyor" to SkyBlue
                                "approved" -> "🟢 Onaylandı" to Turquoise
                                "rejected" -> "🔴 Reddedildi" to Terracotta
                                "completed" -> "✅ Tamamlandı" to Turquoise
                                "cancelled" -> "İptal edildi" to Terracotta
                                else -> "🟡 Beklemede" to DarkInk
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                        }

                        if (app.status !in setOf("rejected", "cancelled", "payment_failed", "pending_payment")) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AppointmentProgressTrack(app.status)
                        }

                        Text("Hizmet bedeli: ${app.price} TL", style = MaterialTheme.typography.bodySmall, color = DarkInk.copy(alpha = 0.8f))
                        Text(
                            "Depozito: ${app.depositAmount} TL · ${depositStatusLabel(app.depositPaymentStatus)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkInk.copy(alpha = 0.8f)
                        )
                        Text(
                            "Kalan: ${(app.price - app.depositAmount).coerceAtLeast(0.0)} TL · ${balanceStatusLabel(app.balancePaymentStatus)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkInk.copy(alpha = 0.8f)
                        )
                        if (app.serviceLocation == "at_customer" && app.serviceAddress.isNotBlank()) {
                            Text("Hizmet adresi: ${app.serviceAddress}", style = MaterialTheme.typography.bodySmall, color = DarkInk.copy(alpha = 0.8f))
                        }
                        if (app.status == "approved" && app.depositAmount > 0.0 && app.depositPaymentStatus == "unpaid") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onPayDeposit(app) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Demo depozito öde · ${app.depositAmount} TL")
                            }
                            Text("Bu yalnızca demo durumudur; gerçek para çekilmez.", style = MaterialTheme.typography.labelSmall, color = DarkInk.copy(alpha = 0.7f))
                        }
                        if (app.status == "service_completed" && app.balancePaymentStatus == "requested") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { onPayBalance(app) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Kalan tutarı demo olarak tamamla · ${(app.price - app.depositAmount).coerceAtLeast(0.0)} TL")
                            }
                            Text("Bu yalnızca demo durumudur; gerçek para çekilmez.", style = MaterialTheme.typography.labelSmall, color = DarkInk.copy(alpha = 0.7f))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Turquoise
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${app.date} - ${app.time}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = DarkInk
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${app.price} TL",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Terracotta
                            )
                        }

                        if (!app.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Not: ${app.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkInk.copy(alpha = 0.8f)
                            )
                        }
                        if (app.status == "cancelled" && app.cancellationReason == "customer_cancelled") {
                            Text("Bu randevu müşteri isteğiyle iptal edildi.", style = MaterialTheme.typography.bodySmall, color = Terracotta)
                        }
                        if (app.status in setOf("pending", "approved")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onCancelAppointment(app) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                            ) { Text("Randevuyu iptal et") }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun AppointmentProgressTrack(status: String) {
    val step = when (status) {
        "completed" -> 3
        "service_completed" -> 2
        "approved" -> 1
        else -> 0
    }
    CompactProgressTrack(listOf("Talep", "Onaylandı", "Hizmet", "Ödeme"), step)
}

@Composable
private fun OrderProgressTrack(status: String, fulfillmentType: String) {
    val step = when (status) {
        "confirmed" -> 1
        "preparing" -> 2
        "ready" -> 3
        "out_for_delivery" -> 4
        "delivered" -> 5
        else -> 0
    }
    val labels = if (fulfillmentType == "pickup") {
        listOf("Talep", "Onay", "Hazırlık", "Hazır", "Teslim")
    } else {
        listOf("Talep", "Onay", "Hazırlık", "Hazır", "Yolda", "Teslim")
    }
    CompactProgressTrack(labels, if (fulfillmentType == "pickup") step.coerceAtMost(4) else step)
}

@Composable
private fun CompactProgressTrack(labels: List<String>, currentStep: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        labels.forEachIndexed { index, label ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(22.dp).background(
                        if (index <= currentStep) Turquoise else DarkInk.copy(alpha = 0.16f),
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (index < currentStep) "✓" else "${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (index <= currentStep) WarmCream else DarkInk
                    )
                }
                Text(label, style = MaterialTheme.typography.labelSmall, color = DarkInk, textAlign = TextAlign.Center, maxLines = 1)
            }
            if (index < labels.lastIndex) {
                Spacer(
                    Modifier.weight(0.45f).height(2.dp).background(
                        if (index < currentStep) Turquoise else DarkInk.copy(alpha = 0.16f)
                    )
                )
            }
        }
    }
}

private fun orderPaymentLabel(method: String, status: String): String = when {
    status == "demo_paid" -> "demo kart ödemesi kaydedildi · gerçek tahsilat yok"
    status == "demo_refunded" -> "demo iadesi işlendi · gerçek para hareketi yok"
    method == "cash_on_delivery" -> "Kapıda nakit"
    method == "pay_at_store" -> "İşletmede ödeme"
    else -> "Ödeme teslimde yapılacak"
}

private fun depositStatusLabel(status: String): String = when (status) {
    "not_required" -> "Depozito yok"
    "demo_paid" -> "demo olarak ödendi"
    "demo_refunded" -> "demo iadesi işlendi"
    "cash_paid" -> "elden alındı"
    else -> "demo ödemesi bekleniyor"
}

private fun balanceStatusLabel(status: String): String = when (status) {
    "requested" -> "esnaf ödeme talep etti"
    "demo_paid" -> "demo olarak tamamlandı"
    "cash_paid" -> "elden alındı"
    else -> "hizmet sonunda ödenecek"
}

/**
 * Müşterinin Siparişlerim Sekmesi
 */
@Composable
private fun CustomerOrdersSection(
    orders: List<OrderModel>,
    onCancelOrder: (OrderModel) -> Unit,
    previousCount: Int = 0,
    onOpenPrevious: () -> Unit = {},
    isHistoryPage: Boolean = false
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (!isHistoryPage) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 16.dp, top = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpenPrevious, modifier = Modifier.height(38.dp)) {
                    Text("Önceki siparişler · $previousCount", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Turquoise
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isHistoryPage) "Önceki sipariş bulunmuyor" else stringResource(R.string.empty_orders_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHistoryPage) "Tamamlanan veya iptal edilen siparişler burada görünecek." else stringResource(R.string.empty_orders_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkInk.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (isHistoryPage) "📦 Önceki siparişler (${orders.size})" else "📦 Siparişlerim (${orders.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )
            }

            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightBeige
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
                                text = order.itemTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkInk
                            )

                            val (statusText, statusColor) = when (order.status) {
                                "confirmed" -> "🟣 Esnaf onayladı" to Turquoise
                                "preparing" -> "🔵 Hazırlanıyor" to SkyBlue
                                "ready" -> "📦 Teslime hazır" to Turquoise
                                "out_for_delivery" -> "🚚 Yolda" to SkyBlue
                                "delivered" -> "✅ Tamamlandı" to Turquoise
                                "cancelled" -> "🔴 İptal edildi" to Terracotta
                                else -> "🟡 Beklemede" to DarkInk
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Adet: ${order.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkInk
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${order.price * order.quantity} TL",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Terracotta
                            )
                        }

                        if (order.status !in setOf("pending", "cancelled")) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OrderProgressTrack(order.status, order.fulfillmentType)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ödeme: ${orderPaymentLabel(order.paymentMethod, order.paymentStatus)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkInk.copy(alpha = 0.8f)
                        )
                        if (order.fulfillmentType == "delivery" && order.deliveryAddress.isNotBlank()) {
                            Text("Teslimat adresi: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = DarkInk.copy(alpha = 0.8f))
                        }
                        if (order.status == "pending") {
                            val deadline = java.text.SimpleDateFormat("HH:mm", java.util.Locale("tr", "TR"))
                                .format(java.util.Date(order.approvalDeadlineAt))
                            Text("Esnafın ${deadline} saatine kadar onaylaması bekleniyor. Süre dolarsa sipariş otomatik iptal edilir.", style = MaterialTheme.typography.bodySmall, color = SkyBlue)
                        }
                        if (order.status == "cancelled" && order.paymentStatus == "demo_refunded") {
                            Text("Demo iadesi işlendi; gerçek para hareketi olmadı.", style = MaterialTheme.typography.bodySmall, color = Turquoise)
                        }
                        if (order.status == "cancelled" && order.cancellationReason == "merchant_approval_timeout") {
                            Text("30 dakika içinde esnaf onayı gelmediği için otomatik iptal edildi.", style = MaterialTheme.typography.bodySmall, color = Terracotta)
                        }
                        if (order.status == "cancelled" && order.cancellationReason == "customer_cancelled") {
                            Text("Sipariş müşteri isteğiyle iptal edildi.", style = MaterialTheme.typography.bodySmall, color = Terracotta)
                        }
                        if (order.status == "pending") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onCancelOrder(order) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                            ) { Text("Siparişi iptal et") }
                        }
                    }
                }
            }
        }
    }
    }
}

/**
 * 2. ALAN: Kategoriler Section
 */
@Composable
private fun CategoriesSection(
    uiState: CustomerUiState,
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🗂️ Tüm Esnaf Kategorileri",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DarkInk
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Yatay Filtre Çipleri
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(uiState.categories) { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category.name,
                    onClick = { onCategorySelected(category.name) },
                    label = { Text(category.name) }
                )
            }
        }

        // Kategori Listesi Card Yapısı
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.categories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategorySelected(category.name) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.selectedCategory == category.name) {
                            SkyBlue
                        } else {
                            LightBeige
                        }
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(Turquoise),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = WarmCream
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = DarkInk
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. ALAN: Favoriler Section
 */
@Composable
private fun FavoritesSection(
    favoriteMerchants: List<UserProfile>,
    onToggleFavorite: (String) -> Unit,
    onNavigateToMerchantDetail: (String) -> Unit
) {
    if (favoriteMerchants.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.empty_favorites_title),
                style = MaterialTheme.typography.bodyMedium,
                color = DarkInk.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "❤️ Favori Esnaflarınız (${favoriteMerchants.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )
            }

            items(favoriteMerchants) { merchant ->
                RealMerchantCard(
                    merchant = merchant,
                    isFavorite = true,
                    onToggleFavorite = { onToggleFavorite(merchant.uid) },
                    onClick = { onNavigateToMerchantDetail(merchant.uid) }
                )
            }
        }
    }
}

@Composable
private fun DemoPaymentConfirmationDialog(
    title: String,
    amount: Double,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tutar: %.2f TL".format(amount))
                Text("Bu bir demo işlemidir. Kart bilgisi alınmaz ve gerçek para çekilmez.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Demo ödemeyi tamamla") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
    )
}

/**
 * Randevu Talebi Oluşturma Diyaloğu (Gelecek Tarih Kısıtlamalı)
 */
@Composable
private fun BookAppointmentDialog(
    service: ServiceModel,
    initialName: String,
    initialPhone: String,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String, note: String?, recipientName: String, phone: String, serviceLocation: String, serviceAddress: String) -> Unit,
    isSaving: Boolean
) {
    val dateOptions = remember {
        val list = mutableListOf<Pair<String, String>>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val cal = Calendar.getInstance()

        for (i in 0..6) {
            val dateVal = dateFormat.format(cal.time)
            val label = when (i) {
                0 -> "Bugün (${displayFormat.format(cal.time)})"
                1 -> "Yarın (${displayFormat.format(cal.time)})"
                else -> displayFormat.format(cal.time)
            }
            list.add(dateVal to label)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    val allTimeSlots = remember {
        listOf("09:00", "10:00", "11:00", "12:00", "13:30", "14:30", "15:30", "16:30", "17:30", "18:30")
    }

    var selectedDate by remember { mutableStateOf(dateOptions.first().first) }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time) }

    val availableTimeSlots = remember(selectedDate) {
        if (selectedDate == todayStr) {
            val cal = Calendar.getInstance()
            val currentHour = cal.get(Calendar.HOUR_OF_DAY)
            val currentMinute = cal.get(Calendar.MINUTE)
            val currentTimeInMinutes = currentHour * 60 + currentMinute

            allTimeSlots.filter { slot ->
                val parts = slot.split(":")
                val slotHour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val slotMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val slotTimeInMinutes = slotHour * 60 + slotMinute
                slotTimeInMinutes > currentTimeInMinutes
            }
        } else {
            allTimeSlots
        }
    }

    var selectedTime by remember(availableTimeSlots) {
        mutableStateOf(availableTimeSlots.firstOrNull() ?: "")
    }
    var note by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var serviceLocation by remember { mutableStateOf("at_business") }
    var serviceAddress by remember { mutableStateOf("") }
    var currentStep by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = if (currentStep == 0) stringResource(R.string.create_appointment_request) else "Depozito ödemesi",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = DarkInk
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = LightBeige
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = service.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkInk
                        )
                        Text(
                            text = "Fiyat: ${service.price} TL ${if (!service.duration.isNullOrBlank()) "• ${service.duration}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkInk.copy(alpha = 0.8f)
                        )
                        Text("Rezervasyon depozitosu: ${service.depositAmount} TL", style = MaterialTheme.typography.bodySmall)
                        Text("Hizmet sonrası kalan tutar: ${(service.price - service.depositAmount).coerceAtLeast(0.0)} TL", style = MaterialTheme.typography.bodySmall)
                        Text(
                            if (service.depositAmount > 0.0) "Depozito onay isteğinden önce ödenir; işlem demo olarak kaydedilir."
                            else "Bu hizmet için depozito yok.",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkInk.copy(alpha = 0.8f)
                        )
                    }
                }

                if (currentStep == 0) {
                Text(
                    text = stringResource(R.string.select_date),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(dateOptions) { (dateVal, label) ->
                        FilterChip(
                            selected = selectedDate == dateVal,
                            onClick = { selectedDate = dateVal },
                            label = { Text(label) }
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.select_time),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = DarkInk
                )

                if (availableTimeSlots.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_time_slots),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(availableTimeSlots) { timeVal ->
                            FilterChip(
                                selected = selectedTime == timeVal,
                                onClick = { selectedTime = timeVal },
                                label = { Text(timeVal) }
                            )
                        }
                    }
                }

                Text("Hizmet nerede alınacak?", fontWeight = FontWeight.Bold, color = DarkInk)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = serviceLocation == "at_business",
                        onClick = { serviceLocation = "at_business" },
                        label = { Text("İşletmede") }
                    )
                    FilterChip(
                        selected = serviceLocation == "at_customer",
                        onClick = { serviceLocation = "at_customer" },
                        label = { Text("Adrese gelsin") }
                    )
                }
                if (serviceLocation == "at_customer") {
                    OutlinedTextField(
                        value = serviceAddress,
                        onValueChange = { serviceAddress = it.take(400) },
                        label = { Text("Hizmet adresi") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it.take(60) },
                    label = { Text("Ad soyad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.take(20) },
                    label = { Text("Telefon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note_optional)) },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Turquoise) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightBeige)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(service.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DarkInk)
                            Text("Randevu: $selectedDate · $selectedTime", color = DarkInk)
                            Text("Müşteri: $recipientName · $phone", style = MaterialTheme.typography.bodySmall, color = DarkInk)
                            Text(
                                if (serviceLocation == "at_business") "Hizmet yeri: İşletmede" else "Hizmet adresi: $serviceAddress",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkInk
                            )
                            if (note.isNotBlank()) Text("Not: $note", style = MaterialTheme.typography.bodySmall, color = DarkInk)
                            Text("Hizmet bedeli: ${service.price} TL", style = MaterialTheme.typography.bodySmall, color = DarkInk)
                            Text("Şimdi ödenecek depozito: ${service.depositAmount} TL", fontWeight = FontWeight.Bold, color = Turquoise)
                            Text("Hizmet sonrası kalan: ${(service.price - service.depositAmount).coerceAtLeast(0.0)} TL", style = MaterialTheme.typography.bodySmall, color = DarkInk)
                            Text("Demo işlemidir; gerçek para çekilmez. Depozito demo olarak işaretlendikten sonra talebiniz esnaf onayına gönderilir.", style = MaterialTheme.typography.labelSmall, color = DarkInk.copy(alpha = 0.8f))
                        }
                    }
                }
                if (errorMessage != null) Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTime.isNotBlank() && recipientName.isNotBlank() && phone.isNotBlank() &&
                        (serviceLocation == "at_business" || serviceAddress.isNotBlank())) {
                        if (currentStep == 0 && service.depositAmount > 0.0) {
                            currentStep = 1
                        } else {
                            onConfirm(selectedDate, selectedTime, note, recipientName.trim(), phone.trim(), serviceLocation, serviceAddress.trim())
                        }
                    }
                },
                enabled = !isSaving && selectedTime.isNotBlank() && recipientName.isNotBlank() && phone.isNotBlank() &&
                    (serviceLocation == "at_business" || serviceAddress.isNotBlank()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue,
                    contentColor = DarkInk
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = DarkInk,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        when {
                            currentStep == 1 -> "Demo depozitoyu öde ve onay iste"
                            service.depositAmount > 0.0 -> "Depozito ödemesine geç"
                            else -> stringResource(R.string.send_request)
                        }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { if (currentStep == 1) currentStep = 0 else onDismiss() },
                enabled = !isSaving
            ) {
                Text(if (currentStep == 1) "Geri" else stringResource(R.string.btn_cancel_request), color = DarkInk)
            }
        }
    )
}
