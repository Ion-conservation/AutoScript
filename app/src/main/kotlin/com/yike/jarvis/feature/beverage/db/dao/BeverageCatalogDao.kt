package com.yike.jarvis.feature.beverage.db.dao

import androidx.room.*
import com.yike.jarvis.feature.beverage.db.entity.BeverageCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BeverageCatalogDao {
    @Query("SELECT * FROM beverage_catalog ORDER BY name ASC")
    fun getAllCatalogItems(): Flow<List<BeverageCatalogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItem(item: BeverageCatalogEntity)

    @Update
    suspend fun updateCatalogItem(item: BeverageCatalogEntity)

    @Delete
    suspend fun deleteCatalogItem(item: BeverageCatalogEntity)
}
