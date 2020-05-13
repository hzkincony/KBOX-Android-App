package com.kincony.KControl.net.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.kincony.KControl.R;

import java.io.Serializable;

@Entity(tableName = "scene")
public class Scene implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id = 0;

    public int icon = R.drawable.icon_up;

    /**
     * 触摸模式
     */
    @ColumnInfo(name = "is_touch")
    public boolean isTouch = false;

    @Ignore
    public String reAction = "";

    public String name = "";

    public String ids = "";

    public String address = "";

    public String length = "";

    public String action = "";

    public Scene() {

    }

//    @NonNull
//    @Override
//    public String toString() {
//        return name + "{" +
//                "ids:" + ids + "," +
//                "address:" + address + "," +
//                "length:" + length + "," +
//                "action:" + action +
//                "}";
//    }
}
