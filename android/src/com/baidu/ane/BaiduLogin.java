package com.baidu.ane;

import android.os.Bundle;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.baidu.api.AccessTokenManager;
import com.baidu.api.Baidu;
import com.baidu.api.BaiduDialogError;
import com.baidu.api.BaiduException;
import com.baidu.api.BaiduDialog.BaiduDialogListener;

/**
 * 执行登录
 * @author Rect
 * @version  Time：2013-5-8 
 */
public class BaiduLogin implements FREFunction {

	private String TAG = "BaiduLogin";
	private FREContext _context;

	private String accessToken = null;
	@Override
	public FREObject call(final FREContext context, FREObject[] arg1) {
		// TODO Auto-generated method stub
		_context = context;
		FREObject result = null;  

		// TODO Auto-generated method stub
		//--------------------------------
		try
		{
			BaiduUtil.clientId = arg1[0].getAsString();
			BaiduUtil.clientSecret = arg1[1].getAsString();
		}
		catch(Exception e)
		{
			_context.dispatchStatusEventAsync(TAG, "输入参数错误！");
			return result;
		}
		//在这里做登录的操作 我这里直接传回。。
		BaiduUtil.baidu = new Baidu(BaiduUtil.clientId, BaiduUtil.clientSecret, this._context.getActivity());
		BaiduUtil.baidu.authorize(this._context.getActivity(), new BaiduDialogListener() {

			@Override
			public void onComplete(Bundle values) {
				AccessTokenManager atm = BaiduUtil.baidu.getAccessTokenManager();
				BaiduUtil.accessToken = atm.getAccessToken();
				accessToken = atm.getAccessToken();
				callBack(accessToken);
			}

			@Override
			public void onBaiduException(BaiduException e) {
				accessToken = e.getMessage();
				callBack(accessToken);
			}

			@Override
			public void onError(BaiduDialogError e) {
				accessToken = e.getMessage();
				callBack(accessToken);
			}

			@Override
			public void onCancel() {
				accessToken = "onCancel";
				callBack(accessToken);
			}
		});
		//--------------------------------

		return result;
	}
	/**
	 * 登录回调 把登录结果传给AS端 一般都会把获得的游戏ID传回去  怎么传自己看着办
	 */
	public void callBack(String str){
		Log.d(TAG, "---------Login返回-------");
		_context.dispatchStatusEventAsync(TAG, "accessToken:"+str);
	}
}
