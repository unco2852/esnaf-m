package com.example.esnafm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.esnafm.data.model.UserRole
import com.example.esnafm.ui.auth.AuthScreen
import com.example.esnafm.ui.customer.CustomerHomeScreen
import com.example.esnafm.ui.customer.MerchantDetailScreen
import com.example.esnafm.ui.customer.ProductDetailScreen
import com.example.esnafm.ui.customer.ServiceDetailScreen
import com.example.esnafm.ui.profile.AddressBookScreen
import com.example.esnafm.ui.profile.FaqScreen
import com.example.esnafm.ui.profile.NotificationPreferencesScreen
import com.example.esnafm.ui.profile.PrivacyPolicyScreen
import com.example.esnafm.ui.profile.ProfileScreen
import com.example.esnafm.ui.profile.SecurityScreen
import com.example.esnafm.ui.profile.TermsOfServiceScreen
import com.example.esnafm.ui.roleselection.RoleSelectionScreen
import com.example.esnafm.ui.splash.SplashScreen
import com.example.esnafm.ui.tradesman.TradesmanPanelScreen

object ScreenRoutes {
    const val SPLASH = "splash"
    const val ROLE_SELECTION = "role_selection"
    const val AUTH = "auth/{role}"
    const val CUSTOMER_HOME = "customer_home"
    const val TRADESMAN_PANEL = "tradesman_panel"
    const val PROFILE = "profile"
    const val ADDRESS_BOOK = "address_book"
    const val FAQ = "faq"
    const val NOTIFICATION_PREFERENCES = "notification_preferences"
    const val PRIVACY_POLICY = "privacy_policy"
    const val TERMS_OF_SERVICE = "terms_of_service"
    const val SECURITY_CENTER = "security_center"
    const val MERCHANT_DETAIL = "merchant_detail/{merchantId}"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val SERVICE_DETAIL = "service_detail/{serviceId}"

    fun authRoute(role: UserRole): String = "auth/${role.name}"
    fun merchantDetailRoute(merchantId: String): String = "merchant_detail/$merchantId"
    fun productDetailRoute(productId: String): String = "product_detail/$productId"
    fun serviceDetailRoute(serviceId: String): String = "service_detail/$serviceId"
}

