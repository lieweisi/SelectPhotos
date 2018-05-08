# SelectPhotos
1.图片选择控件，可绑定在activity与fragment上，使用简单方便。
##效果预览
![image](https://github.com/lieweisi/SelectPhotos/blob/master/selectPhoto.gif)

<br>`gif图片由于录屏软件没搞好，看起会有点慢，实际运行不存在，可以直接clone项目运行查看效果。`
##演示demo下载
[演示apk](https://github.com/lieweisi/SelectPhotos/blob/master/selectPhoto.apk)
### 引入方式：
1.在项目bulid.gradle中添加
    allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
2.在app的bulid.gradle中添加引用  
    compile 'com.github.lieweisi:SelectPhotos:v1.0'
    
### 具体使用步骤：
    1.在布局添加：
    <com.liluo.library.SelectPhotoView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/upv_sporttask"/>
    2.在activity或fragment里面进行初始化
      selectPhotoView = (SelectPhotoView) findViewById(R.id.upv_sporttask);
      selectPhotoView.bind(this);//进行绑定
      selectPhotoView.setMaxPhotos(5);//设置选择图片最大数
      selectPhotoView.setCamera(true);//设置选择图片时是否有拍照功能
      selectPhotoView.onItemClickListener();//单击
      selectPhotoView.onItemLongClickListener()//长按
    2.1在onactivityresulut里面接收返回结果
     @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SelectPhotoView.REQUEST_CODE) {
            selectPhotoView.onActivityResult(requestCode, resultCode, data);
            List<SelectPhotoView.PhotoBean> pics = selectPhotoView.getAttachments();
            //selectPhotoView.getAttachments();这是返回地址
        }
    }
备注:每个人的需求都不相同，此项目只完成基本功能，如需要定制可下载项目在源码基础上进行修改，后续还会陆续加入一些常见操作。
