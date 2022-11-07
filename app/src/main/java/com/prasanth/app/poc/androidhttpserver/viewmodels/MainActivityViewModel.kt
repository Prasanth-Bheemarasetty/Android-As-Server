package com.prasanth.app.poc.androidhttpserver.viewmodels

import android.content.res.AssetManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prasanth.app.poc.androidhttpserver.utils.server.WebServer

class MainActivityViewModel : ViewModel() {
    private val TAG = "pLog: ${javaClass.simpleName}"

    /*
    Livedata
     */
    val networkErrorsListener = MutableLiveData<Boolean>()

    /*
    Public Members
     */
    // Holds asset
    lateinit var assets : AssetManager
    // Indicates whether the server is running or not
    var isServerRunning = false

    /*
    Private Members
     */
    // Webserver instance
    private lateinit var webServer: WebServer

    /*
    Public Methods
     */
    // Start the server in Mobile
    fun startServer(ipAddress : String, port : Int){
        Log.d(TAG, "Starting Server")

        webServer = WebServer(this)
        /*
        To Start the server in current LAN with port address
        If we don't start the server, the client will not find the server at given port in LAN
         */
        webServer.start(ipAddress, port)
        isServerRunning = true
    }

    // Stopping the server
    fun stopServer(){
        webServer.stoop()
        isServerRunning = false
    }

    /*
    Private Methods
     */
}