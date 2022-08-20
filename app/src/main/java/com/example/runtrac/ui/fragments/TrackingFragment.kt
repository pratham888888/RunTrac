package com.example.runtrac.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.ContentProviderCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runtrac.R
import com.example.runtrac.db.Run
import com.example.runtrac.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runtrac.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtrac.other.Constants.ACTION_STOP_SERVICE
import com.example.runtrac.other.Constants.MAP_ZOOM
import com.example.runtrac.other.Constants.POLYLINE_COLOR
import com.example.runtrac.other.Constants.POLYLINE_WIDTH
import com.example.runtrac.other.TrackingUtility
import com.example.runtrac.services.PolyLine
import com.example.runtrac.services.TrackingService
import com.example.runtrac.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.security.AccessController
import java.util.*
import javax.inject.Inject
import kotlin.math.round
const val CANCEL_TRACKING_DIALOG_TAG= "CancelDialog"
@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()
    private var isTracking= false
    private var pathPoints= mutableListOf<PolyLine>()
    private var map:GoogleMap?= null
   private var curTimeInMillis= 0L
    private var menu: Menu?= null
    @set:Inject
     var weight= 58f
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(
            savedInstanceState
        )
        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        if(savedInstanceState!=null){
            val cancelTrackingDialog =parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }
            //to load map
        mapView.getMapAsync{
            map=it
            addAllPolyLines()
        }
     subscribeToObserve()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)

    }
    private fun subscribeToObserve(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer { updateTracking(it) })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints=it
            addLatestPolyline()
            moveCameraToUser()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer { curTimeInMillis=it
        val formattedTime= TrackingUtility.getFormattedStopWatchTime(curTimeInMillis,true)
            tvTimer.text= formattedTime
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu= menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis>0L){
            this.menu?.getItem(0)?.isVisible= true
        }
    }

    private fun showCancelTrackingDialog(){
       CancelTrackingDialog().apply {
           setYesListener {
               stopRun()
           }
       }.show(parentFragmentManager,CANCEL_TRACKING_DIALOG_TAG)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking ->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)

    }
    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible=true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }
    private fun updateTracking(isTracking:Boolean){
        this.isTracking=isTracking
        if(!isTracking&&curTimeInMillis>0L){
            btnToggleRun.text= "Start"
            btnFinishRun.visibility= View.VISIBLE
        }else if(isTracking){
            btnToggleRun.text= "Stop"
            menu?.getItem(0)?.isVisible=true
            btnFinishRun.visibility= View.GONE
        }
    }
    private fun stopRun(){
        tvTimer.text= "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }
    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty()&&pathPoints.last().isNotEmpty()){
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                pathPoints.last().last(), MAP_ZOOM
            ))
        }
    }
    private fun zoomToSeeWholeTrack(){
        val bounds= LatLngBounds.builder()
        for(polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(bounds.build()
                ,mapView.width,
                mapView.height,(mapView.height*0.05f).toInt())
        )
    }
    private fun endRunAndSaveToDb(){
        map?.snapshot {
            bmp ->
            var distanceInMeter =0
            for(polyline in pathPoints){
                distanceInMeter+= TrackingUtility.calculatePolyLineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeter/1000f) *(curTimeInMillis/1000f/60/60)*10)/10f
            val dateTimeStamp= Calendar.getInstance().timeInMillis
            val caloriesBurned= ((distanceInMeter/1000f)*weight).toInt()
            val run = Run(bmp,dateTimeStamp,avgSpeed,distanceInMeter,curTimeInMillis,caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(requireActivity().findViewById(R.id.rootView),"Run saved successfully",Snackbar.LENGTH_LONG).show()
           stopRun()
        }
    }
    private fun addAllPolyLines(){
        for(polyline in pathPoints){
            val polylineOptions =PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
                map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty()&&pathPoints.last().size>1){
            val preLastLatLng= pathPoints.last()[pathPoints.last().size-2]
            val lastLatLng= pathPoints.last().last()
            val polylineOptions =PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
                map?.addPolyline(PolylineOptions())

        }
    }
    private fun sendCommandToService(action:String)= Intent(
        requireContext(), TrackingService::class.java).also {
        it.action=action
        requireContext().startService(it)
    }
//overriding all the lifecycle functions for the map.
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
   override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}