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

	//dubbo�����ַ
	private  String URL;

	private  String VERSION ;

	private  String SERVICE_NAME;   

	private Object object;
	 
	 public void init() {
		// ��ǰӦ������
		ApplicationConfig application = new ApplicationConfig();
		application.setName("hehe_consumer");

		// ע�⣺ReferenceConfigΪ�ض����ڲ���װ����ע�����ĵ����ӣ��Լ�������ṩ��������
		// ����Զ�̷���
		ReferenceConfig  reference = new ReferenceConfig(); // ��ʵ�����أ���װ����ע�����ĵ������Լ����ṩ�ߵ����ӣ������л��棬�����������ڴ������й©
		reference.setApplication(application);
		reference.setId(ID);
		reference.setVersion(VERSION);
		//dubbo������
		reference.setInterface(SERVICE_NAME);

		//dubboip��ַ
		reference.setUrl(URL);

		// �ͱ���beanһ��ʹ��xxxService
	    object = reference.get(); // ע�⣺�˴�������ڲ���װ������ͨѶϸ�ڣ�������أ��뻺�渴��\
		}
	 
    public void setupTest(){  
		//������Գ�ʼֵ��setupTestֻ�ڲ��Կ�ʼǰʹ��  
		System.out.println("setupTest");  
	}  

	public SampleResult runTest(JavaSamplerContext arg0) {
		SampleResult sr = new SampleResult(); ; 
		try {
			//��ȡ����
			ID = arg0.getParameter("ID");  
			URL = arg0.getParameter("URL"); 
			
			VERSION = arg0.getParameter("VERSION");  
			SERVICE_NAME = arg0.getParameter("SERVICE_NAME"); 
			
			//dubbo��ʼ��
			init();
			
			//jmeter�������
		 
			sr.setSampleLabel(label);
	
			sr.sampleStart(); 
			
//			DemoService demoService = (DemoService)object;
//		    String hello = demoService.sayHello("tom");
			
		    sr.setResponseCode("00000");
//		    sr.setResponseMessage(hello);
		    
			sr.setSuccessful(true); 
			sr.sampleEnd(); // jmeter ����ͳ����Ӧʱ����  
		
		} catch (Exception e) {
			e.printStackTrace();
			sr.setResponseCode("999");
			sr.setResponseMessage(e.getMessage());
			sr.setSuccessful(false);
		}
		return sr;
	}
	
	public Arguments getDefaultParameters(){  
		//�������壬��ʾ��ǰ̨��Ҳ���Բ�����  
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
