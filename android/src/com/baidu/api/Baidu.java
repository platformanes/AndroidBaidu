/**
 * Copyright (c) 2011 Baidu.com, Inc. All Rights Reserved
 */
package com.baidu.api;

import java.io.IOException;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.baidu.api.BaiduDialog.BaiduDialogListener;

/**
 * 
 * 封装了oauth2授权和同不api请求的方法
 * 
 * @author chenhetong(chenhetong@baidu.com)
 * 
 */
public class Baidu implements Parcelable {

    private static final String LOG_TAG = "Baidu";

    public static final String CANCEL_URI = "bdconnect://cancel";

    public static final String SUCCESS_URI = "bdconnect://success";

    public static final String OAUTHORIZE_URL = "https://openapi.baidu.com/oauth/2.0/authorize";

    public static final String LoggedInUser_URL = "https://openapi.baidu.com/rest/2.0/passport/users/getLoggedInUser";

    public static final String DISPLAY_STRING = "mobile";

    private static final String[] DEFAULT_PERMISSIONS = { "basic" };

    private static final String KEY_CLIENT_ID = "clientId";

    private static final String KEY_CLIENT_SECRET = "clientSecret";

    /**
     * 应用注册的api key信息
     */
    private String cliendId;

    /**
     * 应用注册的api secret信息
     */
    private String clientSecret;

    private AccessTokenManager accessTokenManager;

    /**
     * 使用应用的基本信息构建Baidu对象
     * 
     * @param clientId 应用注册的api key信息
     * @param clientSecret 应用注册的api secret信息
     * @param context 当前应用的上下文环境
     */
    public Baidu(String clientId, String clientSecret, Context context) {
        if (clientId == null) {
            throw new IllegalArgumentException("apiKey信息必须提供！");
        }
        if (clientSecret == null) {
            throw new IllegalArgumentException("apiSecret信息必须提供！");
        }
        this.cliendId = clientId;
        this.clientSecret = clientSecret;
        init(context);
    }

    /**
     * 使用Parcel流构建Baidu对象
     * 
     * @param in Parcel流信息
     */
    public Baidu(Parcel in) {
        Bundle bundle = Bundle.CREATOR.createFromParcel(in);
        this.cliendId = bundle.getString(KEY_CLIENT_ID);
        this.clientSecret = bundle.getString(KEY_CLIENT_SECRET);
        this.accessTokenManager = AccessTokenManager.CREATOR.createFromParcel(in);
    }

