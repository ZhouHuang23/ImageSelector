package com.hz.android.imageselector.library;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.hz.android.easyadapter.EasyAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/1/24.
 */

public class ImageSelectorView extends RecyclerView {
    //数据
    private List<String> imagePathList;
    //适配器
    private ImageAdapter imageAdapter;

    //一次读取最大数量
    private static final int MAX_READ_COUNT = 18;
    //标记cursor位置
    private int markedCursorPosition = 0;

    //控制子线程状态
    private boolean isImageReading = false;
    private boolean isImageReadCompleted = false;

    private OnImageSelectedListener onImageSelectedListener;

    private List<Uri> selectedImageUriList;

    public ImageSelectorView(Context context) {
        this(context, null, 0);
    }

    public ImageSelectorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageSelectorView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        imagePathList = new ArrayList<>();
        selectedImageUriList = new ArrayList<>();

        //初始化imageAdapter
        imageAdapter = new ImageAdapter(context, imagePathList);


        imageAdapter.setSelectMode(EasyAdapter.SelectMode.MULTI_SELECT);
        imageAdapter.setOnItemMultiSelectListener(new EasyAdapter.OnItemMultiSelectListener() {
            @Override
            public void onSelected(EasyAdapter.Operation operation, int itemPosition, boolean isSelected) {
                if (operation == EasyAdapter.Operation.ORDINARY) {
                    //保存选中图片的Uri
                    if (!imageAdapter.getSourceList().isEmpty()) {
                        Uri imageUri = Uri.fromFile(new File(imageAdapter.getSourceList().get(itemPosition)));
                        if (imageAdapter.isSelected(itemPosition)) {
                            selectedImageUriList.add(imageUri);
                        } else {
                            selectedImageUriList.remove(imageUri);
                        }
                    }
                } else if (operation == EasyAdapter.Operation.ALL_CANCEL) {
                    selectedImageUriList.clear();
                } else if (operation == EasyAdapter.Operation.REVERSE_SELECTED) {
                    for (int judgePosition = 0; judgePosition < imageAdapter.getItemCount(); judgePosition++) {
                        Uri imageUri = Uri.fromFile(new File(imageAdapter.getSourceList().get(judgePosition)));
                        if (imageAdapter.getMultiSelectedPosition().contains(judgePosition)) {
                            selectedImageUriList.remove(imageUri);
                        } else {
                            selectedImageUriList.add(imageUri);
                        }
                    }
                } else if (operation == EasyAdapter.Operation.ALL_SELECTED) {
                    selectedImageUriList.clear();
                    for (int position = 0; position < imageAdapter.getItemCount(); position++) {
                        selectedImageUriList.add(Uri.fromFile(new File(imageAdapter.getSourceList().get(position))));
                    }
                } else if (operation == EasyAdapter.Operation.SET_MAX_COUNT) {
                    if (imageAdapter.getMultiSelectedPosition().isEmpty()){ //内部已经清空
                        selectedImageUriList.clear();
                    }
                }
                if (onImageSelectedListener != null) {
                    onImageSelectedListener.onImageSelectedCount(selectedImageUriList.size());
                }

            }
        });

        ImageSelectorView.this.setAdapter(imageAdapter);

        readSystemImageData();

        this.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isSlideToBottom(ImageSelectorView.this) && !isImageReadCompleted && !isImageReading) {
                    readSystemImageData();
                }
            }
        });

    }

    //访问数据库 读取数据进list

    private synchronized void readSystemImageData() {
        if (isImageReading || isImageReadCompleted) {
            return;
        }
        isImageReading = true;

        //开启子线程读取
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = getContext().getContentResolver();
                // 只查询jpeg和png的图片
                Cursor cursor = contentResolver.query(imageUri, null,// 查找这个mImageUri资源路径下的资源
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC");


                int imageDataIndex;
                String imageDataPath;

                cursor.moveToPosition(markedCursorPosition);

                int readCount = 0;

                while (cursor.moveToNext() && readCount < MAX_READ_COUNT) { //一次读取 MAX_READ_COUNT 个
                    readCount++;
                    //获取数据库数据
                    imageDataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    imageDataPath = cursor.getString(imageDataIndex);

                    //判断文件路径是否存在
                    if (new File(imageDataPath).exists()) {
                        imagePathList.add(imageDataPath);   //添加数据
                    }
                }
                markedCursorPosition += readCount;

                isImageReading = false;

                if (readCount < MAX_READ_COUNT) { //小于成立 则表示读取完毕
                    isImageReadCompleted = true;
                }
                //关闭cursor
                cursor.close();

                //通知主线程
                post(new Runnable() {
                    @Override
                    public void run() {
                        imageAdapter.updateImagePathList(imagePathList);
                    }
                });
            }
        }).start();

    }

    //========API=========

    /**
     * 获取选择的图片数目
     *
     * @return
     */
    public int getSelectedImageCount() {
        return imageAdapter.getSelectedImageCount();
    }

    /**
     * 设置选中图片的位置
     *
     * @param imageUri
     */
    public void setSelectedImageUri(Uri... imageUri) {
        selectedImageUriList.addAll(Arrays.asList(imageUri));
        imageAdapter.notifyDataSetChanged();
    }

    /**
     * 获取选中图片的Uri
     *
     * @return
     */
    public List<Uri> getSelectedImageUri() {
        return selectedImageUriList;
    }

    /**
     * 获取选择图片的位置
     *
     * @return
     */
    public List<Integer> getImageSelectedPosition() {
        return imageAdapter.getSelectedImagePositionList();
    }

    /**
     * 取消选择的图片
     */
    public void cancelSelected() {
        imageAdapter.cancelSelected();
    }

    /**
     * 全部选择
     */
    public void selectedAll() {
        imageAdapter.selectAll();
    }

    /**
     * 反选
     */
    public void reverseSelectedAll() {
        imageAdapter.reverseSelected();
    }

    /**
     * 设置最大可选
     *
     * @param count 数目
     */
    public void setMaxSelectCount(int count) {
        imageAdapter.setMaxSelectedCount(count);
    }


    /**
     * 设置图片选择监听器
     *
     * @param onImageSelectedListener
     */
    public void setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
        this.onImageSelectedListener = onImageSelectedListener;
    }

    /**
     * 图片选中监听器接口
     */
    public interface OnImageSelectedListener {
        /**
         * 图片选中时回调，返回选中图片总数目
         *
         * @param count
         */
        void onImageSelectedCount(int count);
    }

    /**
     * 判断recycleView是否滑动到底部
     *
     * @param recyclerView
     * @return
     */
    public static boolean isSlideToBottom(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //屏幕中最后一个可见子项的position
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        //当前屏幕所看到的子项个数
        int visibleItemCount = layoutManager.getChildCount();
        //当前RecyclerView的所有子项个数
        int totalItemCount = layoutManager.getItemCount();
        //RecyclerView的滑动状态
        if (visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1) {
            return true;
        } else {
            return false;
        }
    }

}
