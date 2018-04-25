package com.xinrui.smart.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.xinrui.chart.LineChartManager;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
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
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=getIntent();
        deviceId=intent.getStringExtra("deviceId");

        LineChartManager lineChartManager1 = new LineChartManager(line_chart);


        //设置x轴的数据
        ArrayList<Float> xValues = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            xValues.add((float) i);
        }

        //设置y轴的数据()
        List<List<Float>> yValues = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<Float> yValue = new ArrayList<>();
            for (int j = 0; j <= 10; j++) {
                yValue.add((float) (Math.random() * 80));
            }
            yValues.add(yValue);
        }

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
        lineChartManager1.setYAxis(100, 0, 11);
        lineChartManager1.setHightLimitLine(70,"高温报警",Color.RED);
        new TempChatAsync().execute();
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
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    class TempChatAsync extends AsyncTask<Void,Void,List<Double>>{

        @Override
        protected List<Double> doInBackground(Void... voids) {
            int code=0;
            List<Double> list=null;
            String tempUrl="http://120.77.36.206:8082/warmer/v1.0/device/everyHourTemp?deviceId="+deviceId;
            try {
                String result=HttpUtils.getOkHpptRequest(tempUrl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject2=new JSONObject(result);
                    code=jsonObject2.getInt("code");
                    if (code==2000){
                        list=new ArrayList<>();
                        JSONObject jsonObject=jsonObject2.getJSONObject("content");
                        double one=jsonObject.getDouble("one");
                        double two=jsonObject.getDouble("two");
                        double three=jsonObject.getDouble("three");
                        double four=jsonObject.getDouble("four");
                        double five=jsonObject.getDouble("five");
                        double six=jsonObject.getDouble("six");
                        double seven=jsonObject.getDouble("seven");
                        double eight=jsonObject.getDouble("eight");
                        double nine=jsonObject.getDouble("nine");
                        double ten=jsonObject.getDouble("ten");
                        double eleven=jsonObject.getDouble("eleven");
                        double twelve=jsonObject.getDouble("twelve");
                        double thirteen=jsonObject.getDouble("thirteen");
                        double fourteen=jsonObject.getDouble("fourteen");
                        double fifteen=jsonObject.getDouble("fifteen");
                        double sixteen=jsonObject.getDouble("sixteen");
                        double seventeen=jsonObject.getDouble("seventeen");
                        double eighteen=jsonObject.getDouble("eighteen");
                        double nineteen=jsonObject.getDouble("nineteen");
                        double twenty=jsonObject.getDouble("twenty");
                        double twentyOne=jsonObject.getDouble("twentyOne");
                        double twentyTwo=jsonObject.getDouble("twentyTwo");
                        double twentyThree=jsonObject.getDouble("twentyThree");
                        double twentyFour=jsonObject.getDouble("twentyFour");
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
    }
}
