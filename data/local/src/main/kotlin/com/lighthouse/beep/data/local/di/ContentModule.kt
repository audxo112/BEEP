package com.lighthouse.beep.data.local.di

import android.content.ContentResolver
import android.content.Context
import com.lighthouse.beep.data.local.repository.gallery.GalleryDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal object ContentModule {

    @Provides
    @Singleton
    fun provideContentResolver(
        @ApplicationContext context: Context,
    ): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun providesGalleryImageDataSource(
        contentResolver: ContentResolver,
    ): GalleryDataSource = GalleryDataSource(contentResolver)
}
