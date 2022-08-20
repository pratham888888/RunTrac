package com.example.runtrac.db
//this is a data class which will form an entity in our database.
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="running_table")
data class Run(var image:Bitmap?=null,var timeStamp:Long=0L,//timestamp used to show time when our run was
               var avgSpeedInKMH:Float=0f,var distanceInMeters:Int=0,
               var timeInMillis:Long=0L ,//this shows length of our run
               var caloriesBurned:Int=0
               ) {
    @PrimaryKey(autoGenerate = true)
    var id:Int?=null
}