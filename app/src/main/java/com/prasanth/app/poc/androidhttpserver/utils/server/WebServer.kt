package com.prasanth.app.poc.androidhttpserver.utils.server

import android.util.Log
import com.prasanth.app.poc.androidhttpserver.viewmodels.MainActivityViewModel
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class WebServer(private val homeActivityVM: MainActivityViewModel) {
    private val TAG = "pLog: " + javaClass.simpleName

    private lateinit var receiveThread: Thread

    //Socket's End point at server
    private lateinit var serverSocket: ServerSocket

    fun stoop() {
        serverSocket.close()
    }

    fun start(ipAddress: String, port: Int) {
        homeActivityVM.isServerRunning = true


        serverSocket = ServerSocket()
        /*
        Enable/disable the SO_REUSEADDR  socket option.
        When a TCP connection is closed the connection may remain in a timeout state for a period of time after the connection is closed
        (typically known as the TIME_WAIT state or 2MSL wait state).
        For applications using a well known socket address or port it may not be possible to bind a socket to the required SocketAddress
        if there is a connection in the timeout state involving the socket address or port.
        Enabling SO_REUSEADDR  prior to binding the socket using bind(java.net.SocketAddress)  allows the socket to be bound even though a previous connection is in a timeout state.
        When a ServerSocket is created the initial setting of SO_REUSEADDR  is not defined. Applications can use getReuseAddress()  to determine the initial setting of SO_REUSEADDR .
        The behaviour when SO_REUSEADDR  is enabled or disabled after a socket is bound (See isBound() ) is not defined.
        Parameters
        on
        boolean: whether to enable or disable the socket option

        If reuseAddress set to false
        It will only connect 1st time with the address
        second time (i.e., reloding the web site or reopeningg the app wll make the app terminate and unable to opn
         */
        serverSocket.reuseAddress = true

        /*
        Binds the ServerSocket to a specific address (IP address and port number).
        If the address is null, then the system will pick up an ephemeral port and a valid local address to bind the socket.
        Parameters
        endpoint
        SocketAddress: The IP address and port number to bind to.
         */
        serverSocket.bind(InetSocketAddress(ipAddress, port))

        receiveThread = thread(start = true) {
            // It will get called at server start
            // Must and should called in thread / Job
            // If not the it blocks the Main thread and app will get crashed

            //Log.d(DEBUG_TAG, "calling listenerThread()")
            this@WebServer.listenerThread()
        }
    }

    private fun listenerThread() {
        /*
        Without this client will find the server but won't get response from serverand keep on waiting(loading)

        [ Called only once when Web Server is started]
         */
        //Log.d(DEBUG_TAG, "listenerThread called")
        while (homeActivityVM.isServerRunning) {
//                Log.d(TAG, "in while")
            try {
                // Called 1 time when server is created
                // and called 2 times client send request to server
                // Here we create client socket by connecting the server socket and keep on opening it
                /*
                     If we run this block once we can only call it once with client after it gets response the connection is closed and can't call from client again.
                     So we use while loop to call multiple times from client
                 */
                /*
                    Listens for a connection to be made to this socket and accepts it. The method(accept) blocks until a connection is made.
                    A new Socket s is created.
                 */
                val socket: Socket =
                    serverSocket.accept() // Socket is created between client and server

//                    Log.d(TAG, "request received from client")

                // After getting request from client it will send the response
                val clientSocketHandler = ClientSocketHandler(socket, homeActivityVM)
                clientSocketHandler.respondAsync()
            } catch (ioEx: IOException) {
                Log.e(TAG, ioEx.stackTraceToString())
                // Sending server stop signal
                homeActivityVM.networkErrorsListener.postValue(true)
                break;
            }
        }
    }
}