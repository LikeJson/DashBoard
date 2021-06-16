package com.dashboard.kotlin.clashhelper

import android.util.Log
import android.view.View
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class clashStatus {
    var trafficThreadFlag: Boolean = true
    var trafficRawText: String = "{\"up\":\"0\",\"down\":\"0\"}"


    fun runStatus(): Boolean {


        return true
    }

    fun getTraffic() {
        trafficThreadFlag = true
        Thread{
            var conn: HttpURLConnection? =null
            try {
                conn = URL("${clashConfig().baseURL}/traffic").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.inputStream.use {
                    while (trafficThreadFlag){
                        trafficRawText = it.bufferedReader().readLine()
                        Log.d("TRAFFIC", trafficRawText)
                    }
                }
            }catch (ex: Exception){
                Log.w("W",ex.toString())
            }
        }.start()
    }
    fun stopGetTraffic() {
        trafficThreadFlag = false
    }

}

private class clashConfig{

    val baseURL: String
    get() {
        return "${getUrl()}:${getPort()}"
    }



    private fun getUrl(): String{ return "http://127.0.0.1"}
    private fun getPort(): String {
        return "9090"
    }



}