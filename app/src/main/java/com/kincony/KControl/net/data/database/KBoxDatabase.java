package com.kincony.KControl.net.data.database;

import android.content.Context;

import com.kincony.KControl.net.data.Device;
import com.kincony.KControl.net.data.IPAddress;
import com.kincony.KControl.net.data.Scene;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Device.class, IPAddress.class, Scene.class}, version = 3, exportSchema = false)
public abstract class KBoxDatabase extends RoomDatabase {
    public static final String DB_NAME = "CompanyDatabase.db";
    private static volatile KBoxDatabase instance;

    public static synchronized KBoxDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static KBoxDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                KBoxDatabase.class,
                DB_NAME)
                .allowMainThreadQueries()
                .addMigrations(new Migration(1, 2) {
                    @Override
                    public void migrate(SupportSQLiteDatabase database) {

                    }
                }, new Migration(2, 3) {
                    @Override
                    public void migrate(SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE scene (" +
                                "id INTEGER PRIMARY KEY NOT NULL," +
                                "device_id INTEGER NOT NULL," +
                                "name TEXT NOT NULL," +
                                "\'action\' INTEGER NOT NULL)");
                    }
                })
                .build();
    }

    public abstract DeviceDao getDeviceDao();

    public abstract AddressDao getAddressDao();

    public abstract SceneDao getSceneDao();

}
