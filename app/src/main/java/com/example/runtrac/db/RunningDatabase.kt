package com.example.runtrac.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Run::class],version = 1)
//telling the class that the converters are present in the converters class
//dagger is the lib for dependency(kotlin object or class is dependent on some object or var) injection.
//it allows us to store the necessary vars at a central place through which we can inject it in the class we need it in.
//it also allows us to provide scope for our objects.
//Injection with dagger happens in compile time to improve performance.recommended by google.
@TypeConverters(Converters::class)
abstract class RunningDatabase:RoomDatabase() {
    abstract fun getRunDao():RunDAO

}