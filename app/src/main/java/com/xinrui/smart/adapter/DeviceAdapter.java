package com.xinrui.smart.adapter;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter;
import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_util.NetWorkUtil;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.DeviceListActivity;
import com.xinrui.smart.activity.AddDeviceActivity;
import com.xinrui.smart.activity.LoginActivity;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.util.NoFastClickUtils;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.view_custom.DeviceChildDialog;
import com.xinrui.smart.view_custom.MyRecyclerViewItem;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by win7 on 2018/3/12.
 */

public class DeviceAdapter extends GroupedRecyclerViewAdapter {

    private Context context;
    private List<DeviceGroup> groups;
    private ImageView image_switch;
    ArrayList<DeviceChild> list;

    private DeviceChildDaoImpl deviceChildDao;
    private List<List<DeviceChild>> childern;
    MQService mqService;
    TextView tv_device_child;

    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch, R.mipmap.image_switch2};

    int[] colors = {R.color.color_white, R.color.color_orange};
    private int groupPosition = 0;
    private int childPosition = 0;
    String deviceId;

    public DeviceAdapter(Context context, List<DeviceGroup> groups, List<List<DeviceChild>> childern) {
        super(context);

        this.context = context;
        this.groups = groups;
        this.childern = childern;
        deviceChildDao = new DeviceChildDaoImpl(context);
    }

    /**
     * 返回组的项目数
     *
     * @return
     */
    @Override
    public int getGroupCount() {
        return groups == null ? 0 : groups.size();
    }

    /**
     * 返回某一组子条目的数目
     *
     * @param groupPosition
     * @return
     */
    @Override
    public int getChildrenCount(int groupPosition) {
//        ArrayList<DeviceChild> childern=groups.get(groupPosition).getChildern();
//        return childern==null?0:childern.size();

        try {
            return childern.get(groupPosition).size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 是否有组头
     *
     * @param groupPosition
     * @return
     */
    @Override
    public boolean hasHeader(int groupPosition) {
        return true;
    }

    /**
     * 是否有组尾
     *
     * @param groupPosition
     * @return
     */
    @Override
    public boolean hasFooter(int groupPosition) {
        return true;
    }

    /**
     * 组头布局
     *
     * @param viewType
     * @return
     */
    @Override
    public int getHeaderLayout(int viewType) {
        return R.layout.device_adapter_header;
    }

    /**
     * 组尾布局
     *
     * @param viewType
     * @return
     */
    @Override
    public int getFooterLayout(int viewType) {
        return R.layout.device_adapter_footer;
    }

    /**
     * 某一组中子条目的布局
     *
     * @param viewType
     * @return
     */
    @Override
    public int getChildLayout(int viewType) {
        return R.layout.device_adapter_child;
    }

    /**
     * 绑定组头数据
     *
     * @param holder
     * @param groupPosition
     */
    @Override
    public void onBindHeaderViewHolder(final BaseViewHolder holder, final int groupPosition) {
        final DeviceGroup entry = groups.get(groupPosition);

        if (groupPosition == groups.size() - 1) {
            holder.itemView.setBackgroundResource(R.drawable.shape_header_blue);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.shape_header);
        }


        if (holder != null) {
            holder.setText(R.id.tv_header, entry.getHeader());
            TextView tv_open = (TextView) holder.itemView.findViewById(R.id.tv_open);
            TextView tv_close = (TextView) holder.itemView.findViewById(R.id.tv_close);
            tv_open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        holder.setTextColor(R.id.tv_close, context.getResources().getColor(colors[0]));
                        holder.setTextColor(R.id.tv_open, context.getResources().getColor(colors[1]));
                        List<DeviceChild> list = childern.get(groupPosition);
                        if (list != null && list.size() > 0) {
                            for (int i = 0; i < list.size(); i++) {
                                DeviceChild childEntry = list.get(i);
                                if (childEntry.getOnLint()) {
                                    childEntry.setImg(imgs[1]);
                                    changeChild(groupPosition,childPosition);
                                    childEntry.setDeviceState("open");
                                    deviceChildDao.update(childEntry);
                                    send(childEntry);
//                                    changeChildren(groupPosition,childPosition);
                                } else {
                                    childEntry.setImg(imgs[0]);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            tv_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        holder.setTextColor(R.id.tv_close, context.getResources().getColor(colors[1]));
                        holder.setTextColor(R.id.tv_open, context.getResources().getColor(colors[0]));
                        List<DeviceChild> list = childern.get(groupPosition);
                        if (list != null && list.size() > 0) {
                            for (int i = 0; i < list.size(); i++) {
                                DeviceChild childEntry = list.get(i);
                                if (childEntry.getOnLint()) {
                                    childEntry.setImg(imgs[0]);
                                    childEntry.setDeviceState("close");
                                    deviceChildDao.update(childEntry);
                                    send(childEntry);
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }


//        tv_open.setTextColor(colors[0]);
    }

    /**
     * 绑定组尾数据
     *
     * @param holder
     * @param groupPosition
     */
    @Override
    public void onBindFooterViewHolder(BaseViewHolder holder, final int groupPosition) {
        ImageView image_footer = (ImageView) holder.itemView.findViewById(R.id.image_footer);
        if (image_footer == null) {
            return;
        }
        image_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddDeviceActivity.class);
                DeviceGroup deviceGroup = groups.get(groupPosition);
                intent.putExtra("houseId", deviceGroup.getId() + "");

                if (groupPosition == groups.size() - 1) {
                    intent.putExtra("wifi", "share");
                } else {
                    intent.putExtra("wifi", "wifi");
                }
                context.startActivity(intent);
            }
        });
    }

    /**
     * 绑定某一组中子条目的数据
     * @param holder
     * @param groupPosition
     * @param childPosition
     */
    /**
     * 绑定某一组中子条目的数据
     *
     * @param holder
     * @param groupPosition
     * @param childPosition
     */

    @Override
    public void onBindChildViewHolder(final BaseViewHolder holder, final int groupPosition, final int childPosition) {
        final DeviceChild entry = childern.get(groupPosition).get(childPosition);

        holder.setText(R.id.tv_device_child, entry.getDeviceName());
        holder.setImageResource(R.id.image_switch, entry.getImg());


        tv_device_child = (TextView) holder.itemView.findViewById(R.id.tv_device_child);
        TextView tv_state = (TextView) holder.itemView.findViewById(R.id.tv_state);
        if (entry.getOnLint()) {
            if (entry.getType() == 1) {
                if (entry.getControlled() == 2 || entry.getControlled() == 0) {
                    tv_state.setText(entry.getRatedPower() + "w");
                } else if (entry.getControlled() == 1) {
                    tv_state.setText("受控机模式");
                }
            } else if (entry.getType() == 2) {
                tv_state.setText("温度：" + entry.getTemp() + "℃");
            }
        } else {
            tv_state.setText("离线");
        }

        if (entry.getType() == 1) {
            if (entry.getControlled() == 2) {
                holder.setImageResource(R.id.image_device_child, R.mipmap.master);
                holder.setVisible(R.id.image_switch, View.VISIBLE);
            } else if (entry.getControlled() == 1) {
                holder.setImageResource(R.id.image_device_child, R.mipmap.controlled);
                holder.setVisible(R.id.image_switch, View.GONE);
            } else if (entry.getControlled() == 0) {
                holder.setImageResource(R.id.image_device_child, R.mipmap.heater2);
                holder.setVisible(R.id.image_switch, View.VISIBLE);
            }
        } else if (entry.getType() == 2) {
            holder.setImageResource(R.id.image_device_child, R.mipmap.estsensor);
            holder.setVisible(R.id.image_switch, View.GONE);
        }

        tv_device_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entry.getOnLint()) {
                    if (entry.getType() == 1) {
                        if (entry.getControlled() == 2 || entry.getControlled() == 0) {
                            DeviceChild deviceChild = childern.get(groupPosition).get(childPosition);
                            long id = deviceChild.getId();
                            Intent intent = new Intent(context, DeviceListActivity.class);
                            intent.putExtra("content", deviceChild.getDeviceName());
                            intent.putExtra("childPosition", id + "");
                            context.startActivity(intent);
                        } else if (entry.getControlled() == 1) {
                            Utils.showToast(context, "受控机不能操作");
                        }
                    } else if (entry.getType() == 2) {
                        Utils.showToast(context, "外置传感器不能操作");
                    }
                } else {
                    Utils.showToast(context, "该设备离线");
                }
            }
        });
        String mac = entry.getMacAddress();
        image_switch = (ImageView) holder.itemView.findViewById(R.id.image_switch);
        image_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NoFastClickUtils.isFastClick()){
                    if (entry.getOnLint()) {
                        String mac = entry.getMacAddress();
                        if (entry.getImg() == imgs[0]) {
                            if (bound) {
                                try {
                                    entry.setImg(imgs[1]);
                                    entry.setDeviceState("open");
                                    deviceChildDao.update(entry);
                                    send(entry);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (entry.getImg() == imgs[1]) {
                            if (bound) {
                                try {
                                    entry.setImg(imgs[0]);
                                    entry.setDeviceState("close");
                                    deviceChildDao.update(entry);
                                    send(entry);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
//                holder.setImageResource(R.id.image_switch,img);
                        changeChild(groupPosition, childPosition);
                    } else {
                        Utils.showToast(context, "该设备离线");
                    }
                }else {
                    Utils.showToast(context,"主人，请对我温柔点!");
                }
            }
        });
        Button btn_editor = (Button) holder.itemView.findViewById(R.id.btn_editor);
        btn_editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDialog(groupPosition, childPosition);
            }
        });
        Button btn_delete = (Button) holder.itemView.findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    DeviceAdapter.this.groupPosition = groupPosition;
                    DeviceAdapter.this.childPosition = childPosition;
                    new DeleteDeviceAsync().execute(entry);

            }
        });

        MyRecyclerViewItem myRecyclerViewItem = (MyRecyclerViewItem) holder.itemView.findViewById(R.id.scroll_item);
        myRecyclerViewItem.reset();
    }

    private void buildDialog(final int groupPosition, final int childPosition) {
        final DeviceChildDialog dialog = new DeviceChildDialog(context);
        dialog.setOnPositiveClickListener(new DeviceChildDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                String child = dialog.getName();
                if (!Utils.isEmpty(child)) {
                    DeviceChild deviceChild = childern.get(groupPosition).get(childPosition);
                    deviceChild.setDeviceName(child);
                    DeviceAdapter.this.groupPosition = groupPosition;
                    DeviceAdapter.this.childPosition = childPosition;
                    new UpdateDeviceNameAsync().execute(deviceChild);
                    dialog.dismiss();
                } else {
                    Utils.showToast(context, "设备名称不能为空");
                }
            }
        });
        dialog.setOnNegativeClickListener(new DeviceChildDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    class UpdateDeviceNameAsync extends AsyncTask<DeviceChild, Void, Integer> {

        @Override
        protected Integer doInBackground(DeviceChild... deviceChildren) {
            int code = 0;
            DeviceChild deviceChild = deviceChildren[0];
            try {
                String updateDeviceNameUrl = "http://120.77.36.206:8082/warmer/v1.0/device/changeDeviceName?deviceId=" +
                        URLEncoder.encode(deviceChild.getId() + "", "UTF-8") + "&newName=" + URLEncoder.encode(deviceChild.getDeviceName(), "UTF-8");
                String result = HttpUtils.getOkHpptRequest(updateDeviceNameUrl);
                JSONObject jsonObject = new JSONObject(result);
                code = jsonObject.getInt("code");
                if (code == 2000) {
                    deviceChildDao.update(deviceChild);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    Utils.showToast(context, "修改成功");
                    changeChild(groupPosition, childPosition);
                    break;
                case -3009:
                    Utils.showToast(context, "修改失败");
                    break;
            }
        }
    }

    class DeleteDeviceAsync extends AsyncTask<DeviceChild, Void, Integer> {

        @Override
        protected Integer doInBackground(DeviceChild... deviceChildren) {
            int code = 0;
            DeviceChild deviceChild = deviceChildren[0];
            try {
                String houseId=null;
                if (Long.MAX_VALUE==deviceChild.getHouseId()){
                    houseId=deviceChild.getShareHouseId()+"";
                }else {
                    houseId=deviceChild.getHouseId()+"";
                }
                SharedPreferences preferences = context.getSharedPreferences("my", Context.MODE_PRIVATE);
                String userId = preferences.getString("userId", "");
                String updateDeviceNameUrl = "http://120.77.36.206:8082/warmer/v1.0/device/deleteDevice?deviceId=" +
                        URLEncoder.encode(deviceChild.getId() + "", "UTF-8") + "&userId=" + URLEncoder.encode(userId, "UTF-8")
                        + "&houseId="+URLEncoder.encode(houseId,"UTF-8");
//                String updateDeviceNameUrl="http://192.168.168.3:8082/warmer/v1.0/device/deleteDevice?deviceId=6&userId=1&houseId=1000";
//                String updateDeviceNameUrl="http://192.168.168.10:8082/warmer/v1.0/device/deleteDevice?deviceId=1004&userId=1&&houseId=1001";
                String result = HttpUtils.getOkHpptRequest(updateDeviceNameUrl);
                JSONObject jsonObject = new JSONObject(result);
                code = jsonObject.getInt("code");
                if (code == 2000) {
                    deviceChildDao.delete(deviceChild);
                    TimeTaskDaoImpl timeTaskDao = new TimeTaskDaoImpl(context);
                    TimeDaoImpl timeDao = new TimeDaoImpl(context);
                    List<TimeTask> timeTasks = timeTaskDao.findTimeTasks(deviceChild.getId());
                    for (TimeTask timeTask : timeTasks) {
                        timeTaskDao.delete(timeTask);
                    }
                    List<Timer> timers = timeDao.findAll(deviceChild.getId());
                    for (Timer timer : timers) {
                        timeDao.delete(timer);
                    }
                    childern.get(groupPosition).remove(childPosition);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    Utils.showToast(context, "解除设备成功");
                    List<DeviceChild> children=deviceChildDao.findAllDevice();
                    if (children==null || children.isEmpty()){
                        context.startActivity(new Intent(context,MainActivity.class));
                    }else {
                        notifyDataSetChanged();
                    }
                    break;
                case -3009:
                    Utils.showToast(context, "解除设备失败");
                    break;
            }
        }
    }

    public ServiceConnection getConnection() {
        return connection;
    }

    public MessageReceiver getMessageReceiver() {
        return new MessageReceiver();
    }

    boolean bound = false;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                DeviceChild child = null;
                int groupPostion = intent.getIntExtra("groupPostion", 0);
                int childPosition = intent.getIntExtra("childPosition", 0);
                String deviceState = intent.getStringExtra("deviceState");
                String noNet = intent.getStringExtra("noNet");
                String Net=intent.getStringExtra("Net");
                if (!Utils.isEmpty(Net)){
                    for (int i = 0; i < groups.size(); i++) {
                        List<DeviceChild> deviceChildren = childern.get(i);
                        for (int j = 0; j < deviceChildren.size(); j++) {
                            DeviceChild deviceChild = deviceChildren.get(j);
                            deviceChild.setOnLint(true);
                            send(deviceChild);
                            if ("open".equals(deviceChild.getDeviceState())) {
                                deviceChild.setImg(imgs[2]);
                            }
                            childern.get(i).set(j, deviceChild);
                        }
                    }
                    changeChildren(groupPostion);
                } else if (!Utils.isEmpty(noNet)) {
                    for (int i = 0; i < groups.size(); i++) {
                        List<DeviceChild> deviceChildren = childern.get(i);
                        for (int j = 0; j < deviceChildren.size(); j++) {
                            DeviceChild deviceChild = deviceChildren.get(j);
                            deviceChild.setOnLint(false);
                            if ("open".equals(deviceChild.getDeviceState())) {
                                deviceChild.setImg(imgs[2]);
                            }
                            childern.get(i).set(j, deviceChild);
                        }
                    }
                    changeChildren(groupPostion);
                } else if (Utils.isEmpty(Net) && Utils.isEmpty(noNet)){
                    DeviceChild deviceChild = (DeviceChild) intent.getSerializableExtra("deviceChild");
                    if (deviceChild == null) {
                        DeviceChild deviceChild2 = childern.get(groupPostion).get(childPosition);
                        if (deviceChild2 != null) {
                            try {
                                List<DeviceChild> deviceChildren = childern.get(groupPostion);
                                for (int i = 0; i < deviceChildren.size(); i++) {
                                    DeviceChild deviceChild3 = deviceChildren.get(i);
                                    if (deviceChild3.getType() == 1 && deviceChild3.getControlled() == 1) {
                                        deviceChild3.setControlled(0);
                                        childern.get(groupPostion).set(i, deviceChild3);
                                    }
                                    if (deviceChild3.getType() == 2 && deviceChild3.getControlled() == 1) {
                                        deviceChild3.setControlled(0);
                                        childern.get(groupPostion).set(i, deviceChild3);
                                    }
                                }
                                childern.get(groupPostion).remove(deviceChild2);
                                Utils.showToast(context, "该设备已重置");

                               List<DeviceChild> children=deviceChildDao.findAllDevice();
                               if (children==null || children.isEmpty()){
                                   context.startActivity(new Intent(context,MainActivity.class));
                               }else {
                                   notifyDataSetChanged();
                               }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (deviceChild != null) {

                        List<DeviceChild> deviceChildren = childern.get(groupPostion);

                        if (deviceChildren.get(childPosition) == null) {
                            childern.get(groupPostion).add(childPosition, deviceChild);
                            child = deviceChild;
                            changeChild(groupPostion, childPosition);
                        } else {
                            childern.get(groupPostion).set(childPosition, deviceChild);
                            child = deviceChild;
                            changeChild(groupPostion, childPosition);
                        }
                    }

                    if (deviceChild != null && deviceChild.getOnLint() && child != null) {
                        if ("close".equals(deviceState)) {
                            if (deviceChild != null) {
                                DeviceChild child2 = deviceChild;
                                child.setRatedPower(child2.getRatedPower());
                                child2.setImg(imgs[0]);
                                child.setImg(imgs[0]);
                                child.setOnLint(true);
                                child2.setOnLint(true);
                                child.setControlled(child2.getControlled());
                                deviceChildDao.update(child2);
//                    deviceChildDao.update(child);
                                changeChild(groupPostion, childPosition);
                            }
                        } else if ("open".equals(deviceState)) {
                            if (child != null) {
                                DeviceChild child2 = deviceChild;
                                child.setRatedPower(child2.getRatedPower());
                                child2.setImg(imgs[1]);
                                child.setImg(imgs[1]);
                                child.setOnLint(true);
                                child2.setOnLint(true);
                                child2.setRatedPower(child2.getRatedPower());
                                child.setControlled(child2.getControlled());
                                deviceChildDao.update(child2);
//                    deviceChildDao.update(child);
                                changeChild(groupPostion, childPosition);
                            }
                        }
                    } else if (deviceChild!=null&& !deviceChild.getOnLint()) {
                        DeviceChild child2 = deviceChild;
                        child.setRatedPower(child2.getRatedPower());
                        child2.setImg(imgs[0]);
                        child.setImg(imgs[0]);
                        if ("open".equals(child.getDeviceState())) {
                            child.setImg(imgs[2]);
                            child2.setImg(imgs[2]);
                        }

                        child.setOnLint(false);
                        child2.setOnLint(false);
                        child2.setRatedPower(child2.getRatedPower());
                        deviceChildDao.update(child2);
//                    deviceChildDao.update(child);
                        changeChild(groupPostion, childPosition);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void send(DeviceChild deviceChild) {
        try {
            if (deviceChild != null) {
                JSONObject maser = new JSONObject();
                maser.put("ctrlMode", deviceChild.getCtrlMode());
                maser.put("workMode", deviceChild.getWorkMode());
                maser.put("MatTemp", deviceChild.getManualMatTemp());
                maser.put("TimerTemp", deviceChild.getTimerTemp());
                maser.put("LockScreen", deviceChild.getLockScreen());
                maser.put("BackGroundLED", deviceChild.getBackGroundLED());
                maser.put("deviceState", deviceChild.getDeviceState());
                maser.put("tempState", deviceChild.getTempState());
                maser.put("outputMode", deviceChild.getOutputMod());
                maser.put("protectProTemp", deviceChild.getProtectProTemp());
                maser.put("protectSetTemp", deviceChild.getProtectSetTemp());

                String s = maser.toString();
                boolean success = false;
                String topicName;
                String mac = deviceChild.getMacAddress();
                if (deviceChild.getType() == 1 && deviceChild.getControlled() == 2) {
                    String houseId=deviceChild.getHouseId()+"";
                    topicName = "rango/masterController/"+houseId+"/"+mac+"/set";
                    if (bound) {
                        success = mqService.publish(topicName, 2, s);
                    }
                } else {
                    topicName = "rango/" + mac + "/set";
                    if (bound) {
                        success = mqService.publish(topicName, 2, s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
