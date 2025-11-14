package com.example.weatherapp


import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.util.Calendar



class WeatherViewModel : ViewModel() {

    // -----------------------------------------------------------------------------------------
    // Repository instance (Weather API client)
    // EN: The ViewModel uses the repository to load weather & geolocation data.
    // AR: الـ ViewModel يستخدم الـ Repository لجلب بيانات الطقس والجغرافيا.
    // -----------------------------------------------------------------------------------------
    private val repo = WeatherRepository(WeatherApi.create())


    // -----------------------------------------------------------------------------------------
    // LiveData for exposing weather results to the UI
    // EN: _state is mutable (internal), state is read-only (public).
    // AR: _state متغيّر داخلي، و state متغيّر للقراءة فقط للواجهة.
    // -----------------------------------------------------------------------------------------
    private val _state = MutableLiveData<Result<WeatherResponse>>()
    val state: LiveData<Result<WeatherResponse>> = _state


    // -----------------------------------------------------------------------------------------
    // 🌍 Fetch weather using latitude & longitude
    // EN: Calls the repository → updates LiveData with success or failure.
    // AR: جلب الطقس باستخدام الإحداثيات وتحديث الـ LiveData بالنتيجة.
    // -----------------------------------------------------------------------------------------
    fun fetch(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val data = repo.loadCity(lat, lon)
                _state.postValue(Result.success(data))
            } catch (e: Exception) {
                _state.postValue(Result.failure(e))
            }
        }
    }


    // -----------------------------------------------------------------------------------------
    // 🔍 Fetch weather by city name
    // EN: 1) Convert city name → coordinates
    //     2) If found → fetch weather
    //     3) If not found → post error
    // AR: 1) تحويل اسم المدينة لإحداثيات
    //     2) إذا وجدت المدينة → جلب الطقس
    //     3) إذا لم توجد → إرجاع خطأ
    // -----------------------------------------------------------------------------------------
    fun fetchByCityName(city: String) {
        viewModelScope.launch {
            try {
                val geo = repo.getCoordinatesByCity(city)

                if (geo != null) {
                    fetch(geo.lat, geo.lon)
                } else {
                    _state.postValue(Result.failure(Exception("City not found")))
                }

            } catch (e: Exception) {
                _state.postValue(Result.failure(e))
            }
        }
    }


    // -----------------------------------------------------------------------------------------
    // 🌙 Check if current time is night
    // EN: Returns true if the hour is outside 6 AM - 6 PM.
    // AR: التحقق إذا الوقت الحالي ليل (خارج الفترة 6 صباحًا – 6 مساءً).
    // -----------------------------------------------------------------------------------------
    fun isNightNow(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour !in 6..<18   // EN: Night hours 0-5 and 18-23 / AR: ساعات الليل
    }
}
