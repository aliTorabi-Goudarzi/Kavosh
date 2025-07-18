package ir.dekot.kavosh.core.util

import android.content.Context
import android.location.LocationManager

/**
 * بررسی می‌کند که آیا سرویس موقعیت مکانی (GPS یا شبکه) در دستگاه روشن است یا خیر.
 */
fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}