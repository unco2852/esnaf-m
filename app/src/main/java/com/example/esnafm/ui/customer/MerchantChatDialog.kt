package com.example.esnafm.ui.customer

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.esnafm.data.model.MerchantMessageModel
import com.example.esnafm.data.repository.MerchantMessageRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class PendingChatAttachment(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val type: String
)

/** Full-screen, live chat shared by the customer and merchant panels. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantChatDialog(
    merchantId: String,
    customerId: String,
    currentUserId: String,
    title: String,
    isMerchant: Boolean,
    onDismiss: () -> Unit,
    onConversationFinished: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { MerchantMessageRepository() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var messages by remember(merchantId, customerId) { mutableStateOf<List<MerchantMessageModel>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var pendingAttachment by remember { mutableStateOf<PendingChatAttachment?>(null) }
    var attachmentMenuExpanded by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var isDeleting by remember(merchantId, customerId) { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fullScreenImage by remember { mutableStateOf<String?>(null) }

    fun selectAttachment(uri: Uri?, fallbackMime: String) {
        if (uri == null) return
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: fallbackMime
        val displayName = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull().orEmpty().ifBlank {
            when {
                mimeType.startsWith("image/") -> "Fotoğraf"
                mimeType.startsWith("video/") -> "Video"
                else -> "Dosya"
            }
        }.replace(Regex("[\\r\\n\\u0000-\\u001f]"), "").take(120)
        val sizeBytes = runCatching {
            resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
        }.getOrDefault(0L)

        val maxBytes = if (mimeType.startsWith("video/")) {
            MerchantMessageRepository.MAX_VIDEO_ATTACHMENT_BYTES
        } else {
            MerchantMessageRepository.MAX_ATTACHMENT_BYTES
        }
        if (sizeBytes > maxBytes) {
            errorMessage = if (mimeType.startsWith("video/")) "Videolar en fazla 100 MB olabilir." else "Fotoğraf ve dosyalar en fazla 25 MB olabilir."
            pendingAttachment = null
            return
        }

        val type = when {
            mimeType.startsWith("image/") -> "image"
            mimeType.startsWith("video/") -> "video"
            else -> "file"
        }
        pendingAttachment = PendingChatAttachment(uri, displayName, mimeType, sizeBytes, type)
        errorMessage = null
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectAttachment(uri, "image/*")
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectAttachment(uri, "video/*")
    }
    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        selectAttachment(uri, "application/octet-stream")
    }

    LaunchedEffect(merchantId, customerId, currentUserId) {
        repository.getConversationFlow(merchantId, customerId, currentUserId)
            .catch { errorMessage = it.localizedMessage ?: "Sohbet yüklenemedi." }
            .collect { messages = it }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Dialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().widthIn(max = 640.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, enabled = !isDeleting) {
                        Icon(Icons.Default.Close, contentDescription = "Sohbeti kapat")
                    }
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = if (isMerchant) "Kullanıcı ID: $customerId" else "Esnafla özel sohbet · profil bilgileriniz paylaşılmaz",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isMerchant) {
                        TextButton(
                            onClick = {
                                if (isDeleting || currentUserId != merchantId) return@TextButton
                                isDeleting = true
                                errorMessage = null
                                scope.launch {
                                    val result = repository.finishConversation(merchantId, customerId)
                                    result.fold(
                                        onSuccess = { onConversationFinished() },
                                        onFailure = {
                                            errorMessage = it.localizedMessage ?: "Sohbet silinemedi."
                                            isDeleting = false
                                        }
                                    )
                                }
                            },
                            enabled = !isDeleting && currentUserId == merchantId
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Bu sohbet bitti", maxLines = 1)
                            }
                        }
                    }
                }

                if (currentUserId.isBlank()) {
                    Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Sohbeti kullanmak için önce hesabınıza giriş yapın.", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Sohbet başladı", fontWeight = FontWeight.Bold)
                                        Text("Mesajınızı yazın veya fotoğraf, video ya da dosya ekleyin.", style = MaterialTheme.typography.bodySmall)
                                        Text("Paylaştığınız eklerin içeriği karşı tarafça görülebilir.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                        items(messages, key = { it.id }) { message ->
                            ChatMessageBubble(
                                message = message,
                                isMine = message.senderId == currentUserId,
                                onImageClick = { fullScreenImage = it },
                                onAttachmentClick = { url, mime -> openSharedFile(context, url, mime) }
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (pendingAttachment != null) {
                    val attachment = pendingAttachment!!
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (attachment.type == "image") {
                            AsyncImage(
                                model = attachment.uri,
                                contentDescription = attachment.name,
                                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(if (attachment.type == "video") Icons.Default.VideoLibrary else Icons.Default.Description, contentDescription = null)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(attachment.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                            Text("${attachment.typeLabel()} · ${formatBytes(attachment.sizeBytes)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                        IconButton(onClick = { pendingAttachment = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Eki kaldır")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box {
                        IconButton(onClick = { attachmentMenuExpanded = true }, enabled = !isSending && !isDeleting && currentUserId.isNotBlank()) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Fotoğraf, video veya dosya ekle")
                        }
                        DropdownMenu(expanded = attachmentMenuExpanded, onDismissRequest = { attachmentMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Fotoğraf seç") },
                                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                                onClick = {
                                    attachmentMenuExpanded = false
                                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Video seç") },
                                leadingIcon = { Icon(Icons.Default.VideoLibrary, contentDescription = null) },
                                onClick = {
                                    attachmentMenuExpanded = false
                                    videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Dosya seç") },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                                onClick = {
                                    attachmentMenuExpanded = false
                                    documentPicker.launch(arrayOf("*/*"))
                                }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { if (it.length <= 2000) draft = it },
                        modifier = Modifier.weight(1f),
                        enabled = !isDeleting,
                        placeholder = { Text("Mesaj yaz…") },
                        maxLines = 5,
                        shape = RoundedCornerShape(24.dp)
                    )
                    IconButton(
                        onClick = {
                            if (isSending || isDeleting || currentUserId.isBlank()) return@IconButton
                            val attachment = pendingAttachment
                            if (draft.isBlank() && attachment == null) return@IconButton
                            isSending = true
                            errorMessage = null
                            scope.launch {
                                val result = repository.sendMessage(
                                    senderId = currentUserId,
                                    customerId = customerId,
                                    merchantId = merchantId,
                                    text = draft,
                                    attachmentUri = attachment?.uri,
                                    attachmentName = attachment?.name.orEmpty(),
                                    attachmentMimeType = attachment?.mimeType.orEmpty(),
                                    attachmentSizeBytes = attachment?.sizeBytes ?: 0L
                                )
                                result.fold(
                                    onSuccess = {
                                        draft = ""
                                        pendingAttachment = null
                                    },
                                    onFailure = { errorMessage = it.localizedMessage ?: "Mesaj gönderilemedi." }
                                )
                                isSending = false
                            }
                        },
                        enabled = !isSending && !isDeleting && currentUserId.isNotBlank() && (draft.isNotBlank() || pendingAttachment != null),
                        modifier = Modifier.padding(bottom = 5.dp).size(46.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        if (isSending) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Mesajı gönder", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }

    if (fullScreenImage != null) {
        Dialog(onDismissRequest = { fullScreenImage = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black), contentAlignment = Alignment.Center) {
                AsyncImage(model = fullScreenImage, contentDescription = "Paylaşılan fotoğraf", modifier = Modifier.fillMaxWidth().fillMaxHeight(), contentScale = ContentScale.Fit)
                IconButton(onClick = { fullScreenImage = null }, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f), CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "Fotoğrafı kapat", tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: MerchantMessageModel,
    isMine: Boolean,
    onImageClick: (String) -> Unit,
    onAttachmentClick: (String, String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMine) 18.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 18.dp
            ),
            color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                when (message.attachmentType) {
                    "image" -> AsyncImage(
                        model = message.attachmentUrl,
                        contentDescription = message.attachmentName.ifBlank { "Paylaşılan fotoğraf" },
                        modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp)).clickable { onImageClick(message.attachmentUrl) },
                        contentScale = ContentScale.Crop
                    )
                    "video" -> Surface(
                        modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)).clickable { onAttachmentClick(message.attachmentUrl, message.attachmentMimeType) },
                        color = MaterialTheme.colorScheme.scrim
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(34.dp))
                            Text("Videoyu oynat", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    "file" -> Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onAttachmentClick(message.attachmentUrl, message.attachmentMimeType) }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Column(Modifier.weight(1f)) {
                            Text(message.attachmentName.ifBlank { "Dosya" }, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(formatBytes(message.attachmentSizeBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                if (message.message.isNotBlank()) Text(message.message, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = SimpleDateFormat("HH:mm", Locale("tr", "TR")).format(Date(message.createdAt)),
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private fun openSharedFile(context: android.content.Context, url: String, mimeType: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setDataAndType(Uri.parse(url), mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}

private fun PendingChatAttachment.typeLabel(): String = when (type) {
    "image" -> "Fotoğraf"
    "video" -> "Video"
    else -> "Dosya"
}

private fun formatBytes(bytes: Long): String = when {
    bytes <= 0L -> "Boyut bilinmiyor"
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "${bytes / 1024L} KB"
    else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
}
