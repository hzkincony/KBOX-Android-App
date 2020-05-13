package com.kincony.KControl.net.data.database;

import com.kincony.KControl.net.data.Device;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM device order by index_n asc")
    List<Device> getAllDevice();

    @Query("SELECT * FROM device WHERE address_id == :id order by number asc")
    List<Device> getDevice(int id);

    /*当数据库中已经有此用户的时候，直接替换*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertDevice(Device... device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertDevice(List<Device> devices);

    @Update
    public void updateDevice(Device device);

    @Update
    public void updateDevice(List<Device> device);

    @Delete
    public void deleteDevice(List<Device> device);

//    //使用内连接查询
//    @Query("SELECT emp_id,name,dept  from company INNER JOIN department ON Company.id=Department.emp_id")
//    List<InnerJoinResult> getDepartmentFromCompany();

}