    /**
     * 初始化accesTokenManager等信息
     * 
     * @param context 当前执行的上下文环境
     */
    public void init(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(LOG_TAG, "App miss permission android.permission.ACCESS_NETWORK_STATE! "
                    + "Some mobile's WebView don't display page!");
        } else {
            WebView.enablePlatformNotifications();
        }
        this.accessTokenManager = new AccessTokenManager(context);
        this.accessTokenManager.initToken();
    }

    /**
     * 完成登录并获取token信息(User-Agent Flow)，该方法使用默认的用户权限
     * 
     * @param activity 需要展示Dialog UI的Activity
     * @param listener Dialog回调接口如Activity跳转，
     */
    public void authorize(Activity activity, final BaiduDialogListener listener) {
        this.authorize(activity, (String[]) null, listener);
    }

    /**
     * 根据相应的permissions信息，完成登录并获取token信息(User-Agent Flow)
     * 
     * @param activity 需要展示Dialog UI的Activity
     * @param permissions 需要获得的授权权限信息
     * @param listener 回调的listener接口，如Activity跳转等
     */
    public void authorize(Activity activity, String[] permissions,
            final BaiduDialogListener listener) {
        if (this.isSessionValid()) {
            listener.onComplete(new Bundle());
            return;
        }
        //使用匿名的BaiduDialogListener对listener进行了包装，并进行一些存储token信息和当前登录用户的逻辑，
        //外部传进来的listener信息不需要在进行存储相关的逻辑
        this.authorize(activity, permissions, new BaiduDialogListener() {

            @Override
            public void onError(BaiduDialogError e) {
                Util.logd("Baidu-BdDialogError", "DialogError " + e);
                listener.onError(e);
            }

            @Override
            public void onComplete(Bundle values) {
                //存储相应的token信息
                getAccessTokenManager().storeToken(values);
                //完成授权操作，使用listener进行回调，eg。跳转到其他的activity
                listener.onComplete(values);
            }

            @Override
            public void onCancel() {
                Util.logd("Baidu-BdDialogCancel", "login cancel");
                listener.onCancel();
            }

            @Override
            public void onBaiduException(BaiduException e) {
                Log.d("Baidu-BaiduException", "BaiduException : " + e);
                listener.onBaiduException(e);
            }
        }, SUCCESS_URI, "token");
    }

    /**
     * 通过Dialog UI展示用户登录、授权页
     * 
     * @param activity 需要展示Dialog UI的Activity
     * @param permissions 需要请求的环境
     * @param listener 用于回调的listener接口方法
     * @param redirectUrl 回调地址
     * @param responseType 授权请求的类型
     */
    private void authorize(Activity activity, String[] permissions,
            final BaiduDialogListener listener, String redirectUrl, String responseType) {
        CookieSyncManager.createInstance(activity);
        Bundle params = new Bundle();
        params.putString("client_id", this.cliendId);
        params.putString("redirect_uri", redirectUrl);
        params.putString("response_type", responseType);
        params.putString("display", DISPLAY_STRING);
        if (permissions == null) {
            permissions = DEFAULT_PERMISSIONS;
        }
        if (permissions != null && permissions.length > 0) {
            String scope = TextUtils.join(" ", permissions);
            params.putString("scope", scope);
        }
        String url = OAUTHORIZE_URL + "?" + Util.encodeUrl(params);
        Util.logd("Baidu-Authorize URL", url);
        if (activity.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Util.showAlert(activity, "没有权限", "应用需要访问互联网的权限");
        } else {
            new BaiduDialog(activity, url, listener).show();
        }

    }

    /**
     * 登出操作，将清除存储的token信息和当前登录用户的信息，用户再下一次登录时需要进行授权操作
     * 
     */
    public void LogOut() {
        if (this.accessTokenManager != null) {
            this.accessTokenManager.clearToken();
            this.accessTokenManager = null;
        }
    }

    /**
     * 针对api请求的方法类
     * 
     * @param url api请求的url地址
     * @param parameters
     *        业务级参数，包括一本的文本参数（key-value都为string类型）、文件参数（key-fileName
     *        String类型；value byte[]）
     * @param method 请求的方法 "GET"/"POST"
     * @return 返回json格式信息
     * @throws IOException 当网络发生异常时的信息
     * @throws BaiduException 当json信息中含有error_code时抛出该异常
     */
    public String request(String url, Bundle parameters, String method) throws IOException,
            BaiduException {
        //截取url中的访问的api的类型 eg https://openapi.baidu.com/public/2.0/mp3/  截取的类型为public类型
        String[] splits = url.split("/");
        String type = splits[3];
        if ("rest".equals(type)) {
            return restRequest(url, parameters, method);
        }
        if ("public".equals(type)) {
            return publicRequest(url, parameters, method);
        }
        if ("file".equals(type)) {
            return fileRequest(url, parameters);
        }
        return null;
    }

    /**
     * 访问rest类型的api
     * 
     * @param url rest类型的api url地址，使用全路径
     * @param parameters 业务级参数，key-value格式，key、value都必须是String类型
     * @param method 请求的方法 "GET"/"POST"
     * @return 返回 json格式的请求信息
     * @throws IOException 网络请求异常时发生IOException
     * @throws BaiduException 当返回的json信息中含有error_code时抛出该异常
     */
    private String restRequest(String url, Bundle parameters, String method) throws IOException,
            BaiduException {
        Bundle params = new Bundle();
        params.putString("access_token", getAccessToken());
        if (parameters != null) {
            params.putAll(parameters);
        }
        String response = Util.openUrl(url, method, params);
        Util.checkResponse(response);
        return response;
    }

    /**
     * 访问public类型的api请求
     * 
     * @param url public类型的api的全路径uri
     * @param parameters 业务级参数 （key、value均为String类型）
     * @param method 访问api的方法“GET”/“POST”
     * @return json格式数据
     * @throws IOException 当网络发生异常时发生IOException
     * @throws BaiduException 当返回的json信息中包含error信息时，抛出BaiduExceptioin
     */
    private String publicRequest(String url, Bundle parameters, String method) throws IOException,
            BaiduException {
        Bundle params = new Bundle();
        params.putString("client_id", this.cliendId);
        if (parameters != null) {
            params.putAll(parameters);
        }
        String response = Util.openUrl(url, method, params);
        Util.checkResponse(response);
        return response;
    }

    /**
     * 
     * @param url 请求file类型api的权路径url地址
     * @param parameters
     *        业务级参数，包含普通文本参数和文件参数，上传文件参数key-fileName-String，value-
     *        fileContent-byte[]
     * @return 返回json格式的响应数据
     * @throws IOException 当网络发生异常时，抛出IOException
     * @throws BaiduException 当json数据中包含error时抛出BaiduException
     */
    private String fileRequest(String url, Bundle parameters) throws IOException, BaiduException {
        Bundle params = new Bundle();
        params.putString("access_token", getAccessToken());
        if (parameters != null) {
            params.putAll(params);
        }
        String response = Util.uploadFile(url, parameters);
        Util.checkResponse(response);
        return response;

    }

    /**
     * 判断token信息是否有效。
     * 
     * @return boolean true/false
     */
    public boolean isSessionValid() {
        return this.accessTokenManager.isSessionVaild();
    }

    /**
     * 获取AccessTokenManager对象
     * 
     * @return accessTokenManager对象
     */
    public AccessTokenManager getAccessTokenManager() {
        return this.accessTokenManager;
    }

    /**
     * 获取AccessToken信息
     * 
     * @return accessToken信息
     */
    public String getAccessToken() {
        return this.accessTokenManager.getAccessToken();
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_CLIENT_ID, this.cliendId);
        bundle.putString(KEY_CLIENT_SECRET, this.clientSecret);
        bundle.writeToParcel(dest, flags);
        this.accessTokenManager.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<Baidu> CREATOR = new Parcelable.Creator<Baidu>() {

        public Baidu createFromParcel(Parcel in) {
            return new Baidu(in);
        }

        public Baidu[] newArray(int size) {
            return new Baidu[size];
        }
    };

}
