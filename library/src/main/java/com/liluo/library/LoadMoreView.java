package com.liluo.library;

import com.liluo.library.base.BaseViewHolder;

/**
 * <pre>
 * author : No.1
 * time : 2017/7/6.
 * desc :
 * </pre>
 */

public abstract class LoadMoreView {
    public static final int STATUS_DEFAULT = 1;
    public static final int STATUS_LOADING = 2;
    public static final int STATUS_FAIL = 3;
    public static final int STATUS_END = 4;

    private int mLoadMoreStatus = STATUS_DEFAULT;
    private boolean mLoadMoreEndGone = false;

    /**
     * 设置加载状态
     *
     * @param loadMoreStatus
     */
    public void setLoadMoreStatus(int loadMoreStatus) {
        this.mLoadMoreStatus = loadMoreStatus;
    }

    /**
     * 获取当前状态
     *
     * @return
     */
    public int getLoadMoreStatus() {
        return mLoadMoreStatus;
    }

    /**
     * 状态改变
     */
    public void convertStatus(BaseViewHolder holder) {
        switch (mLoadMoreStatus) {
            case STATUS_DEFAULT:
                visibleLoading(holder, false);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, false);
                break;
            case STATUS_LOADING:
                visibleLoading(holder, true);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, false);
                break;
            case STATUS_FAIL:
                visibleLoading(holder, false);
                visibleLoadFail(holder, true);
                visibleLoadEnd(holder, false);
                break;
            case STATUS_END:
                visibleLoading(holder, false);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, true);
                break;
        }
    }

    /**
     * 显示加载结束
     *
     * @param holder
     * @param visible
     */
    private void visibleLoadEnd(BaseViewHolder holder, boolean visible) {
        final int loadEndViewId = getLoadEndViewId();
        if (loadEndViewId != 0) {
            holder.setVisible(loadEndViewId, visible);
        }

    }

    /**
     * 显示加载失败
     *
     * @param holder
     * @param visible
     */
    private void visibleLoadFail(BaseViewHolder holder, boolean visible) {
        holder.setVisible(getLoadFailViewId(), visible);
    }

    /**
     * 显示加载中
     *
     * @param holder
     * @param visible
     */
    private void visibleLoading(BaseViewHolder holder, boolean visible) {
        holder.setVisible(getLoadingViewId(), visible);
    }

    /**
     * 设置加载结束隐藏
     *
     * @param loadMoreEndGone
     */
    public final void setLoadMoreEndGone(boolean loadMoreEndGone) {
        this.mLoadMoreEndGone = loadMoreEndGone;
    }

    /**
     * 是否加载结束后影藏
     *
     * @return
     */
    public final boolean isLoadEndMoreGone() {
        if (getLoadEndViewId() == 0) {
            return true;
        }
        return mLoadMoreEndGone;
    }


    /**
     * 是否加载结束
     *
     * @return
     */
    public boolean isLoadEndGone() {
        return mLoadMoreEndGone;
    }


    /**
     * 获取布局id
     *
     * @return
     */
    public abstract int getLayoutId();

    /**
     * 获取加载的viewid
     *
     * @return
     */
    protected abstract int getLoadingViewId();

    /**
     * 获取加载失败的viewId
     *
     * @return
     */
    protected abstract int getLoadFailViewId();

    /**
     * 获取加载结束的viewId
     *
     * @return
     */
    protected abstract int getLoadEndViewId();

}
