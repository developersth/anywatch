package com.anydev.anywatch.viewmodel

import com.starmax.net.NetConstant
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object TestRepository {
    fun test(text: String){
        Thread(object:Runnable{
            override fun run() {
                sendPost("${NetConstant.BaseApi}/check_connect",text,"")
            }
        }).start();
    }

    fun sendPost(urlStr: String?, dataStr: String, paramsStr: String?): String? {
        var result: String? = ""
        try {
            val data = dataStr.toByteArray(charset("UTF-8"))
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.doInput = true
            conn.useCaches = false
            conn.requestMethod = "POST"
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("Charset", "UTF-8")
            conn.setRequestProperty("Content-Length", data.size.toString())
            conn.setRequestProperty("Content-Type", "text/xml")
            conn.connect()
            val out: OutputStream = conn.outputStream
            out.write(data)
            out.flush()
            out.close()
            println(conn.responseCode)
            if (conn.responseCode == 200) {
                println("connection success")
                val `in`: InputStream = conn.inputStream
                val data1: ByteArray = readInputStream(`in`)!!
                result = String(data1)
            } else {
                println("Connection failed")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }

    @Throws(java.lang.Exception::class)
    fun readInputStream(inStream: InputStream): ByteArray? {
        val outStream = ByteArrayOutputStream()
        val buffer = ByteArray(10240)
        //The length of the string read each time. If it is -1, it means that all readings have been completed.
        var len = 0
        while (inStream.read(buffer).also { len = it } != -1) {
            outStream.write(buffer, 0, len)
        }
        inStream.close()
        return outStream.toByteArray()
    }
}
