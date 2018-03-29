package com.xinrui.smart.pojo;

/**
 * Created by win7 on 2018/3/29.
 */

public class Equipment {
    private String equipment_Name;
    private int equipment_image;
    private boolean ischecked;

    public Equipment() {
    }

    public Equipment(String equipment_Name, int equipment_image, boolean ischecked) {
        this.equipment_Name = equipment_Name;
        this.equipment_image = equipment_image;
        this.ischecked = ischecked;
    }

    public String getEquipment_Name() {
        return equipment_Name;
    }

    public void setEquipment_Name(String equipment_Name) {
        this.equipment_Name = equipment_Name;
    }

    public int getEquipment_image() {
        return equipment_image;
    }

    public void setEquipment_image(int equipment_image) {
        this.equipment_image = equipment_image;
    }

    public boolean isIschecked() {
        return ischecked;
    }

    public void setIschecked(boolean ischecked) {
        this.ischecked = ischecked;
    }
}
