package com.xinrui.smart.reveiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.smart.activity.TimeTaskActivity;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MQTTMessageReveiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String message=intent.getStringExtra("message");
        String topicName=intent.getStringExtra("topicName");

        try {
            String macAddress=topicName.substring(6,topicName.lastIndexOf("/"));
            if (!Utils.isEmpty(macAddress)){
                JSONObject device=new JSONObject(message);
                String wifiVersion="";
                String MCUVerion="";
                int MatTemp=0;
                String workMode="";/**manual:手动模式	timer:定时模式*/
                String LockScreen="";/** open:上锁  close:解锁*/
                String BackGroundLED="";/**open:照明  close:节能*/
                String deviceState="";/**open:开机  close：关机*/
                String tempState="";/**nor:正常 err:异常*/
                String outputMode="";
                int curTemp=0;
                int ratedPower=0;
                String protectEnable="";
                String ctrlMode="";
                int powerValue=0;
                int voltageValue=0;
                int currentValue=0;
                String machineFall="";
                int protectSetTemp=0;
                int protectProTemp=0;

                int timerTaskWeek=0;


                if (device.has("wifiVersion")){
                    wifiVersion=device.getString("wifiVersion");/**版本*/
                }
                if (device.has("MCUVerion")){
                    MCUVerion=device.getString("MCUVerion");
                }
                if (device.has("MatTemp")){
                    MatTemp=device.getInt("MatTemp");/**手动/定时模式下的温度*/
                }
                if (device.has("workMode")){
                    workMode=device.getString("workMode");/**manual:手动模式	timer:定时模式*/
                }
                if (device.has("LockScreen")){
                    LockScreen=device.getString("LockScreen");
                }
                if (device.has("BackGroundLED")){
                    BackGroundLED=device.getString("BackGroundLED");
                }if (device.has("deviceState")){
                    deviceState=device.getString("deviceState");
                }
                if (device.has("tempState")){
                    tempState=device.getString("tempState");
                }
                if (device.has("outputMode")){
                    outputMode=device.getString("outputMode");
                }if (device.has("curTemp")){
                    curTemp=device.getInt("curTemp");
                }
                if (device.has("ratedPower")){
                    ratedPower=device.getInt("ratedPower");
                }
                if (device.has("protectEnable")){
                    protectEnable=device.getString("protectEnable");
                }if (device.has("ctrlMode")){
                    ctrlMode=device.getString("ctrlMode");
                }
                if (device.has("powerValue")){
                    powerValue=device.getInt("powerValue");
                }

                if (device.has("voltageValue")){
                    voltageValue=device.getInt("voltageValue");
                }
                if (device.has("voltageValue")){
                    voltageValue=device.getInt("voltageValue");
                }
                if (device.has("currentValue")){
                    currentValue=device.getInt("currentValue");
                }
                if (device.has("machineFall")){
                    machineFall=device.getString("machineFall");
                }
                if (device.has("protectSetTemp")){
                    protectSetTemp=device.getInt("protectSetTemp");
                }
                if (device.has("protectProTemp")){
                    protectProTemp=device.getInt("protectProTemp");
                }

                macAddress=macAddress.substring(1);
                DeviceChild child=null;
                int groupPostion=0;
                int childPosition=0;

                DeviceGroupDaoImpl deviceGroupDao=new DeviceGroupDaoImpl(context);
                DeviceChildDaoImpl deviceChildDao=new DeviceChildDaoImpl(context);
                TimeTaskDaoImpl timeTaskDao=new TimeTaskDaoImpl(context);
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
                        if (!Utils.isEmpty(wifiVersion))
                            child.setWifiVersion(wifiVersion);

                        if (!Utils.isEmpty(MCUVerion))
                            child.setMCUVerion(MCUVerion);

                        if (!Utils.isEmpty(workMode)){
                            child.setWorkMode(workMode);
                            if (MatTemp!=0) {
                                child.setMatTemp(MatTemp);
                                if ("manual".equals(workMode)){
                                    child.setManualMatTemp(MatTemp);
                                }else if ("timer".equals(workMode)){
                                    child.setTimerTemp(MatTemp);
                                }
                            }
                        }


                        if (!Utils.isEmpty(LockScreen))
                            child.setLockScreen(LockScreen);
                        if (!Utils.isEmpty(BackGroundLED))
                            child.setBackGroundLED(BackGroundLED);
                        if (!Utils.isEmpty(deviceState))
                            child.setDeviceState(deviceState);
                        if (!Utils.isEmpty(tempState))
                            child.setTempState(tempState);
                        if (!Utils.isEmpty(outputMode))
                            child.setOutputMod(outputMode);
                        if (curTemp!=0)
                            child.setCurTemp(curTemp);
                        if (ratedPower!=0)
                            child.setRatedPower(ratedPower);
                        if (!Utils.isEmpty(protectEnable))
                            child.setProtectEnable(protectEnable);
                        if (!Utils.isEmpty(ctrlMode))
                            child.setCtrlMode(ctrlMode);
                        if (powerValue!=0)
                            child.setPowerValue(powerValue);
                        if (voltageValue!=0)
                            child.setVoltageValue(voltageValue);
                        if (currentValue!=0)
                            child.setCurrentValue(currentValue);
                        if (!Utils.isEmpty(machineFall))
                            child.setMachineFall(machineFall);
                        if (protectSetTemp!=0)
                            child.setProtectSetTemp(protectSetTemp);
                        if (protectProTemp!=0)
                            child.setProtectProTemp(protectProTemp);

                        child.setOnLint(true);
                        deviceChildDao.update(child);


                        if (device.has("timerTaskWeek")){
                            timerTaskWeek=device.getInt("timerTaskWeek");
                            long deviceId=child.getId();


                            Map<String,String> map=new HashMap<>();
                            StringBuffer sb=new StringBuffer();
                            for (int i = 0; i < 24; i++) {

                                TimeTask timeTask=new TimeTask();

                                while (i<24){
                                    String h=device.getString("h"+i);//时间
                                    int t=device.getInt("t"+i);
                                    String h2=null;

                                    if (device.has("h"+(i-1))){
                                        h2=device.getString("h"+(i-1));
                                    }


                                    if (!(Utils.isEmpty(h2)) && "off".equals(h2) && "on".equals(h)){
                                        timeTask.setDeviceId(deviceId);
                                        timeTask.setStart(i);
                                        i++;
                                        continue;
                                    }
                                    else if ((!Utils.isEmpty(h2)) && "on".equals(h2) && "on".equals(h)){
                                        i++;
                                        continue;
                                    }
                                    else if ((!Utils.isEmpty(h2)) && "on".equals(h2) && "off".equals(h)){
                                        timeTask.setEnd(i-1);
                                        int temp=device.getInt("t"+(i-1));
                                        timeTask.setTemp(temp);
                                        timeTask.setWeek(timerTaskWeek);
                                        List<TimeTask> timeTasks=timeTaskDao.findWeekAll(deviceId,timerTaskWeek);
                                        TimeTask timeTask2=null;
                                        if ( timeTasks!=null && !timeTasks.isEmpty()){
                                            for (TimeTask task :timeTasks) {
                                                if (timeTask.equals(task)){
                                                    timeTask2=task;
                                                    timeTask2.setTemp(timeTask.getTemp());
                                                    break;
                                                }
                                            }
                                        }
                                        if (timeTask2!=null){
                                            timeTaskDao.update(timeTask2);
                                        }else {
                                            timeTaskDao.insert(timeTask);
                                        }
                                        i++;
                                        break;
                                    }
                                    i++;
                                }
                            }


                        }
                    }
                    if (DeviceFragment.running==1){
                        child=deviceChildDao.findDeviceById(child.getId());
                        Intent mqttIntent=new Intent("DeviceFragment");
                        mqttIntent.putExtra("groupPostion",groupPostion);
                        mqttIntent.putExtra("childPosition",childPosition);
                        mqttIntent.putExtra("deviceState",deviceState);
                        mqttIntent.putExtra("deviceChild",child);
                        context.sendBroadcast(mqttIntent);
                    }else if (HeaterFragment.running){
                        child=deviceChildDao.findDeviceById(child.getId());
                        long houseId=child.getHouseId();
                        long deviceId=child.getId();
                        Intent mqttIntent=new Intent("HeaterFragment");
                        mqttIntent.putExtra("houseId",houseId);
                        mqttIntent.putExtra("deviceId",deviceId);
                        mqttIntent.putExtra("deviceChild",child);
                        context.sendBroadcast(mqttIntent);
                    } else if (TimeTaskActivity.running){
                        Intent mqttIntent=new Intent("TimeTaskActivity");
                        mqttIntent.putExtra("timerTaskWeek",timerTaskWeek);
                        mqttIntent.putExtra("deviceId",child.getId());
                        deviceChildDao.update(child);
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
