package com.lighthouse.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.lighthouse.database.entity.BrandEntity.Companion.BRAND_TABLE

@Entity(
    tableName = BRAND_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = arrayOf("section_id"),
            childColumns = arrayOf("parent_section_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BrandEntity(
    @PrimaryKey
    @ColumnInfo(name = "place_url")
    val placeUrl: String,
    @ColumnInfo(name = "address_name") val addressName: String,
    @ColumnInfo(name = "parent_section_id") val sectionId: Long,
    @ColumnInfo(name = "place_name") val placeName: String,
    @ColumnInfo(name = "brand") val brand: String,
    @ColumnInfo(name = "x") val x: String,
    @ColumnInfo(name = "y") val y: String
) {

    companion object {
        const val BRAND_TABLE = "brand_table"
    }
}
