package com.liluo.library.base;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.liluo.library.LoadMoreView;
import com.liluo.library.SimpleLoadMoreView;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
public abstract class BaseRecyclerViewAdapter<T, V extends BaseViewHolder> extends RecyclerView.Adapter<V> {
    public static final int HEADER_VIEW = 0x00000111;
    public static final int LOADING_VIEW = 0x00000222;
    public static final int FOOTER_VIEW = 0x00000333;
    public static final int EMPTY_VIEW = 0x00000555;

    public List<T> mDatas;
    OnItemClickListener onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;
    OnItemChildClickListener onItemChildClickListener;
    RequestLoadMoreListener requestLoadMoreListener;
    SpanSizeLookup mSpanSizeLookup;
    protected boolean isEdit = false;//是否进入编辑模式
    LinearLayout mHeadLayout;
    LinearLayout mFootlayout;
    FrameLayout mEmptyLayout;
    LoadMoreView mloadingView = new SimpleLoadMoreView();
    protected Context mContext;
    private LayoutInflater mLayoutInflater;
    private boolean mEnableLoadMoreEndClick;
    private boolean mNextLoadEnable;//自动加载
    private boolean mLoading = false;//加载状态
    BaseAnimation mAnimation = new DefaultAnimation(AnimType.SCALEIN);
    BaseAnimation mCustomAnimtion;
    private long mDuration = 500;//动画持续时间
    private Interpolator mInterpolator = new LinearInterpolator();

    boolean mFirstOnlyEnable;//是否仅第一次绘制加载动画
    int mLastPosition = -1;//最后绘制的位置
    boolean mOpenAnimationEnable;

