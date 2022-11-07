package com.prasanth.app.poc.androidhttpserver.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import com.prasanth.app.poc.androidhttpserver.databinding.ActivityMainBinding
import com.prasanth.app.poc.androidhttpserver.utils.MyConstants
import com.prasanth.app.poc.androidhttpserver.viewmodels.MainActivityViewModel
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "pbLog: ${javaClass.simpleName}"

    private lateinit var binder: ActivityMainBinding
    private val mainActivityVM by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding the view
        binder = ActivityMainBinding.inflate(layoutInflater)
        // Attaching assets to VM
        mainActivityVM.assets = assets

        /*
            Listening to Live data
        */
        // Listening for connectivity Errors
        mainActivityVM.networkErrorsListener.observe(this) {
            if (it) {
                // When the error occured
                binder.amServerAddress.text = "Sever Stopped"
                mainActivityVM.stopServer()
            }
        }

        /*
            Handling Interactions
         */
        // Handle clicking on start button
        binder.amStartButton.setOnClickListener {
            val thisButton = it as Button
            try{

                if(!mainActivityVM.isServerRunning){
                    // Starting the server
                    val ipAddress = getIPAddress(true)
                    mainActivityVM.startServer(ipAddress, MyConstants.PORT)

                    // Updating text
                    binder.amServerAddress.text = "$ipAddress:${MyConstants.PORT}"
                    thisButton.text = "Stop"
                }
                else{
                    // Stopping the server
                    mainActivityVM.stopServer()

                    // Updating text
                    binder.amServerAddress.text = "Sever Stopped"
                    thisButton.text = "Start"
                }
            }
            catch (ex : Exception){
                Log.e(TAG, ex.stackTraceToString())
            }
        }

        setContentView(binder.root)
    }


    /*
    Private Methods
     */
    private fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr: String = addr.hostAddress as String
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                    0,
                                    delim
                                ).uppercase(
                                    Locale.getDefault()
                                )
                            }
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
            return ""
        } // for now eat exceptions
        return ""
    }
}