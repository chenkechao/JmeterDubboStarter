package com.one;

import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
//import com.unj.dubbotest.provider.DemoService;

public class TestConsumer  extends AbstractJavaSamplerClient {

	private static final Logger logger = LoggerFactory.getLogger(TestConsumer.class);

	private long start = 0;//记录测试开始时间；
	private long end = 0;//记录测试结束时间；

	private String APP_NAME = "";
	private String URL = "zookeeper://11:2181?backup=12:2181,13:2181";
	private String PROTO = "dubbo";
	private String SERVICE_NAME;
	private GenericService genericService;
	private ReferenceConfig<GenericService> reference;

	private void initDubboClient(){
		reference = new ReferenceConfig<GenericService>();
		reference.setApplication(new ApplicationConfig(APP_NAME));
		reference.setInterface(SERVICE_NAME);
		reference.setProtocol(PROTO);
		reference.setTimeout(3000);
		//reference.setLazy(true);
		reference.setRegistry(new RegistryConfig(URL));
		reference.setGeneric(true);
	}

	//初始化操作
	@Override
	public void setupTest(JavaSamplerContext javaSamplerContext) {
//        ID = javaSamplerContext.getParameter("ID");
//        URL = javaSamplerContext.getParameter("URL");
//        VERSION = javaSamplerContext.getParameter("VERSION");
//        SERVICE_NAME = javaSamplerContext.getParameter("SERVICE_NAME");
		Iterator<String> iterator = javaSamplerContext.getParameterNamesIterator();
		SERVICE_NAME = javaSamplerContext.getParameter(iterator.next());
		initDubboClient();
	}

