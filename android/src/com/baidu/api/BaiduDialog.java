/**
 * Copyright (c) 2011 Baidu.com, Inc. All Rights Reserved
 */
package com.baidu.api;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * 自定义的Dialog UI类，用来展示WebView界面，即用户登录和授权的页面
 * 
 * @author chenhetong(chenhetong@baidu.com)
 * 
 */
public class BaiduDialog extends Dialog {

    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);

    private static final String LOG_TAG = "Baidu-WebView";

    private String mUrl;

    private BaiduDialogListener mListener;

    private ProgressDialog mSpinner;

    private WebView mWebView;

    private FrameLayout mContent;

    /**
     * 构造BaiduDialog
     * 
     * @param context 展示Dialog UI的上下文环境，通常是XXActivity.this
     * @param url 用户请求的url地址
     * @param listener 用于对请求回调的Listener对象
     */
    public BaiduDialog(Context context, String url, BaiduDialogListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置ProgressDialog的样式
        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContent = new FrameLayout(getContext());
        setUpWebView();
        addContentView(mContent, FILL);
    }

    private void setUpWebView() {
        RelativeLayout webViewContainer = new RelativeLayout(getContext());
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new BaiduDialog.BdWebViewClient());
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mWebView.setVisibility(View.INVISIBLE);
        mWebView.getSettings().setSavePassword(false);
        webViewContainer.addView(mWebView);
        mContent.addView(webViewContainer, FILL);
    }

    private class BdWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Util.logd(LOG_TAG, "Redirect URL: " + url);
            //如果url的地址为bdconnect://success，即使用User-Agent方式获取用户授权的redirct url,则截取url中返回的各种token参数，
            //如果出错，则通过listener的相应处理方式回调
            if (url.startsWith(Baidu.SUCCESS_URI)) {
                Bundle values = Util.parseUrl(url);
                if (values != null && !values.isEmpty()) {
                    String error = values.getString("error");
                    //用户取消授权返回error=access_denied
                    if ("access_denied".equals(error)) {
                        mListener.onCancel();
                        BaiduDialog.this.dismiss();
                        return true;
                    }
                    //请求出错时返回error=1100&errorDesp=error_desp
                    String errorDesp = values.getString("error_description");
                    if (error != null && errorDesp != null) {
                        mListener.onBaiduException(new BaiduException(error, errorDesp));
                        BaiduDialog.this.dismiss();
                        return true;
                    }
                    mListener.onComplete(values);
                    BaiduDialog.this.dismiss();
                    return true;
                }
            } else if (url.startsWith(Baidu.CANCEL_URI)) {
                mListener.onCancel();
                BaiduDialog.this.dismiss();
                return true;
            } else if (url.contains(Baidu.DISPLAY_STRING)) {
                return false;
            }
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(new BaiduDialogError(description, errorCode, failingUrl));
            BaiduDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Util.logd(LOG_TAG, "Webview loading URL: " + url);
            super.onPageStarted(view, url, favicon);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mSpinner.dismiss();
            mContent.setBackgroundColor(Color.TRANSPARENT);
            mWebView.setVisibility(View.VISIBLE);
        }
    }
    /**
     * 用于BaiduDialog回调接口
     * 
     * @author chenhetong(chenhetong@baidu.com)
     * 
     */
    public static  interface BaiduDialogListener {

        /**
         * BaiduDailog请求成功时，执行该法方法，实现逻辑包括存储value信息，跳转activity的过程
         * 
         * @param values token信息的key-value存储
         */
        public void onComplete(Bundle values);

        /**
         * 当BaiduDialog执行发生BaiduException时执行的方法。
         * @param e BaiduException信息
         */
        public void onBaiduException(BaiduException e);

        /**
         *发生DialogError时执行的方法
         * @param e BaiduDialogError异常类
         */
        public void onError(BaiduDialogError e);
        /**
         * 当BaiduDialog执行取消操作时，执行该方法
         */
        public void onCancel();
    }
}
