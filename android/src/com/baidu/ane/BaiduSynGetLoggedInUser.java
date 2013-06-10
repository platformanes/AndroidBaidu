package com.baidu.ane;

import java.io.IOException;

import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.baidu.api.BaiduException;

/**
 * 初始化SDK
 * @author Rect
 * @version  Time：2013-5-8 
 */
public class BaiduSynGetLoggedInUser implements FREFunction {

	private String TAG = "BaiduSynGetLoggedInUser";
	private FREContext _context;
	@Override
	public FREObject call(final FREContext context, FREObject[] arg1) {
		// TODO Auto-generated method stub
		_context = context;
		FREObject result = null; 
		// TODO Auto-generated method stub
		//--------------------------------
		//在这里做初始化的操作 我这里直接传回。。
		if(BaiduUtil.baidu == null)
		{
			_context.dispatchStatusEventAsync(TAG, "尚未登录");
			return null;
		}
		
		try {

            String json = BaiduUtil.baidu.request(BaiduUtil.url, null, "GET");
            if (json != null) {
                callBack(json);
            }
        } catch (IOException e) {
            callBack("api exception"+ e.toString());
        } catch (BaiduException e) {
            callBack("baidu exception"+ e.toString());
        }
		
		//--------------------------------
		
		return result;
	}

	/**
	 * 初始化回调 把初始化结果传给AS端
	 */
	public void callBack(String str){
		Log.d(TAG, "---------初始化返回-------");
		_context.dispatchStatusEventAsync(TAG, str);
	}

}
