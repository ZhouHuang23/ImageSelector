package com.hz.android.imageselector.library;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hz.android.easyadapter.EasyAdapter;

import java.util.Arrays;
import java.util.List;


/**
 * Created by Administrator on 2018/1/24.
 */

public class ImageSelectorView2 extends FrameLayout {
    //数据
    private List<String> list = Arrays.asList("a", "b", "c");
    //适配器
    private ImageAdapter imageAdapter;

    public ImageSelectorView2(Context context) {
        this(context, null, 0);
    }

    public ImageSelectorView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageSelectorView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        RecyclerView recyclerView  = new RecyclerView(getContext());
        addView(recyclerView); //直接把recycleview添加进来

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new ImageAdapter());



        /* or
          View view = View.inflate(getContext(),R.layout.recycler_view,null); 渲染一个布局文件并添加到当前容器
           addView(view);
           recyclerView =view.findViewById(image_recycler_view);
           ....
           */

    }


    private class ImageAdapter extends EasyAdapter {

        @Override
        public void whenBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(list.get(position));
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(new TextView(getContext())) {
            };
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
