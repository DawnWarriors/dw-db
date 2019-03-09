package dw.common.util.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import dw.common.util.aop.ProxyTargetUtils;
import dw.common.util.str.StrUtil;
import dw.common.util.type.TypeUtil;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapUtil
{
	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 合并Map，将from中的键值复制到to中，并将to返回；
	 *
	 * @param to   复制至
	 * @param from 复制来源
	 * @return 合并后的Map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Map mergeMap(Map to, Map from)
	{
		if (from == null)
			return to;
		if (to == null)
			to = new HashMap();
		Set<String> keys = from.keySet();
		for (String key : keys)
		{
			if (to.get(key) == null)
			{
				to.put(key, from.get(key));
			}
		}
		return to;
	}

	/**
	 * 从map按找keys指定的键中取出一个新的Map
	 *
	 * @param map  Map
	 * @param keys 需要抽出的Key集合
	 * @return 抽出的Map
	 */
	public static Map<String,Object> getMapByKeyList(Map<String,Object> map, List<String> keys)
	{
		Map<String,Object> newMap = new HashMap<>();
		if (keys != null && keys.size() > 0)
		{
			for (String key : keys)
			{
				Object val = map.get(key);
				if (val != null)
				{
					newMap.put(key, val);
				}
			}
		}
		return newMap;
	}

	/**
	 * 根据给定的key和value集合，创建Map
	 * 长度以key为准，
	 *
	 * @param keys           key串
	 * @param keySeparator   key串分隔符
	 * @param values         value串
	 * @param valueSeparator value串分隔符
	 * @return 生成的新Map
	 */
	public static Map<String,String> createMap(String keys, String keySeparator, String values, String valueSeparator)
	{
		if (keys == null || keys.length() == 0)
		{
			return null;
		}
		String keyAry[] = keys.split(keySeparator);
		String valueAry[] = values.split(valueSeparator);
		return createMap(keyAry,valueAry);
	}

	/**
	 * 根据给定的key和value集合，创建Map
	 * 长度以key为准，
	 * @param keys
	 * @param values
	 * @param <K> K数组
	 * @param <V> V数组
	 * @return Map
	 */
	public static <K,V> Map<K,V> createMap(K[] keys,V values[])
	{
		if (keys == null || keys.length == 0)
		{
			return null;
		}
		Map<K,V> map = new HashMap<>();
		for (int i = 0 ; i < keys.length ; i++)
		{
			K key = keys[i];
			if (values.length - 1 < i)
			{
				map.put(key, null);
			} else
			{
				map.put(key, values[i]);
			}
		}
		return map;
	}

	/**
	 * 将Map数据赋值给一个新的Bean对象
	 *
	 * @param map       Map对象
	 * @param beanClass Class
	 * @param <T>       泛型
	 * @return 赋值好的Bean对象
	 */
	public static <T> T mapToObject(Map<String,Object> map, Class<T> beanClass)
	{
		return objectMapper.convertValue(map, beanClass);
	}

	/**
	 * 为指定对象赋值
	 *
	 * @param map Map对象
	 * @param obj Bean对象
	 * @param <T> 泛型
	 * @return 赋值后的Bean对象
	 */
	public static <T> T mapToObject(Map<String,Object> map, T obj)
	{
		if (map == null)
		{
			return obj;
		}
		try
		{
			Object target;
			try
			{
				target = (T) ProxyTargetUtils.getTarget(obj);
			} catch (Exception e)
			{
				throw new RuntimeException(e);
			}
			Field[] fields = target.getClass().getDeclaredFields();
			for (Field field : fields)
			{
				int mod = field.getModifiers();
				if (Modifier.isStatic(mod) || Modifier.isFinal(mod))
				{
					continue;
				}
				if (!field.isAccessible())
				{
					field.setAccessible(true);
				}
				Object old_value = map.get(field.getName());
				if (old_value == null)
				{
					continue;
				}
				//将Map中的对象转换成属性对应的类型
				Object value = TypeUtil.typeConversion(old_value, field.getType());
				//field.set(obj, value);
				//----cyf 修改为调用set方法设值 2017-1-1
				PropertyDescriptor pd = new PropertyDescriptor(field.getName(), obj.getClass());
				Method method = pd.getWriteMethod();
				method.invoke(obj, value);
			}
			return obj;
		} catch (IllegalAccessException | IntrospectionException | IllegalArgumentException | InvocationTargetException | ParseException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * 将一个Bean对象数据转换为Map数据
	 *
	 * @param obj Bean对象
	 * @return Map
	 */
	public static Map<String,Object> objectToMap(Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		try
		{
			obj = ProxyTargetUtils.getTarget(obj);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		Field[] declaredFields = obj.getClass().getDeclaredFields();
		for (Field field : declaredFields)
		{
			field.setAccessible(true);
			try
			{
				//----cyf 修改为调用get方法取值 2017-1-1
				PropertyDescriptor pd = new PropertyDescriptor(field.getName(), obj.getClass());
				Method method = pd.getReadMethod();
				Object value = method.invoke(obj);
				map.put(field.getName(), value);
			} catch (IllegalArgumentException | IllegalAccessException | IntrospectionException | InvocationTargetException e)
			{
				throw new RuntimeException(e);
			}
		}
		return map;
	}

	/**
	 * objectList 转换成以指定属性名的属性值为key,object为value的Map
	 * 注意：需要保证属性值在该集合中唯一，否则可能导致部分对象被覆盖，一般使用ID
	 * 属性类型为字符串类型
	 *
	 * @param list    对象列表
	 * @param keyName 作为主键的属性名
	 * @param <T>     泛型
	 * @return Map对象
	 */
	public static <T> Map<String,T> objListToMapByKey(List<T> list, String keyName)
	{
		Map<String,T> map = new HashMap<>();
		if (list != null && list.size() > 0)
		{
			for (T t : list)
			{
				try
				{
					Field field = t.getClass().getDeclaredField(keyName);
					field.setAccessible(true);
					//String key = StrUtil.objToString(field.get(t));
					//----cyf 修改为调用get方法取值 2017-1-1
					PropertyDescriptor pd = new PropertyDescriptor(field.getName(), t.getClass());
					Method method = pd.getReadMethod();
					Object value = method.invoke(t);
					String key = StrUtil.objToString(value);
					map.put(key, t);
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | IntrospectionException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		return map;
	}

	/**
	 * 根据keyCross指定的key转换规则，将dataMap中的数据，按照新key重新复制组装成一个新的Map
	 *
	 * @param dataMap  数据集
	 * @param keyCross key转换规则
	 * @return 新组装的Map
	 */
	public static Map<String,Object> mapCopyByKeyCross(Map<String,Object> dataMap, Map<String,String> keyCross)
	{
		Map<String,Object> map = new HashMap<>();
		Set<String> oldKeys = keyCross.keySet();
		for (String oldKey : oldKeys)
		{
			String newKey = keyCross.get(oldKey);
			map.put(newKey, dataMap.get(oldKey));
		}
		return map;
	}
}
