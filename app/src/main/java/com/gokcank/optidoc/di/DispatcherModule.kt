package com.gokcank.optidoc.di

import com.gokcank.optidoc.core.dispatcher.DefaultDispatcherProvider
import com.gokcank.optidoc.core.dispatcher.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [DispatcherProvider]'ı Hilt grafiğine bağlar.
 *
 * Tek başına bir modül: [RepositoryModule]'ün her implementasyonu bu
 * soyutlamaya bağımlı olabileceği için, [RepositoryModule]'den önce/bağımsız
 * çözülebilen ayrı bir modülde tutmak grafiği daha okunur kılıyor.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        impl: DefaultDispatcherProvider
    ): DispatcherProvider
}
