package com.example.esnafm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esnafm.R
import com.example.esnafm.ui.theme.Turquoise
import com.example.esnafm.ui.theme.WarmWhite

/**
 * Resmi Esnafım Logo bileşeni (Yüksek kontrastlı, orijinal renkleri koruyan ve kesintisiz ölçeklenen yapı)
 */
@Composable
fun EsnafımLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 96.dp,
    showSubtitle: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Logo için nötr, yüksek kontrast sağlayan beyaz zeminli ve Turkuaz kenarlıklı dairesel kapsayıcı
        Surface(
            modifier = Modifier
                .size(iconSize + 20.dp),
            shape = CircleShape,
            color = WarmWhite,
            border = BorderStroke(2.dp, Turquoise),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit, // Kesilmeyi önlemek için ContentScale.Fit
                    colorFilter = null // Orijinal logonun renklerini korumak için renk filtresi uygulanmadı
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (showSubtitle) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_slogan),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Turquoise
            )
        }
    }
}
