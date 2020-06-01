package com.mydeerlet.carlog.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * @author myDeerlet
 * @date 2020/6/1
 * email：kuaileniaofei@163.com
 */

@Entity(tableName = "videolist")
public class Video implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int id;

    public String name;

    public String url;

    //@Ignore所以该字段不会映射到User表中。
    @Ignore
    public String time;

}
