package com.gokcank.optidoc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gokcank.optidoc.ui.home.HomeScreen
import com.gokcank.optidoc.ui.review.ReviewEditScreen
import com.gokcank.optidoc.ui.review.ReviewScreen
import com.gokcank.optidoc.ui.scan.ScanScreen
import com.gokcank.optidoc.ui.detail.DetailScreen

/**
 * Uygulamanın tüm ekran geçişlerini yöneten NavHost.
 *
 * Akış: [Screen.Home] → [Screen.Scan] → [Screen.Review] (OCR uygulansın mı
 * kararı) → "Hayır": doğrudan kaydet → Home; "Evet": OCR çalıştır →
 * [Screen.ReviewEdit] (düzenle + dışa aktar) → Home.
 *
 * Ekranların kendisi şimdilik placeholder (bkz. ilgili dosyalardaki el
 * notları); burada netleşmesi gereken yalnızca route'lar ve geri tuşu/
 * back stack davranışıydı:
 * - Scan → Review: [Screen.Scan] back stack'ten çıkarılır, geri tuşu
 *   tekrar tarayıcı açmasın diye.
 * - Review → ReviewEdit: [Screen.Review] back stack'ten çıkarılır, OCR
 *   zaten çalıştıktan sonra "Evet/Hayır" kararına geri dönmek anlamsız.
 * - Doğrudan kaydet / export tamamlanınca: Home zaten back stack'te
 *   (start destination) olduğu için `navigate` değil `popBackStack` ile
 *   ona dönülür.
 */
@Composable
fun OptiDocNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home
    ) {
        composable<Screen.Home> {
            HomeScreen(
                onScanNewDocument = { navController.navigate(Screen.Scan) },
                onDocumentClick = { documentId -> navController.navigate(Screen.Detail(documentId)) }
            )
        }

        composable<Screen.Scan> {
            ScanScreen(
                onScanCompleted = { pageImageUris, nativePdfUri ->
                    navController.navigate(
                        Screen.Review(pageImageUris = pageImageUris, nativePdfUri = nativePdfUri)
                    ) {
                        popUpTo<Screen.Scan> { inclusive = true }
                    }
                },
                onScanCancelled = { navController.popBackStack() }
            )
        }

        composable<Screen.Review> { backStackEntry ->
            val route: Screen.Review = backStackEntry.toRoute()
            val activity = androidx.compose.ui.platform.LocalContext.current as android.app.Activity
            ReviewScreen(
                pageImageUris = route.pageImageUris,
                nativePdfUri = route.nativePdfUri,
                onSavedDirectly = {
                    com.gokcank.optidoc.ui.components.InterstitialAdManager.showAd(activity) {
                        navController.popBackStack<Screen.Home>(inclusive = false)
                    }
                },
                onOcrCompleted = { documentId ->
                    navController.navigate(Screen.ReviewEdit(documentId = documentId)) {
                        popUpTo<Screen.Review> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.ReviewEdit> { backStackEntry ->
            val route: Screen.ReviewEdit = backStackEntry.toRoute()
            val activity = androidx.compose.ui.platform.LocalContext.current as android.app.Activity
            ReviewEditScreen(
                documentId = route.documentId,
                onExportCompleted = {
                    com.gokcank.optidoc.ui.components.InterstitialAdManager.showAd(activity) {
                        navController.popBackStack<Screen.Home>(inclusive = false)
                    }
                }
            )
        }

        composable<Screen.Detail> { backStackEntry ->
            DetailScreen(
                documentId = backStackEntry.toRoute<Screen.Detail>().documentId,
                onBack = { navController.popBackStack() },
                onEdit = { docId -> navController.navigate(Screen.ReviewEdit(docId)) }
            )
        }
    }
}
