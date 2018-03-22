package com.xinrui.smart.view_custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
public class DeviceHomeDialog extends Dialog {
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.button_cancel)
    Button button_cancel;
    @BindView(R.id.button_ensure)
    Button button_ensure;
    private String name;
    public DeviceHomeDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_devicehome);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        name=et_name.getText().toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OnClick({R.id.button_cancel, R.id.button_ensure})
    public void onClick(View view){
        switch(view.getId()){
            case R.id.button_cancel:
                if (onNegativeClickListener!=null){
                    onNegativeClickListener.onNegativeClick();
                }
                break;
            case R.id.button_ensure:
                if (onPositiveClickListener!=null){
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
