package com.example.esnafm.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.esnafm.data.model.AddressModel
import com.example.esnafm.data.model.ItemModel
import com.example.esnafm.ui.components.PaymentCardFields
import com.example.esnafm.ui.components.isPaymentCardComplete

@Composable
fun ProductCheckoutDialog(
    item: ItemModel,
    quantity: Int,
    savedAddresses: List<AddressModel>,
    initialAddress: String,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (address: String, fulfillmentType: String, paymentMethod: String) -> Unit
) {
    var address by remember(item.id) {
        mutableStateOf(initialAddress.ifBlank { savedAddresses.firstOrNull { it.isDefault }?.fullAddress ?: savedAddresses.firstOrNull()?.fullAddress.orEmpty() })
    }
    var currentStep by remember(item.id) { mutableIntStateOf(0) }
    var fulfillmentType by remember { mutableStateOf("delivery") }
    var paymentMethod by remember { mutableStateOf("mock_card") }
    var cardHolder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    LaunchedEffect(initialAddress) {
        if (address.isBlank() && initialAddress.isNotBlank()) address = initialAddress
    }
    val total = item.price * quantity
    val isCardComplete = isPaymentCardComplete(cardHolder, cardNumber, cardExpiry, cardCvv)
    val canContinueToPayment = !isSubmitting && (fulfillmentType == "pickup" || address.isNotBlank())
    val canSubmit = canContinueToPayment && (paymentMethod != "mock_card" || isCardComplete)

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxSize().widthIn(max = 640.dp).padding(8.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(if (currentStep == 0) "Teslimat bilgileri" else "Ödeme", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            if (currentStep == 0) "Sipariş bilgilerinizi kontrol edin" else "Ödeme yöntemini seçip işlemi tamamlayın",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { if (currentStep == 0) onDismiss() else currentStep = 0 },
                        enabled = !isSubmitting
                    ) {
                        Icon(
                            imageVector = if (currentStep == 0) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (currentStep == 0) "Kapat" else "Geri"
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(item.title, fontWeight = FontWeight.SemiBold)
                                Text("${quantity} adet × ${item.price} TL", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("%.2f TL".format(total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (currentStep == 1) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Teslimat bilgileri", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { currentStep = 0 }, enabled = !isSubmitting) { Text("Düzenle") }
                                }
                                if (fulfillmentType == "delivery") Text(address, style = MaterialTheme.typography.bodySmall)
                                else Text("İşletmeden teslim al", style = MaterialTheme.typography.bodySmall)
                                Text("İletişim uygulama içi mesajlaşmadan yapılır.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (currentStep == 0) {
                    Text("Teslim alma şekli", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = fulfillmentType == "delivery",
                            onClick = {
                                fulfillmentType = "delivery"
                                if (paymentMethod == "pay_at_store") paymentMethod = "cash_on_delivery"
                            },
                            label = { Text("Adrese teslim") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                        )
                        FilterChip(
                            selected = fulfillmentType == "pickup",
                            onClick = {
                                fulfillmentType = "pickup"
                                if (paymentMethod == "cash_on_delivery") paymentMethod = "pay_at_store"
                            },
                            label = { Text("İşletmeden al") }
                        )
                    }

                    if (fulfillmentType == "delivery") {
                        Text("Teslimat adresi", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        if (savedAddresses.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(savedAddresses, key = { it.id }) { saved ->
                                    FilterChip(
                                        selected = address == saved.fullAddress,
                                        onClick = { address = saved.fullAddress },
                                        label = { Text(saved.title.ifBlank { "Kayıtlı adres" }) }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it.take(400) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("İl, ilçe, mahalle ve açık adres") },
                            minLines = 3,
                            maxLines = 5,
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                        )
                    }
                    }

                    if (currentStep == 1) {
                    Text("Ödeme yöntemi", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    CheckoutPaymentChoice(
                        selected = paymentMethod == "mock_card",
                        title = "Kartla ödeme",
                        description = "Kart bilgileri sipariş kaydına eklenmez",
                        onClick = { paymentMethod = "mock_card" },
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = null) }
                    )
                    if (paymentMethod == "mock_card") {
                        PaymentCardFields(
                            cardHolder = cardHolder,
                            cardNumber = cardNumber,
                            cardExpiry = cardExpiry,
                            cardCvv = cardCvv,
                            onCardHolderChange = { cardHolder = it },
                            onCardNumberChange = { cardNumber = it },
                            onCardExpiryChange = { cardExpiry = it },
                            onCardCvvChange = { cardCvv = it }
                        )
                    }
                    CheckoutPaymentChoice(
                        selected = paymentMethod == "cash_on_delivery" && fulfillmentType == "delivery",
                        title = "Kapıda nakit",
                        description = "Ödemeyi ürün teslim edilirken yap",
                        onClick = { if (fulfillmentType == "delivery") paymentMethod = "cash_on_delivery" },
                        enabled = fulfillmentType == "delivery"
                    )
                    if (fulfillmentType == "pickup") {
                        CheckoutPaymentChoice(
                            selected = paymentMethod == "pay_at_store",
                            title = "İşletmede ödeme",
                            description = "Ürünü alırken işletmede öde",
                            onClick = { paymentMethod = "pay_at_store" }
                        )
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ürün toplamı")
                                Text("%.2f TL".format(total), fontWeight = FontWeight.SemiBold)
                            }
                            Text("Sipariş 30 dakika içinde onaylanmazsa iptal edilir ve iade kaydı oluşturulur.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    }
                    if (errorMessage != null) Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    if (currentStep == 0) {
                        Text("Ad, telefon ve e-posta işletmeyle paylaşılmaz. Adrese teslimat için seçtiğin adres işletmeye iletilir. İletişim uygulama içi mesajlaşmadan yapılır.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentStep == 0) {
                        Button(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canContinueToPayment
                        ) { Text("Sipariş ver · ödeme adımına geç") }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { currentStep = 0 }, enabled = !isSubmitting) { Text("Geri") }
                            Button(
                                onClick = {
                                    val selectedPaymentMethod = paymentMethod
                                    onConfirm(address.trim(), fulfillmentType, selectedPaymentMethod)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = canSubmit
                            ) {
                                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                                Text(if (paymentMethod == "mock_card") "Ödemeyi tamamla · %.2f TL".format(total) else "Siparişi oluştur · %.2f TL".format(total))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutPaymentChoice(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick, enabled = enabled)
            if (icon != null) {
                Spacer(Modifier.padding(horizontal = 4.dp))
                icon()
                Spacer(Modifier.padding(horizontal = 4.dp))
            }
            Column(Modifier.padding(horizontal = 8.dp).weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)
                Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
