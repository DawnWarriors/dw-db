package dw.db.proxy;

import net.sf.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;

public class DwModelProxyFilter implements CallbackFilter
{
	@Override
	public int accept(Method method)
	{
		//TODO 过滤掉从MapUtil中发起的调用
		if (method.getName().startsWith("get") && method.getParameterTypes().length == 0)
		{
			return 0;
		} else
		{
			return 1;
		}
	}
}
