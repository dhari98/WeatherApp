package com.dharidodi.weatherapp

class WeatherRepository(private val api: WeatherApi) {

    // -----------------------------------------------------------------------------------------
    // 🌍 Load weather data using coordinates (latitude & longitude)
    // EN: Fetches the full weather forecast (current + daily) from Open-Meteo API.
    // AR: جلب بيانات الطقس كاملة باستخدام إحداثيات الموقع.
    // -----------------------------------------------------------------------------------------
    suspend fun loadCity(lat: Double, lon: Double) = api.getForecast(lat, lon)



    // -----------------------------------------------------------------------------------------
    // 🔍 Convert city name → coordinates (latitude & longitude)
    // EN: Searches for the city using the Geo API and returns the first matching result.
    //     If found, returns a City object containing: name, lat, lon.
    // AR: البحث عن مدينة وإرجاع أول نتيجة تحتوي على الاسم مع الإحداثيات.
    // -----------------------------------------------------------------------------------------
    suspend fun getCoordinatesByCity(city: String): City? {

        // EN: Call Geo API (create new instance because geo uses a different base URL)
        // AR: الاتصال بواجهة API الخاصة بالجغرافيا (تختلف عن API الطقس).
        val geoResponse = WeatherApi.createGeo().getGeoLocation(city)

        // EN: Take the first result from the API list (if exists)
        // AR: أخذ أول نتيجة للمدينة من القائمة.
        val first = geoResponse.results?.firstOrNull()

        // EN: Convert API result into our 'City' model
        // AR: تحويل نتيجة API إلى كائن City الخاص بالتطبيق.
        return first?.let { City(it.name, it.latitude, it.longitude) }
    }
}
