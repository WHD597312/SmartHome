package com.xinrui.smart.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.xinrui.smart.R;
import com.xinrui.smart.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ClockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);


        try {
            String s=Utils.getJson("china_city_data.json",this);
            JSONArray jsonArray=new JSONArray(s);
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                String name=jsonObject.getString("name");
                JSONArray array=jsonObject.getJSONArray("cityList");
                for (int j=0;j<array.length();j++){
                    JSONObject object=array.getJSONObject(j);
                    String name2=object.getString("name");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
