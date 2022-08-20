package com.example.runtrac.repositories

import com.example.runtrac.db.Run
import com.example.runtrac.db.RunDAO
import javax.inject.Inject

//to provide functions of our database using dao object by injecting it
//In mvvm architecture job of repository is to collect the data from all the data sources .here we have room database as data source.
// had there been an api then data from api would also have been collected here
class MainRepository @Inject constructor(
    val runDao:RunDAO
){
    suspend fun insertRun(run: Run)= runDao.insertRun(run)
    suspend fun deleteRun(run: Run)= runDao.deleteRun(run)
    //didnt use suspend here bcoz this fun returns live data which is done inside a coroutine anyway
    fun getAllRunsSortedByDate()= runDao.getAllRunsSortedByDate()
    fun getAllRunsSortedByDistance()= runDao.getAllRunsSortedByDistance()
    fun getAllRunsSortedByTimeInMillis()= runDao.getAllRunsSortedByTimeInMillis()
    fun getAllRunsSortedByAvgSpeed()= runDao.getAllRunsSortedByAvgSpeed()
    fun getAllRunsSortedByCaloriesBurned()= runDao.getAllRunsSortedByCaloriesBurned()
    fun getTotalAvgSpeed()= runDao.getTotalAvgSpeed()
    fun getTotalDistance() = runDao.getTotalDistance()
    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalTimeInMillis()= runDao.getTotalTimeInMillis()
}