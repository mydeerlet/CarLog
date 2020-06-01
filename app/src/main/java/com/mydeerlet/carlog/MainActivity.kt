package com.mydeerlet.carlog

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mydeerlet.carlog.ui.SettingActivity
import com.mydeerlet.carlog.utils.RxTimerUtil
import com.mydeerlet.carlog.utils.StatusBar
import com.mydeerlet.carlog.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val TAG = "MYlOG"

    var mTimeLong = 0L
    var mSpacing = 20L+1


    var preview: Preview? = null
    var imageCapture: ImageCapture? = null

    lateinit var mVideoCapture: VideoCapture
    lateinit var mContext: Context


    var mSensorManager: SensorManager? = null


    lateinit var myOrientoinListener: MyOrientoinListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatusBar.fitSystemBar(this)
        StatusBar.lightStatusBar(this, false)

        setContentView(R.layout.activity_main)

        mContext = this

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Setup the listener for take photo button
        camera_capture_button.setOnClickListener { takePhoto(true) }

//        starDetection()

        iv_img.setOnClickListener { startActivity(Intent(mContext, SettingActivity::class.java)) }



        myOrientoinListener = MyOrientoinListener(this)
        val autoRotateOn = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
        if (autoRotateOn) {
            myOrientoinListener.enable();
        }

    }

    /**
     * 重力感应
     */
    inner class MyOrientoinListener(context: Context) : OrientationEventListener(context) {
        @SuppressLint("SourceLockedOrientationActivity")
        override fun onOrientationChanged(orientation: Int) {
            val screenOrientation: Int = getResources().getConfiguration().orientation

            if (orientation >= 0 && orientation < 45 || orientation > 315) {    //设置竖屏
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && orientation !== ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                }
            } else if (orientation > 225 && orientation < 315) { //设置横屏
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                }
            } else if (orientation > 45 && orientation < 135) { // 设置反向横屏
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                }
            } else if (orientation > 135 && orientation < 225) { //反向竖屏
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
                }
            }
        }

    }


    @SuppressLint("RestrictedApi")
    private fun myTimer() {
        RxTimerUtil.interval(1000, object : RxTimerUtil.IRxNext {

            override fun doNext(number: Long) {

                Log.i(TAG, "doNext:$number")
                v_view.visibility = if (number % 2 == 0L) View.VISIBLE else View.INVISIBLE
                mTimeLong = number % mSpacing
                tv_time.text = Utils.longToString(mTimeLong)
                if (mTimeLong + 1 == mSpacing) {
                    mTimeLong = 0
                    mVideoCapture.stopRecording()
                    takePhoto(true)
                }
            }
        })
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("RestrictedApi")
    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // Select back camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            preview = Preview.Builder().build()

            imageCapture = ImageCapture.Builder().build()

            mVideoCapture = VideoCaptureConfig.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build()



            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    mVideoCapture
                )
                preview!!.setSurfaceProvider(viewFinder.createSurfaceProvider())


            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }


    @SuppressLint("RestrictedApi")
    private fun takePhoto(isVideo: Boolean) {
        if (isVideo) {
            myTimer()
            val photoFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".mp4"
            )
            mVideoCapture.startRecording(
                photoFile,
                ContextCompat.getMainExecutor(this),
                object : VideoCapture.OnVideoSavedCallback {
                    override fun onVideoSaved(file: File) {
                        val savedUri = Uri.fromFile(photoFile)
                        val msg = "Photo capture succeeded: $savedUri"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    }

                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                        Log.e(
                            TAG,
                            "Photo capture failed: ${message} +${videoCaptureError} ${cause.toString()} "
                        )
                    }
                })
        } else {
            val photoFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                SimpleDateFormat(
                    FILENAME_FORMAT,
                    Locale.US
                ).format(System.currentTimeMillis()) + ".jpeg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            imageCapture!!.takePicture(outputOptions, ContextCompat.getMainExecutor(this),

                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(photoFile)
                        val msg = "Photo capture succeeded: $savedUri"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    }
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd HH:mm:ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
    }


    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        mTimeLong = 0
        tv_time.text = Utils.longToString(mTimeLong)
        mVideoCapture.stopRecording()
        RxTimerUtil.cancel()
        super.onBackPressed()
    }

    @SuppressLint("RestrictedApi")
    override fun onPause() {
        mTimeLong = 0
        tv_time.text = Utils.longToString(mTimeLong)
        mVideoCapture.stopRecording()
        RxTimerUtil.cancel()
        super.onPause()
    }

    @SuppressLint("RestrictedApi")
    override fun onDestroy() {
        mVideoCapture.stopRecording()
        RxTimerUtil.cancel()
        super.onDestroy()
    }
}
