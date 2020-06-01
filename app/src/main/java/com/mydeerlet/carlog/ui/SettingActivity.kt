package com.mydeerlet.carlog.ui

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mydeerlet.carlog.R
import com.mydeerlet.carlog.adapter.VideoListAdapter
import com.mydeerlet.carlog.model.Video
import kotlinx.android.synthetic.main.activity_setting.*
import java.io.File
import java.text.SimpleDateFormat

class SettingActivity : AppCompatActivity() {


    val mList = ArrayList<Video>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)


        init()

    }

    private fun init() {
        val mAdatper = VideoListAdapter(this, mList)
        recycleview.layoutManager = LinearLayoutManager(this)
        recycleview.adapter = mAdatper





        mList.addAll(getAllFile())
        mAdatper.notifyDataSetChanged()
    }


    /**
     * 获取指定目录下所有的mp4文件
     * 加入列表
     */
    fun getAllFile(): ArrayList<Video> {

        val mutableList = ArrayList<Video>()

        val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files: Array<File> = file.listFiles()
        if (files.isNullOrEmpty()) {
            Toast.makeText(this, "暂时没有录制视频", Toast.LENGTH_SHORT).show()
            return ArrayList()
        }

        for (element in files) {
            if (element.name.endsWith(".mp4")) {
                val video = Video()
                video.name = element.name
                video.url = element.path
                video.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(element.lastModified())
                mutableList.add(video)
            }
        }
        return mutableList
    }


}


