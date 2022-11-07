package com.prasanth.app.poc.androidhttpserver.utils.server

import android.util.Log
import com.prasanth.app.poc.androidhttpserver.viewmodels.MainActivityViewModel
import java.io.*
import java.net.Socket
import kotlin.concurrent.thread


class ClientSocketHandler(
    private val establishedSocket: Socket,
    private val mainActivityVM: MainActivityViewModel
) {

    private val TAG = "pLog: " + javaClass.simpleName

    private var responseThread: Thread? = null

    init {
        //Log.d(DEBUG_TAG, "ClientSocketHandler constructor called")
        // Creating Response thread
        this.responseThread = thread(start = false) {
            try {
                this.respond()
            } catch (ex: Exception) {
                Log.e("$TAG download interrupted", ex.stackTraceToString())
            }
        }
    }

    fun respondAsync() {
        // Now response thread is started
        this.responseThread?.start()
    }

    // Called twice when client sends request
    private fun respond() {
        val socketInputStream =
            establishedSocket.getInputStream()    // Stream sent from client to server
        val socketOutputStream =
            establishedSocket.getOutputStream()  // Stream which will be sent to client from server
        var requestReceived = false

        //Log.d(DEBUG_TAG, "${inputStream.available()} $requestReceived")
        while (socketInputStream.available() > 0 && !requestReceived) {
            val requestLine = socketInputStream.bufferedReader().readLine()
            Log.i(TAG, "Request Line: $requestLine}")
            if (processRequestLine(requestLine)) {
                requestReceived = true;
            }
        }

        /*
        Sending response by writing to socket's output stream
         */
        // Preparing Response
        val sb: StringBuilder = StringBuilder()
        val string = ipsToStr(mainActivityVM.assets.open("index/index.html"))   //Opening file from asset
        sb.appendLine(string)   //string contains file(html, css, js) content
        sb.appendLine()
        val responseString = sb.toString()
        val responseBytes = responseString.toByteArray(Charsets.UTF_8)
        val responseSize = responseBytes.size

        // Preparing Response Header
        val sbHeader = StringBuilder()
        sbHeader.appendLine("HTTP/1.1 200 OK")
        sbHeader.appendLine("Content-Type: text/html")
        sbHeader.append("Content-Length: ")
        sbHeader.appendLine(responseSize)
        sbHeader.appendLine()

        val responseHeaderString = sbHeader.toString()
        val responseHeaderBytes = responseHeaderString.toByteArray(Charsets.UTF_8)

        socketOutputStream.write(responseHeaderBytes)
        socketOutputStream.write(responseBytes)
        socketOutputStream.flush()
        socketOutputStream.close()    //Closes the socket
        Log.d(TAG, "Response sent")

    }

    private fun processRequestLine(requestLine: String): Boolean {
        if (requestLine == "") {
            return true
        }
        return false
    }

    private fun ipsToStr(ipS: InputStream): String {
        return try {
            val size: Int = ipS.available()
            val buffer = ByteArray(size)
            ipS.read(buffer)
            String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
            "IO Exception"
        }
    }
}