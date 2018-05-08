package com.liluo.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.liluo.library.base.BaseRecyclerViewAdapter;
import com.liluo.library.base.BaseViewHolder;
import com.liluo.library.util.CommUtil;
import com.liluo.library.util.LogUtils;
import com.liluo.library.util.SizeUtils;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumWrapper;
import com.yanzhenjie.album.GalleryWrapper;

import java.util.ArrayList;
import java.util.List;

import static android.animation.ValueAnimator.REVERSE;
import static android.app.Activity.RESULT_OK;
import static android.view.animation.Animation.INFINITE;

/**
 * Created by liuhang on 2017/11/6.
 */
public class SelectPhotoView extends LinearLayout implements BaseRecyclerViewAdapter.OnItemClickListener, BaseRecyclerViewAdapter.OnItemChildClickListener, BaseRecyclerViewAdapter.OnItemLongClickListener {
    RecyclerView mRecyclerView;
    public static final int DEFAULT_COUNT = 4;
    public static final int REQUEST_CODE  = 1001;
    int spanCount   = 4;
    int columnCount = 3;
    int maxPhotos   = 6;//最大图片选择数
    Drawable uploadDrawable;//
    String  albumTitle = "图库";//图库taitle
    Boolean hasCamear  = true;
    Context         mContext;
    List<PhotoBean> mList;
    UploadAdapter   adapter;
    Fragment        fragment;
    Activity        activity;

    public SelectPhotoView(@NonNull Context context) {
        super(context, null);
    }

