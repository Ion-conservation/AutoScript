package com.yike.jarvis.di.entrypoints

import com.yike.jarvis.core.shizuku.repository.ShizukuRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ShizukuEntryPoint {
    fun shizukuRepository(): ShizukuRepository
}