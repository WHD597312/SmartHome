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
import com.xinrui.smart.adapter.ControlledAdapter;
import com.xinrui.smart.pojo.Controlled;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/14.
 */

/**
 * 受控机
 */
public class ControlledFragment extends Fragment {

    View view;
    Unbinder unbinder;
    @BindView(R.id.lv_homes)
    ListView lv_homes;
    @BindView(R.id.tv_home) TextView tv_home;//受控机头部
    @BindView(R.id.view) View view2;//受控机尾部
    @BindView(R.id.textView) TextView textView;//受控机提示用户
    private List<Controlled> controlleds;
    private ControlledAdapter adapter;//受控机适配器

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_control,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }


    private List<Controlled> getControlleds(){
        List<Controlled> controlleds=new ArrayList<>();
        for (int i=0;i<5;i++){
            Controlled control=new Controlled();
            control.setName("智能"+i);
            controlleds.add(control);
        }
        return controlleds;
    }
    @Override
    public void onStart() {
        super.onStart();
        controlleds=getControlleds();
        adapter=new ControlledAdapter(getActivity(),controlleds);
        lv_homes.setAdapter(adapter);
        tv_home.setBackgroundResource(R.drawable.shape_header);
        view2.setBackgroundResource(R.drawable.shape_footer);
        textView.setText("选中的主控制设备不出现在受控制设备中");
        textView.setPadding(60,0,0,0);
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
}
