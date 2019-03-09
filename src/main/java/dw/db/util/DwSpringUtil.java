package dw.db.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DwSpringUtil implements ApplicationContextAware
{
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		DwSpringUtil.applicationContext = applicationContext;
	}

	/**
	 * 获取applicationContext
	 *
	 * @return
	 */
	public static ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	/**
	 * 通过name获取 Bean.
	 *
	 * @param name BeanName
	 * @return 实例
	 */
	public static Object getBean(String name)
	{
		return getApplicationContext().getBean(name);
	}

	/**
	 * 通过class获取Bean.
	 *
	 * @param clazz BeanClass
	 * @param <T>   泛型T
	 * @return 实例
	 */
	public static <T> T getBean(Class<T> clazz)
	{
		return getApplicationContext().getBean(clazz);
	}

	/**
	 * 通过name,以及Clazz返回指定的Bean
	 *
	 * @param name  BeanName
	 * @param clazz BeanClass
	 * @param <T>   泛型T
	 * @return 实例
	 */
	public static <T> T getBean(String name, Class<T> clazz)
	{
		return getApplicationContext().getBean(name, clazz);
	}

	/**
	 * 返回继承自某类的所有实例
	 *
	 * @param clazz BeanClass
	 * @param <T>   泛型T
	 * @return 实例Map
	 */
	public static <T> Map<String,T> getBeanByInterface(Class<T> clazz)
	{
		return getApplicationContext().getBeansOfType(clazz);
	}
}