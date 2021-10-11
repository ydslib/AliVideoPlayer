package com.yds.aliyunvideocommon.okhttp.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class LoggingInterceptor : Interceptor {
    private val TAG = javaClass.simpleName
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        Log.d(TAG, "Sending request: ${request.url} \n ${request.headers}")
        val response = chain.proceed(request)
        val t2 = System.nanoTime()
        Log.d(
            TAG, "Received response for ${response.request.url}  in " +
                    "${(t2 - t1) / 1e6} ms\n ${response.headers}"
        )
        return response
    }
}