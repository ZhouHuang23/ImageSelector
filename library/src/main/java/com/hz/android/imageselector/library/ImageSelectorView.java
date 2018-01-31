package com.hz.android.imageselector.library;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.hz.android.easyadapter.EasyAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 使用组合view方法，自定义图片选择器View
 * Created by Administrator on 2018/1/24.
 */

public class ImageSelectorView extends FrameLayout {
    private List<String> imagePathList;
    private ImageAdapter imageAdapter;

    private static final int MAX_READ_COUNT = 18; //一次读取最大数量

    private int markedCursorPosition = 0; //标记cursor位置
    //标记子线程状态
    private boolean isImageReading = false;
    private boolean isImageReadCompleted = false;

    private int itemColumnCount = 3;//默认展示的列数

    private int itemSpace = 5;//默认item的间隔（单位：dp）

    private boolean itemIncludeEdge = false; //默认不包含边缘

    private OnImageSelectedListener onImageSelectedListener;

    private List<Uri> selectedImageUriList;

    public ImageSelectorView(Context context) {
        this(context, null, 0);
    }

    public ImageSelectorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private RecyclerView recyclerView;
    private GridSpacingItemDecoration itemDecoration;

    public ImageSelectorView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        recyclerView = new RecyclerView(getContext());
        addView(recyclerView);

        //设置RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), itemColumnCount)); // 设置图片展的列数
        itemDecoration = new GridSpacingItemDecoration(itemColumnCount, dp2px(getContext(), itemSpace), itemIncludeEdge);
        recyclerView.addItemDecoration(itemDecoration); //设置recycleView中item的间隔、是否包含边缘

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
                    if (imageAdapter.getMultiSelectedPosition().isEmpty()) { //内部已经清空
                        selectedImageUriList.clear();
                    }
                }
                if (onImageSelectedListener != null) {
                    onImageSelectedListener.onImageSelectedCount(selectedImageUriList.size());//通知改变
                }

            }
        });

        recyclerView.setAdapter(imageAdapter);

        readSystemImageData();

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isSlideToBottom(recyclerView) && !isImageReadCompleted && !isImageReading) {
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
                Cursor cursor = contentResolver.query(imageUri, null,// 查找这个imageUri资源路径下的资源
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
     * 设置展示列数
     *
     * @param itemColumnCount
     */
    public void setItemColumnCount(int itemColumnCount) {
        this.itemColumnCount = itemColumnCount;
        // 重新设置布局
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), itemColumnCount));
        recyclerView.removeItemDecoration(itemDecoration);
        itemDecoration = new GridSpacingItemDecoration(itemColumnCount, dp2px(getContext(), itemSpace), itemIncludeEdge);
        recyclerView.addItemDecoration(itemDecoration);

    }

    /**
     * 获取列数
     *
     * @return
     */
    public int getItemColumnCount() {
        return itemColumnCount;
    }

    /**
     * 设置Item间隙
     *
     * @param itemSpace
     */
    public void setItemSpace(int itemSpace) {
        this.itemSpace = itemSpace;
        recyclerView.removeItemDecoration(itemDecoration);
        itemDecoration = new GridSpacingItemDecoration(itemColumnCount, dp2px(getContext(), itemSpace), itemIncludeEdge);
        recyclerView.addItemDecoration(itemDecoration);

    }

    /**
     * 获取item间隙
     *
     * @return
     */
    public int getItemSpace() {
        return itemSpace;
    }


    /**
     * 获取item是否包含边界
     *
     * @return
     */
    public boolean isItemIncludeEdge() {
        return itemIncludeEdge;
    }

    /**
     * 设置Item是否包含边界
     *
     * @param itemIncludeEdge
     */
    public void setItemIncludeEdge(boolean itemIncludeEdge) {
        this.itemIncludeEdge = itemIncludeEdge;
        recyclerView.removeItemDecoration(itemDecoration);
        itemDecoration = new GridSpacingItemDecoration(itemColumnCount, dp2px(getContext(), itemSpace), itemIncludeEdge);
        recyclerView.addItemDecoration(itemDecoration);
    }

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
     * 设置最大可选数目
     *
     * @param count 数目
     */
    public void setMaxSelectCount(int count) {
        imageAdapter.setMaxSelectedCount(count);
    }

    /**
     * 设置选中图片后的标记icon
     *
     * @param selectedIcon
     */
    public void setSelectedIcon(Drawable selectedIcon) {
        imageAdapter.setSelectedIcon(selectedIcon);
    }

    /**
     * 获取选中图片的标记icon
     *
     * @return
     */
    public Drawable getSelectedIcon() {
        return imageAdapter.getSelectedIcon();
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

    /**
     * 工具，dp转换成px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}
