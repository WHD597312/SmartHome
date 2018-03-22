package com.xinrui.smart.pojo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by win7 on 2018/3/22.
 */

@Entity
public class DeviceHome {
    @Id(autoincrement = true)
    private Long id;
    private String name;

    @Generated(hash = 1631711473)
    public DeviceHome(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Generated(hash = 1500475353)
    public DeviceHome() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
