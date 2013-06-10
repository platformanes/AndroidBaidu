package com.baidu.ane 
{ 
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.events.StatusEvent;
	import flash.external.ExtensionContext;
	
	/**
	 * 
	 * @author Rect  2013-5-6 
	 * 
	 */
	public class BaiduExtension extends EventDispatcher 
	{ 
		public static const BAIDU_FUNCTION_SYN:String = "baidu_function_syn";//与java端中Map里的key一致
		public static const BAIDU_FUNCTION_LOGIN:String = "baidu_function_login";//与java端中Map里的key一致
		public static const BAIDU_FUNCTION_ASYN:String = "baidu_function_asyn";//与java端中Map里的key一致
		public static const BAIDU_FUNCTION_EXIT:String = "baidu_function_exit";//与java端中Map里的key一致
		
		public static const EXTENSION_ID:String = "com.baidu.ane";//与extension.xml中的id标签一致
		private var extContext:ExtensionContext; 
		
		/**单例的实例*/
		private static var _instance:BaiduExtension; 
		public function BaiduExtension(target:IEventDispatcher=null)
		{
			super(target);
			if(extContext == null) {
				extContext = ExtensionContext.createExtensionContext(EXTENSION_ID, "");
				extContext.addEventListener(StatusEvent.STATUS, statusHandler);
				
			}
			
			
		} 
		
		//第二个为参数，会传入java代码中的FREExtension的createContext方法
		/**
		 * 获取实例
		 * @return DLExtension 单例
		 */
		public static function getInstance():BaiduExtension
		{
			if(_instance == null) 
				_instance = new BaiduExtension();
			return _instance;
		}
		
		/**
		 * 转抛事件
		 * @param event 事件
		 */
		private function statusHandler(event:StatusEvent):void
		{
			dispatchEvent(event);
		}
		
		/**
		 *init发送函数  
		 * @param key 暂时传什么都可以  留着可能要用
		 * @return 
		 * 
		 */		
		public function BaiduSynGetLoggedInUser(key:int):String{
			if(extContext ){
				return extContext.call(BAIDU_FUNCTION_SYN,key) as String;
			}
			return "call login failed";
		} 
		
		/**
		 * 
		 * @param clientId
		 * @param clientSecret
		 * @return 
		 * 
		 */				
		public function BaiduLogIn(clientId:String,clientSecret:String):String{
			if(extContext ){
				return extContext.call(BAIDU_FUNCTION_LOGIN,clientId,clientSecret) as String;
			}
			return "call login failed";
		} 
		/**
		 *付费发送函数 
		 * @param key 暂时传什么都可以 留着以后可能要用
		 * @return 
		 * 
		 */		 
		public function BaiduASynGetLoggedInUser(key:int):String{
			if(extContext ){ 
				return extContext.call(BAIDU_FUNCTION_ASYN,key)as String;
			}
			return "call pay failed";
		}
		
		/**
		 *退出SDK时候调用   这个函数只在退出游戏的时候调用  
		 * @param key
		 * @return 
		 * 
		 */		
		public function BaiduLogout(key:int):String{
			if(extContext){ 
				return extContext.call(BAIDU_FUNCTION_EXIT,key) as String;
			}
			return "call exit failed";
		}
	} 
}