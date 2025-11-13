package com.dharidodi.weatherapp


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApi {

    // -----------------------------------------------------------------------------------------
    // 🌤️ Forecast API – Fetch weather data based on latitude & longitude
    // EN: Retrieves current and daily forecast data from Open-Meteo API.
    //     - "current": temperature, humidity, weather code
    //     - "daily":   max/min temperature and daily weather code
    // AR: جلب بيانات الطقس الحالية واليومية باستخدام إحداثيات الموقع.
    // -----------------------------------------------------------------------------------------

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,     // EN: Device latitude    | AR: خط العرض
        @Query("longitude") lon: Double,    // EN: Device longitude   | AR: خط الطول

        // EN: Fields to include in "current weather" section
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code",

        // EN: Fields for daily forecast (high/low temps + weather code)
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code",

        // EN: Automatically detect timezone based on location
        @Query("timezone") tz: String = "auto"
    ): WeatherResponse


    // -----------------------------------------------------------------------------------------
    // 🗺️ Geo API – Convert city name → latitude & longitude
    // EN: Searches for a city name and returns geographical coordinates.
    // AR: تحويل اسم المدينة إلى إحداثيات (لتحديد الموقع والطقس).
    // -----------------------------------------------------------------------------------------

    @GET("v1/search")
    suspend fun getGeoLocation(@Query("name") name: String): GeoResponse


    companion object {

        // -------------------------------------------------------------------------------------
        // 🌦️ Create main Weather API client (forecast)
        // EN: Builds Retrofit instance with logging enabled (for debugging network requests).
        // AR: إنشاء كائن API خاص بالطقس مع تمكين تسجيل الطلبات.
        // -------------------------------------------------------------------------------------
        fun create(): WeatherApi {

            // EN: Interceptor to print basic HTTP logs (request/response info)
            // AR: أداة لتسجيل معلومات أساسية عن طلبات HTTP.
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            // EN: OkHttp client with logging support
            // AR: كائن OkHttp يحتوي على مسجل الطلبات.
            val client = OkHttpClient.Builder().addInterceptor(logger).build()

            // EN: Retrofit builder for the weather forecast API
            // AR: تهيئة Retrofit واختيار عنوان الـ API الخاص بالطقس.
            return Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")  // EN: Base URL for weather API
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) // EN: JSON → Kotlin models
                .build()
                .create(WeatherApi::class.java)
        }


        // -------------------------------------------------------------------------------------
        // 🗺️ Create Geo API client (search by city name)
        // EN: Retrofit instance for geolocation API (city → coordinates).
        // AR: إنشاء API لتحويل اسم المدينة إلى إحداثيات.
        // -------------------------------------------------------------------------------------
        fun createGeo(): WeatherApi {

            // EN: Simple OkHttp client without logging (lighter)
            // AR: كائن بسيط بدون تسجيل لطلبات الشبكة.
            val client = OkHttpClient.Builder().build()

            return Retrofit.Builder()
                .baseUrl("https://geocoding-api.open-meteo.com/") // EN: Base URL for geo API
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApi::class.java)
        }
    }
}
