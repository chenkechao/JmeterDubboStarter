package com.one;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
//import com.unj.dubbotest.provider.DemoService;

public class TestConsumer  extends AbstractJavaSamplerClient {
	
	private static String label = "consumer"; 
	
	private   String ID;

	//dubbo服务地址
	private  String URL;

	private  String VERSION ;

	private  String SERVICE_NAME;   

	private Object object;
	 
	 public void init() {
		// 当前应用配置
		ApplicationConfig application = new ApplicationConfig();
		application.setName("hehe_consumer");

		// 注意：ReferenceConfig为重对象，内部封装了与注册中心的连接，以及与服务提供方的连接
		// 引用远程服务
		ReferenceConfig  reference = new ReferenceConfig(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
		reference.setApplication(application);
		reference.setId(ID);
		reference.setVersion(VERSION);
		//dubbo服务名
		reference.setInterface(SERVICE_NAME);

		//dubboip地址
		reference.setUrl(URL);

		// 和本地bean一样使用xxxService
	    object = reference.get(); // 注意：此代理对象内部封装了所有通讯细节，对象较重，请缓存复用\
		}
	 
    public void setupTest(){  
		//定义测试初始值，setupTest只在测试开始前使用  
		System.out.println("setupTest");  
	}  

	public SampleResult runTest(JavaSamplerContext arg0) {
		SampleResult sr = new SampleResult(); ; 
		try {
			//获取参数
			ID = arg0.getParameter("ID");  
			URL = arg0.getParameter("URL"); 
			
			VERSION = arg0.getParameter("VERSION");  
			SERVICE_NAME = arg0.getParameter("SERVICE_NAME"); 
			
			//dubbo初始化
			init();
			
			//jmeter结果对象
		 
			sr.setSampleLabel(label);
	
			sr.sampleStart(); 
			
//			DemoService demoService = (DemoService)object;
//		    String hello = demoService.sayHello("tom");
			
		    sr.setResponseCode("00000");
//		    sr.setResponseMessage(hello);
		    
			sr.setSuccessful(true); 
			sr.sampleEnd(); // jmeter 结束统计响应时间标记  
		
		} catch (Exception e) {
			e.printStackTrace();
			sr.setResponseCode("999");
			sr.setResponseMessage(e.getMessage());
			sr.setSuccessful(false);
		}
		return sr;
	}
	
	public Arguments getDefaultParameters(){  
		//参数定义，显示在前台，也可以不定义  
		Arguments params = new Arguments();  
		params.addArgument("ID", "");  
		params.addArgument("URL", ""); 
		params.addArgument("VERSION", "");  
		params.addArgument("SERVICE_NAME", "");
		return params;  
		}  
	
	public void teardownTest(JavaSamplerContext arg0){  
		super.teardownTest(arg0);  
	}  
}
