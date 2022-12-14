package com.example.runtrac.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.example.runtrac.services.PolyLine
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {
    fun hasLocationPermissions(context: Context)=if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
        EasyPermissions.hasPermissions(
            context,Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }else{
        //In Android Q(10), due to restrictions on background location updates, there is a new permission
            // android.Manifest.permission.ACCESS_BACKGROUND_LOCATION that we need to ask the user to access location
                // while app is in background.
        EasyPermissions.hasPermissions(
            context,Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION )
    }
    fun calculatePolyLineLength(polyline:PolyLine):Float{
        var distance= 0f
        for(i in 0..polyline.size-2){
            val pos1= polyline[i]
            val pos2= polyline[i+1]
            val result= FloatArray(1)
            Location.distanceBetween(pos1.latitude,
                pos1.longitude,pos2.latitude,
                pos2.longitude,result)
            distance+=result[0]
        }
        return distance
    }
    fun getFormattedStopWatchTime(ms:Long,includeMillis:Boolean = false):String{
        var milliseconds= ms
        val hours= TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds-=TimeUnit.HOURS.toMillis(hours)
        val minutes= TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds-= TimeUnit.MINUTES.toMillis(minutes)
        val seconds= TimeUnit.MILLISECONDS.toSeconds(milliseconds)
      if(!includeMillis){
          return "${if(hours<10)"0" else ""}$hours:"+
          "${if(minutes<10)"0" else ""}$minutes:"+
                  "${if(seconds<10)"0" else ""}$seconds"
      }
        milliseconds-=TimeUnit.SECONDS.toMillis(seconds)
        milliseconds/=10
        return  "${if(hours<10)"0" else ""}$hours:"+
                "${if(minutes<10)"0" else ""}$minutes:"+
                "${if(seconds<10)"0" else ""}$seconds:"+
                "${if(milliseconds<10)"0" else ""}$milliseconds"
    }
}