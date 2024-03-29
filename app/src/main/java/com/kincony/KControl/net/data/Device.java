package com.kincony.KControl.net.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.kincony.KControl.R;
import com.kincony.KControl.utils.IPUtils;

import static androidx.room.ForeignKey.CASCADE;

/**
 * 设备实体类
 */
@Entity(
        tableName = "device",
        foreignKeys = @ForeignKey(
                entity = IPAddress.class,
                parentColumns = "id",
                childColumns = "address_id",
                onDelete = CASCADE
        )
)
public class Device implements MultiItemEntity {
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
     * 8位继电器开关数据
     * 比如：0b00000001
     */
    public int body;

    /**
     * path
     */
    public int path;

    /**
     * 状态
     * 比如调光器：
     * 字符串为数字是表示单个亮度
     * 如果数字中间有逗号，表示多个亮度
     */
    public String state;

    /**
     * 最大值
     */
    public String max;

    /**
     * 最小值
     */
    public String min;

    /**
     * 单位
     */
    public String unit;

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


    @Override
    public int getItemType() {
        return address == null ? DeviceType.Unknown.getValue() : address.getDeviceType();
    }
}