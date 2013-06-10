/**
 * Copyright (c) 2011 Baidu.com, Inc. All Rights Reserved
 */
package com.baidu.api;

import java.io.IOException;

import android.os.Bundle;

/**
 * 调用api请求的异步类
 * 
 * @author chenhetong(chenhetong@baidu.com)
 * 
 */
public class AsyncBaiduRunner {

    private Baidu baidu;

    public AsyncBaiduRunner(Baidu baidu) {
        super();
        this.baidu = baidu;
    }

    /**
     * api异步调用类
     * 
     * @param url api请求的url地址
     * @param parameters api请求附带的业务级参数 key-value类型，key、value均为String类型
     * @param method api请求的方法，默认使用GET方式
     * @param listener 异步请求的回调接口
     */
    public void request(final String url, final Bundle parameters, final String method,
            final RequestListener listener) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String response = baidu.request(url, parameters, method);
                    listener.onComplete(response);
                } catch (BaiduException e) {
                    listener.onBaiduException(e);
                } catch (IOException e) {
                    listener.onIOException(e);
                }
            }
        }).start();
    }

    /**
     * 用于异步调用api的回调接口
     * 
     * @author chenhetong(chenhetong@baidu.com)
     * 
     */
    public static interface RequestListener {

        /**
         * 当request请求执行成功时，执行该方法
         * 
         * 该方法在新建线程中执行，不要在该方法中进行更新UI的操作
         * 
         * @param response
         */
        public void onComplete(String response);

        /**
         * 当request请求发生网络异常时，执行该方法
         * 
         * 该方法在新建线程中执行，不要在该方法中进行更新UI的操作
         * 
         * @param e IoException
         */
        public void onIOException(IOException e);

        /**
         * 当request请求发生BaiduException时，执行该方法
         * 
         * 该方法在新建线程中执行，不要在该方法中进行更新UI的操作
         * 
         * @param e BaiduException
         */
        public void onBaiduException(BaiduException e);
    }

}
