package dev.shira.anime.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val ANIMEX_BASE_URL = "https://wincamp.web.id/"
    private const val ANIMEX_DATA_AGENT = "AnimeXNonton 26.9.5/18"
    private const val ANIMEX_USER_AGENT = "okhttp/5.5.0"

    private const val ANIMEKU_DATA_AGENT = "Your Videos Channel"
    private const val ANIMEKU_USER_AGENT = "Dalvik/7.1.12.1.0 (com.newanimeku.animechanneldonghuasubindosubenglish U; Android ; 20175 Build/NMF260)"
    private const val ANIMEKU_ACCEPT = "application/vnd.yourapi.v1.full+json"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val animexHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Cache-Control", "max-age=0")
                .header("Data-Agent", ANIMEX_DATA_AGENT)
                .header("User-Agent", ANIMEX_USER_AGENT)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    private val animekuHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Cache-Control", "max-age=0")
                .header("Data-Agent", ANIMEKU_DATA_AGENT)
                .header("User-Agent", ANIMEKU_USER_AGENT)
                .header("Accept", ANIMEKU_ACCEPT)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    private val animexRetrofit = Retrofit.Builder()
        .baseUrl(ANIMEX_BASE_URL)
        .client(animexHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val animekuRetrofit = Retrofit.Builder()
        .baseUrl(AnimekuApiService.BASE_URL)
        .client(animekuHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val animeApiService: AnimeApiService = animexRetrofit.create(AnimeApiService::class.java)
    val animekuApiService: AnimekuApiService = animekuRetrofit.create(AnimekuApiService::class.java)
}
