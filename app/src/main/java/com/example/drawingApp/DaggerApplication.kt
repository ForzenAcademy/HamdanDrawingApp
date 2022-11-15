package com.example.drawingApp

import android.app.Application
import com.example.drawingApp.di.MainActivityModule
import dagger.Component
import dagger.Module

@Component(modules = [ApplicationModule::class, MainActivityModule::class])
interface ApplicationComponent {
    fun inject(activity: MainActivity)
}

@Module
class ApplicationModule {

}

class DaggerApplication : Application() {
    val appComponent = DaggerApplicationComponent.create()

}

