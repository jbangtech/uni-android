package com.jbangit.unimini.network

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * create by erolc on 23/02/2021
 *
 * api管理类 retrofit 处理api入口
 * 在仓库或者是applicatin对方法中对retrofit进行重新调整，比如拦截器，比如字段样式等等，可以在application中加入统一的处理
 * 通过方法[makeOkHttpClient]和[makeRetrofit]。
 * 需要注意的是：某些设置是可以叠加的，比如拦截器，所以设置几个是没有问题的，但是有些设置是不能叠加的，比如超时，需要注意不能叠加的设置会被覆盖掉
 */
object ApiManager {

    private var retrofitBuilderBodys: MutableList<(Retrofit.Builder.() -> Unit)?> = mutableListOf()
    private var clientBodys: MutableList<(OkHttpClient.Builder.(Context) -> Unit)?> =
        mutableListOf()
    private var clientBuilder: OkHttpClient.Builder? = null
    private var builder: Retrofit.Builder? = null


    private var interceptors: MutableList<(() -> Interceptor)?> = mutableListOf()

    private val retrofitMap = mutableMapOf<String, Retrofit>()


    private fun initInterceptors(context: Context): Array<Interceptor> {
        return arrayOf()
    }

    fun <T> build(context: Context, url: String, clazz: Class<T>): T {
        val retrofit = init(context.applicationContext, url)
        return retrofit.create(clazz)
    }

    private fun init(context: Context, url: String): Retrofit {
        if (retrofitMap.containsKey(url)) {
            return retrofitMap[url]!!
        }
        if (clientBuilder == null) {
            clientBuilder = OkHttpClient.Builder()
            initClient(clientBuilder!!, context)
        }

        if (builder == null) {
            builder = Retrofit.Builder()
            builder?.apply {
                initRetrofit(this)
                client(clientBuilder!!.build())
            }
        }
        builder?.baseUrl(url)
        retrofitMap[url] = builder!!.build()
        return retrofitMap[url]!!
    }

    private fun initClient(clientBuilder: OkHttpClient.Builder, context: Context) {
        val interceptors = initInterceptors(context)
        for (inter in interceptors) {
            clientBuilder.addInterceptor(inter)
        }
        this.interceptors.forEach {
            it?.invoke()?.apply {
                clientBuilder.addInterceptor(this)
            }
        }
        clientBuilder.sslSocketFactory(createSSLSocketFactory(), TrustAllCerts())
        clientBuilder.connectTimeout(10, TimeUnit.SECONDS) //网络连接10秒超时
        clientBuilder.readTimeout(30, TimeUnit.SECONDS) //读取数据30秒超时
        clientBodys.forEach {
            it?.invoke(clientBuilder, context)
        }
    }

    internal fun initClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        this.interceptors.forEach {
            it?.invoke()?.apply {
                clientBuilder.addInterceptor(this)
            }
        }
        clientBuilder.sslSocketFactory(createSSLSocketFactory(), TrustAllCerts())

        clientBuilder.connectTimeout(10, TimeUnit.SECONDS) //网络连接10秒超时
        clientBuilder.readTimeout(30, TimeUnit.SECONDS) //读取数据30秒超时
        return clientBuilder.build()
    }

    private fun initRetrofit(builder: Retrofit.Builder) {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        builder.addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
        retrofitBuilderBodys.forEach {
            it?.invoke(builder)
        }
    }

    /**
     * 自定义retrofit
     */
    fun makeRetrofit(body: Retrofit.Builder.() -> Unit) {
        this.retrofitBuilderBodys.add(body)
    }

    /**
     * 自定义OkHttpClient
     */
    fun makeOkHttpClient(clientBody: (OkHttpClient.Builder.(Context) -> Unit)) {
        this.clientBodys.add(clientBody)
    }

    /**
     * 增加额外的拦截器
     */
    fun addInterceptor(interceptor: () -> Interceptor) {
        interceptors.add(interceptor)
    }


    private class TrustAllCerts : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            return arrayOfNulls(0)
        }
    }

    private fun createSSLSocketFactory(): SSLSocketFactory? {
        var sSLSocketFactory: SSLSocketFactory? = null
        try {
            val sc: SSLContext = SSLContext.getInstance("TLS")
            sc.init(
                null, arrayOf<TrustManager>(TrustAllManager()),
                SecureRandom()
            )
            sSLSocketFactory = sc.getSocketFactory()
        } catch (ignored: Exception) {
        }
        return sSLSocketFactory
    }

    private class TrustAllManager : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }


}