package com.mydeerlet.carlog

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mydeerlet.carlog.utils.StatusBar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(),SensorEventListener {


    var preview: Preview? = null
    var imageCapture: ImageCapture? = null

    lateinit var videoCapture: VideoCapture
    lateinit var mContext: Context


    var mSensorManager:SensorManager?=null



    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatusBar.fullSystemBar(this)

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
        camera_capture_button.setOnClickListener { takePhoto(false) }
        camera_capture_button.setOnLongClickListener {
            takePhoto(true)
            true
        }
//        videoCapture!!.stopRecording()


        starDetection()


    }


    /**
     * 设置传感器
     */
    fun starDetection() {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)// TYPE_GRAVIT
        // 参数三，检测的精准度
        mSensorManager!!.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    @SuppressLint("RestrictedApi")
    override fun onSensorChanged(event: SensorEvent?) {

        if(event!!.sensor.getType() != Sensor.TYPE_ACCELEROMETER){
            return
        }
        val values = event.values
        val ax = values[0]
        val ay = values[1]

        val g = Math.sqrt(ax * ax + ay * ay.toDouble())
        var cos = ay / g
        if (cos > 1) {
            cos = 1.0
        } else if (cos < -1) {
            cos = -1.0
        }
        var rad = Math.acos(cos)
        if (ax < 0) {
            rad = 2 * Math.PI - rad
        }

        val uiRot: Int = this.getWindowManager().getDefaultDisplay().getRotation()
        val uiRad = Math.PI / 2 * uiRot
        rad -= uiRad

        checkBundray(rad.toInt())
    }


    /**
     * 旋转检测
     */

    private var curRotateCode = Surface.ROTATION_0

    @SuppressLint("RestrictedApi")
    private fun checkBundray(rotateCode: Int) {
        var tmp =  rotateCode

        if (tmp == 2) {
            tmp = 1
        }
        if (tmp == -1) {
            tmp = 4
        }


        var angle = Surface.ROTATION_0

        when (tmp) {
            0 -> angle = Surface.ROTATION_0
            1 -> angle = Surface.ROTATION_90
            3 -> angle = Surface.ROTATION_180
            4 -> angle = Surface.ROTATION_270
        }

        if(angle == curRotateCode){

        }else{
            curRotateCode = angle
            videoCapture.setTargetRotation(curRotateCode)
        }
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

            videoCapture = VideoCaptureConfig.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build()



            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture)
                preview!!.setSurfaceProvider(viewFinder.createSurfaceProvider())


            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }


    @SuppressLint("RestrictedApi")
    private fun takePhoto(isVideo: Boolean) {
        if (isVideo) {
            val photoFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                SimpleDateFormat(
                    FILENAME_FORMAT,
                    Locale.US
                ).format(System.currentTimeMillis()) + ".mp4"
            )

            videoCapture.startRecording(
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
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        mSensorManager!!.unregisterListener(this);
    }
}
