package com.xinrui.smart.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainControlActivity;
import com.xinrui.smart.adapter.SmartSetAdapter;
import com.xinrui.smart.pojo.SmartSet;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class SmartFragment extends Fragment {

    Unbinder unbinder;
    View view;
    @BindView(R.id.tv_home)
    TextView tv_home;
    @BindView(R.id.tv_home2)
    TextView tv_home2;
    @BindView(R.id.tv_home3)
    TextView tv_home3;
    @BindView(R.id.smart_set)
    ListView smart_set;
    private List<SmartSet> list;
    private SmartSetAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_smart,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter=new SmartSetAdapter(getActivity());
        smart_set.setAdapter(adapter);

        smart_set.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tv_smart= (TextView) view.findViewById(R.id.tv_smart);
                String content=tv_smart.getText().toString();
                Intent intent=new Intent(getActivity(), MainControlActivity.class);
                intent.putExtra("content",content);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }



}
