package com.xinrui.smart.fragment;

import android.app.Service;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceHomeDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.DeviceAdapter;
import com.xinrui.smart.pojo.DeviceHome;
import com.xinrui.smart.pojo.GroupEntry;
import com.xinrui.smart.pojo.GroupModel;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.DeviceHomeDialog;
import com.xinrui.smart.view_custom.DividerItemDecoration;
import com.xinrui.smart.view_custom.OnRecyclerItemClickListener;
import com.xinrui.smart.view_custom.SwipeRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/8.
 */

public class DeviceFragment extends Fragment {
    private View view;
    private Unbinder unbinder;
    /** children items with a key and value list */
    @BindView(R.id.rv_list) SwipeRecyclerView rv_list;

    ArrayList<GroupEntry> groups;
    DeviceAdapter adapter;
    private ItemTouchHelper itemTouchHelper;

    private DeviceHomeDaoImpl deviceHomeDao;
    private List list;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_device,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }
    private int group;
    @Override
    public void onStart() {
        super.onStart();

        deviceHomeDao=new DeviceHomeDaoImpl(getActivity());
        rv_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        groups= GroupModel.getGroups(10,5);
        adapter=new DeviceAdapter(getActivity(),groups);
        rv_list.setAdapter(adapter);


        rv_list.addOnItemTouchListener(new OnRecyclerItemClickListener(rv_list) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder viewHolder) {

            }
            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {
                //判断被拖拽的是否是前两个，如果不是则执行拖拽
                if (vh.getLayoutPosition() != 0 && vh.getLayoutPosition() != 1) {
                    itemTouchHelper.startDrag(vh);
                    //获取系统震动服务
                    Vibrator vib = (Vibrator) getActivity().getSystemService(Service.VIBRATOR_SERVICE);//震动70毫秒
                    vib.vibrate(70);
                }
            }
        });
        itemTouchHelper=new ItemTouchHelper(new ItemTouchHelper.Callback() {

            /**
             * 是否处理滑动事件 以及拖拽和滑动的方向 如果是列表类型的RecyclerView的只存在UP和DOWN，如果是网格类RecyclerView则还应该多有LEFT和RIGHT
             * @param recyclerView
             * @param viewHolder
             * @return
             */
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                           ItemTouchHelper.RIGHT;
                    final int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                } else {
                    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    final int swipeFlags = 0;
//                    final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
            }


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //得到当拖拽的viewHolder的Position
                int fromPosition = viewHolder.getAdapterPosition();
                Log.d("fromPosition","fromPosition："+fromPosition);
                //拿到当前拖拽到的item的viewHolder
                int toPosition = target.getAdapterPosition();
                Log.d("toPosition","toPosition："+toPosition);
                int count=0;
                int childFrom=0;
                int childTo=0;
                for(int i=0;i<groups.size();i++){
                    if (count==toPosition){
                        Log.d("count","count："+count);
                        break;
                    }
                    count++;
                    group=i;
                    Log.d("group","group："+group);

                    Log.d("count","count："+count);
                    for (int j=0;j<groups.get(i).getChildern().size();j++){
                        if (count==fromPosition){
                            childFrom=j;
                            Log.d("count","count："+count);
                        }
                        if (count==toPosition){
                            childTo=j;
                            Log.d("count","count："+count);
                            break;
                        }
                        count++;
                    }
                }
                if (fromPosition <toPosition) {
                    for(int i=childFrom;i<childTo;i++){
                        Collections.swap(groups.get(group).getChildern(),i,i+1);
                    }
                } else {
                    for (int i = childFrom; i > childTo; i--) {
                        Collections.swap(groups.get(group).getChildern(), i, i - 1);
                    }
                }
                adapter.notifyItemMoved(fromPosition,toPosition);
//                adapter.changeGroup(group);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//                int position = viewHolder.getAdapterPosition();
//                myAdapter.notifyItemRemoved(position);
//                datas.remove(position);
            }
            /**
             * 重写拖拽可用
             * @return
             */
            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
            /**
             * 长按选中Item的时候开始调用
             *
             * @param viewHolder
             * @param actionState
             */
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            /**
             * 手指松开的时候还原
             * @param recyclerView
             * @param viewHolder
             */
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setBackgroundColor(Color.WHITE);
//                adapter.notifyDataSetChanged();

            }
        });
        itemTouchHelper.attachToRecyclerView(rv_list);
    }




    @OnClick({R.id.btn_add_residence})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_add_residence:
                buildDialog();
                break;
        }
    }
    private void buildDialog(){
        final DeviceHomeDialog dialog=new DeviceHomeDialog(getActivity());

        dialog.setOnNegativeClickListener(new DeviceHomeDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new DeviceHomeDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                String name=dialog.getName();
                DeviceHome home=new DeviceHome();
                home.setName(name);
                boolean success=deviceHomeDao.insert(home);
                if (success){
                    Utils.showToast(getActivity(),"创建成功");
                    dialog.dismiss();
                }else {
                    Utils.showToast(getActivity(),"创建失败");
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }





}