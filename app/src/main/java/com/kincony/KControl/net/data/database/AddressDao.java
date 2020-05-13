package com.kincony.KControl.net.data.database;


import com.kincony.KControl.net.data.IPAddress;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface AddressDao {
    @Query("SELECT * FROM address")
    List<IPAddress> getAllAddress();

    @Query("SELECT * FROM address WHERE ip == :ip AND port == :port")
    IPAddress getAddress(String ip, int port);

    @Query("SELECT * FROM address WHERE id == :id")
    IPAddress getAddress(int id);

    /*当数据库中已经有此用户的时候，直接替换*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertAddress(IPAddress... address);

    //删除某一项
    @Delete
    public void delete(IPAddress... address);

}
