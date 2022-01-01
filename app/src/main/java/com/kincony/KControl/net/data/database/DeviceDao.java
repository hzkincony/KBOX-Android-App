package com.kincony.KControl.net.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kincony.KControl.net.data.Device;

import java.util.List;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM device order by index_n asc")
    List<Device> getAllDevice();

    @Query("SELECT * FROM device WHERE address_id == :id order by number asc")
    List<Device> getDevice(int id);

    @Query("SELECT * FROM device order by index_n DESC LIMIT 1")
    List<Device> getLastDevice();

    /*当数据库中已经有此用户的时候，直接替换*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertDevice(Device device);

    @Update
    public void updateDevice(Device device);

    @Update
    public void updateDevice(List<Device> device);

    @Delete
    public void deleteDevice(List<Device> device);

}
