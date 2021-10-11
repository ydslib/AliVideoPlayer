package com.yds.aliyunvideocommon.okhttp

import android.os.Handler
import android.os.Looper
import com.yds.aliyunvideocommon.okhttp.interceptor.LoggingInterceptor
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

object AlivcOkHttpClient {

    private var okHttpClient: OkHttpClient? = null

    private val okHttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder().addNetworkInterceptor(
        LoggingInterceptor()
    )

    private var handler: Handler = Handler(Looper.getMainLooper())

    private fun build() {
        okHttpBuilder.connectTimeout(10, TimeUnit.SECONDS)
        okHttpBuilder.writeTimeout(10, TimeUnit.SECONDS)
        okHttpBuilder.readTimeout(10, TimeUnit.SECONDS)
        okHttpClient = okHttpBuilder.build()
    }

    class StringCallback(var httpCallback: HttpCallback?, var request: Request) : Callback {

        override fun onFailure(call: Call, e: IOException) {
            httpCallback?.let {
                handler.post {
                    it.onError(request, e)
                }
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val result = response.body?.string() ?: ""
            try {
                val jo = JSONObject(result)
                if ("200" == jo.getString("code")) {
                    httpCallback?.also {
                        handler.post {
                            it.onSuccess(request, result)
                        }
                    } ?: kotlin.run {
                        handler.post {
                            //
                        }
                    }
                } else {
                    httpCallback?.let {
                        handler.post {
                            it.onError(request, IOException(jo.getString("message")))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    interface HttpCallback {
        fun onError(request: Request, e: IOException)

        fun onSuccess(request: Request, result: String)
    }

    fun formBody(params: Map<String, String>?): FormBody {
        val builder = FormBody.Builder()
        params?.let {
            val keys = params.keys
            if (keys.isNotEmpty()) {
                keys.forEach { k ->
                    val value = params[k]
                    value?.run {
                        builder.add(k, this)
                    }
                }
            }
        }
        return builder.build()
    }

    fun urlWithParam(url: String, params: Map<String, String>?): String {
        params?.let {
            val keys = params.keys
            if (keys.isNotEmpty()) {
                val sb = StringBuilder()
                var needAnd = false
                keys.forEach {
                    if (needAnd) {
                        sb.append("&")
                    }
                    sb.append(it).append("=").append(params[it])
                    needAnd = true
                }
                return "$url?${sb.toString()}"
            }
        }
        return url
    }

    fun get(url: String, httpCallback: HttpCallback?) {
        val request = Request.Builder().url(url).build()
        okHttpClient?.newCall(request)
            ?.enqueue(StringCallback(request = request, httpCallback = httpCallback))
    }

    /**
     * 带参数带get请求
     */
    fun get(url: String, params: HashMap<String, String>?, httpCallback: HttpCallback?) {
        val request = Request.Builder().url(urlWithParam(url, params)).build()
        okHttpClient?.newCall(request)
            ?.enqueue(StringCallback(request = request, httpCallback = httpCallback))
    }


    fun post(url: String, params: Map<String, String>?, httpCallback: HttpCallback?) {
        val request = Request.Builder().url(url).post(formBody(params)).build()
        okHttpClient?.newCall(request)
            ?.enqueue(StringCallback(request = request, httpCallback = httpCallback))
    }


}