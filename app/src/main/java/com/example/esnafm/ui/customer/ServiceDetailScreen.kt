package com.example.esnafm.ui.customer

import com.example.esnafm.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.esnafm.data.model.AppointmentModel
import com.example.esnafm.data.model.ServiceModel
import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.repository.AppointmentRepository
import com.example.esnafm.data.repository.AuthRepository
import com.example.esnafm.data.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Hizmet Detay Ekranı
 * Hizmet detayları, tahmini süre, esnaf yönlendirmesi ve Randevu Talebi Oluşturma akışını içerir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMerchantDetail: (String) -> Unit
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var serviceModel by remember { mutableStateOf<ServiceModel?>(null) }
    var merchantProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBookingDialogOpen by remember { mutableStateOf(false) }
    var isBookingSaving by remember { mutableStateOf(false) }
    var bookingSuccessMessage by remember { mutableStateOf<String?>(null) }

    val appointmentRepository = remember { AppointmentRepository() }
    val authRepository = remember { AuthRepository() }
    val serviceRepository = remember { ServiceRepository() }

    LaunchedEffect(serviceId) {
        if (serviceId.isBlank()) {
            errorMessage = "Geçersiz hizmet kimliği."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            isLoading = true
            errorMessage = null
            val serviceResult = serviceRepository.getServiceById(serviceId)
            if (serviceResult.isSuccess) {
                val service = serviceResult.getOrThrow()
                serviceModel = service

                // 2. İlgili Esnaf Profilini Çek
                if (service.merchantId.isNotBlank()) {
                    merchantProfile = authRepository.getPublicMerchantProfile(service.merchantId).getOrNull()
                }
            } else {
                errorMessage = serviceResult.exceptionOrNull()?.localizedMessage ?: "Hizmet bulunamadı."
            }

            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Hizmet detayları yüklenirken hata oluştu: ${e.localizedMessage}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = serviceModel?.title ?: "Hizmet Detayı",
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
                val service = serviceModel!!
                val profile = merchantProfile

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (bookingSuccessMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = bookingSuccessMessage!!,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Hizmet Görseli / Yer Tutucu
                    if (!service.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = service.imageUrl,
                            contentDescription = service.title,
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
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Hizmet Başlığı & Fiyat
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
                                text = service.title,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            if (service.category.isNotBlank()) {
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
                                        text = service.category,
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
                                    text = "${service.price} TL",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )

                                if (!service.duration.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = service.duration,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }

                            if (!service.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Açıklama:",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = service.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { isBookingDialogOpen = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.book_appointment),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
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

            // Randevu Oluşturma Diyaloğu
            if (isBookingDialogOpen && serviceModel != null) {
                ServiceDetailBookingDialog(
                    service = serviceModel!!,
                    onDismiss = { isBookingDialogOpen = false },
                    initialName = FirebaseAuth.getInstance().currentUser?.displayName.orEmpty(),
                    initialPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber.orEmpty(),
                    errorMessage = errorMessage,
                    onConfirm = { date, time, note, recipientName, phone, serviceLocation, serviceAddress ->
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser == null) {
                            errorMessage = "Randevu alabilmek için lütfen giriş yapınız."
                            return@ServiceDetailBookingDialog
                        }

                        isBookingSaving = true
                        coroutineScope.launch {
                            val newAppointment = AppointmentModel(
                                customerId = currentUser.uid,
                                customerEmail = currentUser.email ?: "",
                                merchantId = serviceModel!!.merchantId,
                                serviceId = serviceModel!!.id,
                                serviceTitle = serviceModel!!.title,
                                price = serviceModel!!.price,
                                date = date,
                                time = time,
                                note = note?.takeIf { it.isNotBlank() },
                                status = "pending",
                                recipientName = recipientName,
                                customerPhone = phone,
                                serviceLocation = serviceLocation,
                                serviceAddress = if (serviceLocation == "at_customer") serviceAddress else "",
                                depositAmount = serviceModel!!.depositAmount,
                                depositPaidAt = if (serviceModel!!.depositAmount > 0.0) System.currentTimeMillis() else null,
                                depositPaymentStatus = if (serviceModel!!.depositAmount > 0.0) "demo_paid" else "not_required",
                                balancePaymentStatus = "unpaid"
                            )
                            val result = appointmentRepository.createAppointment(newAppointment)
                            isBookingSaving = false
                            result.fold(
                                onSuccess = {
                                    isBookingDialogOpen = false
                                    bookingSuccessMessage = if (serviceModel!!.depositAmount > 0.0) {
                                        "✅ Demo depozito kaydedildi; randevu talebiniz esnaf onayına iletildi. Gerçek tahsilat yapılmadı."
                                    } else {
                                        "✅ Randevu talebiniz esnaf onayına iletildi."
                                    }
                                },
                                onFailure = { err -> errorMessage = "Randevu alınamadı: ${err.localizedMessage}" }
                            )
                        }
                    },
                    isSaving = isBookingSaving
                )
            }
        }
    }

}

@Composable
private fun ServiceDetailBookingDialog(
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
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentStep == 0) stringResource(R.string.create_appointment_request) else "Depozito ödemesi",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = service.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Fiyat: ${service.price} TL ${if (!service.duration.isNullOrBlank()) "• ${service.duration}" else ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text("Depozito: ${service.depositAmount} TL · kalan: ${(service.price - service.depositAmount).coerceAtLeast(0.0)} TL", style = MaterialTheme.typography.bodySmall)
                        Text(
                            if (service.depositAmount > 0.0) "Depozito onay isteğinden önce demo olarak işlenir."
                            else "Bu hizmet için depozito yok.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (currentStep == 0) {
                Text(
                    text = stringResource(R.string.select_date),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
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
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
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

                Text("Hizmet nerede alınacak?", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = serviceLocation == "at_business",
                        onClick = { serviceLocation = "at_business" },
                        label = { Text("İşletmede") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) }
                    )
                    FilterChip(
                        selected = serviceLocation == "at_customer",
                        onClick = { serviceLocation = "at_customer" },
                        label = { Text("Adrese gelsin") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
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
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(service.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Randevu: $selectedDate · $selectedTime")
                            Text("Müşteri: $recipientName · $phone", style = MaterialTheme.typography.bodySmall)
                            Text(
                                if (serviceLocation == "at_business") "Hizmet yeri: İşletmede" else "Hizmet adresi: $serviceAddress",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (note.isNotBlank()) Text("Not: $note", style = MaterialTheme.typography.bodySmall)
                            Text("Şimdi ödenecek demo depozito: ${service.depositAmount} TL", fontWeight = FontWeight.Bold)
                            Text("Hizmet sonrası kalan: ${(service.price - service.depositAmount).coerceAtLeast(0.0)} TL", style = MaterialTheme.typography.bodySmall)
                            Text("Bu bir demo işlemidir; gerçek para çekilmez. Depozito demo olarak işaretlendikten sonra randevu esnaf onayına gönderilir.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    (serviceLocation == "at_business" || serviceAddress.isNotBlank())
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
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
            TextButton(onClick = { if (currentStep == 1) currentStep = 0 else onDismiss() }, enabled = !isSaving) {
                Text(if (currentStep == 1) "Geri" else stringResource(R.string.btn_cancel_request))
            }
        }
    )
}
