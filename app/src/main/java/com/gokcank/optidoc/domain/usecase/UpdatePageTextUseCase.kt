package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.repository.DocumentRepository
import javax.inject.Inject

/**
 * Kullanıcı Review ekranında OCR metnini düzenledikten sonra kaydet'e bastığında çalışır.
 * Tekil sayfa güncellemesi — tüm belgeyi yeniden yüklemez.
 */
class UpdatePageTextUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(pageId: Long, text: String) =
        repository.updatePageText(pageId, text)
}
