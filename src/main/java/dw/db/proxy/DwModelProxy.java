package dw.db.proxy;

import dw.common.util.aop.ProxyTargetUtils;
import dw.db.annotation.DwDbSub;
import dw.db.base.DwSubsetLoadService;
import dw.db.base.ModelBase;
import dw.db.util.DwSpringUtil;
import lombok.Data;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Data
public class DwModelProxy implements MethodInterceptor
{
	Object target;

	public <T extends ModelBase> T getProxyObject(T target)
	{
		Class<? extends ModelBase> cls = target.getClass();
		this.target = target;
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(cls);
		enhancer.setCallbacks(new Callback[] { this, new OtherMethodInterceptor(), NoOp.INSTANCE });
		enhancer.setCallbackFilter(new DwModelProxyFilter());
		enhancer.setClassLoader(cls.getClassLoader());
		return (T) enhancer.create();
	}

	@Override
	public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
	{
		Class cls = target.getClass();
		//获取对应的值，如果有值直接返回，没值再进行处理
		Object oldValue = method.invoke(target);
		if (oldValue != null)
		{
			return oldValue;
		}
		//获取对应的属性
		String methodName = method.getName();
		String fldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
		Field field = cls.getDeclaredField(fldName);
		if (field == null)
		{
			return oldValue;
		}
		//获取属性注解，如果属于懒加载再进行处理，不是懒加载不需要处理
		DwDbSub dwDbSub = field.getAnnotation(DwDbSub.class);
		if (dwDbSub == null || !dwDbSub.lazy())
		{
			return oldValue;
		}
		//处理，通过获取一个Service进行处理
		DwSubsetLoadService dwSubsetLoadService = (DwSubsetLoadService) DwSpringUtil.getBean("dwSubsetLoadService");
		dwSubsetLoadService.loadToModel(o, fldName);
		return methodProxy.invokeSuper(o, objects);
	}
}
class OtherMethodInterceptor implements MethodInterceptor
{
	@Override
	public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
	{
		Object target = ProxyTargetUtils.getTarget(o);
		method.invoke(target, objects);
		return methodProxy.invokeSuper(o, objects);
	}
}
