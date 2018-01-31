package com.hz.android.imageselector.library;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hz.android.easyadapter.EasyAdapter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/24.
 */

public class ImageAdapter extends EasyAdapter<ImageAdapter.ImageViewHolder> {

    private List<String> sourceList;
    private Context context;
    private Drawable selectedIcon = null;

    public ImageAdapter(Context context, List<String> sourceList) {
        this.sourceList = new ArrayList<>(sourceList);
        this.context = context;
        setHasStableIds(true); // 解决刷新 闪烁问题
    }

    public void updateImagePathList(List<String> list) {
        sourceList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) { //固定每个位置的itemID
        //return super.getItemId(position);
        return position;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.item_image, null);
        return new ImageViewHolder(view);
    }

    @Override
    public void whenBindViewHolder(final ImageViewHolder holder, int position) {
        holder.sourceImage.setTag(position);
        holder.selectedImage.setTag(position);
        final Uri imageUri = Uri.fromFile(new File(sourceList.get(position)));
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                int width = holder.sourceImage.getWidth();
                Picasso.with(context).load(imageUri).resize(width, width).centerCrop().into(holder.sourceImage);
            }
        };

        if (holder.sourceImage.getWidth() > 0) {  //尺寸已确定，直接加载图片
            task.run();
        } else {
            holder.sourceImage.post(task);
        }

        if (isSelected(position)) {
            if (selectedIcon != null) {
                holder.selectedImage.setImageDrawable(selectedIcon);
            }
            holder.selectedImage.setVisibility(View.VISIBLE);
        } else {
            holder.selectedImage.setVisibility(View.GONE);
        }
    }


    public List<String> getSourceList() {
        return sourceList;
    }

    public List<Integer> getSelectedImagePositionList() {
        return getMultiSelectedPosition();
    }

    public void setSelectedImagePosition(int... imagePositions) {
        setSelected(imagePositions);
    }

    public int getSelectedImageCount() {
        return getMultiSelectedPosition().size();
    }

    public void cancelSelected() {
        clearSelected();
    }

    public Drawable getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(Drawable selectedIcon) {
        this.selectedIcon = selectedIcon;
        notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    @Override

    public int getItemCount() {
        return sourceList.size();
    }

    public static class ImageViewHolder extends ViewHolder {
        private ImageView sourceImage;
        private ImageView selectedImage;

        public ImageViewHolder(View itemView) {
            super(itemView);
            sourceImage = (ImageView) itemView.findViewById(R.id.source_image_view);
            selectedImage = (ImageView) itemView.findViewById(R.id.select_image_view);
        }
    }

    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}
