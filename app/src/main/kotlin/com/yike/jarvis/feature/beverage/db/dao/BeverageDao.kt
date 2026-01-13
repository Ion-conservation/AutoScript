package com.yike.jarvis.feature.beverage.db.dao

import androidx.room.*
import com.yike.jarvis.feature.beverage.db.entity.BeverageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BeverageDao {
    @Query("SELECT * FROM beverages ORDER BY timestamp DESC")
    fun getAllBeverages(): Flow<List<BeverageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeverage(beverage: BeverageEntity)

    @Update
    suspend fun updateBeverage(beverage: BeverageEntity)

    @Delete
    suspend fun deleteBeverage(beverage: BeverageEntity)

    @Query("DELETE FROM beverages WHERE id = :id")
    suspend fun deleteById(id: Int)
}