	/**
	 * 设置默认值
	 * @return
	 */
	public Arguments getDefaultParameters() {
		Arguments arguments = new Arguments();
		arguments.addArgument("接口名(必填)", "com.xx.order.OsdSearchService");
		arguments.addArgument("方法名(必填)", "searchOxx");
		arguments.addArgument("方法参数类型数组(必填)", "['java.lang.String','java.lang.String']");
		arguments.addArgument("请求参数1", "");
		arguments.addArgument("请求参数2", "");
		arguments.addArgument("请求参数3", "");
		arguments.addArgument("请求参数4", "");
		arguments.addArgument("请求参数5", "");
		arguments.addArgument("请求参数6", "");
		arguments.addArgument("请求参数7", "");
		arguments.addArgument("请求参数8", "");
		arguments.addArgument("请求参数9", "");
		arguments.addArgument("请求参数10", "");

		return arguments;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
		SampleResult sr = new SampleResult();
		sr.sampleStart();
		start = System.currentTimeMillis();

		try {
			Iterator<String> iterator = javaSamplerContext.getParameterNamesIterator();
			//1.判断接口类是否在lib目录下
			Class<?> clazz = null;
			String SERVICE_NAME = iterator.next();
			String clazzString = javaSamplerContext.getParameter(SERVICE_NAME);

			try {
				clazz = Class.forName(clazzString);
			} catch (Exception e) {
				sr.setResponseData(("请确认" + clazzString + "是否正确，而且相应包已放入lib目录下").getBytes("UTF-8"));
				return sr;
			}

			Method method = null;
			String METHOD_NAME = iterator.next();
			String METHOD_TYPES = iterator.next();
			String methodName = null;
			List<String> methodTypes = null;
			try {
				methodName = javaSamplerContext.getParameter(METHOD_NAME);
				methodTypes = JSON.parseArray(javaSamplerContext.getParameter(METHOD_TYPES), String.class);

				if (methodTypes != null && methodTypes.size() > 0) {
					Class<?>[] methodTypeArray = new Class<?>[methodTypes.size()];
					for (int i = 0; i < methodTypes.size(); i++) {
						methodTypeArray[i] = forName(methodTypes.get(i));
					}
					method = clazz.getMethod(methodName, methodTypeArray);
				} else {
					method = clazz.getMethod(methodName);
				}
			} catch (Exception e) {
				sr.setResponseData(listAllMethod(clazz));
				return sr;
			}

			if (method.getParameterTypes() == null || method.getParameterTypes().length == 0) {
				return invoke(sr, methodName, genericService,null,null);
			}else{
				Object[] args = new Object[method.getParameterTypes().length];
				for (int i = 0; i < method.getParameterTypes().length; i++) {
					if (!iterator.hasNext()) {
						sr.setResponseData("方法参数有误".getBytes("UTF-8"));
						return sr;
					}

					String value = iterator.next();
					try {
						if (Collection.class.isAssignableFrom(method.getParameterTypes()[i])) {
							//仅支持list内直接嵌套真实类型
							args[i] = JSON.parseArray(javaSamplerContext.getParameter(value), (Class<?>) ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0]);
						} else {
							args[i] = JSON.parseObject(javaSamplerContext.getParameter(value), method.getParameterTypes()[i]);
						}
					} catch (RuntimeException e) {
						sr.setResponseData("参数格式错误".getBytes("UTF-8"));
						return sr;
					}
					return invoke(sr, methodName, genericService,methodTypes.toArray(new String[]{}),args);

				}
			}

		} catch (Exception e) {
			logger.error("ExpertServiceJMeter response error = " + e.getMessage());
		} finally {
			sr.sampleEnd();
		}
		return sr;
	}

	@Override
	public void teardownTest(JavaSamplerContext arg0) {
		end = System.currentTimeMillis();
		logger.info("    cost time: " + (end - start) + "ms");
	}

	private SampleResult invoke(SampleResult sampleResult, String methodName,GenericService genericService, String[] methodTypes,Object... args) throws IOException {
		ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
		try {
			ReferenceConfigCache cache = ReferenceConfigCache.getCache();
			genericService = cache.get(reference);
			Object result = genericService.$invoke(methodName,methodTypes,args);
			sampleResult.setSuccessful(true);
			sampleResult.setResponseData(JSON.toJSONString(result).getBytes("UTF-8"));
		} catch (Exception e) {
			sampleResult.setSuccessful(false);
			e.printStackTrace(new java.io.PrintWriter(buf, true));
			sampleResult.setResponseData(("运行错误,错误信息如下:" + buf.toString()).getBytes("UTF-8"));
		} finally {
			buf.close();
		}
		return sampleResult;
	}


	static Map<String, Class<?>> classForNameMap = new HashMap<String, Class<?>>();
	static {
		classForNameMap.put(int.class.getName(), int.class);
		classForNameMap.put(long.class.getName(), long.class);
		classForNameMap.put(float.class.getName(), float.class);
		classForNameMap.put(double.class.getName(), double.class);
		classForNameMap.put(byte.class.getName(), byte.class);
		classForNameMap.put(boolean.class.getName(), boolean.class);
		classForNameMap.put(short.class.getName(), short.class);
		classForNameMap.put(char.class.getName(), char.class);
	}

	private Class<?> forName(String string) throws ClassNotFoundException {
		if (classForNameMap.get(string) != null) {
			return classForNameMap.get(string);
		}
		return Class.forName(string);
	}

	private List<String> toList(Class<?>[] parameterTypes) {
		List<String> list = new ArrayList<String>();
		for (Class<?> parameterType : parameterTypes) {
			list.add(parameterType.getName());
		}
		return list;
	}

	private byte[] listAllMethod(Class<?> clazz) throws UnsupportedEncodingException {
		JSONArray jsonArray = new JSONArray();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("方法参数类型数组", toList(method.getParameterTypes()));
			jsonObject.put("方法名", method.getName());
			jsonArray.add(jsonObject);
		}
		return ("方法不存在,请核对.接口" + clazz.getName() + "可访问的方法一共有:" + jsonArray.toJSONString()).getBytes("UTF-8");
	}
}
