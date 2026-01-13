package com.yike.jarvis.feature.beverage.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beverages")
data class BeverageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val brand: String = "",
    val sugar: Double,
    val caffeine: Double,
    val timestamp: Long, // 毫秒级时间戳
    val tags: String // 以逗号分隔的标签，例如 "High Sugar,High Caffeine"
)
