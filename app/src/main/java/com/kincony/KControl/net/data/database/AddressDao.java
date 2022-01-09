package com.kincony.KControl.net.data.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kincony.KControl.net.data.IPAddress;

import java.util.List;

@Dao
public interface AddressDao {
    @Query("SELECT * FROM address")
    List<IPAddress> getAllAddress();

    @Query("SELECT * FROM address WHERE ip == :ip AND port == :port AND protocolType == '1' AND deviceType == :deviceType AND username == :userName AND password == :password AND deviceId=:deviceId")
    IPAddress getMQTTAddress(String ip, int port, int deviceType, String userName, String password, String deviceId);

    @Query("SELECT * FROM address WHERE ip == :ip AND port == :port AND protocolType == '0' AND deviceType == :deviceType")
    IPAddress getTCPAddress(String ip, int port, int deviceType);

    @Query("SELECT * FROM address WHERE id == :id")
    IPAddress getAddress(int id);

    /*当数据库中已经有此用户的时候，直接替换*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAddress(IPAddress address);

    //删除某一项
    @Delete
    void delete(IPAddress address);

}
