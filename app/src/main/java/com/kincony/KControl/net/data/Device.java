package com.kincony.KControl.net.data;

import com.kincony.KControl.R;
import com.kincony.KControl.utils.IPUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * 设备实体类
 */
@Entity(
        tableName = "device",
        foreignKeys = @ForeignKey(entity = IPAddress.class,
                parentColumns = "id",
                childColumns = "address_id",
                onDelete = CASCADE))
public class Device {
    /**
     * ip地址id
     */
    @ColumnInfo(name = "address_id")
    public int addressId;
    /**
     * 设备id
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "device_id")
    public int deviceId;
    /**
     * 排序字段
     */
    @ColumnInfo(name = "index_n")
    public int index;
    /**
     * 编号（几路）
     */
    public int number;
    /**
     * 触摸模式
     */
    @ColumnInfo(name = "is_touch")
    public boolean isTouch = false;
    /**
     * 是否是打开状态
     */
    public boolean open = false;
    /**
     * 名称（可以编辑修改）
     */
    public String name;
    /**
     * icon
     */
    public int icon = R.drawable.icon6;

    /**
     * icon
     */
    @ColumnInfo(name = "icon_touch")
    public int iconTouch = R.drawable.icon6;
    /**
     * 关联的地址
     */
    @Ignore
    public IPAddress address;

    /**
     * type
     */
    @ColumnInfo(name = "type")
    public int type;

    @ColumnInfo(name = "itemName")
    public String itemName;

    /**
     * data
     */
    public int body;

    /**
     * path
     */
    public int path;

    public Device() {
    }

    public Device(IPAddress address, int number, int index) {
        this.address = address;
        this.addressId = address.getId();
        this.number = number;
        this.name = IPUtils.INSTANCE.getDefaultName(address, number);
        this.index = index;
        this.type = 0;
        this.itemName = null;
        this.body = 0;
        this.path = 0;
    }

    public Device(IPAddress address, int number, int index, int type, int path, String itemName) {
        this.address = address;
        this.addressId = address.getId();
        this.number = number;
        this.name = IPUtils.INSTANCE.getDefaultName(address, number);
        this.index = index;
        this.type = type;
        this.body = 0;
        this.path = path;
        this.itemName = itemName;
    }


}