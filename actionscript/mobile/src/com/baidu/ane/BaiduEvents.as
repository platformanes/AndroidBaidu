package com.baidu.ane 
{ 
	/**
	 * 
	 * @author Rect  2013-5-6 
	 * 
	 */
	public class BaiduEvents 
	{ 
		public function BaiduEvents()
		{
		} 
		/**************************平台通知************************************/
		/**
		 *init 
		 */		
		public static const BAIDU_SYN_STATUS:String = "BaiduSynGetLoggedInUser";
		/**
		 * 用户登录
		 */
		public static const BAIDU_LOGIN_STATUS : String = "BaiduLogin";
		
		/**
		 * 用户注销
		 */
		public static const BAIDU_LOGOUT_STATUS : String = "BaiduLogout";
		
		/**
		 * 充值
		 */
		public static const BAIDU_ASYN_STATUS : String = "BaiduASynGetLoggedInUser";
	} 
}