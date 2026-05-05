package id.pbbku.mobileportal.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import id.pbbku.mobileportal.BuildConfig
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object SimpbbApiClient {
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    fun create(
        baseUrl: String = SimpbbApiConfig.BASE_URL,
        debug: Boolean = BuildConfig.DEBUG,
    ): SimpbbApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = loggingLevelFor(debug)
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(SensitiveHeaderInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SimpbbApiService::class.java)
    }

    internal fun loggingLevelFor(debug: Boolean): HttpLoggingInterceptor.Level {
        return if (debug) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}
