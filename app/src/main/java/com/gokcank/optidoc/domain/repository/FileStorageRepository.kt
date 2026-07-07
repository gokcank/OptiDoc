package com.gokcank.optidoc.domain.repository

/**
 * Belgeyle ilişkili dosyaların (sayfa görselleri, dışa aktarılan çıktılar)
 * diskten temizlenmesi sözleşmesi.
 *
 * Implementasyon [data.storage.FileStorageRepositoryImpl] içindedir.
 */
interface FileStorageRepository {

    /**
     * Verilen Uri'lerin işaret ettiği dosyaları siler.
     *
     * Best-effort çalışır: bir Uri silinemezse (dosya zaten yok, ya da
     * sahibi olmadığımız bir content provider — örn. ML Kit'in ürettiği
     * sayfa görseli Uri'si — silmeye izin vermiyorsa) hata fırlatmaz,
     * yalnızca o Uri'yi atlar ve kalanlarla devam eder.
     */
    suspend fun deleteFiles(uris: List<String>)
}
