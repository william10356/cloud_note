package com.example.notes.dialog;

import android.app.FragmentManager;

import com.loading.dialog.AndroidLoadingDialog;

public class LoadingDialogManager {
    private AndroidLoadingDialog mLoadingDialog;
    private boolean mIsShow;//是否显示

    private  LoadingDialogManager() {
        init();
    }

    public static LoadingDialogManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static LoadingDialogManager INSTANCE = new LoadingDialogManager();
    }

    public void init() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new AndroidLoadingDialog();
        }
    }

    /**
     * 展示加载框
     */
    public synchronized void show(FragmentManager manager) {
        if (manager != null && mLoadingDialog != null && !mIsShow) {
            mLoadingDialog.showAllowingStateLoss(manager, "loadingDialog");
            mIsShow = true;
        }
    }

    /**
     * 隐藏加载框
     */
    public synchronized void dismiss() {
        if (mLoadingDialog != null && mIsShow) {
            mLoadingDialog.dismissAllowingStateLoss();
            mIsShow = false;
        }

    }
}
