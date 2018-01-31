# FileSelectorView
##### 介绍
ImageSelector 是自定义的图片选择器view，可轻松实现系统图片浏览和选择，用户可自定义图片选择器风格，比如：展示照片的列数、间距。
##### 功能
- 浏览系统图片资源
- 可对图片进行多选、全选、反选、取消选择等操作
- 可获取选中图片的Uri
- 可设置展示图片的列数、间距
- 可设置选中图片风格
##### 使用
ImageSelectorView 使用简单，只需将其加入到布局文件即可，无其他使用限制。

- 布局文件

```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.hz.android.imageselector.demo.MainActivity">

    、、、
    
    <com.hz.android.imageselector.library.ImageSelectorView
        android:id="@+id/image_selector_view"
        android:layout_width="match_parent"
        android:layout_height="400dp" /> 
        
    、、、
        
</LinearLayout>

```
- 代码中

```java
、、、

ImageSelectorView imageSelectorView = (ImageSelectorView) findViewById(R.id.image_selector_view);
 
//设置图片展示列数，默认3列
imageSelectorView.setItemColumnCount(5);
//设置图片间的间距，默认5dp
imageSelectorView.setItemSpace(10);
//设置图片边缘是否有间距，默认false
imageSelectorView.setItemIncludeEdge(true);
//设置选中图片后的标记
imageSelectorView.setSelectedIcon(getResources().getDrawable(R.drawable.selected_icon));//设置选中图片后的标记
 、、、

```
##### 注意
读取文件路径需要涉及到用户的许可：

```
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```



