package com.yike.jarvis.feature.beverage.db.repository

import com.yike.jarvis.feature.beverage.db.dao.BeverageCatalogDao
import com.yike.jarvis.feature.beverage.db.dao.BeverageDao
import com.yike.jarvis.feature.beverage.db.entity.BeverageCatalogEntity
import com.yike.jarvis.feature.beverage.db.entity.BeverageEntity
import kotlinx.coroutines.flow.Flow

class BeverageRepository(
    private val beverageDao: BeverageDao,
    private val beverageCatalogDao: BeverageCatalogDao
) {
    // Beverage Records
    fun getAllBeverages(): Flow<List<BeverageEntity>> = beverageDao.getAllBeverages()

    suspend fun addBeverage(beverage: BeverageEntity) = beverageDao.insertBeverage(beverage)

    suspend fun updateBeverage(beverage: BeverageEntity) = beverageDao.updateBeverage(beverage)

    suspend fun deleteBeverage(beverage: BeverageEntity) = beverageDao.deleteBeverage(beverage)

    suspend fun deleteById(id: Int) = beverageDao.deleteById(id)

    // Beverage Catalog
    fun getAllCatalogItems(): Flow<List<BeverageCatalogEntity>> = beverageCatalogDao.getAllCatalogItems()

    suspend fun addCatalogItem(item: BeverageCatalogEntity) = beverageCatalogDao.insertCatalogItem(item)

    suspend fun deleteCatalogItem(item: BeverageCatalogEntity) = beverageCatalogDao.deleteCatalogItem(item)
}
