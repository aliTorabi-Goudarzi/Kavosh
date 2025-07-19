package ir.dekot.kavosh.feature_deviceInfo.model

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * منبع داده برای اطلاعات کارت گرافیک
 * مسئول دریافت اطلاعات GPU و مانیتورینگ بار کارت گرافیک
 */
@Singleton
class GpuDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    /**
     * دریافت درصد بار کارت گرافیک
     * @return درصد استفاده از GPU یا null در صورت عدم دسترسی
     */
    fun getGpuLoadPercentage(): Int? {
        val kgslPath = "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage"
        return try {
            val file = File(kgslPath)
            if (file.exists() && file.canRead()) {
                file.readText().trim().substringBefore(" ").toIntOrNull()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * دریافت اطلاعات کارت گرافیک
     * @param activity Activity مورد نیاز برای دسترسی به OpenGL
     * @return اطلاعات کامل GPU
     */
    @Suppress("DEPRECATION")
    suspend fun getGpuInfo(activity: Activity): GpuInfo {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        val deferred = CompletableDeferred<GpuInfo>()

        withContext(Dispatchers.Main) {
            val glSurfaceView = GLSurfaceView(activity).apply {
                setRenderer(object : GLSurfaceView.Renderer {
                    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                        val vendor = gl?.glGetString(GL10.GL_VENDOR)
                            ?: context.getString(R.string.label_undefined)
                        val renderer = gl?.glGetString(GL10.GL_RENDERER)
                            ?: context.getString(R.string.label_undefined)
                        deferred.complete(GpuInfo(vendor = vendor, model = renderer))
                        rootView.post {
                            rootView.removeView(this@apply)
                        }
                    }

                    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
                    override fun onDrawFrame(gl: GL10?) {}
                })
                layoutParams = FrameLayout.LayoutParams(1, 1)
            }
            rootView.addView(glSurfaceView)
        }
        return deferred.await()
    }
}
