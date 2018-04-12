package com.xinrui.smart.adapter;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter;
import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddDeviceActivity;
import com.xinrui.smart.activity.ClockActivity;
import com.xinrui.smart.activity.DeviceListActivity;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.GroupEntry;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.view_custom.DeviceChildDialog;
import com.xinrui.smart.view_custom.DeviceHomeDialog;
import com.xinrui.smart.view_custom.MyRecyclerViewItem;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by win7 on 2018/3/12.
 */

public class DeviceAdapter extends GroupedRecyclerViewAdapter{

    private Context context;
    private List<DeviceGroup> groups;
    private ImageView image_switch;
    ArrayList<DeviceChild> list;

    private DeviceChildDaoImpl deviceChildDao;
    private List<List<DeviceChild>> childern;
    MQService mqService;
    TextView tv_device_child;

    int[] imgs={R.mipmap.image_unswitch, R.mipmap.image_switch};

    int []colors={R.color.color_white,R.color.color_orange};
    private int groupPosition=0;
    private int childPosition=0;
    public DeviceAdapter(Context context, List<DeviceGroup> groups,List<List<DeviceChild>> childern) {
        super(context);

        this.context=context;
        this.groups = groups;
        this.childern=childern;
        deviceChildDao=new DeviceChildDaoImpl(context);
    }

    /**
     * 返回组的项目数
     * @return
     */
    @Override
    public int getGroupCount() {
        return groups==null?0:groups.size();
    }

    /**
     * 返回某一组子条目的数目
     * @param groupPosition
     * @return
     */
    @Override
    public int getChildrenCount(int groupPosition) {
//        ArrayList<DeviceChild> childern=groups.get(groupPosition).getChildern();
//        return childern==null?0:childern.size();

        try {
            return childern.get(groupPosition).size();
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 是否有组头
     * @param groupPosition
     * @return
     */
    @Override
    public boolean hasHeader(int groupPosition) {
        return true;
    }

    /**
     * 是否有组尾
     * @param groupPosition
     * @return
     */
    @Override
    public boolean hasFooter(int groupPosition) {
        return true;
    }

    /**
     * 组头布局
     * @param viewType
     * @return
     */
    @Override
    public int getHeaderLayout(int viewType) {
        return R.layout.device_adapter_header;
    }

    /**
     * 组尾布局
     * @param viewType
     * @return
     */
    @Override
    public int getFooterLayout(int viewType) {
        return R.layout.device_adapter_footer;
    }

    /**
     * 某一组中子条目的布局
     * @param viewType
     * @return
     */
    @Override
    public int getChildLayout(int viewType) {
        return R.layout.device_adapter_child;
    }

    /**
     * 绑定组头数据
     * @param holder
     * @param groupPosition
     */
    @Override
    public void onBindHeaderViewHolder(final BaseViewHolder holder, final int groupPosition) {
        DeviceGroup entry=groups.get(groupPosition);

        if (groupPosition==groups.size()-1){
            holder.itemView.setBackgroundResource(R.drawable.shape_header_blue);
        }else {
            holder.itemView.setBackgroundResource(R.drawable.shape_header);
        }


        if (holder!=null){
            holder.setText(R.id.tv_header,entry.getHeader());
            TextView tv_open=(TextView) holder.itemView.findViewById(R.id.tv_open);
            TextView tv_close= (TextView) holder.itemView.findViewById(R.id.tv_close);
            tv_open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        List<DeviceChild> list=childern.get(groupPosition);
                        if (list!=null && list.size()>0){
                            for (int i=0;i<list.size();i++){
                                DeviceChild childEntry=list.get(i);
                                childEntry.setImg(imgs[1]);
                            }
                            holder.setTextColor(R.id.tv_close,context.getResources().getColor(colors[0]));
                            holder.setTextColor(R.id.tv_open,context.getResources().getColor(colors[1]));
                            changeChildren(groupPosition);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            tv_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        List<DeviceChild> list=childern.get(groupPosition);
                        if (list!=null && list.size()>0){
                            for (int i=0;i<list.size();i++){
                                DeviceChild childEntry=list.get(i);
                                childEntry.setImg(imgs[0]);
                            }
                            holder.setTextColor(R.id.tv_close,context.getResources().getColor(colors[1]));
                            holder.setTextColor(R.id.tv_open,context.getResources().getColor(colors[0]));
                            changeChildren(groupPosition);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });
        }


//        tv_open.setTextColor(colors[0]);
    }

    /**
     * 绑定组尾数据
     * @param holder
     * @param groupPosition
     */
    @Override
    public void onBindFooterViewHolder(BaseViewHolder holder, final int groupPosition) {
        ImageView image_footer= (ImageView) holder.itemView.findViewById(R.id.image_footer);
        if (image_footer==null){
            return;
        }
        image_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, AddDeviceActivity.class);
                DeviceGroup deviceGroup=groups.get(groupPosition);
                intent.putExtra("houseId",deviceGroup.getId()+"");


                if (groupPosition==groups.size()-1){
                    intent.putExtra("wifi","share");
                }else {
                    intent.putExtra("wifi","wifi");
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
     * @param holder
     * @param groupPosition
     * @param childPosition
     */

    @Override
    public void onBindChildViewHolder(final BaseViewHolder holder, final int groupPosition, final int childPosition) {
        final DeviceChild entry=childern.get(groupPosition).get(childPosition);
        holder.setText(R.id.tv_device_child,entry.getDeviceName());
        holder.setImageResource(R.id.image_switch,entry.getImg());
        if (entry.getImg()==imgs[1]){
            holder.setText(R.id.tv_state,"在线");
        }else {
            holder.setText(R.id.tv_state,"离线");
        }
        tv_device_child= (TextView) holder.itemView.findViewById(R.id.tv_device_child);

        if (entry.getType()==1){
            if (entry.getControlled()==2){
                holder.setImageResource(R.id.image_device_child,R.mipmap.master);
            }else if (entry.getControlled()==1){
                holder.setImageResource(R.id.image_device_child,R.mipmap.controlled);
            }else if (entry.getControlled()==0){
                holder.setImageResource(R.id.image_device_child,R.mipmap.heater2);
            }
        }else if (entry.getType()==2){
            holder.setImageResource(R.id.image_device_child,R.mipmap.estsensor);
        }

        tv_device_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entry.getImg()==imgs[1]){

                    DeviceChild deviceChild =childern.get(groupPosition).get(childPosition);
                    long id=deviceChild.getId();
                    Intent intent=new Intent(context, DeviceListActivity.class);
                    intent.putExtra("content","取暖器");
                    intent.putExtra("childPosition",id+"");
                    context.startActivity(intent);
                }else {
                    Utils.showToast(context,"设备不在线");
                }
            }
        });
        String mac=entry.getMacAddress();



