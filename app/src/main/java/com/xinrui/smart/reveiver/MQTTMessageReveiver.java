package com.xinrui.smart.reveiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MQTTMessageReveiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String message=intent.getStringExtra("message");
        String topicName=intent.getStringExtra("topicName");
        try {
            String macAddress=topicName.substring(topicName.indexOf("/"),topicName.lastIndexOf("/"));
            if (!Utils.isEmpty(macAddress)){
                JSONObject device=new JSONObject(message);

                String wifiVersion=device.getString("wifiVersion");/**版本*/
                String MCUVerion=device.getString("MCUVerion");
                int MatTemp=device.getInt("MatTemp");/**手动/定时模式下的温度*/
                String workMode=device.getString("workMode");/**manual:手动模式	timer:定时模式*/
                String LockScreen=device.getString("LockScreen");/** open:上锁  close:解锁*/
                String BackGroundLED=device.getString("BackGroundLED");/**open:照明  close:节能*/
                String deviceState=device.getString("deviceState");/**open:开机  close：关机*/
                String tempState=device.getString("tempState");/**nor:正常 err:异常*/
                String outputMode=device.getString("outputMode");
                int curTemp=device.getInt("curTemp");
                int ratedPower=device.getInt("ratedPower");
                String protectEnable=device.getString("protectEnable");
                String ctrlMode=device.getString("ctrlMode");
                int powerValue=device.getInt("powerValue");
                int voltageValue=device.getInt("voltageValue");
                int currentValue=device.getInt("currentValue");
                String machineFall=device.getString("machineFall");
                int protectSetTemp=device.getInt("protectSetTemp");
                int protectProTemp=device.getInt("protectProTemp");

                macAddress=macAddress.substring(1);
                DeviceChild child=null;
                int groupPostion=0;
                int childPosition=0;

                DeviceGroupDaoImpl deviceGroupDao=new DeviceGroupDaoImpl(context);
                DeviceChildDaoImpl deviceChildDao=new DeviceChildDaoImpl(context);
                List<DeviceGroup> deviceGroups=deviceGroupDao.findAllDevices();
                List<List<DeviceChild>> childern=new ArrayList<>();
                try {
                    for (DeviceGroup deviceGroup:deviceGroups){
                        List<DeviceChild> deviceChildren=deviceChildDao.findGroupIdAllDevice(deviceGroup.getId());
                        childern.add(deviceChildren);
                    }
                    for (List<DeviceChild> deviceChildren :childern){
                        childPosition=0;
                        for (DeviceChild deviceChild:deviceChildren){
                            String mac=deviceChild.getMacAddress();
                            if (!Utils.isEmpty(mac) && macAddress.equals(mac)) {
                                child=deviceChild;
                                break;
                            }
                            childPosition++;
                        }
                        if (child!=null){
                            break;
                        }
                        groupPostion++;
                    }
                    if (child!=null){
//                        child.setVersion(version);
                        child.setWifiVersion(wifiVersion);
                        child.setMCUVerion(MCUVerion);
                        child.setMatTemp(MatTemp);
                        child.setWorkMode(workMode);
                        child.setLockScreen(LockScreen);
                        child.setBackGroundLED(BackGroundLED);
                        child.setDeviceState(deviceState);
                        child.setTempState(tempState);
                        child.setOutputMod(outputMode);
                        child.setCurTemp(curTemp);
                        child.setRatedPower(ratedPower);
                        child.setProtectEnable(protectEnable);
                        child.setCtrlMode(ctrlMode);
                        child.setPowerValue(powerValue);
                        child.setVoltageValue(voltageValue);
                        child.setCurrentValue(currentValue);
                        child.setMachineFall(machineFall);
                        child.setProtectSetTemp(protectSetTemp);
                        child.setProtectProTemp(protectProTemp);


                        deviceChildDao.update(child);

                    }
                    if (DeviceFragment.running==1){
                        Intent mqttIntent=new Intent("DeviceFragment");
                        mqttIntent.putExtra("groupPostion",groupPostion);
                        mqttIntent.putExtra("childPosition",childPosition);
                        mqttIntent.putExtra("deviceState",deviceState);
                        context.sendBroadcast(mqttIntent);
                    }
                    if (HeaterFragment.running){
                        child=deviceChildDao.findDeviceById(child.getId());
                        long houseId=child.getHouseId();
                        long deviceId=child.getId();
                        Intent mqttIntent=new Intent("HeaterFragment");
//                        mqttIntent.putExtra("houseId",houseId);
//                        mqttIntent.putExtra("deviceId",deviceId);
                        mqttIntent.putExtra("deviceChild",child);
                        context.sendBroadcast(mqttIntent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }



        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
