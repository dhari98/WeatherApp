package com.dharidodi.weatherapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dharidodi.weatherapp.databinding.ActivityMainBinding
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat
@Suppress("DEPRECATION", "SameParameterValue")
// EN: Suppresses warnings for deprecated APIs or unused parameters.
// AR: إخفاء التحذيرات المتعلقة بدوال قديمة أو باراميترات غير مستخدمة.

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    // EN: ViewBinding instance to access XML views safely.
    // AR: كائن ViewBinding للوصول إلى عناصر XML بأمان.

    private val vm: WeatherViewModel by viewModels()
    // EN: ViewModel scoped to this Activity to manage UI data.
    // AR: ViewModel لإدارة بيانات واجهة المستخدم (الطقس).

    private lateinit var locationClient: FusedLocationProviderClient
    // EN: Client used to retrieve the device’s current location.
    // AR: كائن للحصول على موقع الجهاز باستخدام خدمات Google.

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // EN: Inflate layout using ViewBinding and set as content view.
        // AR: ربط الواجهة باستخدام ViewBinding.

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        // EN: Initialize the location provider.
        // AR: تهيئة خدمة الموقع.

        getCurrentLocationWeather()
        // EN: Fetch weather for the current location on startup.
        // AR: جلب الطقس عند فتح التطبيق مباشرة.

        // --- Button: get weather by current location ---
        binding.btnMyLocation.setOnClickListener { getCurrentLocationWeather() }
        // EN: Refresh weather using GPS location.
        // AR: تحديث الطقس حسب الموقع.

        // --- Button: search by city name ---
        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                vm.fetchByCityName(query)
                binding.tvCityName.text = query
                // EN: Fetch weather by the entered city.
                // AR: جلب الطقس حسب المدينة التي كتبها المستخدم.
            } else {
                Toast.makeText(this, getString(R.string.toast_enter_city), Toast.LENGTH_SHORT).show()
                // EN: Input is empty → show message.
                // AR: إذا الحقل فارغ يظهر تنبيه.
            }
        }

        // --- Weather data observer ---
        vm.state.observe(this) { result ->
            result.onSuccess { resp ->
                val c = resp.current
                val daily = resp.daily
                val isNight = vm.isNightNow()

                // --- Weather icon (day/night) ---
                binding.imgWeatherIcon.setImageResource(
                    if (isNight) R.drawable.ic_moon else R.drawable.ic_sunny
                )
                // EN: Display moon icon at night, sun during the day.
                // AR: عرض أيقونة شمس أو قمر حسب الوقت.

                // --- Current conditions ---
                binding.tvTemp.text = getString(R.string.format_temperature, c?.temperature_2m ?: "--")
                binding.tvHumidity.text = getString(R.string.format_humidity, c?.relative_humidity_2m ?: "--")
                // EN: Show temperature and humidity.
                // AR: عرض درجة الحرارة والرطوبة.

                val nowCode = c?.weather_code
                binding.tvDescription.text = codeToDescriptionDual(nowCode)
                // EN: Convert weather code to text description.
                // AR: تحويل كود الطقس إلى وصف نصي (عربي + إنجليزي).

                // --- High/low temperatures ---
                val hi = daily?.temperature_2m_max?.firstOrNull()
                val lo = daily?.temperature_2m_min?.firstOrNull()
                binding.tvDaily.text = getString(R.string.format_high_low, hi ?: "--", lo ?: "--")
                // EN: Show today's high and low temperatures.
                // AR: عرض أعلى وأقل درجة حرارة اليوم.

                // --- Next days data ---
                val days = daily?.time ?: emptyList()
                val highs = daily?.temperature_2m_max ?: emptyList()
                val lows  = daily?.temperature_2m_min ?: emptyList()
                val codes = daily?.weather_code ?: emptyList()

                // EN: Convert API date (yyyy-MM-dd) → day name (Mon, Tue…)
                // AR: تحويل تاريخ API إلى اسم يوم (مثل Mon، Tue…)
                fun formatDay(index: Int): String = try {
                    val sdfOut = SimpleDateFormat("EEE", Locale.getDefault())
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(days[index])
                    sdfOut.format(date ?: Date())
                } catch (_: Exception) { "--" }

                // EN: Safe getter for Doubles (high/low temps)
                // AR: دالة آمنة لجلب درجة الحرارة بدون أخطاء.
                fun safeD(list: List<Double?>?, i: Int): Int? =
                    if (list != null && i in list.indices && list[i] != null) list[i]!!.toInt() else null

                // EN: Safe getter for weather codes
                // AR: دالة آمنة لجلب كود الطقس.
                fun safeC(list: List<Int?>?, i: Int): Int? =
                    if (list != null && i in list.indices) list[i] else null

                // --- Display next 3 days ---
                if (days.size >= 4) {

                    // Day 1
                    val c1 = safeC(codes, 1)
                    binding.tvDay1.text = formatDay(1)
                    binding.tvTemp1.text = "${safeD(highs,1) ?: "--"}° / ${safeD(lows,1) ?: "--"}°"
                    binding.tvDesc1.text = codeToDescriptionDual(c1)
                    binding.tvIcon1.text = codeToEmoji(c1, false)

                    // Day 2
                    val c2 = safeC(codes, 2)
                    binding.tvDay2.text = formatDay(2)
                    binding.tvTemp2.text = "${safeD(highs,2) ?: "--"}° / ${safeD(lows,2) ?: "--"}°"
                    binding.tvDesc2.text = codeToDescriptionDual(c2)
                    binding.tvIcon2.text = codeToEmoji(c2, false)

                    // Day 3
                    val c3 = safeC(codes, 3)
                    binding.tvDay3.text = formatDay(3)
                    binding.tvTemp3.text = "${safeD(highs,3) ?: "--"}° / ${safeD(lows,3) ?: "--"}°"
                    binding.tvDesc3.text = codeToDescriptionDual(c3)
                    binding.tvIcon3.text = codeToEmoji(c3, false)
                }

            }.onFailure {
                Toast.makeText(this, getString(R.string.toast_failed, it.localizedMessage), Toast.LENGTH_SHORT).show()
                // EN: If API fails → show error message.
                // AR: إذا فشل الاتصال بالـ API → عرض رسالة خطأ.
            }
        }
    }

    // -----------------[ LOCATION + CITY NAME ]-----------------

    @SuppressLint("SetTextI18n")
    private fun getCurrentLocationWeather() {

        // EN: Request location permission if not granted.
        // AR: طلب صلاحية الموقع إذا لم تكن موجودة.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        // EN: Get last known device location.
        // AR: جلب آخر موقع معروف للجهاز.
        locationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {

                vm.fetch(location.latitude, location.longitude)
                // EN: Fetch weather from coordinates.
                // AR: جلب الطقس حسب إحداثيات الموقع.

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                        val result = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val city = result?.firstOrNull()?.locality ?: getString(R.string.unknown)

                        runOnUiThread {
                            binding.tvCityName.text =
                                "${getString(R.string.label_current_location)} $city"
                            // EN: Update UI with city name.
                            // AR: تحديث الواجهة باسم المدينة.
                        }

                    } catch (_: Exception) {
                        runOnUiThread { binding.tvCityName.text = getString(R.string.unknown) }
                        // EN: If geocoder fails.
                        // AR: عند فشل معرفة اسم المدينة.
                    }
                }

            } else {
                Toast.makeText(this, getString(R.string.toast_no_location), Toast.LENGTH_SHORT).show()
                // EN: Device location is unavailable.
                // AR: لا يمكن الحصول على موقع الجهاز.
            }
        }
    }

    // -----------------[ PERMISSION RESULT ]-----------------

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) getCurrentLocationWeather()
            else Toast.makeText(this, getString(R.string.toast_permission_denied), Toast.LENGTH_SHORT).show()
            // EN: Handle location permission approval/denial.
            // AR: التعامل مع قبول أو رفض صلاحية الموقع.
        }

    // -----------------[ WEATHER ICON BY CODE ]-----------------

    private fun codeToEmoji(code: Int?, night: Boolean): String = when (code) {
        0 -> if (night) "🌙" else "☀️"        // EN: Clear sky | AR: سماء صافية
        1 -> "🌤️"                             // EN: Mostly sunny | AR: مشمس جزئياً
        2 -> "⛅"                              // EN: Partly cloudy | AR: غائم جزئياً
        3 -> "☁️"                              // EN: Cloudy | AR: غائم
        in 45..48 -> "🌫️"                     // EN: Fog | AR: ضباب
        in 51..55 -> "🌦️"                     // EN: Drizzle | AR: رذاذ مطر
        in 56..57 -> "🌧️"                     // EN: Freezing drizzle | AR: رذاذ متجمد
        in 61..67 -> "🌧️"                     // EN: Rain | AR: مطر
        in 71..77 -> "🌨️"                     // EN: Snow | AR: ثلج
        in 80..82 -> "🌦️"                     // EN: Rain showers | AR: زخات مطر
        in 95..99 -> "⛈️"                     // EN: Thunderstorms | AR: عواصف رعدية
        else -> "🌍"                           // EN: Unknown | AR: غير معروف
    }

    // -----------------[ TEXT DESCRIPTION BY CODE ]-----------------

    private fun codeToDescriptionDual(code: Int?): String = when (code) {
        0 -> getString(R.string.desc_clear_sky)
        1 -> getString(R.string.desc_mostly_sunny)
        2 -> getString(R.string.desc_partly_cloudy)
        3 -> getString(R.string.desc_cloudy)
        45, 48 -> getString(R.string.desc_fog)
        in 51..55 -> getString(R.string.desc_light_drizzle)
        in 56..57 -> getString(R.string.desc_freezing_drizzle)
        in 61..63 -> getString(R.string.desc_light_moderate_rain)
        in 64..67 -> getString(R.string.desc_heavy_rain)
        in 71..75 -> getString(R.string.desc_snowfall)
        77 -> getString(R.string.desc_heavy_snow)
        in 80..82 -> getString(R.string.desc_rain_showers)
        in 85..86 -> getString(R.string.desc_snow_showers)
        in 95..99 -> getString(R.string.desc_thunderstorms)
        else -> getString(R.string.desc_unknown)
        // EN: Map weather codes to text (Arabic + English).
        // AR: تحويل كود الطقس إلى وصف نصي.
    }

    // -----------------[ SPINNER COMPAT LISTENER ]-----------------

    @Suppress("unused")
    inline fun Spinner.setOnItemSelectedListenerCompat(crossinline onSelected: (position: Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                onSelected(position)
                // EN: Trigger callback when item is selected.
                // AR: تنفيذ دالة عند اختيار عنصر من القائمة.
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
            // EN: Required override but unused.
            // AR: دالة مطلوبة لكن غير مستخدمة.
        }
    }
}
