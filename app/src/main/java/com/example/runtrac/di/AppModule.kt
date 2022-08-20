package com.example.runtrac.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runtrac.db.RunningDatabase
import com.example.runtrac.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runtrac.other.Constants.KEY_NAME
import com.example.runtrac.other.Constants.KEY_WEIGHT
import com.example.runtrac.other.Constants.RUNNING_DATABASE_NAME
import com.example.runtrac.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
//manuals or functions that provide dependencies for us are stored here.
@Module
//we tell our app to install this object inside our applicationComponent class.and decides the creation and destroying time of the dependencies
@InstallIn(ApplicationComponent::class)
object AppModule {
   //only a single instance will be created of the dependencies at a time.
    @Singleton
    @Provides   //tells that the result of the function can be used to create other dependencies&can be used to be injected in other classes
    fun provideRunningDatabase(
        @ApplicationContext app:Context
    )= Room.databaseBuilder(
        app,RunningDatabase::class.java,RUNNING_DATABASE_NAME
    ).build()
    @Singleton
    @Provides
    fun provideRunDao(db:RunningDatabase)=db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app:Context){
        app.getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE)
    }
    @Singleton
    @Provides
    fun provideName(sharedPref:SharedPreferences)=sharedPref.getString(KEY_NAME,"")?:""

    @Singleton
    @Provides
    fun provideWeight(sharedPref:SharedPreferences)=sharedPref.getFloat(KEY_WEIGHT,80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref:SharedPreferences)=sharedPref.getBoolean(
        KEY_FIRST_TIME_TOGGLE,true)
}