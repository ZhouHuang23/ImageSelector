package com.hz.android.imageselector.library;

import android.content.Context;
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


    public ImageAdapter(Context context, List<String> sourceList) {
        this.sourceList = new ArrayList<>(sourceList);
        this.context = context;
    }

    public void updateImagePathList(List<String> list) {
        this.sourceList = new ArrayList<>(list);
        notifyDataSetChanged();
    }


    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.item_image, null);
        return new ImageViewHolder(view);
    }

    @Override
    public void whenBindViewHolder(ImageViewHolder holder, int position) {
        holder.sourceImage.setTag(position);
        holder.selectedImage.setTag(position);
        Uri imageUri = Uri.fromFile(new File(sourceList.get(position)));
        //holder.sourceImage.setImageURI(imageUri); // picasso
        Picasso.with(context).load(imageUri).into(holder.sourceImage);

        if (isSelected(position)) {
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

}
