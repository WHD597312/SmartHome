package com.xinrui.smart.view_custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.xinrui.smart.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by win7 on 2018/3/9.
 */

/**
 * 创建新家
 */
public class HomeDialog extends Dialog {
    @BindView(R.id.et_homed_name)
    EditText et_name;
    @BindView(R.id.tv_dialog_qx)
    Button button_cancel;
    @BindView(R.id.tv_dialog_qd)
    Button button_ensure;
    Context context;
    private String name;
    public HomeDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
        this.context=context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_home);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int len=s.length();
                if (len>8){
                    Toast.makeText(context,"最多可以输入8个字",Toast.LENGTH_SHORT).show();
                    et_name.setText(s.subSequence(0,8));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OnClick({R.id.tv_dialog_qx, R.id.tv_dialog_qd})
    public void onClick(View view){
        switch(view.getId()){
            case R.id.tv_dialog_qx:
                if (onNegativeClickListener!=null){
                    onNegativeClickListener.onNegativeClick();
                }
                break;
            case R.id.tv_dialog_qd:
                if (onPositiveClickListener!=null){
                    name=et_name.getText().toString();
                    onPositiveClickListener.onPositiveClick();
                }
                break;
        }
    }
    private OnPositiveClickListener onPositiveClickListener;

    public void setOnPositiveClickListener(OnPositiveClickListener onPositiveClickListener) {


        this.onPositiveClickListener = onPositiveClickListener;
    }

    private OnNegativeClickListener onNegativeClickListener;

    public void setOnNegativeClickListener(OnNegativeClickListener onNegativeClickListener) {

        this.onNegativeClickListener = onNegativeClickListener;
    }

    public interface OnPositiveClickListener {
        void onPositiveClick();
    }

    public interface OnNegativeClickListener {
        void onNegativeClick();
    }
}
