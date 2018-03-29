package com.xinrui.smart.pojo;

import android.widget.ImageView;
import android.widget.TextView;

import com.xinrui.smart.R;

import java.io.Serializable;

/**
 * Created by win7 on 2018/3/8.
 */

public class Room implements Serializable{
    private int Id;
    private int room_image;
    private String room_name;//房间名
    private ImageView pull_down;//下拉
    private double  temperature;//温度
    private double humidity;//湿度
    private double PM25;//PM2.5
    int equipment[] = {R.drawable.equipment,R.drawable.equipment,R.drawable.equipment,R.drawable.equipment};
    public int width;
    public int height;
    private int add_equipment;

    public Room() {
    }

    public int getAdd_equipment() {
        return add_equipment;
    }

    public void setAdd_equipment(int add_equipment) {
        this.add_equipment = add_equipment;
    }

    public Room(int id, int room_image, String room_name, ImageView pull_down, double temperature, double humidity, double PM25, int[] equipment, int width, int height, int add_equipment) {
        Id = id;
        this.room_image = room_image;
        this.room_name = room_name;
        this.pull_down = pull_down;
        this.temperature = temperature;
        this.humidity = humidity;
        this.PM25 = PM25;
        this.equipment = equipment;
        this.width = width;
        this.height = height;
        this.add_equipment = add_equipment;
    }

    public Room(int room_image, String room_name) {
        this.room_image = room_image;
        this.room_name = room_name;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getRoom_image() {
        return room_image;
    }

    public void setRoom_image(int room_image) {
        this.room_image = room_image;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public ImageView getPull_down() {
        return pull_down;
    }

    public void setPull_down(ImageView pull_down) {
        this.pull_down = pull_down;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getPM25() {
        return PM25;
    }

    public void setPM25(double PM25) {
        this.PM25 = PM25;
    }

    public int[] getEquipment() {
        return equipment;
    }

    public void setEquipment(int[] equipment) {
        this.equipment = equipment;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
