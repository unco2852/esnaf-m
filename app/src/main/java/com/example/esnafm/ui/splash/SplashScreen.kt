package com.example.esnafm.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.esnafm.ui.components.EsnafımLogo
import com.example.esnafm.ui.theme.WarmCream
import kotlinx.coroutines.delay

/**
 * Uygulama ilk açıldığında gösterilen ve ilk giriş ekranına yönlendiren Açılış Ekranı (Splash Screen)
 */
@Composable
fun SplashScreen(
    onNavigateToRoleSelection: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1200) // Akıcı açılış deneyimi için logo gösterim süresi
        onNavigateToRoleSelection()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream),
        contentAlignment = Alignment.Center
    ) {
        EsnafımLogo(showSubtitle = true)
    }
}