    public SelectPhotoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs);

    }


    public SelectPhotoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setWillNotDraw(false);//onDraw()方法不被执行的解决方法(setWillNotDraw)
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.selectRecycler);
        spanCount = typedArray.getInteger(R.styleable.selectRecycler_spanCount, DEFAULT_COUNT);
        columnCount = typedArray.getInteger(R.styleable.selectRecycler_spanCount, 3);
        uploadDrawable = typedArray.getDrawable(R.styleable.selectRecycler_uploadImage);
        albumTitle = typedArray.getString(R.styleable.selectRecycler_albumTitle);
        hasCamear = typedArray.getBoolean(R.styleable.selectRecycler_camera, true);
        View view = inflate(context, R.layout.view_select_recycler, this);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.upload_recycler);
        GridLayoutManager layoutm = new GridLayoutManager(context, spanCount);
        layoutm.setSmoothScrollbarEnabled(true);
        layoutm.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(layoutm);
        mRecyclerView.setNestedScrollingEnabled(false);
        mList = new ArrayList<>();
        mList.add(new PhotoBean("add"));
        adapter = new UploadAdapter(mList);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        adapter.setOnItemChildClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    public void setMaxPhotos(int maxPhotos) {
        this.maxPhotos = maxPhotos;
    }

    public void bind(Activity activity) {
        this.activity = activity;
        albumWrapper = Album.album(activity);
        galleryWrapper = Album.gallery(activity);
    }

    AlbumWrapper   albumWrapper;
    GalleryWrapper galleryWrapper;

    public void bind(Fragment fragment) {
        this.fragment = fragment;
        albumWrapper = Album.album(fragment);
        galleryWrapper = Album.gallery(fragment);
    }

    public void setCamera(boolean hasCamear) {
        this.hasCamear = hasCamear;

    }

    @Override
    public void onItemClickListener(View view, int position) {
        if (activity == null && fragment == null) {
            throw new UnsupportedOperationException("you muset bind activity or fragment");
        }

        if (fragment != null) {
            albumWrapper = Album.album(fragment);
        } else {
            albumWrapper = Album.album(activity);
        }
        if (position + 1 == adapter.getDatas().size()) {
            if (maxPhotos + 1 - mList.size() < 1) {
                Toast.makeText(mContext, "最多只能选择" + maxPhotos + "张图片!", Toast.LENGTH_LONG).show();
                return;
            }
            adapter.setEdit(false);
            albumWrapper.title(albumTitle) // 配置title。
                    .selectCount(maxPhotos + 1 - mList.size()) // 最多选择几张图片。
                    .columnCount(spanCount) // 相册展示列数，默认是2列。
                    .camera(hasCamear) // 是否有拍照功能。
                    .start(REQUEST_CODE); // 999是请求码，返回时onActivityResult()的第一个参数。
        } else {
            if (mList.size() <= 1) {
                return;
            }
            ArrayList<String> showList = new ArrayList<>();
            for (PhotoBean bean : mList) {
                showList.add(bean.getPath());
            }
            showList.remove(showList.size() - 1);
            galleryWrapper
                    .checkedList(showList) // 要预览的图片list。
                    .currentPosition(position) // 预览的时候要显示list中的图片的index。
                    .start();
        }
    }

    @Override
    public void onItemChildClick(BaseRecyclerViewAdapter adapter, View view, int position) {
        //删除图片 TODO
        attachments.remove(position);
        LogUtils.i(mList.size());
        adapter.removeItem(position);
    }

    @Override
    public boolean onItemLongClickListener(View view, int position) {
        adapter.setEdit(true);
        return false;
    }


    public class UploadAdapter extends BaseRecyclerViewAdapter<PhotoBean, BaseViewHolder> {

        public UploadAdapter(List<PhotoBean> datas) {
            super(datas);
        }

        @Override
        public int getLayoutId(int viewType) {
            return R.layout.item_select;
        }


        @Override
        public void bindView(BaseViewHolder holder, PhotoBean s, int position) {
            ImageView view = holder.getView(R.id.iv_img);
            View cover = holder.getView(R.id.tv_cover);
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            int width = (CommUtil.getScreenWidth(mContext) - (spanCount + 1) * SizeUtils.dp2px(10)) / spanCount;
            lp.width = width;
            lp.height = width;
            view.setLayoutParams(lp);
            cover.setLayoutParams(lp);
            ImageView deleteView = holder.getView(R.id.iv_delimg);
            Log.e("liluo", "mDatas:" + mDatas.size() + "postion:" + position);
            if (mDatas.size() == 1) {
                Glide.with(mContext).load(R.mipmap.icon_addalbum).into(view);
                deleteView.setVisibility(GONE);
                cover.setVisibility(GONE);
            } else {
                if (position + 1 == mDatas.size()) {
                    Glide.with(mContext).load(R.mipmap.icon_addalbum).into(view);
                    deleteView.setVisibility(GONE);
                    cover.setVisibility(GONE);
                } else {
                    view.setImageBitmap(BitmapFactory.decodeFile(s.getPath()));
                    deleteView.setVisibility(isEdit ? VISIBLE : GONE);
                    if (isEdit) {
                        deleteView.setAnimation(shakeAnimation(5));
                    } else {
                        deleteView.clearAnimation();
                    }
                }

            }
            holder.addClickListener(R.id.iv_delimg);
        }
    }

    /**
     * 晃动动画
     * @param counts
     *         1秒钟晃动多少下
     * @return
     */
    public static Animation shakeAnimation(int counts) {
        Animation translateAnimation = new TranslateAnimation(-0.5f, 0.5f, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        translateAnimation.setDuration(1000);
        translateAnimation.setRepeatMode(REVERSE);
        translateAnimation.setRepeatCount(INFINITE);
        return translateAnimation;
    }

    /**
     * 选择后返回
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                ArrayList<String> pathList = Album.parseResult(data);
                List<PhotoBean> templist = new ArrayList<>();
                for (String s : pathList) {
                    templist.add(new PhotoBean(s));
                    attachments.add(new PhotoBean(s));
                }
                adapter.addData(mList.size() - 1, templist);
            }
        }
    }

    List<PhotoBean> attachments = new ArrayList<>();

    public void setAttachments(List<PhotoBean> list) {
        if (list == null) {
            return;
        }
        this.attachments = list;
        List<PhotoBean> temp = new ArrayList<>();
        for (PhotoBean att : attachments) {
            PhotoBean photoBean = att;
            photoBean.setType(1);
            temp.add(photoBean);
        }
        adapter.addData(0, temp);
    }

    /**
     * 获取上传成功的附件
     * @return
     */
    public List<PhotoBean> getAttachments() {
        if (attachments == null || attachments.size() == 0) {
            return null;
        }
        return attachments;
    }

    public class PhotoBean {
        String path;
        int    type;// 0 上传中 1 上传成功

        public PhotoBean(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            PhotoBean other = (PhotoBean) obj;
            return path.equals(other.getPath());
        }

        @Override
        public String toString() {
            return "PhotoBean{" +
                    "path='" + path + '\'' +
                    ", type=" + type +
                    '}';
        }
    }
}
