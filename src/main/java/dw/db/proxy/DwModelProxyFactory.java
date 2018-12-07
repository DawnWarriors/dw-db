package dw.db.proxy;

import dw.db.base.ModelBase;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class DwModelProxyFactory
{
	public static <T extends ModelBase> T getProxyObject(T target)
	{
		DwModelProxy dwModelProxy = new DwModelProxy();
		return dwModelProxy.getProxyObject(target);
	}

	public static <T extends ModelBase> T getProxyObject(Class<? extends ModelBase> cls)
	{
		T target = null;
		try
		{
			target = (T) cls.newInstance();
		} catch (InstantiationException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		DwModelProxy dwModelProxy = new DwModelProxy();
		return dwModelProxy.getProxyObject(target);
	}
}
