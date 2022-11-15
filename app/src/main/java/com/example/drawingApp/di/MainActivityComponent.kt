package com.example.drawingApp.di

import com.example.drawingApp.ApplicationComponent
import com.example.drawingApp.MainActivity
import com.example.drawingApp.utils.ColorPickerUtility
import com.example.drawingApp.utils.DialogUtility
import com.example.drawingApp.utils.ImageUtility
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(dependencies = [ApplicationComponent::class], modules = [MainActivityModule::class])
interface MainActivityComponent {
    fun inject(activity: MainActivity)
}

@Module
class MainActivityModule {
    @Provides
    fun providesColorPickerUtility() = ColorPickerUtility()

    @Provides
    fun providesImageUtility() = ImageUtility()

    @Provides
    fun providesDialogUtility() = DialogUtility()
}