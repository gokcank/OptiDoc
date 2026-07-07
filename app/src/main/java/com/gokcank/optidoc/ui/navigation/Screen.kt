package com.gokcank.optidoc.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Uygulamadaki tüm ekranların tip-güvenli route tanımları
 * (Navigation Compose 2.9 — `@Serializable` route sınıfları, string route
 * yoktur). [com.gokcank.optidoc.ui.navigation.OptiDocNavHost]
 * içinde `composable<Screen.X>` ile kaydedilir, `navController.navigate(Screen.X(...))`
 * ile gidilir, hedef ekranda `backStackEntry.toRoute<Screen.X>()` ile okunur.
 *
 * ## Review tek route mu, iki route mu?
 * İki ayrı route'a bölündü: [Review] (OCR uygulansın mı kararı) ve
 * [ReviewEdit] (metni düzenle + format seç). Gerekçe:
 * 1. **Farklı argüman tipleri taşıyorlar** — [Review] henüz Room'a
 *    kaydedilmemiş ham tarama çıktısını ([pageImageUris]/[nativePdfUri])
 *    taşır; [ReviewEdit] ise zaten kaydedilmiş ve OCR'lanmış bir belgenin
 *    [documentId]'sini taşır. Tek route'a sığdırmak "hepsi nullable" bir
 *    argüman karması gerektirirdi — tip güvenliğini baltalardı.
 *    (Bkz. [com.gokcank.optidoc.domain.usecase.RunOcrUseCase] ve
 *    [com.gokcank.optidoc.domain.usecase.SaveDirectDocumentUseCase]:
 *    belge satırı DB'ye ancak kullanıcı "Evet/Hayır" kararını verdikten
 *    SONRA yazılıyor — yani Tarama bitince elimizde henüz documentId yok.)
 * 2. **Geri tuşu davranışı** sistem back stack'iyle bedavaya doğru çalışıyor:
 *    ReviewEdit'ten geri → Review'a değil (OCR zaten çalıştı, kararı geri
 *    almak anlamsız — bkz. NavHost'taki `popUpTo<Review>`), Review'dan
 *    geri → Scan'a değil Home'a (bkz. `popUpTo<Scan>`). Tek route + dahili
 *    state machine seçilseydi bu davranış için manuel `BackHandler`
 *    gerekirdi.
 * 3. İki ekran görsel olarak tamamen farklı (Evet/Hayır kararı vs. tam
 *    ekran metin editörü) — ayrı route'lar, ayrı ve odaklı ViewModel'ler
 *    anlamına geliyor.
 */
sealed interface Screen {

    /** Geçmiş taramaların listesi. Başlangıç ekranı. */
    @Serializable
    data object Home : Screen

    /** ML Kit Document Scanner'ı tetikler. Argüman almaz. */
    @Serializable
    data object Scan : Screen

    /**
     * "OCR uygulansın mı?" kararı. Henüz Room'a kaydedilmemiş, ML Kit'in
     * ham tarama çıktısını taşır.
     *
     * @property pageImageUris ML Kit'in ürettiği sayfa JPEG Uri'leri (String).
     * @property nativePdfUri  ML Kit'in ürettiği birleşik PDF Uri'si (String);
     *                        yalnızca "Hayır" (direkt kaydet) yolunda kullanılır.
     */
    @Serializable
    data class Review(
        val pageImageUris: List<String>,
        val nativePdfUri: String? = null
    ) : Screen

    /**
     * OCR metnini düzenleme + .txt/.pdf format seçimi. Bu route'a girildiğinde
     * belge zaten Room'a kaydedilmiş ve OCR çalışmış durumdadır.
     *
     * @property documentId Düzenlenecek/dışa aktarılacak belgenin DB id'si.
     */
    @Serializable
    data class ReviewEdit(
        val documentId: Long
    ) : Screen

    /**
     * Zaten kaydedilmiş ve dışa aktarılmış/işlenmiş bir belgenin detaylarını
     * gösterme (sayfaları görme, açma/paylaşma).
     *
     * @property documentId Gösterilecek belgenin DB id'si.
     */
    @Serializable
    data class Detail(
        val documentId: Long
    ) : Screen
}
