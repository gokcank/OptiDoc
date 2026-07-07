package com.gokcank.optidoc.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.gokcank.optidoc.core.dispatcher.DispatcherProvider
import com.gokcank.optidoc.domain.model.AppError
import com.gokcank.optidoc.domain.repository.OcrRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * [OcrRepository] implementasyonu — ML Kit Text Recognition v2 (Latin).
 *
 * [TextRecognizer], [com.gokcank.optidoc.di.MlKitModule] tarafından
 * [javax.inject.Singleton] olarak sağlanır; her çağrıda yeniden oluşturulmaz.
 *
 * [InputImage.fromFilePath] diskten senkron okuma yaptığından (blocking I/O),
 * tüm fonksiyon [DispatcherProvider.io]'da çalışır.
 */
class OcrRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val textRecognizer: TextRecognizer,
    private val dispatcherProvider: DispatcherProvider
) : OcrRepository {

    override suspend fun recognizeText(imageUri: String): Result<String> = withContext(dispatcherProvider.io) {
        val inputImage = try {
            InputImage.fromFilePath(context, Uri.parse(imageUri))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return@withContext Result.failure(AppError.ImageNotReadable(imageUri, e))
        }

        try {
            val recognizedText = textRecognizer.process(inputImage).await()
            Result.success(recognizedText.text)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AppError.OcrFailed(e))
        }
    }
}
