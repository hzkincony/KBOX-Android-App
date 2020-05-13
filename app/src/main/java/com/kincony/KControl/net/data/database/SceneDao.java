package com.kincony.KControl.net.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kincony.KControl.net.data.Scene;

import java.util.List;

@Dao
public interface SceneDao {
    @Query("SELECT * FROM scene")
    List<Scene> getAllScene();


    @Query("SELECT * FROM scene WHERE id == :id")
    Scene getScene(int id);

    /*当数据库中已经有此用户的时候，直接替换*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertScene(Scene... scene);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertScene(List<Scene> scenes);

    @Update
    public void updateScene(Scene scene);

    @Update
    public void updateScene(List<Scene> scenes);

    @Delete
    public void deleteScene(Scene device);

    @Delete
    public void deleteScene(List<Scene> device);


//    //使用内连接查询
//    @Query("SELECT emp_id,name,dept  from company INNER JOIN department ON Company.id=Department.emp_id")
//    List<InnerJoinResult> getDepartmentFromCompany();

}
