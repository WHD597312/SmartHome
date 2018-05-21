package com.xinrui.smart.view_custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddDeviceActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by win7 on 2018/3/9.
 */

/**
 * 创建新家
 */
public class AddDeviceDialog extends Dialog {


    @BindView(R.id.add_image) ImageView add_image;

    private Context context;
    public AddDeviceDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
        this.context=context;

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_device);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Glide.with(context)
                .load(R.drawable.touxiang)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop()
                .into(new GlideDrawableImageViewTarget(add_image, 100));
    }






}
