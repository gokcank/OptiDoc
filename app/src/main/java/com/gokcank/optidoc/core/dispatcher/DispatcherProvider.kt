package com.gokcank.optidoc.core.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Repository'lerin [kotlinx.coroutines.Dispatchers.IO] gibi somut
 * dispatcher'ları doğrudan referans almak yerine enjekte ettiği soyutlama.
 *
 * Üretimde [DefaultDispatcherProvider] gerçek dispatcher'lara delege eder.
 * Unit testlerde üçü de aynı `TestDispatcher`'a eşlenen sahte bir
 * implementasyon enjekte edilerek coroutine zamanlaması deterministik hale
 * getirilebilir — gerçek thread pool'lara (`Dispatchers.IO`) bağımlı kalmadan.
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
