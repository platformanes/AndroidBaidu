/**
 * Copyright (c) 2011 Baidu.com, Inc. All Rights Reserved
 */
package com.baidu.api;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * 对Token相关信息的管理类，包括初始化、存储、清除相应的token信息
 * 由于AccessTokenManager涉及到在多个Activity中传递的，所以实现了Parcelable接口
 * 
 * @author chenhetong(chenhetong@baidu.com)
 * 
 */
public class AccessTokenManager implements Parcelable {

    private static final String BAIDU_SDK_CONFIG = "baidu_sdk_config";

    //持久化token信息的各种监制
    private static final String BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN = "baidu_sdk_config_prop_access_token";

    private static final String BAIDU_SDK_CONFIG_PROP_CREATE_TIME = "baidu_sdk_config_prop_create_time";

    private static final String BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS = "baidu_sdk_config_prop_expire_secends";

    //在多个Activity中传递AccessTokenManager的键值
    private static final String KEY_ACCESS_TOKEN = "baidu_token_manager_access_token";

    private static final String KEY_EXPIRE_TIME = "baidu_token_manager_expire_time";


    //accessToken信息
    private String accessToken = null;

    //token过期时间
    private long expireTime = 0;

    //当前的上下文环境
    private Context context = null;

    /**
     * 构建AccessTokenManager类
     * 
     * @param context 当前的上下文环境，通常为××Activity。this等
     */
    public AccessTokenManager(Context context) {
        this.context = context;
        compareWithConfig();
    }

    /**
     * 通过Parcel流构建AccessTokenManager，主要用在Parcelable.Creator中
     * 
     * @param source Parcel 流信息
     * @throws BaiduException 
     * @throws JSONException 
     * 
     */
    public AccessTokenManager(Parcel source) {
        Bundle bundle = Bundle.CREATOR.createFromParcel(source);
        if (bundle != null) {
            this.accessToken = bundle.getString(KEY_ACCESS_TOKEN);
            this.expireTime = bundle.getLong(KEY_EXPIRE_TIME);
        }
        compareWithConfig();
    }

    /**
     * 检查当token信息与配置文件是否保持一致，若不一致则对当前的token信息进行初始化
     */
    private void compareWithConfig() {
        if (this.context == null) {
            return;
        }
        /**
         * 对配置的权限信息进行监控，保持多个AccessTokenManager对象之间的，权限信息一致。
         */
        final SharedPreferences sp = this.context.getSharedPreferences(BAIDU_SDK_CONFIG,
                Context.MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String acToken = sp.getString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, null);
                if (accessToken != null && !accessToken.equals(acToken)) {
                        initToken();                   
                }
            }
        });

    }

    /**
     * 从SharedPreference中读取token数据，并初步判断数据的有效性
     * @throws BaiduException 
     * @throws JSONException 
     * 
     */
    protected void initToken() {
        SharedPreferences sp = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE);
        if (sp == null) {
            return;
        }
        this.accessToken = sp.getString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, null);      
        long expires = sp.getLong(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS, 0);
        long createTime = sp.getLong(BAIDU_SDK_CONFIG_PROP_CREATE_TIME, 0);
        long current = System.currentTimeMillis();
        this.expireTime = createTime + expires;
        if (expireTime != 0 && expireTime < current) {
            clearToken();
        }

    }

    /**
     * 清楚SharedPreference中的所有数据
     */
    protected void clearToken() {
        Editor editor = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE).edit();
        editor.remove(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN);
        editor.remove(BAIDU_SDK_CONFIG_PROP_CREATE_TIME);
        editor.remove(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS);
        editor.commit();
        this.accessToken = null;
        this.expireTime = 0;
    }

    /**
     * 将token信息存储到SharedPreference中
     * 
     * @param values token信息的key-value形式
     */
    protected void storeToken(Bundle values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        this.accessToken = values.getString("access_token");
        long expiresIn = Long.parseLong(values.getString("expires_in"));
        this.expireTime = System.currentTimeMillis() + expiresIn;
        Editor editor = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE).edit();
        editor.putString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, this.accessToken);
        editor.putLong(BAIDU_SDK_CONFIG_PROP_CREATE_TIME, System.currentTimeMillis());
        editor.putLong(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS, expiresIn);
        editor.commit();

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        if (this.accessToken != null) {
            bundle.putString(KEY_ACCESS_TOKEN, this.accessToken);
        }
        if (this.expireTime != 0) {
            bundle.putLong(KEY_EXPIRE_TIME, this.expireTime);
        }
        bundle.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<AccessTokenManager> CREATOR = new Parcelable.Creator<AccessTokenManager>() {

        @Override
        public AccessTokenManager createFromParcel(Parcel source) {
            return new AccessTokenManager(source);
        }

        @Override
        public AccessTokenManager[] newArray(int size) {
            return new AccessTokenManager[size];
        }

    };

    /**
     * 判断当前的token信息是否有效
     * 
     * @return true/false
     * @throws BaiduException 
     */
    protected boolean isSessionVaild()  {
        if (this.accessToken == null || this.expireTime == 0) {
            initToken();
        }
        if (this.accessToken != null && this.expireTime != 0
                && System.currentTimeMillis() < this.expireTime) {
            return true;
        }
        return false;
    }

    /**
     * 获取AccessToken信息
     * 
     * @return accessToken
     * @throws BaiduException 
     * 
     */
    public String getAccessToken() {
        if (this.accessToken == null) {
            initToken();
        }
        return this.accessToken;
    }

}
