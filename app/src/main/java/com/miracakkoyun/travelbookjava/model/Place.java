package com.miracakkoyun.travelbookjava.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity // entity koymamızın sebebi roomdatabsein bunu bu sınıfı kullanacağını anlamasını sağlamak
public class Place implements Serializable {


    @PrimaryKey(autoGenerate = true)// otomatik olarak idleri oluşturmayı sağlıyor
    public int id;


    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name="latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    public Place(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        //constructor içine id eklemedik id kendisi otomatik eklesin istedik
    }




}
