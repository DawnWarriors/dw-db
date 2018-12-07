package dw.common.util.aop;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;

public class ProxyTargetUtils
{
	/**
	 * 获取 目标对象
	 *
	 * @param proxy 代理对象
	 * @return
	 */
	public static Object getTarget(Object proxy)
	{
		if (!isProxy(proxy))
		{
			return proxy;//不是代理对象
		}
		return getCglibProxyTargetObject(proxy);
	}

	private static Object getCglibProxyTargetObject(Object proxy)
	{
		try
		{
			Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
			h.setAccessible(true);
			Object dynamicAdvisedInterceptor = h.get(proxy);
			Field target = dynamicAdvisedInterceptor.getClass().getDeclaredField("target");
			target.setAccessible(true);
			PropertyDescriptor pd = new PropertyDescriptor(target.getName(), dynamicAdvisedInterceptor.getClass());
			Method getTargetMethod = pd.getReadMethod();
			return getTargetMethod.invoke(dynamicAdvisedInterceptor);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static boolean isProxy(Object proxy)
	{
		return Proxy.isProxyClass(proxy.getClass()) || ClassUtils.isCglibProxyClass(proxy.getClass());
	}
}