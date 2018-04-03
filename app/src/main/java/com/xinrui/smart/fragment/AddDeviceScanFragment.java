package com.xinrui.smart.fragment;



import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.ScanActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddDeviceScanFragment extends Fragment {
    View view;
    Unbinder unbinder;
    @BindView(R.id.tv_result) TextView tv_result;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_add_device_scan,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }

    @OnClick({R.id.btn_scan})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_scan:
                scanQrCode();
                break;
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    SharedPreferences preferences;
    @Override
    public void onStart() {
        super.onStart();
        preferences=getActivity().getSharedPreferences("scan", Context.MODE_PRIVATE);
        if (preferences.contains("content")){
            String content=preferences.getString("content","");
            tv_result.setText(content);
        }
        Log.d("ss","sssssssssssssssssssssss");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult intentResult=IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult!=null){
            if(intentResult.getContents()==null){
                Toast.makeText(getActivity(),"内容为空",Toast.LENGTH_LONG).show();
            }else{
                String content=intentResult.getContents();

            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**扫描二维码*/
    public void scanQrCode(){
        new IntentIntegrator(getActivity())
                .setOrientationLocked(true)
                .setCaptureActivity(ScanActivity.class)
                .initiateScan();
    }
}
