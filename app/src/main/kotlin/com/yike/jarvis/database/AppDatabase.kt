package com.yike.jarvis.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yike.jarvis.BuildConfig
import com.yike.jarvis.feature.beverage.db.dao.BeverageCatalogDao
import com.yike.jarvis.feature.beverage.db.dao.BeverageDao
import com.yike.jarvis.feature.beverage.db.entity.BeverageCatalogEntity
import com.yike.jarvis.feature.beverage.db.entity.BeverageEntity
import com.yike.jarvis.feature.scheduler.db.dao.TaskDao
import com.yike.jarvis.feature.scheduler.db.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, BeverageEntity::class, BeverageCatalogEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun beverageDao(): BeverageDao
    abstract fun beverageCatalogDao(): BeverageCatalogDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 增加 category 字段，默认为 'LIFE'
                database.execSQL("ALTER TABLE tasks ADD COLUMN category TEXT NOT NULL DEFAULT 'LIFE'")
            }
        }

        private val CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // 预置饮料目录数据
                db.execSQL("INSERT INTO beverage_catalog (name, brand, sugar, caffeine, defaultTags) VALUES ('可乐', '可口可乐/百事', 35.0, 33.0, '330ml')")
                db.execSQL("INSERT INTO beverage_catalog (name, brand, sugar, caffeine, defaultTags) VALUES ('小奶茉', '喜茶', 30.0, 30.0, '茉莉绿茶')")
                db.execSQL("INSERT INTO beverage_catalog (name, brand, sugar, caffeine, defaultTags) VALUES ('烤黑糖波波牛乳', '喜茶', 50.0, 0.0, '无茶')")
                db.execSQL("INSERT INTO beverage_catalog (name, brand, sugar, caffeine, defaultTags) VALUES ('加浓美式', '瑞幸', 0.0, 175.0, '极高咖啡因')")
                db.execSQL("INSERT INTO beverage_catalog (name, brand, sugar, caffeine, defaultTags) VALUES ('美式咖啡', '古茗', 0.0, 125.0, '标准美式')")
                db.execSQL("INSERT INTO beverage_catalog (name, brand, sugar, caffeine, defaultTags) VALUES ('椰椰芒芒', '喜茶', 37.0, 0.0, '无咖啡因')")
                db.execSQL("INSERT INTO beverage_catalog (name, brand, sugar, caffeine, defaultTags) VALUES ('贪杯乌龙', '茶理宜世', 15.0, 40.0, '乌龙茶')")
                db.execSQL("INSERT INTO beverage_catalog (name, brand, sugar, caffeine, defaultTags) VALUES ('黑糖珍珠奶茶', '茶百道', 50.0, 118.0, '经典奶茶')")

                // 预置示例任务数据
                db.execSQL("INSERT INTO tasks (time, name, isActive, importance, isLocked, category) VALUES ('08:00 AM', 'Morning Workout', 1, 'NORMAL', 0, 'LIFE')")
                db.execSQL("INSERT INTO tasks (time, name, isActive, importance, isLocked, category) VALUES ('09:30 AM', 'Team Standup', 1, 'NORMAL', 0, 'LIFE')")
                db.execSQL("INSERT INTO tasks (time, name, isActive, importance, isLocked, category) VALUES ('12:00 PM', 'Lunch Break', 0, 'NORMAL', 0, 'LIFE')")
                db.execSQL("INSERT INTO tasks (time, name, isActive, importance, isLocked, category) VALUES ('03:00 PM', 'Code Review', 1, 'NORMAL', 0, 'LIFE')")
                db.execSQL("INSERT INTO tasks (time, name, isActive, importance, isLocked, category) VALUES ('06:00 PM', 'Evening Walk', 0, 'NORMAL', 0, 'LIFE')")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database_" + BuildConfig.BUILD_TYPE
                ).addMigrations(MIGRATION_1_2)
                    .addCallback(CALLBACK)
                    .fallbackToDestructiveMigration(true) // 开发版本自动重建
                    .build().also { instance = it }
            }
        }
    }
}