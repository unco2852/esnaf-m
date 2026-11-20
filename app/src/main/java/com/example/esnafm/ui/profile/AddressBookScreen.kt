package com.example.esnafm.ui.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esnafm.data.model.AddressModel
import com.example.esnafm.ui.theme.DarkInk
import com.example.esnafm.ui.theme.LightBeige
import com.example.esnafm.ui.theme.SkyBlue
import com.example.esnafm.ui.theme.Terracotta
import com.example.esnafm.ui.theme.Turquoise
import com.example.esnafm.ui.theme.WarmCream

/**
 * Kayıtlı Adreslerin Listelendiği ve Yönetildiği Özel Adreslerim Ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressBookScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isAddAddressDialogOpen by remember { mutableStateOf(false) }
    var addressToDelete by remember { mutableStateOf<AddressModel?>(null) }

    Scaffold(
        containerColor = WarmCream,
        topBar = {
            TopAppBar(
                title = { Text("Kayıtlı Adreslerim 📍", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBeige,
                    titleContentColor = DarkInk
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddAddressDialogOpen = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = DarkInk) },
                text = { Text("Yeni Adres Ekle", color = DarkInk) },
                containerColor = SkyBlue,
                contentColor = DarkInk
            )
        }
    ) { innerPadding ->
        if (uiState.addresses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Turquoise
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Henüz kayıtlı bir adresiniz bulunmuyor.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        color = DarkInk
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aşağıdaki 'Yeni Adres Ekle' butonuna basarak teslimat ve konum adreslerinizi ekleyebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = DarkInk.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Kayıtlı Teslimat Adresleriniz (${uiState.addresses.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkInk
                    )
                }

                items(uiState.addresses) { addr ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = LightBeige),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = addr.title, fontWeight = FontWeight.Bold, color = DarkInk)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = addr.fullAddress, style = MaterialTheme.typography.bodyMedium, color = DarkInk)
                                if (addr.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "Tarif: ${addr.description}", style = MaterialTheme.typography.bodySmall, color = DarkInk.copy(alpha = 0.7f))
                                }
                            }

                            IconButton(onClick = { addressToDelete = addr }) {
                                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Terracotta)
                            }
                        }
                    }
                }
            }
        }

        // Yeni Adres Ekleme Form Diyaloğu
        if (isAddAddressDialogOpen) {
            AddAddressDialog(
                onDismiss = { isAddAddressDialogOpen = false },
                onAdd = { title, fullAddr, desc ->
                    viewModel.addNewAddress(title, fullAddr, desc)
                    isAddAddressDialogOpen = false
                },
                isSaving = uiState.isSaving
            )
        }

        // Adres Silme Onay Diyaloğu
        if (addressToDelete != null) {
            AlertDialog(
                onDismissRequest = { addressToDelete = null },
                title = { Text("Adresi Sil", fontWeight = FontWeight.Bold) },
                text = { Text("'${addressToDelete!!.title}' başlıklı adresi silmek istediğinize emin misiniz?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAddress(addressToDelete!!.id)
                            addressToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
                    ) {
                        Text("Sil")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { addressToDelete = null }) {
                        Text("Vazgeç", color = DarkInk)
                    }
                }
            )
        }
    }
}

@Composable
private fun AddAddressDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, fullAddress: String, description: String) -> Unit,
    isSaving: Boolean
) {
    var title by remember { mutableStateOf("Ev") }
    var fullAddress by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Adres Ekle 📍", fontWeight = FontWeight.Bold, color = DarkInk) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Adres Başlığı (Örn: Ev, İş, Dükkan)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = { fullAddress = it },
                    label = { Text("Açık Adres (İl, İlçe, Mahalle, Sokak, No)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Adres Tarifi / Özel Not (İsteğe Bağlı)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(title, fullAddress, description) },
                enabled = !isSaving && fullAddress.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = DarkInk)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DarkInk)
                } else {
                    Text("Adresi Kaydet")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = DarkInk)
            }
        }
    )
}
