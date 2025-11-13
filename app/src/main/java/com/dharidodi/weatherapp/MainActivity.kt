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
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: WeatherViewModel by viewModels()
    private lateinit var locationClient: FusedLocationProviderClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        // عند الفتح، اعرض طقس الموقع
        getCurrentLocationWeather()

        binding.btnMyLocation.setOnClickListener { getCurrentLocationWeather() }

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                vm.fetchByCityName(query)
                binding.tvCityName.text = query
            } else {
                Toast.makeText(this, getString(R.string.toast_enter_city), Toast.LENGTH_SHORT).show()
            }
        }

        vm.state.observe(this) { result ->
            result.onSuccess { resp ->
                val c = resp.current
                val daily = resp.daily
                val isNight = vm.isNightNow()

                // الأيقونة الرئيسية (نهار أو ليل)
                binding.imgWeatherIcon.setImageResource(
                    if (isNight) R.drawable.ic_moon else R.drawable.ic_sunny
                )

                binding.tvTemp.text = getString(R.string.format_temperature, c?.temperature_2m ?: "--")
                binding.tvHumidity.text = getString(R.string.format_humidity, c?.relative_humidity_2m ?: "--")

                val nowCode = c?.weather_code
                binding.tvDescription.text = codeToDescriptionDual(nowCode)

                val hi = daily?.temperature_2m_max?.firstOrNull()
                val lo = daily?.temperature_2m_min?.firstOrNull()
                binding.tvDaily.text = getString(R.string.format_high_low, hi ?: "--", lo ?: "--")

                // الأيام القادمة
                val days = daily?.time ?: emptyList()
                val highs = daily?.temperature_2m_max ?: emptyList()
                val lows  = daily?.temperature_2m_min ?: emptyList()
                val codes = daily?.weather_code ?: emptyList()

                fun formatDay(index: Int): String = try {
                    val sdfOut = SimpleDateFormat("EEE", Locale.getDefault())
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(days[index])
                    sdfOut.format(date ?: Date())
                } catch (_: Exception) { "--" }

                fun safeD(list: List<Double?>?, i: Int): Int? =
                    if (list != null && i in list.indices && list[i] != null) list[i]!!.toInt() else null


                fun safeC(list: List<Int?>?, i: Int): Int? =
                    if (list != null && i in list.indices && list[i] != null) list[i] else null




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
            }
        }
    }

    // جلب الموقع الحالي واسم المدينة
    @SuppressLint("SetTextI18n")
    private fun getCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        locationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                vm.fetch(location.latitude, location.longitude)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                        val result = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val city = result?.firstOrNull()?.locality ?: getString(R.string.unknown)
                        runOnUiThread {
                            binding.tvCityName.text =
                                "${getString(R.string.label_current_location)} $city"
                        }
                    } catch (_: Exception) {
                        runOnUiThread { binding.tvCityName.text = getString(R.string.unknown) }
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.toast_no_location), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) getCurrentLocationWeather()
            else Toast.makeText(this, getString(R.string.toast_permission_denied), Toast.LENGTH_SHORT).show()
        }

    // ✅ أيقونة (إيموجي) صحيحة حسب الكود
    private fun codeToEmoji(code: Int?, night: Boolean): String = when (code) {
        0 -> if (night) "🌙" else "☀️"        // سماء صافية
        1 -> "🌤️"                             // مشمس جزئيًا
        2 -> "⛅"                              // سحب متفرقة
        3 -> "☁️"                              // غائم
        in 45..48 -> "🌫️"                     // ضباب
        in 51..55 -> "🌦️"                     // رذاذ
        in 56..57 -> "🌧️"                     // رذاذ متجمد
        in 61..67 -> "🌧️"                     // مطر
        in 71..77 -> "🌨️"                     // ثلج
        in 80..82 -> "🌦️"                     // زخات مطر
        in 95..99 -> "⛈️"                     // عواصف
        else -> "🌍"
    }

    // وصف عربي/إنجليزي حسب الكود
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
    }

    @Suppress("unused")
    inline fun Spinner.setOnItemSelectedListenerCompat(crossinline onSelected: (position: Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                onSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
