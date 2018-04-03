package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xinrui.smart.R;
import com.xinrui.smart.adapter.ETSControlAdapter;
import com.xinrui.smart.adapter.MainControlAdapter;
import com.xinrui.smart.pojo.MainControl;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/14.
 */

public class MainControlFragment extends Fragment {
    View view;
    Unbinder unbinder;
    @BindView(R.id.lv_homes)
    ListView lv_homes;
    private List<MainControl> mainControls;//主控机数量
    private MainControlAdapter adapter;//主控制设置适配器
    public int runing=0;
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
        mainControls=getMainControls();
        adapter=new MainControlAdapter(getActivity(),mainControls);
        lv_homes.setAdapter(adapter);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private List<MainControl> getMainControls(){
        List<MainControl> mainControls=new ArrayList<>();
        for (int i=0;i<5;i++){
            MainControl control=new MainControl();
            control.setName("智能"+i);
            mainControls.add(control);
        }
        return mainControls;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ETSControlAdapter.checked==false){
            ETSControlAdapter.checked=true;
        }

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
        if (ETSControlAdapter.checked==false){
            ETSControlAdapter.checked=true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ETSControlAdapter.checked==false){
            ETSControlAdapter.checked=true;
        }
    }


}
