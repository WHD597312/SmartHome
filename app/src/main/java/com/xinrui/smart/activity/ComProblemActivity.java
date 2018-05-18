package com.xinrui.smart.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xinrui.secen.scene_activity.AddEquipmentActivity;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.device.ReasonActivity;
import com.xinrui.smart.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ComProblemActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    @BindView(R.id.tv_set) TextView tv_set;
    @BindView(R.id.listview) ListView listview;
    Unbinder unbinder;
    CommonProblemAdapter adapter;
    private List<String> list;
    MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_problem);
        unbinder=ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);
        list=new ArrayList<>();
        list.add("指示灯不亮，加热器不加热");
        list.add("指示灯不亮，加热器加热");
        list.add("指示灯亮，加热器不加热");
        list.add("控制面板的触摸按键均不起作用");
        list.add("控制面板按键均正常,但加热器不加热");
        list.add("初次使用时有不同程度地烟雾、异味");
        list.add("开关机后一段时间内,机器有嚓嚓声");
        list.add("房间温度上不去");
        list.add("触摸按键不起作用");
        list.add("遥控器不能遥控加热器");
        adapter=new CommonProblemAdapter(this,list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }
    @OnClick({R.id.image_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_back:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                String reason="原因:\n\n1.温控器未打开或温控器损坏\n" +
                        "2.加热器未直立放置";
                String cs="措施:\n\n1.把温控器旋至加热器接通的位置，联系维修\n" +
                        "2.正确放置加热器";
                Intent intent=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent.putExtra("reason",reason);
                intent.putExtra("cs",cs);
                startActivity(intent);
                break;
            case 1:
                String reason2="原因:\n\n1.指示灯损坏";
                String cs2="措施:\n\n1.联系维修，更换指示灯";
                Intent intent2=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent2.putExtra("reason",reason2);
                intent2.putExtra("cs",cs2);
                startActivity(intent2);
                break;
            case 2:
                String reason3="原因:\n\n1.功率旋钮处于“关”位置\n" +
                "2.电加热管烧坏或连接线松脱";
                String cs3="措施:\n\n1.调整旋钮到相应的功率处\n" +
                "2.联系维修";
                Intent intent3=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent3.putExtra("reason",reason3);
                intent3.putExtra("cs",cs3);
                startActivity(intent3);
                break;
            case 3:
                String reason4="原因:\n\n1.未按下电源开关\n" +
                        "2.加热器未直立放置";
                String cs4="措施:\n\n1.按下电源开关使加热器处于待机状态\n" +
                        "2.正确放置加热器";
                Intent intent4=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent4.putExtra("reason",reason4);
                intent4.putExtra("cs",cs4);
                startActivity(intent4);
                break;
            case 4:
                String reason5="原因:\n\n电加热管烧坏或连接线松脱";

                String cs5="措施:\n\n联系维修";
                Intent intent5=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent5.putExtra("reason",reason5);
                intent5.putExtra("cs",cs5);
                startActivity(intent5);
                break;
            case 5:
                String reason6="原因:\n\n散热翅片上少量挥发油残留";

                String cs6="措施:\n\n初次使用时，打开门窗，让室内通风，等其消散后，再关闭门窗。属正常情况";
                Intent intent6=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent6.putExtra("reason",reason6);
                intent6.putExtra("cs",cs6);
                startActivity(intent6);
                break;
            case 6:
                String reason7="原因:\n\n热胀冷缩的原因";

                String cs7="措施:\n\n正常现象，可放心使用";
                Intent intent7=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent7.putExtra("reason",reason7);
                intent7.putExtra("cs",cs7);
                startActivity(intent7);
                break;
            case 7:
                String reason8="原因:\n\n与房间保温效果，环境温度，房间面积相关";
                String cs8="措施:\n\n只要加热器能加热，说明机器正常，建议使用更高功率的加热器或改善房间保温效果";
                Intent intent8=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent8.putExtra("reason",reason8);
                intent8.putExtra("cs",cs8);
                startActivity(intent8);
                break;
            case 8:
                String reason9="原因:\n\n1.已开启了童锁功能\n" +
                        "2.关机键，选择键需触摸2秒方可作用（防误操作）";
                String cs9="措施:\n\n1.打开童锁（按遥控器上解锁键）\n" +
                        "2.触摸达2秒，即可进行相关操作";
                Intent intent9=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent9.putExtra("reason",reason9);
                intent9.putExtra("cs",cs9);
                startActivity(intent9);
                break;
            case 9:
                String reason10="原因:\n\n遥控器发射口没有对准显示屏上的红外接收口";
                String cs10="措施:\n\n操作时，需对准显示屏上的红外接收口";
                Intent intent10=new Intent(ComProblemActivity.this, ReasonActivity.class);
                intent10.putExtra("reason",reason10);
                intent10.putExtra("cs",cs10);
                startActivity(intent10);
                break;
        }
    }

    class CommonProblemAdapter extends BaseAdapter{
        private Context context;
        private List<String> list;


        public CommonProblemAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int position) {
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
                convertView=View.inflate(context,R.layout.item_common_problem,null);
                viewHolder=new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else {
                viewHolder= (ViewHolder) convertView.getTag();
            }
            String s=list.get(position);
            if (!Utils.isEmpty(s)){
                viewHolder.tv_text.setText(s);
            }
            return convertView;
        }
    }
    class ViewHolder{
        @BindView(R.id.tv_text) TextView tv_text;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
