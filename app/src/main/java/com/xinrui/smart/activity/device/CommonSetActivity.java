package com.xinrui.smart.activity.device;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.secen.scene_activity.AddEquipmentActivity;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.ComProblemActivity;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.activity.PersonInfoActivity;
import com.xinrui.smart.pojo.CommonSet;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.DeviceUpdateAppDialog;
import com.xinrui.smart.view_custom.DeviceUpdatePersonDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class CommonSetActivity extends AppCompatActivity {

    Unbinder unbinder;
    @BindView(R.id.listview) ListView listview;
    MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_set);
        unbinder=ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
            application.addActivity(this);
        }
    }


    String main;
    String common;
    String device;
    String change;
    String smart;
    String live;
    private List<CommonSet> list;
    CommonSetAdapter adapter;
    private int mPosition=-1;
    DeviceUpdateAppDialog dialog;
    @Override
    protected void onStart() {
        super.onStart();
        if (dialog==null){
            dialog = new DeviceUpdateAppDialog(this);
        }
        Intent intent=getIntent();
        main=intent.getStringExtra("main");
        common=intent.getStringExtra("common");
        device = intent.getStringExtra("device");
        change = intent.getStringExtra("change");
        smart=intent.getStringExtra("smart");
        live=intent.getStringExtra("live");

        CommonSet commonSet=new CommonSet("清除缓存","当前版本1.0",R.mipmap.clear_cache);
        CommonSet commonSe2=new CommonSet("检查当前版本更新","当前App版本版本rango 1.0",R.mipmap.app_update);
        CommonSet commonSe3=new CommonSet("刷新用户配置","点击立即和服务器同步账户信息",R.mipmap.image_refresh);
        list=new ArrayList<>();
        list.add(commonSet);
        list.add(commonSe2);
        list.add(commonSe3);

        adapter=new CommonSetAdapter(this,list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
//                        Utils.showToast(CommonSetActivity.this,"缓存已清!");
                        mPosition=0;
                        buildUpdatePersonDialog();
                        if (dialog!=null){
                            dialog.et_name.setText("是否立即清除缓存?");
                        }
                        break;
                    case 1:
                        Utils.showToast(CommonSetActivity.this,"已经是最新版本啦!");
                        mPosition=1;
                        break;
                    case 2:
                        Utils.showToast(CommonSetActivity.this,"同步成功!");
                        break;
                }
            }
        });

    }
    @OnClick({R.id.image_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_back:
                if (!Utils.isEmpty(device)){
                    Intent intent=new Intent(this,MainActivity.class);
                    intent.putExtra("deviceList","deviceList");
                    startActivity(intent);
                } else if (!Utils.isEmpty(smart)){
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("mainControl","mainControl");
                    startActivity(intent);
                }else if (!Utils.isEmpty(live)){
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("live","live");
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!Utils.isEmpty(device)){
            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("deviceList","deviceList");
            startActivity(intent);
        } else if (!Utils.isEmpty(smart)){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("mainControl","mainControl");
            startActivity(intent);
        }else if (!Utils.isEmpty(live)){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("live","live");
            startActivity(intent);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }


    private void buildUpdatePersonDialog() {

        dialog.setOnNegativeClickListener(new DeviceUpdateAppDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnPositiveClickListener(new DeviceUpdateAppDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                switch (mPosition){
                    case 0:
                        Utils.showToast(CommonSetActivity.this,"缓存已清");
                        dialog.dismiss();
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        });
        dialog.show();
    }

    class CommonSetAdapter extends BaseAdapter {
        private Context context;
        private List<CommonSet> list;


        public CommonSetAdapter(Context context, List<CommonSet> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public CommonSet getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder=null;

            if (convertView==null){
                convertView=View.inflate(context,R.layout.item_app_set,null);
                viewHolder=new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else {
                viewHolder= (ViewHolder) convertView.getTag();
            }
            CommonSet commonSet=list.get(position);
            if (commonSet!=null){
                viewHolder.image_clear_cache.setImageResource(commonSet.getImg());
                viewHolder.tv_clear_cache.setText(commonSet.getS1());
                viewHolder.tv_current.setText(commonSet.getS2());
            }
            return convertView;
        }
    }
    class ViewHolder{
       @BindView(R.id.image_clear_cache) ImageView image_clear_cache;
       @BindView(R.id.tv_clear_cache) TextView tv_clear_cache;
       @BindView(R.id.tv_current) TextView tv_current;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
