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

    public int body;

    public Device() {
    }

    public Device(IPAddress address, int number, int index) {
        this.address = address;
        this.addressId = address.getId();
        this.number = number;
        this.name = IPUtils.INSTANCE.getDefaultName(address, number);
        this.index = index;
        this.type = 0;
        this.itemName = getInPutState(0);
    }

    public Device(IPAddress address, int number, int index, int type) {
        this.address = address;
        this.addressId = address.getId();
        this.number = number;
        this.name = IPUtils.INSTANCE.getDefaultName(address, number);
        this.index = index;
        this.type = type;
        switch (address.getPort()) {
            case 4102:
                this.itemName = getInPutState(0);
                break;
            case 4104:
                this.itemName = getInPutState(8);
                break;
            case 4108:
                this.itemName = getInPutState(8);
                break;
            case 4116:
                this.itemName = getInPutState(8);
                break;
            case 4132:
                this.itemName = getInPutState(6);
                break;
        }


    }

    public String getInPutState(int num){
        String itemName = null;
        for (int i=0 ; i<num ; i++) {
            if (itemName == null) itemName = "";
            itemName += "输入端" + (i+1) + ";";
        }
        if (itemName != null) {
            itemName = itemName.substring(0, itemName.length()-1);
        }

        return itemName;
    }

}