/**
 * Esnafım - Ana Gezinme Graph'ı (Navigation Compose + Firebase Auth Entegrasyonu)
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.SPLASH
    ) {
        // 0. Açılış Ekranı (Splash Screen)
        composable(ScreenRoutes.SPLASH) {
            SplashScreen(
                onNavigateToRoleSelection = {
                    navController.navigate(ScreenRoutes.ROLE_SELECTION) {
                        popUpTo(ScreenRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 1. Rol Seçim Ekranı
        composable(ScreenRoutes.ROLE_SELECTION) {
            RoleSelectionScreen(
                onNavigateToCustomer = {
                    navController.navigate(ScreenRoutes.authRoute(UserRole.CUSTOMER))
                },
                onNavigateToTradesman = {
                    navController.navigate(ScreenRoutes.authRoute(UserRole.MERCHANT))
                }
            )
        }

        // 2. Giriş / Kayıt Ekranı (Dinamik Rol Seçimli)
        composable(
            route = ScreenRoutes.AUTH,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val roleParam = backStackEntry.arguments?.getString("role") ?: UserRole.CUSTOMER.name
            val roleEnum = try {
                UserRole.valueOf(roleParam)
            } catch (e: Exception) {
                UserRole.CUSTOMER
            }

            AuthScreen(
                role = roleEnum,
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(ScreenRoutes.ROLE_SELECTION) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onAuthSuccess = { profile ->
                    val destination = if (profile.role == UserRole.MERCHANT) {
                        ScreenRoutes.TRADESMAN_PANEL
                    } else {
                        ScreenRoutes.CUSTOMER_HOME
                    }
                    android.util.Log.d("EsnafmAuthDebug", "7. AppNavigation.onAuthSuccess - Profile UID: '${profile.uid}', Profile Role: '${profile.role}' -> Target Route: '$destination'")
                    navController.navigate(destination) {
                        popUpTo(ScreenRoutes.ROLE_SELECTION) { inclusive = true }
                    }
                }
            )
        }

        // 3. Müşteri Ana Ekranı
        composable(ScreenRoutes.CUSTOMER_HOME) {
            CustomerHomeScreen(
                onNavigateBack = {
                    val authRoute = ScreenRoutes.authRoute(UserRole.CUSTOMER)
                    navController.navigate(authRoute) {
                        popUpTo(ScreenRoutes.CUSTOMER_HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = { navController.navigate(ScreenRoutes.PROFILE) },
                onNavigateToMerchantDetail = { merchantId ->
                    navController.navigate(ScreenRoutes.merchantDetailRoute(merchantId))
                },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(ScreenRoutes.productDetailRoute(productId))
                },
                onNavigateToServiceDetail = { serviceId ->
                    navController.navigate(ScreenRoutes.serviceDetailRoute(serviceId))
                }
            )
        }

        // 4. Esnaf Yönetim Paneli
        composable(ScreenRoutes.TRADESMAN_PANEL) {
            TradesmanPanelScreen(
                onNavigateBack = {
                    val authRoute = ScreenRoutes.authRoute(UserRole.MERCHANT)
                    navController.navigate(authRoute) {
                        popUpTo(ScreenRoutes.TRADESMAN_PANEL) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = { navController.navigate(ScreenRoutes.PROFILE) }
            )
        }

        // 5. Profil Ekranı
        composable(ScreenRoutes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(ScreenRoutes.ROLE_SELECTION) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAddressBook = { navController.navigate(ScreenRoutes.ADDRESS_BOOK) },
                onNavigateToNotificationPrefs = { navController.navigate(ScreenRoutes.NOTIFICATION_PREFERENCES) },
                onNavigateToFaq = { navController.navigate(ScreenRoutes.FAQ) },
                onNavigateToPrivacyPolicy = { navController.navigate(ScreenRoutes.PRIVACY_POLICY) },
                onNavigateToTermsOfService = { navController.navigate(ScreenRoutes.TERMS_OF_SERVICE) },
                onNavigateToSecurity = { navController.navigate(ScreenRoutes.SECURITY_CENTER) }
            )
        }

        // 5a. Adres Defteri Ekranı
        composable(ScreenRoutes.ADDRESS_BOOK) {
            AddressBookScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5b. Bildirim Tercihleri Ekranı
        composable(ScreenRoutes.NOTIFICATION_PREFERENCES) {
            NotificationPreferencesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5c. SSS Yardım Ekranı
        composable(ScreenRoutes.FAQ) {
            FaqScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5d. Gizlilik Politikası & KVKK Ekranı
        composable(ScreenRoutes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5e. Kullanım Koşulları Ekranı
        composable(ScreenRoutes.TERMS_OF_SERVICE) {
            TermsOfServiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5f. Güvenlik Ekranı
        composable(ScreenRoutes.SECURITY_CENTER) {
            SecurityScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Esnaf Detay Ekranı
        composable(
            route = ScreenRoutes.MERCHANT_DETAIL,
            arguments = listOf(navArgument("merchantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val merchantId = backStackEntry.arguments?.getString("merchantId") ?: ""
            MerchantDetailScreen(
                merchantId = merchantId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(ScreenRoutes.productDetailRoute(productId))
                },
                onNavigateToServiceDetail = { serviceId ->
                    navController.navigate(ScreenRoutes.serviceDetailRoute(serviceId))
                }
            )
        }

        // 7. Ürün Detay Ekranı
        composable(
            route = ScreenRoutes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMerchantDetail = { merchantId ->
                    navController.navigate(ScreenRoutes.merchantDetailRoute(merchantId))
                }
            )
        }

        // 8. Hizmet Detay Ekranı
        composable(
            route = ScreenRoutes.SERVICE_DETAIL,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            ServiceDetailScreen(
                serviceId = serviceId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMerchantDetail = { merchantId ->
                    navController.navigate(ScreenRoutes.merchantDetailRoute(merchantId))
                }
            )
        }
    }
}