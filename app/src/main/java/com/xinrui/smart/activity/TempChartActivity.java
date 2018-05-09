package com.xinrui.smart.activity;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.xinrui.chart.LineChartManager;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**温度曲线
 * */
public class TempChartActivity extends AppCompatActivity {

    MyApplication application;
    private Unbinder unbinder;
    @BindView(R.id.line_chart) LineChart line_chart;
    @BindView(R.id.tv_power) TextView tv_power;
    @BindView(R.id.tv_voltage) TextView tv_voltage;
    @BindView(R.id.tv_current) TextView tv_current;
    private DeviceChildDaoImpl deviceChildDao;
    public static boolean running=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_chart);
        unbinder=ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            application.removeActivity(this);/**退出主页面*/
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    String deviceId;
    DeviceChild deviceChild;
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=getIntent();
        deviceId=intent.getStringExtra("deviceId");
        deviceChildDao=new DeviceChildDaoImpl(this);
        deviceChild=deviceChildDao.findDeviceById(Integer.parseInt(deviceId));

        tv_power.setText("功率:"+deviceChild.getRatedPower()+"w");
        tv_voltage.setText("电压:"+deviceChild.getVoltageValue()+"v");
        tv_current.setText("电流:"+deviceChild.getCurrentValue()+"A");
        new TempChatAsync().execute();
    }

    MessageReceiver receiver;
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("TempChartActivity");
        receiver = new MessageReceiver();
        registerReceiver(receiver, intentFilter);
        running=true;
    }

    @OnClick({R.id.img_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.img_back:
                finish();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceChildDao.closeDaoSession();
        deviceChild=null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
        if (receiver!=null){
            unregisterReceiver(receiver);
        }

        running=false;
    }

    class TempChatAsync extends AsyncTask<Void,Void,List<Integer>>{

        @Override
        protected List<Integer> doInBackground(Void... voids) {
            int code=0;
            List<Integer> list=null;
            String tempUrl="http://120.77.36.206:8082/warmer/v1.0/device/everyHourTemp?deviceId="+deviceId;
            try {
                String result=HttpUtils.getOkHpptRequest(tempUrl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject2=new JSONObject(result);
                    code=jsonObject2.getInt("code");
                    if (code==2000){
                        list=new ArrayList<>();
                        JSONObject jsonObject=jsonObject2.getJSONObject("content");
                        int one=jsonObject.getInt("one");
                        int two=jsonObject.getInt("two");
                        int three=jsonObject.getInt("three");
                        int four=jsonObject.getInt("four");
                        int five=jsonObject.getInt("five");
                        int six=jsonObject.getInt("six");
                        int seven=jsonObject.getInt("seven");
                        int eight=jsonObject.getInt("eight");
                        int nine=jsonObject.getInt("nine");
                        int ten=jsonObject.getInt("ten");
                        int eleven=jsonObject.getInt("eleven");
                        int twelve=jsonObject.getInt("twelve");
                        int thirteen=jsonObject.getInt("thirteen");
                        int fourteen=jsonObject.getInt("fourteen");
                        int fifteen=jsonObject.getInt("fifteen");
                        int sixteen=jsonObject.getInt("sixteen");
                        int seventeen=jsonObject.getInt("seventeen");
                        int eighteen=jsonObject.getInt("eighteen");
                        int nineteen=jsonObject.getInt("nineteen");
                        int twenty=jsonObject.getInt("twenty");
                        int twentyOne=jsonObject.getInt("twentyOne");
                        int twentyTwo=jsonObject.getInt("twentyTwo");
                        int twentyThree=jsonObject.getInt("twentyThree");
                        int twentyFour=jsonObject.getInt("twentyFour");
                        list.add(one);
                        list.add(two);
                        list.add(three);
                        list.add(four);
                        list.add(five);
                        list.add(six);
                        list.add(seven);
                        list.add(eight);
                        list.add(nine);
                        list.add(ten);
                        list.add(eleven);
                        list.add(twelve);
                        list.add(thirteen);
                        list.add(fourteen);
                        list.add(fifteen);
                        list.add(sixteen);
                        list.add(seventeen);
                        list.add(eighteen);
                        list.add(nineteen);
                        list.add(twenty);
                        list.add(twentyOne);
                        list.add(twentyTwo);
                        list.add(twentyThree);
                        list.add(twentyFour);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Integer> doubles) {
            super.onPostExecute(doubles);
            if (!doubles.isEmpty()){

                LineChartManager lineChartManager1 = new LineChartManager(line_chart);


                //设置x轴的数据
                ArrayList<Integer> xValues = new ArrayList<>();
                for (int i = 0; i <24; i++) {
                    xValues.add(i);
                }

                //设置y轴的数据()
                List<List<Integer>> yValues = new ArrayList<>();

                yValues.add(doubles);

                //颜色集合
                List<Integer> colours = new ArrayList<>();
                colours.add(Color.GREEN);
                colours.add(Color.BLUE);
                colours.add(Color.RED);
                colours.add(Color.CYAN);

                //线的名字集合
                List<String> names = new ArrayList<>();
                names.add("温度曲线");
                names.add("折线二");
                names.add("折线三");
                names.add("折线四");

                //创建多条折线的图表
                lineChartManager1.showLineChart(xValues, yValues.get(0), names.get(0), colours.get(3));
                lineChartManager1.setDescription("温度");
                lineChartManager1.setYAxis(42, 0, 24);
                lineChartManager1.setHightLimitLine(42,"高温报警",Color.RED);
            }
        }
    }

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String macAddress=intent.getStringExtra("macAddress");
            String noNet=intent.getStringExtra("noNet");
            if (!Utils.isEmpty(noNet)){
                Utils.showToast(TempChartActivity.this,"网络已断开，请设置网络");
            }else {
                if (!Utils.isEmpty(macAddress) && deviceChild.getMacAddress().equals(macAddress)){
                    Utils.showToast(TempChartActivity.this,"该设备已被重置");
                    Intent intent2=new Intent(TempChartActivity.this,MainActivity.class);
                    intent2.putExtra("deviceList","deviceList");
                    startActivity(intent2);
                }
            }
        }
    }
}
