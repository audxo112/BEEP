package com.lighthouse.repository.brand

import com.lighthouse.beep.model.brand.BrandPlaceInfo
import com.lighthouse.beep.model.location.Dms
import com.lighthouse.common.converter.LocationConverter
import com.lighthouse.domain.repository.brand.BrandRepository
import javax.inject.Inject

internal class BrandRepositoryImpl @Inject constructor(
    private val brandRemoteRepository: BrandRemoteRepository,
    private val brandDatabaseRepository: BrandDatabaseRepository
) : BrandRepository {

    override suspend fun getAroundBrandPlaceInfo(
        brandNames: List<String>,
        x: Double,
        y: Double,
        size: Int
    ): Result<List<BrandPlaceInfo>> = runCatching {
        val cardinalLocations = LocationConverter.getCardinalDirections(x, y)
        cardinalLocations.flatMap { location ->
            brandNames.flatMap { brandName ->
                getBrandPlaceInfo(brandName, location.x, location.y, size).getOrThrow()
            }
        }
    }

    override suspend fun getBrandPlaceInfo(
        brandName: String,
        x: Dms,
        y: Dms,
        size: Int
    ): Result<List<BrandPlaceInfo>> {
        var result = brandDatabaseRepository.getBrands(x, y, brandName)
        if (result.isFailure) {
            result = brandRemoteRepository.getBrandPlaceInfo(brandName, x, y, size)
            val list = result.getOrNull()
            if (list != null) {
                brandDatabaseRepository.insertBrands(list, x, y, brandName)
            }
        }
        return result
    }

    override suspend fun removeExpirationBrands(): Result<Unit> {
        return brandDatabaseRepository.removeExpirationBrands()
    }
}