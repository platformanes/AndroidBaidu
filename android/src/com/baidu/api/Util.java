/**
 * Copyright (c) 2011 Baidu.com, Inc. All Rights Reserved
 */
package com.baidu.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * Util类封装了一些基本的方法
 * 
 * @author chenhetong(chenhetong@baidu.com)
 * 
 */
public class Util {

    private static boolean ENABLE_LOG = true;

    /**
     * 判断字符串是否为空
     * 
     * @param query 待检测的字符串
     * @return boolean
     */
    public static boolean isEmpty(String query) {
        boolean ret = false;
        if (query == null || query.trim().equals("")) {
            ret = true;
        }
        return ret;
    }

    /**
     * 将key-value形式的参数串，转换成key1=value1&key2=value2格式的query
     * 
     * @param params key-value参数
     * @return key1=value1&key2=value2格式的query
     */
    public static String encodeUrl(Bundle params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            String paramValue = params.getString(key);
            if (paramValue == null) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(key)).append("=").append(URLEncoder.encode(paramValue));
        }
        return sb.toString();
    }

    /**
     * 将key1=value1&key2=value2格式的query转换成key-value形式的参数串
     * 
     * @param query key1=value1&key2=value2格式的query
     * @return key-value形式的bundle
     */
    public static Bundle decodeUrl(String query) {
        Bundle ret = new Bundle();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyAndValues = pair.split("=");
                if (keyAndValues != null && keyAndValues.length == 2) {
                    String key = keyAndValues[0];
                    String value = keyAndValues[1];
                    if (!isEmpty(key) && !isEmpty(value)) {
                        ret.putString(URLDecoder.decode(key), URLDecoder.decode(value));
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 提取回调url中的token信息，用于User-Agent Flow中的授权操作
     * 
     * @param url 回调的url，包括token信息
     * @return 返回bundle类型的token信息
     */
    public static Bundle parseUrl(String url) {
        Bundle ret = null;
        url = url.replace("bdconnect", "http");
        try {
            URL urlParam = new URL(url);
            ret = decodeUrl(urlParam.getQuery());
            ret.putAll(decodeUrl(urlParam.getRef()));
            return ret;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }

    /**
     * 访问url信息，并获取返回信息
     * 
     * @param url url请求
     * @param method 请求的方法
     * @param params 请求的参数
     * @return 返回的信息
     * @throws MalformedURLException url地址格式异常时，抛出该异常
     * @throws IOException 当请求发生网络异常时，抛出该异常
     */
    public static String openUrl(String url, String method, Bundle params)
            throws MalformedURLException, IOException {
        HttpURLConnection conn = null;
        String response = "";
        String charset = "UTF-8";
        InputStream is = null;
        String ctype = "application/x-www-form-urlencoded;charset=" + charset;
        try {
            if ("GET".equals(method)) {
                url = url + "?" + encodeUrl(params);
                conn = getURLConnection(url, method, ctype);
            } else {
                conn = getURLConnection(url, method, ctype);
                String query = encodeUrl(params);
                byte[] content = query.getBytes(charset);
                conn.getOutputStream().write(content);
            }
            int respCode = conn.getResponseCode();
            if (200 == respCode) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            response = read(is);
            return response;
        } finally {
            if (is != null) {
                is.close();
                is = null;
            }
        }
    }

    /**
     * 执行文件上传的方法
     * 
     * @param url 文件上传操作的全路径
     * @param parameters 文本参数和文件参数
     * @return 返回 json格式的响应字符串
     * @throws IOException 当网络发生异常时抛出该异常
     */
    public static String uploadFile(String url, Bundle parameters) throws IOException {
        String charset = "UTF-8";
        String boundary = System.currentTimeMillis() + "";
        String ctype = "multipart/form-data;charset=" + charset + ";boundary=" + boundary;
        String method = "POST";
        String response = "";
        OutputStream out = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            conn = getURLConnection(url, method, ctype);
            out = conn.getOutputStream();
            byte[] entryBoundaryBytes = ("\r\n--" + boundary + "\r\n").getBytes(charset);
            //发送文本参数
            if (parameters != null) {
                for (String key : parameters.keySet()) {
                    Object paramValue = parameters.get(key);
                    if ((paramValue instanceof byte[])) {
                        //发送文件参数
                        byte[] fileParameters = getFileParameters(key, "content/unknown", charset);
                        out.write(entryBoundaryBytes);
                        out.write(fileParameters);
                        out.write((byte[]) paramValue);
                        continue;
                    }
                    String value = (String) paramValue;
                    byte[] textParameters = getTextParameters(key, value, charset);
                    out.write(entryBoundaryBytes);
                    out.write(textParameters);
                }
            }

            //请求结束标志
            byte[] endBoundaryBytes = ("\r\n--" + boundary + "--\r\n").getBytes(charset);
            out.write(endBoundaryBytes);
            out.flush();
            int respCode = conn.getResponseCode();
            if (respCode == 200) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            response = read(is);
            return response;
        } finally {
            if (is != null) {
                is.close();
                is = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
        }
    }

    /**
     * 构建文本参数的byte流信息
     * 
     * @param fieldName 参数名称
     * @param fieldValue 参数值
     * @param charset 字符编码格式
     * @return 返回文件的字节流信息
     * @throws UnsupportedEncodingException
     */
    private static byte[] getTextParameters(String fieldName, String fieldValue, String charset)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append("Content-Disposition:form-data;name=\"");
        sb.append(fieldName);
        sb.append("\"Content-Type:text/plain\r\n\r\n");
        sb.append(fieldValue);
        return sb.toString().getBytes(charset);
    }

    /**
     * 构建文件的流信息
     * 
     * @param fieldName 参数名称
     * @param fileName 文件名称
     * @param contentType 文件类型
     * @param charset 编码格式
     * @return 文件的byte信息
     * @throws UnsupportedEncodingException
     */
    private static byte[] getFileParameters(String fileName, String contentType, String charset)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append("Content-Disposition:form-data;name=\"");
        sb.append("upload");
        sb.append("\";filename=\"");
        sb.append(fileName);
        sb.append("\"\r\nContent-Type:");
        sb.append(contentType);;
        sb.append("\r\n\r\n");
        return sb.toString().getBytes(charset);
    }

    /**
     * 将InputStream中的流信息读取字符串
     * 
     * @param is InputStream的流信息
     * @return 流信息转换成的字符串
     * @throws IOException 当网络异常时，抛出IOException
     */
    private static String read(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader bf = new BufferedReader(new InputStreamReader(is), 1000);
        String str = null;
        while ((str = bf.readLine()) != null) {
            sb.append(str);
        }
        if (bf != null) {
            bf.close();
            bf = null;
        }
        return sb.toString();
    }

    /**
     * 根据基本信息获取URLConnection
     * 
     * @param url url地址
     * @param method 请求方法“GET”/“POST”
     * @param ctype Content-Type的格式
     * @return 返回HttpURLException
     * @throws MalformedURLException url格式异常时，抛出该异常
     * @throws IOException 网络异常时抛出该异常
     */
    private static HttpURLConnection getURLConnection(String url, String method, String ctype)
            throws MalformedURLException, IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("User-Agent", System.getProperties().getProperty("http.agent")
                + " BaiduOpenApiAndroidSDK " + " os: " + android.os.Build.VERSION.SDK + ","
                + android.os.Build.VERSION.RELEASE);
        conn.setRequestProperty("Content-Type", ctype);
        conn.setRequestProperty("Connection", "Keep-Alive");
        return conn;
    }

    public static void clearCookies(Context context) {
        @SuppressWarnings("unused")
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    /**
     * 将json信息转换成JSONbject
     * 
     * @param response json字符串信息
     * @return JSONObject对象
     * @throws JSONException json格式错误，无法转换成JSONObject对象时抛出该异常
     * @throws BaiduException 当json信息中包含error_code信息时抛出该异常
     */
/*    public static JSONObject parseJSON(String response) throws JSONException, BaiduException {
        if (isEmpty(response)) {
            return null;
        }
        JSONObject json = new JSONObject(response);
        //抛出OAuth2授权异常，
        if (json.has("error") && json.has("error_description")) {
            String errorCode = json.getString("errorCode");
            String errorDesp = json.getString("error_description");
            throw new BaiduException(errorCode, errorDesp);
        }
        //抛出Api调用异常，封装在BaiduApiException
        if (json.has("error_code") && json.has("error_msg")) {
            String errorCode = json.getString("erro_code");
            String errorMsg = json.getString("error_msg");
            throw new BaiduException(errorCode, errorMsg);
        }

        return json;
    }*/

    public static void checkResponse(String response) throws BaiduException {
        if (isEmpty(response)) {
            return;
        }
        JSONObject json = null;
        try {
            json = new JSONObject(response);
            //抛出OAuth2授权异常，
            if (json.has("error") && json.has("error_description")) {
                String errorCode = json.getString("errorCode");
                String errorDesp = json.getString("error_description");
                throw new BaiduException(errorCode, errorDesp);
            }
            //抛出Api调用异常，封装在BaiduApiException
            if (json.has("error_code") && json.has("error_msg")) {
                String errorCode = json.getString("erro_code");
                String errorMsg = json.getString("error_msg");
                throw new BaiduException(errorCode, errorMsg);
            }
        } catch (JSONException e) {
            Util.logd("Baidu Parse Json Exception ", ""+e);
        }

    }

    /**
     * 展示一个通用的弹出框UI
     * 
     * @param context 展示弹出框的上下文环境
     * @param title 警告的title信息
     * @param text 警告信息
     */
    public static void showAlert(Context context, String title, String text) {
        AlertDialog alertDialog = new Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    /**
     * 记录相应的log信息
     * 
     * @param tag log tag 信息
     * @param msg log msg 信息
     */
    public static void logd(String tag, String msg) {
        if (ENABLE_LOG) {
            Log.d(tag, msg);
        }
    }

}
