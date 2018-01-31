package com.hz.android.imageselector.demo;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
        btnEnter = (Button) findViewById(R.id.btn_enter);

        //设置
        imageSelectorView.setItemColumnCount(5);
        imageSelectorView.setItemSpace(10);
        imageSelectorView.setItemIncludeEdge(true);
        imageSelectorView.setSelectedIcon(getResources().getDrawable(R.drawable.selected_icon));//设置选中图片后的标记

        imageSelectorView.setOnImageSelectedListener(new ImageSelectorView.OnImageSelectedListener() {
            @Override
            public void onImageSelectedCount(int count) {
                btnEnter.setText("确定(" + count + ")");
            }
        });

    }

    public void enter(View view) {
        List<Uri> selectedImageUri = imageSelectorView.getSelectedImageUri(); //获取选中图片的Uri
        Toast.makeText(this, "SelectedImageUriList = " + selectedImageUri.toString(), Toast.LENGTH_SHORT).show();
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


}
