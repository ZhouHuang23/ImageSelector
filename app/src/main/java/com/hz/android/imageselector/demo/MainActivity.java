package com.hz.android.imageselector.demo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hz.android.imageselector.library.GridSpacingItemDecoration;
import com.hz.android.imageselector.library.ImageSelectorView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageSelectorView imageSelectorView;
    private Button btnEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageSelectorView = (ImageSelectorView) findViewById(R.id.image_selector_view);
        View btnCancel = findViewById(R.id.btn_cancel);
        btnEnter = (Button) findViewById(R.id.btn_enter);

        int spanCount = 3;
        imageSelectorView.setLayoutManager(new GridLayoutManager(this, spanCount)); // 变成3列显
        imageSelectorView.addItemDecoration(new GridSpacingItemDecoration(spanCount, dp2px(getApplicationContext(), 10), false)); //设置recycleView中item的间隔



       /* imageSelectorView.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageSelectorView.setSelectedImagePosition(0,1,2);// 我是模拟任何时候都可能调用你的接口 现在生效吗 或者我根本不调用你的接口
            }
        },3000);*/


        imageSelectorView.setOnImageSelectedListener(new ImageSelectorView.OnImageSelectedListener() {
            @Override
            public void onImageSelectedCount(int count) {
                btnEnter.setText("确定(" + count + ")");
            }
        });

    }

    public void enter(View view) {
        Toast.makeText(this, "SelectedImageUriList = " + imageSelectorView.getSelectedImageUri().toString(), Toast.LENGTH_SHORT).show();
    }

    public void cancel(View view) {
        imageSelectorView.cancelSelected();
        btnEnter.setText("确定");
    }
    public void allSelected(View view) {
        imageSelectorView.selectedAll();

    }

    public void reverseSelected(View view) {
        imageSelectorView.reverseSelectedAll();

    }
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}
