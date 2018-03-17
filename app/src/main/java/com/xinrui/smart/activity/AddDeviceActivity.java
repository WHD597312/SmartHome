package com.xinrui.smart.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.xinrui.smart.R;
import com.xinrui.smart.fragment.AddDeviceScanFragment;
import com.xinrui.smart.fragment.AddDeviceWifiFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddDeviceActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_wifi)
    Button btn_wifi;
    @BindView(R.id.btn_scan)
    Button btn_scan;
    int[] wifi_drawables={R.drawable.shape_btnwifi_connect,R.drawable.shape_btnwifi_noconnect};

    int[] scan_drawables={R.drawable.shape_btnzxscan_connect,R.drawable.shape_btnzxscan_noconnect};
    Fragment[]fragments={new AddDeviceWifiFragment(),new AddDeviceScanFragment()};
    int wifi_drawable;
    int scan_drawable;

    FragmentManager fragmentManager;

    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fragmentManager=getFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragment=fragments[0];
        fragmentTransaction.replace(R.id.linearout_adddevice,fragment);
        fragmentTransaction.commit();
    }

    @OnClick({R.id.btn_wifi,R.id.btn_scan})
    public void onClick(View view){

        switch (view.getId()){
            case R.id.btn_wifi:
                wifi_drawable=wifi_drawables[0];
                wifi_drawables[0]=wifi_drawables[1];
                wifi_drawables[1]=wifi_drawable;
                scan_drawable=scan_drawables[1];
                scan_drawables[1]=scan_drawables[0];
                scan_drawables[0]=scan_drawable;

                btn_wifi.setBackgroundResource(wifi_drawables[0]);
                btn_scan.setBackgroundResource(scan_drawables[1]);
                fragment=fragments[0];
                fragments[0]=fragments[1];
                fragments[1]=fragment;
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.linearout_adddevice,fragments[0]);
                fragmentTransaction.commit();
               break;
            case R.id.btn_scan:
                scan_drawable=scan_drawables[0];
                scan_drawables[0]=scan_drawables[1];
                scan_drawables[1]=scan_drawable;
                wifi_drawable=wifi_drawables[1];
                wifi_drawables[1]=wifi_drawables[0];
                wifi_drawables[0]=wifi_drawable;
                btn_wifi.setBackgroundResource(wifi_drawables[0]);
                btn_scan.setBackgroundResource(scan_drawables[1]);
                fragment=fragments[0];
                fragments[0]=fragments[1];
                fragments[1]=fragment;
                FragmentTransaction fragmentTransaction2=fragmentManager.beginTransaction();
                fragmentTransaction2.replace(R.id.linearout_adddevice,fragments[0]);
                fragmentTransaction2.commit();
                break;
        }
    }
}
