package com.example.esnafm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PaymentCardFields(
    cardHolder: String,
    cardNumber: String,
    cardExpiry: String,
    cardCvv: String,
    onCardHolderChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvvChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "Kart bilgileri",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                "Kart bilgileri sipariş veya randevu kaydına eklenmez.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = cardHolder,
                onValueChange = { onCardHolderChange(it.take(80)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kart üzerindeki ad") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { input ->
                    onCardNumberChange(input.filter(Char::isDigit).take(16).chunked(4).joinToString(" "))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kart numarası") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = cardExpiry,
                    onValueChange = { input ->
                        val digits = input.filter(Char::isDigit).take(4)
                        onCardExpiryChange(if (digits.length > 2) "${digits.take(2)}/${digits.drop(2)}" else digits)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("SKT · AA/YY") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = cardCvv,
                    onValueChange = { onCardCvvChange(it.filter(Char::isDigit).take(4)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("CVV") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
            }
        }
    }
}

fun isPaymentCardComplete(
    cardHolder: String,
    cardNumber: String,
    cardExpiry: String,
    cardCvv: String
): Boolean {
    val expiryDigits = cardExpiry.filter(Char::isDigit)
    val expiryMonth = expiryDigits.take(2).toIntOrNull() ?: 0
    return cardHolder.isNotBlank() &&
        cardNumber.filter(Char::isDigit).length == 16 &&
        expiryDigits.length == 4 && expiryMonth in 1..12 &&
        cardCvv.filter(Char::isDigit).length in 3..4
}
