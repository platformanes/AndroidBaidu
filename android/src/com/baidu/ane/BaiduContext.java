package com.baidu.ane;

import java.util.HashMap;
import java.util.Map;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;

/**
 * @author Rect
 * @version  Time：2013-5-8 
 */
public class BaiduContext extends FREContext {
	/**
	 * INIT sdk
	 */
	public static final String BAIDU_FUNCTION_SYN = "baidu_function_syn";
	/**
	 * 登录Key
	 */
	public static final String BAIDU_FUNCTION_LOGIN = "baidu_function_login";
	/**
	 * 付费Key
	 */
	public static final String BAIDU_FUNCTION_ASYN = "baidu_function_asyn";
	/**
	 * 退出Key
	 */
	public static final String BAIDU_FUNCTION_EXIT = "baidu_function_exit";
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, FREFunction> getFunctions() {
		// TODO Auto-generated method stub
		Map<String, FREFunction> map = new HashMap<String, FREFunction>();
//	       //映射
		   map.put(BAIDU_FUNCTION_SYN, new BaiduSynGetLoggedInUser());
	       map.put(BAIDU_FUNCTION_LOGIN, new BaiduLogin());
	       map.put(BAIDU_FUNCTION_ASYN, new BaiduASynGetLoggedInUser());
	       map.put(BAIDU_FUNCTION_EXIT, new BaiduLogout());
	       return map;
	}

}
