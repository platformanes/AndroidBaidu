package com.baidu.ane;

import java.io.IOException;

import android.os.Handler;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.baidu.api.AsyncBaiduRunner;
import com.baidu.api.BaiduException;
import com.baidu.api.AsyncBaiduRunner.RequestListener;

/**
 * 执行付费
 * @author Rect
 * @version  Time：2013-5-8 
 */
public class BaiduASynGetLoggedInUser implements FREFunction {

	private String TAG = "BaiduASynGetLoggedInUser";
	private FREContext _context;
	private Handler mHandler = null;
	@Override
	public FREObject call(final FREContext context, FREObject[] arg1) {
		// TODO Auto-generated method stub
		_context = context;
		FREObject result = null; 
		mHandler = new Handler();
		// TODO Auto-generated method stub
		//--------------------------------
		//在这里做付费的操作 我这里直接传回。。
		if(BaiduUtil.baidu == null)
		{
			_context.dispatchStatusEventAsync(TAG, "尚未登录");
			return null;
		}
		AsyncBaiduRunner runner = new AsyncBaiduRunner(BaiduUtil.baidu);
		runner.request(BaiduUtil.url, null, "POST", new DefaultRequstListener());
		//--------------------------------

		return result;
	}

	public class DefaultRequstListener implements RequestListener {

		/* (non-Javadoc)
		 * @see com.baidu.android.RequestListener#onBaiduException(com.baidu.android.BaiduException)
		 */
		@Override
		public void onBaiduException(BaiduException arg0) {
			// TODO Auto-generated method stub
			callBack(arg0.getMessage());
		}

		/* (non-Javadoc)
		 * @see com.baidu.android.RequestListener#onComplete(java.lang.String)
		 */
		@Override
		public void onComplete(final String value) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					callBack(value);
				}
			});

		}

		/* (non-Javadoc)
		 * @see com.baidu.android.RequestListener#onIOException(java.io.IOException)
		 */
		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			callBack(arg0.getMessage());
		}

	}
	/**
	 * 付费回调 把付费结果传给AS端
	 */
	public void callBack(String str){
		_context.dispatchStatusEventAsync(TAG, "回调:"+str);
	}
}
