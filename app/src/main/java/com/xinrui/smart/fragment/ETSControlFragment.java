package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.adapter.ETSControlAdapter;
import com.xinrui.smart.pojo.ETSControl;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/14.
 */

public class ETSControlFragment extends Fragment {
    View view;
    Unbinder unbinder;
    @BindView(R.id.lv_homes)
    ListView lv_homes;
    @BindView(R.id.tv_home) TextView tv_home;//外置传感器头部
    @BindView(R.id.view) View view2;//传感器尾部
    @BindView(R.id.textView) TextView textView;//外置传感器提示
    private List<ETSControl> mainControls;//外置传感器数量
    private ETSControlAdapter adapter;//外置传感器适配器

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_control,container,false);
        unbinder= ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mainControls=getETSControls();
        adapter=new ETSControlAdapter(getActivity(),mainControls);
        lv_homes.setAdapter(adapter);
        tv_home.setBackgroundResource(R.drawable.shape_header_blue);
        view2.setBackgroundResource(R.drawable.shape_footer);
        textView.setText("外置传感器设备只能单选");
        textView.setPadding(140,0,0,0);
    }
    private List<ETSControl> getETSControls(){
        List<ETSControl> mainControls=new ArrayList<>();
        for (int i=0;i<5;i++){
            ETSControl control=new ETSControl();
            control.setName("智能"+i);
            mainControls.add(control);
        }
        return mainControls;
    }

    /**
     * 解绑界面元素
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
