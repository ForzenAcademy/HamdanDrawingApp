package com.example.drawingApp

import android.app.Application
import com.example.drawingApp.utils.ColorPickerUtility
import com.example.drawingApp.utils.DialogUtility
import com.example.drawingApp.utils.ImageUtility
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(activity: MainActivity)
}

@Module
class ApplicationModule {


    @Provides
    fun providesColorPickerUtility() = ColorPickerUtility()

    @Provides
    fun providesImageUtility() = ImageUtility()

    @Provides
    fun providesDialogUtility() = DialogUtility()

}

class DaggerApplication : Application() {
    val appComponent = DaggerApplicationComponent.create()

}

