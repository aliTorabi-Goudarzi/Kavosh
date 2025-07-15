package ir.dekot.kavosh.data.source

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkToolsDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * یک اسکن Wi-Fi را به صورت یک‌باره انجام می‌دهد و نتایج را برمی‌گرداند.
     * این تابع حالا یک suspend function با مکانیزم انتظار و timeout است.
     */
    suspend fun scanForWifiNetworks(): List<ScanResult> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return emptyList() // اگر مجوز نباشد، عملیات را انجام نده
        }

        val deferred = CompletableDeferred<List<ScanResult>>()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // وقتی نتایج آماده شد، deferred را کامل می‌کنیم
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    try {
                        deferred.complete(wifiManager.scanResults)
                    } catch (e: SecurityException) {
                        deferred.completeExceptionally(e)
                    }
                }
            }
        }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        }

        // شروع اسکن
        @Suppress("DEPRECATION")
        val scanInitiated = wifiManager.startScan()

        if (!scanInitiated) {
            context.unregisterReceiver(receiver)
            return emptyList() // اگر اسکن شروع نشد، لیست خالی برگردان
        }

        // حداکثر ۱۵ ثانیه منتظر نتیجه بمان
        val result = withTimeoutOrNull(15000) {
            deferred.await()
        }

        // حتماً گیرنده را unregister کن
        context.unregisterReceiver(receiver)
        return result ?: emptyList() // اگر timeout رخ داد، لیست خالی برگردان
    }

    // تابع pingHost بدون تغییر باقی می‌ماند
    fun pingHost(host: String): Flow<String> = flow {
        val command = "ping -c 10 $host"
        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line!!)
            }
            process.waitFor()
            reader.close()
        } catch (e: Exception) {
            emit("Error: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
}