package com.example.runtrac.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runtrac.R
import com.example.runtrac.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runtrac.other.Constants.KEY_NAME
import com.example.runtrac.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment:Fragment(R.layout.fragment_setup) {
    @Inject
    lateinit var sharedPref:SharedPreferences
    @set:Inject //boolean is a primitive data type thats why used set:Inject instead of inject
    var isFirstAppOpen= true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!isFirstAppOpen){
            val navOptions= NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)//we remove the setup activity from
        // back stack so that we do not nav to it even after pressing back button once we have already setup our profile
                .build()
            findNavController().navigate(R.id.action_setupFragment_to_runFragment,savedInstanceState,navOptions)

        }
        tvContinue.setOnClickListener {
            val success= writePersonalDataToSharedPref()
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }
            Snackbar.make(requireView(),"Please enter values in all the fields",Snackbar.LENGTH_SHORT).show()
        }
    }
    private fun writePersonalDataToSharedPref():Boolean{
        val name= etName.text.toString()
        val weight = etWeight.text.toString()
        if(name.isEmpty()||weight.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply() //apply is asynchronous while commit is synchronous
        val toolbarText= "Let's go $name!"
        requireActivity().tvToolbarTitle.text= toolbarText
        return true
    }
}