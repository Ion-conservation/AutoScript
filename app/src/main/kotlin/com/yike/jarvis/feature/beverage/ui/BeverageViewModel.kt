package com.yike.jarvis.feature.beverage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yike.jarvis.feature.beverage.db.entity.BeverageCatalogEntity
import com.yike.jarvis.feature.beverage.db.entity.BeverageEntity
import com.yike.jarvis.feature.beverage.db.repository.BeverageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DailyBeverageSummary(
    val date: String,
    val dateLong: Long, // 某天的一个时间戳，用于标识该天
    val dateDisplay: String, // 用于 UI 显示，例如 "Today", "Yesterday" 或日期
    val totalSugar: Double,
    val totalCaffeine: Double,
    val beverages: List<BeverageEntity>
)

@HiltViewModel
class BeverageViewModel @Inject constructor(
    private val repository: BeverageRepository
) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    val beverageCatalog: StateFlow<List<BeverageCatalogEntity>> = repository.getAllCatalogItems()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val dailySummaries: StateFlow<List<DailyBeverageSummary>> = repository.getAllBeverages()
        .map { beverages ->
            beverages.groupBy { 
                dateFormatter.format(Date(it.timestamp)) 
            }.map { (dateStr, items) ->
                val date = dateFormatter.parse(dateStr) ?: Date()
                val dateDisplay = formatDateDisplay(date)
                DailyBeverageSummary(
                    date = dateStr,
                    dateLong = items.firstOrNull()?.timestamp ?: date.time,
                    dateDisplay = dateDisplay,
                    totalSugar = items.sumOf { it.sugar },
                    totalCaffeine = items.sumOf { it.caffeine },
                    beverages = items.sortedByDescending { it.timestamp }
                )
            }.sortedByDescending { it.date }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addBeverage(name: String, brand: String, sugar: Double, caffeine: Double, tags: List<String>, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val beverage = BeverageEntity(
                name = name,
                brand = brand,
                sugar = sugar,
                caffeine = caffeine,
                timestamp = timestamp,
                tags = tags.joinToString(",")
            )
            repository.addBeverage(beverage)
        }
    }

    fun addCatalogItem(name: String, brand: String, sugar: Double, caffeine: Double, tags: String = "") {
        viewModelScope.launch {
            repository.addCatalogItem(
                BeverageCatalogEntity(
                    name = name,
                    brand = brand,
                    sugar = sugar,
                    caffeine = caffeine,
                    defaultTags = tags
                )
            )
        }
    }

    fun deleteBeverage(beverage: BeverageEntity) {
        viewModelScope.launch {
            repository.deleteBeverage(beverage)
        }
    }

    private fun formatDateDisplay(date: Date): String {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time

        return when {
            isSameDay(date, today) -> "Today"
            isSameDay(date, yesterday) -> "Yesterday"
            else -> displayDateFormatter.format(date)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
