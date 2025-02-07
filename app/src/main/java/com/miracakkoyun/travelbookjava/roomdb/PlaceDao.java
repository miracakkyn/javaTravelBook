package com.miracakkoyun.travelbookjava.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.miracakkoyun.travelbookjava.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlaceDao {
    // burada roomdb üzerindeki verilere dao(database access onject) ile erişebileceğiz. Bu arayüz bir nevi

    @Query("SELECT * FROM Place")
    Flowable<List<Place>> getAll();



    @Insert
    Completable insert(Place place);//çok basit ve pratik tek satır kod ile halledilebilir

    @Delete
    Completable delete(Place place);


}
