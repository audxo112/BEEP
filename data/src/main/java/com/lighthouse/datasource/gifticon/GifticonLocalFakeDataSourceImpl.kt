package com.lighthouse.datasource.gifticon

import com.lighthouse.database.dao.GifticonDao
import com.lighthouse.database.entity.GifticonEntity
import com.lighthouse.database.mapper.toEntity
import com.lighthouse.database.mapper.toUsageHistory
import com.lighthouse.database.mapper.toUsageHistoryEntity
import com.lighthouse.domain.model.Brand
import com.lighthouse.domain.model.Gifticon
import com.lighthouse.domain.model.SortBy
import com.lighthouse.domain.model.UsageHistory
import com.lighthouse.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GifticonLocalFakeDataSourceImpl @Inject constructor(
    private val gifticonDao: GifticonDao
) : GifticonLocalDataSource {

    override fun getGifticon(id: String): Flow<Gifticon> {
        return gifticonDao.getGifticon(id).map { entity ->
            entity.toDomain()
        }
    }

    override fun getAllGifticons(userId: String, sortBy: SortBy): Flow<List<Gifticon>> {
        val gifticons = when (sortBy) {
            SortBy.DEADLINE -> gifticonDao.getAllGifticonsSortByDeadline("이름")
            SortBy.RECENT -> gifticonDao.getAllGifticons("이름")
        }
        return gifticons.map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFilteredGifticons(userId: String, filter: Set<String>, sortBy: SortBy): Flow<List<Gifticon>> {
        val gifticons = when (sortBy) {
            SortBy.DEADLINE -> gifticonDao.getFilteredGifticonsSortByDeadline("이름", filter)
            SortBy.RECENT -> gifticonDao.getFilteredGifticons("이름", filter)
        }
        return gifticons.map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllBrands(userId: String): Flow<List<Brand>> {
        return gifticonDao.getAllBrands("이름")
    }

    override suspend fun updateGifticon(gifticon: Gifticon) {
        gifticonDao.updateGifticon(gifticon.toEntity())
    }

    override suspend fun insertGifticons(gifticons: List<GifticonEntity>) {
        gifticonDao.insertGifticon(*gifticons.toTypedArray())
    }

    override suspend fun useGifticon(gifticonId: String, usageHistory: UsageHistory) {
        gifticonDao.useGifticonTransaction(usageHistory.toUsageHistoryEntity(gifticonId))
    }

    override suspend fun useCashCardGifticon(gifticonId: String, amount: Int, usageHistory: UsageHistory) {
        gifticonDao.useCashCardGifticonTransaction(amount, usageHistory.toUsageHistoryEntity(gifticonId))
    }

    override suspend fun unUseGifticon(gifticonId: String) {
        gifticonDao.unUseGifticon(gifticonId)
    }

    override fun getUsageHistory(gifticonId: String): Flow<List<UsageHistory>> {
        return gifticonDao.getUsageHistory(gifticonId).map { list ->
            list.map { entity ->
                entity.toUsageHistory()
            }
        }
    }

    override suspend fun insertUsageHistory(gifticonId: String, usageHistory: UsageHistory) {
        gifticonDao.insertUsageHistory(usageHistory.toUsageHistoryEntity(gifticonId))
    }

    override fun getGifticonByBrand(brand: String): Flow<List<GifticonEntity>> {
        return gifticonDao.getGifticonByBrand(brand)
    }
}
