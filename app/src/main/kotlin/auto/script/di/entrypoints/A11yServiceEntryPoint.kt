package auto.script.di.entrypoints

import auto.script.A11yService.A11yServiceRepository
import auto.script.A11yService.A11yServiceTool
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface A11yServiceEntryPoint {
    fun a11yServiceRepository(): A11yServiceRepository
    fun a11yServiceTool(): A11yServiceTool

}