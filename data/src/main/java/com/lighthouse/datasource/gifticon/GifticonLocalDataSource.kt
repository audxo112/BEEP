package com.lighthouse.datasource.gifticon

import com.lighthouse.database.entity.GifticonEntity
import com.lighthouse.domain.model.Brand
import com.lighthouse.domain.model.Gifticon
import com.lighthouse.domain.model.UsageHistory
import kotlinx.coroutines.flow.Flow

interface GifticonLocalDataSource {
    fun getGifticon(id: String): Flow<Gifticon>
    fun getAllGifticons(userId: String): Flow<List<Gifticon>>
    fun getFilteredGifticons(userId: String, filter: Set<String>): Flow<List<Gifticon>>
    fun getAllBrands(userId: String): Flow<List<Brand>>

    suspend fun insertGifticons(gifticons: List<GifticonEntity>)
    suspend fun updateGifticon(gifticon: Gifticon)
    suspend fun useGifticon(gifticonId: String, usageHistory: UsageHistory)
    suspend fun useCashCardGifticon(gifticonId: String, amount: Int, usageHistory: UsageHistory)
    suspend fun unUseGifticon(gifticonId: String)

    fun getUsageHistory(gifticonId: String): Flow<List<UsageHistory>>
    suspend fun insertUsageHistory(gifticonId: String, usageHistory: UsageHistory)
    fun getGifticonByBrand(brand: String): Flow<List<GifticonEntity>>
}