        image_switch= (ImageView) holder.itemView.findViewById(R.id.image_switch);
        image_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mac=entry.getMacAddress();
                if (entry.getImg()==imgs[0]){
                    if (bound){
                        try {
                            JSONObject jsonObject=new JSONObject();
                            jsonObject.put("id",entry.getMacAddress());
                            jsonObject.put("deviceState","open");
                            String s=jsonObject.toString();
                            boolean open=false;
                            String topicName;
                            if (entry.getType()==1 && entry.getControlled()==2){
                                topicName="warmer1.0/"+mac+"/masterController/set";
                                open=mqService.publish(topicName,2,s);
                            }else {
                                topicName="warmer1.0/"+mac+"/set";
                                open=mqService.publish(topicName,2,s);
                            }
                            if (open){
                                entry.setImg(imgs[1]);
                                holder.setText(R.id.tv_state,"在线");
                                deviceChildDao.update(entry);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }else if(entry.getImg()==imgs[1]){
                    if (bound){
                        try {
                            JSONObject jsonObject=new JSONObject();
                            jsonObject.put("id",entry.getMacAddress());
                            jsonObject.put("deviceState","close");
                            String s=jsonObject.toString();
                            boolean open=false;
                            String topicName;
                            if (entry.getType()==1 && entry.getControlled()==2){
                                topicName="warmer1.0/"+mac+"/masterController/set";
                                open=mqService.publish(topicName,2,s);
                            }else {
                                topicName="warmer1.0/"+mac+"/set";
                                open=mqService.publish(topicName,2,s);
                            }
                            if (open){
                                entry.setImg(imgs[0]);
                                holder.setText(R.id.tv_state,"离线");
                                deviceChildDao.update(entry);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
//                holder.setImageResource(R.id.image_switch,img);
                changeChild(groupPosition,childPosition);
            }
        });
        ImageView btn_editor= (ImageView) holder.itemView.findViewById(R.id.btn_editor);
        btn_editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groups.size()-1==groupPosition){
                    Utils.showToast(context,"分享设备不能更改");
                }else {
                    buildDialog(groupPosition,childPosition);
                }

            }
        });
        ImageView btn_delete= (ImageView) holder.itemView.findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groups.size()-1==groupPosition){
                    Utils.showToast(context,"分享设备不能删除");
                }else {
                    DeviceAdapter.this.groupPosition=groupPosition;
                    DeviceAdapter.this.childPosition=childPosition;
                    new DeleteDeviceAsync().execute(entry);
                }
            }
        });

        MyRecyclerViewItem myRecyclerViewItem= (MyRecyclerViewItem) holder.itemView.findViewById(R.id.scroll_item);
        myRecyclerViewItem.reset();
    }
    private void buildDialog(final int groupPosition, final int childPosition){
        final DeviceChildDialog dialog=new  DeviceChildDialog(context);
        dialog.setOnPositiveClickListener(new DeviceChildDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                String child=dialog.getName();
                if (!Utils.isEmpty(child)){
                    DeviceChild deviceChild=childern.get(groupPosition).get(childPosition);
                    deviceChild.setDeviceName(child);
                    DeviceAdapter.this.groupPosition=groupPosition;
                    DeviceAdapter.this.childPosition=childPosition;
                    new UpdateDeviceNameAsync().execute(deviceChild);
                    dialog.dismiss();
                }else {
                    Utils.showToast(context,"设备名称不能为空");
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
    class UpdateDeviceNameAsync extends AsyncTask<DeviceChild,Void,Integer>{

        @Override
        protected Integer doInBackground(DeviceChild... deviceChildren) {
            int code=0;
            DeviceChild deviceChild=deviceChildren[0];
            try {
                String updateDeviceNameUrl="http://120.77.36.206:8082/warmer/v1.0/device/changeDeviceName?deviceId="+
                        URLEncoder.encode(deviceChild.getId()+"","UTF-8")+"&newName="+URLEncoder.encode(deviceChild.getDeviceName(),"UTF-8");
                String result=HttpUtils.getOkHpptRequest(updateDeviceNameUrl);
                JSONObject jsonObject=new JSONObject(result);
                code=jsonObject.getInt("code");
                if (code==2000){
                    deviceChildDao.update(deviceChild);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code){
                case 2000:
                    Utils.showToast(context,"修改成功");
                    changeChild(groupPosition,childPosition);
                    break;
                case -3009:
                    Utils.showToast(context,"修改失败");
                    break;
            }
        }
    }
    class DeleteDeviceAsync extends AsyncTask<DeviceChild,Void,Integer>{

        @Override
        protected Integer doInBackground(DeviceChild... deviceChildren) {
            int code=0;
            DeviceChild deviceChild=deviceChildren[0];
            try {
                SharedPreferences preferences =context.getSharedPreferences("my", Context.MODE_PRIVATE);
                String userId=preferences.getString("userId","");
                String updateDeviceNameUrl="http://120.77.36.206:8082/warmer/v1.0/device/deleteDevice?deviceId="+
                        URLEncoder.encode(deviceChild.getId()+"","UTF-8")+"&userId="+URLEncoder.encode(userId,"UTF-8")
                        +"&houseId="+URLEncoder.encode(deviceChild.getHouseId()+"","UTF-8");
//                String updateDeviceNameUrl="http://192.168.168.3:8082/warmer/v1.0/device/deleteDevice?deviceId=6&userId=1&houseId=1000";
//                String updateDeviceNameUrl="http://192.168.168.10:8082/warmer/v1.0/device/deleteDevice?deviceId=1004&userId=1&&houseId=1001";
                String result=HttpUtils.getOkHpptRequest(updateDeviceNameUrl);
                JSONObject jsonObject=new JSONObject(result);
                code=jsonObject.getInt("code");
                if (code==2000){
                    deviceChildDao.delete(deviceChild);
                    childern.get(groupPosition).remove(childPosition);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code){
                case 2000:
                    Utils.showToast(context,"解除设备成功");
                    notifyDataSetChanged();
                    break;
                case -3009:
                    Utils.showToast(context,"解除设备失败");
                    break;
            }
        }
    }

    public ServiceConnection getConnection() {
        return connection;
    }
    public MessageReceiver getMessageReceiver(){
        return new MessageReceiver();
    }

    boolean bound=false;
    ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder= (MQService.LocalBinder) service;
            mqService=binder.getService();
            bound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound=false;
        }
    };

    public class  MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String topicName=intent.getStringExtra("topicName");
            String message=intent.getStringExtra("message");

            if (!Utils.isEmpty(message) && !Utils.isEmpty(topicName)){
                int groupPostion=0;
                int childPosition=0;
                try {
                    JSONObject jsonObject=new JSONObject(message);
                    String macAddress=jsonObject.getString("id");
                    DeviceChild child=null;

                    Log.d("ss","-->"+topicName+","+message);

                    for (List<DeviceChild> deviceChildren :childern){

                        childPosition=0;
                        for (DeviceChild deviceChild:deviceChildren){
                            String mac=deviceChild.getMacAddress();
                            if (!Utils.isEmpty(macAddress) && macAddress.equals(mac)) {
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

                    String deviceState=jsonObject.getString("deviceState");
                    if ("close".equals(deviceState)){
                        if (child!=null){
                            child.setImg(imgs[0]);
                            deviceChildDao.update(child);
                            changeChild(groupPosition,childPosition);
                        }
                    }else if ("open".equals(deviceState)){
                        if (child!=null){
                            child.setImg(imgs[1]);
                            deviceChildDao.update(child);
                            changeChild(groupPosition,childPosition);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }
}