    public BaseRecyclerViewAdapter(List<T> datas) {
        this.mDatas = datas;
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }
    }

    private RecyclerView mRecyclerView;

    private void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    private RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
        notifyDataSetChanged();
    }

    public boolean isEdit() {
        return isEdit;
    }

    /**
     * 刷新数据
     *
     * @param datas
     */
    public void notify(@NonNull Collection<? extends T> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void notify(@NonNull List<T> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    /**
     * 添加数据
     *
     * @param t        添加的数据
     * @param position 添加位置
     */
    public void addData(@NonNull T t, int position) {
        mDatas.add(position, t);
        notifyItemInserted(position + getHeaderLayoutCount());
    }

    /**
     * 添加数据
     *
     * @param t
     */
    public void addData(@NonNull T t) {
        mDatas.add(t);
        notifyItemInserted(mDatas.size() + getHeaderLayoutCount());
        compatibilityDataSizeChanged(1);
    }

    /**
     * 添加数据到指定位置
     *
     * @param newData  添加的数据列表
     * @param position 添加位置
     */
    public void addData(@IntRange(from = 0) int position, @NonNull Collection<? extends T> newData) {
        mDatas.addAll(position, newData);
        notifyItemRangeInserted(position + getHeaderLayoutCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * 添加数据到最后
     *
     * @param newData the new data collection
     */
    public void addData(@NonNull Collection<? extends T> newData) {
        mDatas.addAll(newData);
        notifyItemRangeInserted(mDatas.size() - newData.size() + getHeaderLayoutCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * 当getLoadMoreViewCount  getEmptyViewCount 改变是需要刷新界面
     *
     * @param size 刷新数据的大小
     */
    private void compatibilityDataSizeChanged(int size) {
        final int dataSize = mDatas == null ? 0 : mDatas.size();
        if (dataSize == size) {
            notifyDataSetChanged();
        }
    }

    /**
     * 删除数据
     *
     * @param position 数据位置
     */
    public void removeItem(@IntRange(from = 0) int position) {
        mDatas.remove(position);
        int internalPosition = position + getHeaderLayoutCount();//获取真实位置 position
        notifyItemRemoved(internalPosition);
        compatibilityDataSizeChanged(0);
        notifyItemRangeChanged(internalPosition, mDatas.size() - internalPosition);
    }

     /**
     * 删除数据
     *
     * @param t 数据
     */
    public void removeItem(T t) {
        mDatas.remove(t);
        notifyDataSetChanged();
    }

    /**
     * 修改数据
     */
    public void updateItem(@IntRange(from = 0) int index, @NonNull T data) {
        mDatas.set(index, data);
        notifyItemChanged(index + getHeaderLayoutCount());
    }

    /**
     * 修改数据
     *
     * @param t        修改的数据
     * @param position 数据位置
     */
    public void updateItem(T t, int position) {
        mDatas.set(position, t);
        notifyItemChanged(position);

    }

    public List<T> getDatas() {
        return mDatas;
    }

    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (getEmptyViewCount() == 1) {
            switch (position) {
                case 0:
                    if (getHeaderLayoutCount() > 0) {
                        return HEADER_VIEW;
                    } else {
                        return EMPTY_VIEW;
                    }
                case 1:
                    if (getEmptyViewCount() > 0) {
                        return EMPTY_VIEW;
                    } else {
                        return FOOTER_VIEW;
                    }
                case 2:
                    return FOOTER_VIEW;
                default:
                    return EMPTY_VIEW;
            }
        }
        int headerNum = getHeaderLayoutCount();
        if (position < headerNum) {
            return HEADER_VIEW;
        } else {
            int resultPosition = position - headerNum;
            int adapterCount = mDatas.size();
            if (resultPosition < adapterCount) {//显示的列表数据
                return defaultItemViewType(position);
            } else {
                resultPosition = resultPosition - adapterCount;
                int numFooters = getFooterLayoutCount();
                if (resultPosition < numFooters) {
                    return FOOTER_VIEW;
                } else {
                    return LOADING_VIEW;
                }
            }
        }
    }

    public int defaultItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public int getHeaderLayoutCount() {
        if (mHeadLayout == null || mHeadLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    public int getFooterLayoutCount() {
        if (mFootlayout == null || mFootlayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    public void addHeadView(View headView) {
        addHeadView(headView, 0, LinearLayout.VERTICAL);
    }

    public void addHeadView(View headView, int index) {
        addHeadView(headView, index, LinearLayout.VERTICAL);
    }

    public int addHeadView(View headView, int index, int orientation) {
        if (mHeadLayout == null) {
            if (orientation == LinearLayout.VERTICAL) {
                mHeadLayout = new LinearLayout(headView.getContext());
                mHeadLayout.setOrientation(LinearLayout.VERTICAL);
                mHeadLayout.setLayoutParams(new RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mHeadLayout = new LinearLayout(headView.getContext());
                mHeadLayout.setOrientation(LinearLayout.HORIZONTAL);
                mHeadLayout.setLayoutParams(new RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        int childCount = mHeadLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mHeadLayout.addView(headView, index);
        if (mHeadLayout.getChildCount() == 1) {//第一次添加头信息
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }

        return index;
    }

    /**
     * 获取头的位置position
     *
     * @return
     */
    private int getHeaderViewPosition() {
//        if (mHeadLayout != null && getHeaderLayoutCount() > 0) {
        if (mHeadLayout != null) {
            return 0;
        }
        return -1;
    }

    public int setHeaderView(View header, int index, int orientation) {
        if (mHeadLayout == null || mHeadLayout.getChildCount() <= index) {
            return addHeadView(header, index, orientation);
        } else {
            mHeadLayout.removeViewAt(index);
            mHeadLayout.addView(header, index);
            return index;
        }
    }

    /**
     * 移除headview
     *
     * @param header
     */
    public void removeHeaderView(View header) {
        if (getHeaderLayoutCount() == 0) return;

        mHeadLayout.removeView(header);
        if (mHeadLayout.getChildCount() == 0) {
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }


    /**
     * 移除所有头布局
     */
    public void removeAllHeaderView() {
        if (getHeaderLayoutCount() == 0) return;

        mHeadLayout.removeAllViews();
        int position = getHeaderViewPosition();
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }


    /**
     * 添加尾布局
     *
     * @param footView
     */
    public void addFootView(View footView) {
        addFootView(footView, -1, LinearLayout.VERTICAL);
    }

    public void addFootView(View footView, int index) {
        addFootView(footView, index, LinearLayout.VERTICAL);
    }

    public int addFootView(View footView, int index, int orientation) {
        if (mFootlayout == null) {
            if (orientation == LinearLayout.VERTICAL) {
                mFootlayout = new LinearLayout(footView.getContext());
                mFootlayout.setOrientation(LinearLayout.VERTICAL);
                mFootlayout.setLayoutParams(new RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mFootlayout = new LinearLayout(footView.getContext());
                mFootlayout.setOrientation(LinearLayout.HORIZONTAL);
                mFootlayout.setLayoutParams(new RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        int childCount = mFootlayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mFootlayout.addView(footView, index);
        if (mFootlayout.getChildCount() == 1) {//第一次添加头信息
            int position = getFootViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    /**
     * 设置尾布局
     *
     * @param header
     * @param index
     * @param orientation
     * @return
     */
    public int setFooterView(View header, int index, int orientation) {
        if (mFootlayout == null || mFootlayout.getChildCount() <= index) {
            return addFootView(header, index, orientation);
        } else {
            mFootlayout.removeViewAt(index);
            mFootlayout.addView(header, index);
            return index;
        }
    }

    /**
     * 移除尾布局
     *
     * @param footer
     */
    public void removeFooterView(View footer) {
        if (getFooterLayoutCount() == 0) return;

        mFootlayout.removeView(footer);
        if (mFootlayout.getChildCount() == 0) {
            int position = getFootViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * 移除所有尾布局
     */
    public void removeAllFooterView() {
        if (getFooterLayoutCount() == 0) return;

        mFootlayout.removeAllViews();
        int position = getFootViewPosition();
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }

    public int getFootViewPosition() {
        return getHeaderLayoutCount() + (mDatas == null ? 0 : mDatas.size());
    }


    public void setEmptyView(View emptyView) {
        if (mEmptyLayout == null) {
            mEmptyLayout = new FrameLayout(emptyView.getContext());
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.MATCH_PARENT);
            ViewGroup.LayoutParams lp = emptyView.getLayoutParams();
            if (lp != null) {
                lp.width = layoutParams.width;
                lp.height = layoutParams.height;
            }
            mEmptyLayout.setLayoutParams(layoutParams);
        }
        mEmptyLayout.removeAllViews();
        mEmptyLayout.addView(emptyView);
        if (getEmptyViewCount() == 1) {
            int position = 0;
            if (getHeaderLayoutCount() != 0) {
                position++;
            }
            notifyItemInserted(position);
        }
    }

    /**
     * 如果现实emptview 返回1 否则返回0
     *
     * @return 当有设置emptviewq且数据列表不为空时 返回1 否则返回0
     */
    public int getEmptyViewCount() {
        if (mEmptyLayout == null || mEmptyLayout.getChildCount() == 0) {//没有设置emptview
            return 0;
        }
//        if (!mIsUseEmpty) {
//            return 0;
//        }
        if (mDatas != null && mDatas.size() != 0) {//有数据
            return 0;
        }
        return 1;
    }


    @Override
    public V onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();
        this.mLayoutInflater = LayoutInflater.from(mContext);
        V baseViewHolder = null;
        switch (viewType) {
            case HEADER_VIEW:
                baseViewHolder = createBaseViewHolder(mHeadLayout);
                break;
            case FOOTER_VIEW:
                baseViewHolder = createBaseViewHolder(mFootlayout);
                break;
            case EMPTY_VIEW:
                baseViewHolder = createBaseViewHolder(mEmptyLayout);
                break;
            case LOADING_VIEW:
                baseViewHolder = getLoadingView(parent);
                break;
            default:
                baseViewHolder = createDefaultViewHolder(parent, viewType);
                bindViewClickListener(baseViewHolder);
                break;
        }
        baseViewHolder.setAdapter(this);
        return baseViewHolder;
    }

    private int getLoadMoreViewCount() {
        if (requestLoadMoreListener == null) {
            return 0;
        }
        if (!mNextLoadEnable && mloadingView.isLoadEndMoreGone()) {//自动加载 加载完成后隐藏
            return 0;
        }
        if (mDatas.size() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * 获取加载的viewholder
     *
     * @param parent
     * @return
     */
    private V getLoadingView(ViewGroup parent) {
        View view = getItemView(mloadingView.getLayoutId(), parent);
        V holder = createBaseViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mloadingView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {//加载失败时  点击加载跟多
                    notifyLoadMoreToLoading();
                }
                if (mEnableLoadMoreEndClick && mloadingView.getLoadMoreStatus() == LoadMoreView.STATUS_END) {//有跟多数据时点击加载更多
                    notifyLoadMoreToLoading();
                }
            }
        });
        return holder;
    }

    /**
     * 设置点击加载更多的数据
     *
     * @param enable
     */
    public void enableLoadMoreEndClick(boolean enable) {
        mEnableLoadMoreEndClick = enable;
    }

    /**
     * 通知回调加载更多
     */
    public void notifyLoadMoreToLoading() {
        if (mloadingView.getLoadMoreStatus() == LoadMoreView.STATUS_LOADING) {
            return;
        }
        mloadingView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        notifyItemChanged(getLoadMoreViewPosition());
    }

    /**
     * 获取loadmoreview的位置
     *
     * @return
     */
    private int getLoadMoreViewPosition() {
        return getHeaderLayoutCount() + mDatas.size() + getFooterLayoutCount();

    }

    private void autoLoadMore(int position) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        if (position < getItemCount() - 1) {//loadmoreview是否绘制
            return;
        }
        if (mloadingView.getLoadMoreStatus() != LoadMoreView.STATUS_DEFAULT) {//当前状态不是默认状态
            return;
        }
        mloadingView.setLoadMoreStatus(LoadMoreView.STATUS_LOADING);
        //TODO
        if (!mLoading) {
            mLoading = true;
            if (getRecyclerView() != null) {
                getRecyclerView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestLoadMoreListener.onLoadMoreRequested();
                    }
                }, 1000);
            } else {
                requestLoadMoreListener.onLoadMoreRequested();
            }
        }
    }

    /**
     * 加载完成 没有更多数据
     */
    public void loadMoreEnd() {
        loadMoreEnd(false);
    }

    /**
     * @param gone true 隐藏loadmoreview false 不隐藏 显示loadmoreview 没有更多数据
     */
    public void loadMoreEnd(boolean gone) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mNextLoadEnable = false;
        mloadingView.setLoadMoreEndGone(gone);
        if (gone) {
            notifyItemRemoved(getLoadMoreViewPosition());
        } else {
            mloadingView.setLoadMoreStatus(LoadMoreView.STATUS_END);
            notifyItemChanged(getLoadMoreViewPosition());
        }
    }

    /**
     * 加载完成
     */
    public void loadMoreComplete() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mNextLoadEnable = true;
        mloadingView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        notifyItemChanged(getLoadMoreViewPosition());
    }

    /**
     * 加载失败
     */
    public void loadMoreFail() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mloadingView.setLoadMoreStatus(LoadMoreView.STATUS_FAIL);
        notifyItemChanged(getLoadMoreViewPosition());
    }

    /**
     * 绑定item监听
     *
     * @param baseViewHolder
     */
    private void bindViewClickListener(final V baseViewHolder) {
        if (baseViewHolder == null) {
            return;
        }
        View itemView = baseViewHolder.itemView;
        if (itemView == null) {
            return;
        }
        if (onItemClickListener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClickListener(v, baseViewHolder.getLayoutPosition() - getHeaderLayoutCount());
                }
            });
        }
        if (onItemLongClickListener != null) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onItemLongClickListener.onItemLongClickListener(v, baseViewHolder.getLayoutPosition() - getHeaderLayoutCount());
                }
            });
        }

    }


    private V createDefaultViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, getLayoutId(viewType));
    }

    private V createBaseViewHolder(ViewGroup parent, int layoutId) {
        return createBaseViewHolder(getItemView(layoutId, parent));
    }

    private V createBaseViewHolder(View view) {
        Class temp = getClass();
        Class z = null;
        while (z == null && null != temp) {
            z = getInstancedGenericKClass(temp);
            temp = temp.getSuperclass();
        }
        V v = createGenericKInstance(z, view);
        return null != v ? v : (V) new BaseViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final V holder, int position) {
        switch (getItemViewType(position)) {
            case 0:
                bindView(holder, mDatas.get(holder.getLayoutPosition() - getHeaderLayoutCount()), holder.getLayoutPosition() - getHeaderLayoutCount());
                break;
            case HEADER_VIEW:
                break;
            case FOOTER_VIEW:
                break;
            case EMPTY_VIEW:
                break;
            case LOADING_VIEW:
                autoLoadMore(position);
                mloadingView.convertStatus(holder);
                break;
            default:
                bindView(holder, mDatas.get(holder.getLayoutPosition() - getHeaderLayoutCount()), holder.getLayoutPosition() - getHeaderLayoutCount());
                break;
        }

    }


    @Override
    public int getItemCount() {
        int count = 0;
        if (getEmptyViewCount() == 1) {
            count = 1;
            if (getHeaderLayoutCount() > 0) {
                count++;
            }
            if (getFooterLayoutCount() > 0) {
                count++;
            }
        } else {
            int dataSize = mDatas == null ? 0 : mDatas.size();
            count = dataSize + getHeaderLayoutCount() + getFooterLayoutCount() + getLoadMoreViewCount();
        }
        return count;
    }

    /**
     * inflate布局
     *
     * @param layoutResId
     * @param parent
     * @return
     */
    protected View getItemView(@LayoutRes int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) layoutManager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (mSpanSizeLookup == null) {
                        return isFixedViewType(type) ? gridManager.getSpanCount() : 1;
                    } else {
                        return isFixedViewType(type) ? gridManager.getSpanCount() :
                                mSpanSizeLookup.getSpanSize(gridManager, position - getHeaderLayoutCount());
                    }
                }
            });
        }
    }

    protected boolean isFixedViewType(int type) {
        return type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type ==
                LOADING_VIEW;
    }

    @Override
    public void onViewAttachedToWindow(V holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW || type == EMPTY_VIEW) {
            setFullSpan(holder);
        } else {
            addAnimation(holder);
        }
    }


    private void addAnimation(V holder) {
        if (mOpenAnimationEnable) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mLastPosition) {//每次绘制显示||第一次绘制显示动画
                BaseAnimation animation = null;
                if (mCustomAnimtion == null) {
                    animation = mAnimation;
                } else {
                    animation = mCustomAnimtion;
                }
                for (Animator anim : animation.getAnimators(holder.itemView)) {
                    startAnim(anim, holder.getAdapterPosition());
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    /**
     * 设置动画持续时间
     *
     * @param duration
     */
    public void setAnimDuration(int duration) {
        this.mDuration = duration;
    }


    /**
     * 使用内置动画
     *
     * @param animType
     */
    public void openLoadAnimation(AnimType animType) {
        this.mOpenAnimationEnable = true;
        mAnimation = new DefaultAnimation(animType);
        mCustomAnimtion = null;
    }

    /**
     * 使用默认动画
     */
    public void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }

    /**
     * 使用自定义动画
     *
     * @param animation
     */
    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        mCustomAnimtion = animation;
    }

    /**
     * 开始动画
     *
     * @param anim
     * @param index
     */
    protected void startAnim(Animator anim, int index) {
        anim.setDuration(mDuration).start();
        anim.setInterpolator(mInterpolator);
    }

    /**
     * 如果layoutmanager是流式布局
     * 将headview footview loadingview emptyview宽度设为满屏
     *
     * @param holder
     */
    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    /**
     * 创建布局
     *
     * @return
     */
    public abstract int getLayoutId(int viewType);

    /**
     * 设置数据
     *
     * @param t
     * @param position
     * @return
     */
    public abstract void bindView(V holder, T t, int position);

    /**
     * 设置点击事件
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 设置长按事件
     *
     * @param onItemLongClickListener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 设置子项点击事件
     *
     * @param onItemChildClickListener
     */
    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public OnItemChildClickListener getOnItemChildClickListener() {
        return onItemChildClickListener;
    }

    public void setOnloadMoreListener(RecyclerView recyclerView, RequestLoadMoreListener requestLoadMoreListener) {
        openLoadMore(requestLoadMoreListener);
        if (getRecyclerView() == null) {
            setRecyclerView(recyclerView);
        }

    }

    public void setSpanSizeLookup(SpanSizeLookup mSpanSizeLookup) {
        this.mSpanSizeLookup = mSpanSizeLookup;
    }

    /**
     * 开启加载更多
     *
     * @param requestLoadMoreListener
     */
    public void openLoadMore(RequestLoadMoreListener requestLoadMoreListener) {
        this.requestLoadMoreListener = requestLoadMoreListener;
    }

    public interface OnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClickListener(View view, int position);
    }

    /**
     * 子项点击事件
     */
    public interface OnItemChildClickListener {
        void onItemChildClick(BaseRecyclerViewAdapter adapter, View view, int position);
    }

    public interface RequestLoadMoreListener {
        void onLoadMoreRequested();
    }

    public interface SpanSizeLookup {
        int getSpanSize(GridLayoutManager gridLayoutManager, int position);
    }

    /**
     * 获取通用V
     *
     * @param z
     * @return
     */
    private Class getInstancedGenericKClass(Class z) {
        Type type = z.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type temp : types) {
                if (temp instanceof Class) {
                    Class tempClass = (Class) temp;
                    if (BaseViewHolder.class.isAssignableFrom(tempClass)) {
                        return tempClass;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 创建v的实例
     *
     * @param z
     * @param view
     * @return
     */
    @SuppressWarnings("unchecked")
    private V createGenericKInstance(Class z, View view) {
        try {
            Constructor constructor;
            // inner and unstatic class
            if (z.isMemberClass() && !Modifier.isStatic(z.getModifiers())) {
                constructor = z.getDeclaredConstructor(getClass(), View.class);
                constructor.setAccessible(true);
                return (V) constructor.newInstance(this, view);
            } else {
                constructor = z.getDeclaredConstructor(View.class);
                constructor.setAccessible(true);
                return (V) constructor.newInstance(view);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface BaseAnimation {
        Animator[] getAnimators(View view);
    }

    public class DefaultAnimation implements BaseAnimation {

        AnimType animType;

        public DefaultAnimation(@Nullable AnimType animType) {
            this.animType = animType;
        }

        @Override
        public Animator[] getAnimators(View view) {
            switch (animType) {
                case ALPHAIN:
                    return new Animator[]{ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)};
                case SCALEIN:
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f);
                    return new Animator[]{scaleX, scaleY};
                case SLIDEIN_BOTTOM:
                    return new Animator[]{ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0)};
                case SLIDEIN_LEFT:
                    return new Animator[]{ObjectAnimator.ofFloat(view, "translationX", -view.getRootView().getWidth(), 0)};
                case SLIDEIN_RIGHT:
                    return new Animator[]{ObjectAnimator.ofFloat(view, "translationX", view.getRootView().getWidth(), 0)};
                case SCALEIN_OUT:
                    return new Animator[]{ObjectAnimator.ofFloat(view, "scaleX", 1, 1.1f, 1),
                            ObjectAnimator.ofFloat(view, "scaleY", 1, 1.1f, 1)};

            }
            return new Animator[]{ObjectAnimator.ofFloat(view, "scaleX", 1, 1.1f, 1),
                    ObjectAnimator.ofFloat(view, "scaleY", 1, 1.1f, 1)};
        }
    }

    enum AnimType {
        ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT, SCALEIN_OUT
    }
}
