package auto.script.di.entrypoints

import auto.script.shizuku.ShizukuRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ShizukuEntryPoint {
    fun shizukuRepository(): ShizukuRepository
}