package com.yike.jarvis.feature.beverage.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beverage_catalog")
data class BeverageCatalogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val brand: String = "",
    val sugar: Double,
    val caffeine: Double,
    val defaultTags: String = ""
)
