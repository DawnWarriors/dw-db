package dw.db.base;

import dw.common.util.aop.ProxyTargetUtils;
import dw.common.util.str.StrUtil;
import dw.common.util.type.TypeUtil;
import dw.db.annotation.ServiceAutoTrans;
import dw.db.constant.DwDbConstant.SubsetTypeInfo;
import dw.db.constant.DwDbConstant.SubsetSelectInfo;
import dw.db.constant.DwDbConstant.SubsetType;
import dw.db.constant.DwDbConstant.SubsetSelect;
import dw.db.sql.SqlFilterUtil;
import dw.db.trans.Database;
import dw.db.trans.TransactionManager;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.*;

@Service
public class DwSubsetLoadService
{
	/**
	 * 懒加载数据加载
	 *
	 * @param o         model对象
	 * @param fieldName 字段名
	 */
	@ServiceAutoTrans
	public void loadToModel(Object o, String fieldName)
	{
		//根据字段类型、过滤条件、查询SQL去执行对应查询，并为字段设值
		Object target = ProxyTargetUtils.getTarget(o);
		ObjectProxyFactory.SubsetInfo subsetInfo = ObjectProxyFactory.getDBModelInfo(target.getClass()).getSubsetInfoMap().get(fieldName);
		SubsetTypeInfo subsetTypeInfo = subsetInfo.getSubsetTypeInfo();
		if (subsetTypeInfo == null || subsetTypeInfo.getSubsetType() == null)
		{
			return;
		}
		SubsetSelectInfo subsetSelectInfo = subsetInfo.getSubsetSelectInfo();
		//加载数据
		Database db = TransactionManager.getCurrentDBSession();
		Object result = null;
		try
		{
			if (subsetTypeInfo.getModelClass() != null)
			{
				result = loadModel(db, o, subsetTypeInfo, subsetSelectInfo);
			} else
			{
				result = loadObject(db, o, subsetTypeInfo, subsetSelectInfo);
			}
			if (result != null)
			{
				PropertyDescriptor pd = new PropertyDescriptor(fieldName, o.getClass());
				Method method = pd.getWriteMethod();
				if (SubsetType.OBJECT == subsetTypeInfo.getSubsetType())
				{
					Object value = TypeUtil.typeConversion(result, subsetTypeInfo.getType());
					method.invoke(o, value);
				} else
				{
					method.invoke(o, result);
				}
			}
		} catch (IntrospectionException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
		} catch (ParseException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 懒加载数据加载——Model加载
	 *
	 * @param db               DB Session
	 * @param o                model对象
	 * @param subsetTypeInfo   子集类型
	 * @param subsetSelectInfo 子集加载条件
	 * @return 子集数据
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 */
	private Object loadModel(Database db, Object o, SubsetTypeInfo subsetTypeInfo, SubsetSelectInfo subsetSelectInfo) throws IllegalAccessException, IntrospectionException, InvocationTargetException
	{
		Class modelCls = subsetTypeInfo.getModelClass();
		Map<String,Object> paramGetter = getLoadOneParamsBySelectInfo(o, subsetSelectInfo);
		switch (subsetTypeInfo.getSubsetType())
		{
		case SubsetType.DW_MODEL:
			switch (subsetSelectInfo.getSelectType())
			{
			case SubsetSelect.FILTER:
				return ObjectProxyFactory.queryObject(db, modelCls, subsetSelectInfo.getFinalFilter(), paramGetter);
			case SubsetSelect.SQL:
			case SubsetSelect.SQL_FILTER:
				return ObjectProxyFactory.queryObjectBySql(db, modelCls, subsetSelectInfo.getFinalSql(), paramGetter);
			default:
				return null;
			}
		case SubsetType.DW_MODEL_LIST:
			switch (subsetSelectInfo.getSelectType())
			{
			case SubsetSelect.FILTER:
				return ObjectProxyFactory.queryObjects(db, modelCls, subsetSelectInfo.getFinalFilter(), paramGetter, subsetSelectInfo.getOrdBy());
			case SubsetSelect.SQL:
			case SubsetSelect.SQL_FILTER:
				String sql = subsetSelectInfo.getSql();
				if (StrUtil.isNotStrTrimNull(subsetSelectInfo.getFinalFilter()))
				{
					sql += " where " + subsetSelectInfo.getFinalFilter();
				}
				if (StrUtil.isNotStrTrimNull(subsetSelectInfo.getOrdBy()))
				{
					sql += " order by " + subsetSelectInfo.getOrdBy();
				}
				return ObjectProxyFactory.queryObjectsBySql(db, modelCls, sql, paramGetter);
			default:
				return null;
			}
		}
		return null;
	}

	/**
	 * 懒加载数据加载——Object加载
	 *
	 * @param db               DBSession
	 * @param o                model对象
	 * @param subsetTypeInfo   子集类型
	 * @param subsetSelectInfo 子集加载条件
	 * @return 子集数据
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 */
	private Object loadObject(Database db, Object o, SubsetTypeInfo subsetTypeInfo, SubsetSelectInfo subsetSelectInfo) throws IllegalAccessException, IntrospectionException, InvocationTargetException
	{
		Map<String,Object> paramGetter = getLoadOneParamsBySelectInfo(o, subsetSelectInfo);
		switch (subsetTypeInfo.getSubsetType())
		{
		case SubsetType.LIST_MAP:
			switch (subsetSelectInfo.getSelectType())
			{
			case SubsetSelect.SQL:
			case SubsetSelect.SQL_FILTER:
				return db.queryListMap(subsetSelectInfo.getFinalSql(), paramGetter);
			default:
				return null;
			}
		case SubsetType.MAP:
			switch (subsetSelectInfo.getSelectType())
			{
			case SubsetSelect.SQL:
			case SubsetSelect.SQL_FILTER:
				return db.queryMap(subsetSelectInfo.getFinalSql(), paramGetter);
			default:
				return null;
			}
		case SubsetType.OBJECT:
			switch (subsetSelectInfo.getSelectType())
			{
			case SubsetSelect.SQL:
			case SubsetSelect.SQL_FILTER:
				return db.queryObject(subsetSelectInfo.getFinalSql(), paramGetter);
			default:
				return null;
			}
		case SubsetType.OBJECT_ARRAY:
			switch (subsetSelectInfo.getSelectType())
			{
			case SubsetSelect.SQL:
			case SubsetSelect.SQL_FILTER:
				return db.queryObject1Row(subsetSelectInfo.getFinalSql(), paramGetter);
			default:
				return null;
			}
		case SubsetType.OBJECT_ARRAY_2:
			switch (subsetSelectInfo.getSelectType())
			{
			case SubsetSelect.SQL:
			case SubsetSelect.SQL_FILTER:
				return db.queryObject2(subsetSelectInfo.getFinalSql(), paramGetter);
			default:
				return null;
			}
		}
		return null;
	}

	/**
	 * 为model列表加载子集数据
	 *
	 * @param modelList model列表
	 * @param fieldName 字段名
	 */
	@ServiceAutoTrans
	public void loadToModelList(List<? extends ModelBase> modelList, String fieldName)
	{
		if (modelList == null || modelList.size() == 0)
		{
			return;
		}
		Object target = ProxyTargetUtils.getTarget(modelList.get(0));
		Class cls = target.getClass();
		Map<String,ObjectProxyFactory.SubsetInfo> subsetInfoMap = ObjectProxyFactory.getDBModelInfo(cls).getSubsetInfoMap();
		if (subsetInfoMap != null && subsetInfoMap.size() > 0)
		{
			ObjectProxyFactory.SubsetInfo subsetInfo = subsetInfoMap.get(fieldName);
			SubsetTypeInfo subsetTypeInfo = subsetInfo.getSubsetTypeInfo();
			if (subsetTypeInfo == null || subsetTypeInfo.getSubsetType() == null)
			{
				return;
			}
			SubsetSelectInfo subsetSelectInfo = subsetInfo.getSubsetSelectInfo();
			//加载数据
			Database db = TransactionManager.getCurrentDBSession();
			try
			{
				if (subsetTypeInfo.getModelClass() != null)
				{
					Map<String,List<Object>> result = loadModels(db, modelList, subsetTypeInfo, subsetSelectInfo);
					setSubsetToModelList_models(modelList, result, fieldName, subsetTypeInfo, subsetSelectInfo);
				} else
				{
					Map<String,List<Map<String,Object>>> result = loadObjects(db, modelList, subsetTypeInfo, subsetSelectInfo);
					setSubsetToModelList_objects(modelList, result, fieldName, subsetTypeInfo, subsetSelectInfo);
				}
			} catch (IntrospectionException e)
			{
				e.printStackTrace();
			} catch (IllegalAccessException e)
			{
				e.printStackTrace();
			} catch (InvocationTargetException e)
			{
				e.printStackTrace();
			} catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 加载子集Model
	 *
	 * @param modelList        modelList
	 * @param result           结果集
	 * @param fieldName        字段名
	 * @param subsetTypeInfo   子集类型信息
	 * @param subsetSelectInfo 子集加载条件
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private void setSubsetToModelList_models(List<? extends ModelBase> modelList, Map<String,List<Object>> result, String fieldName, SubsetTypeInfo subsetTypeInfo, SubsetSelectInfo subsetSelectInfo)
			throws IntrospectionException, InvocationTargetException, IllegalAccessException
	{
		if (result == null)
		{
			return;
		}
		Collection<String> vs = subsetSelectInfo.getUnionFilterMap().values();
		for (Object o : modelList)
		{
			String key = "";
			for (String v : vs)
			{
				PropertyDescriptor pd = new PropertyDescriptor(v, o.getClass());
				Method method = pd.getReadMethod();
				Object value = method.invoke(o);
				if (key.length() > 0)
				{
					key += "@";
				}
				key += value.toString();
			}
			List<Object> objectList = result.get(key);
			if (objectList == null)
			{
				continue;
			}
			PropertyDescriptor pd = new PropertyDescriptor(fieldName, o.getClass());
			Method method = pd.getWriteMethod();
			switch (subsetTypeInfo.getSubsetType())
			{
			case SubsetType.DW_MODEL:
				method.invoke(o, objectList.get(0));
				break;
			case SubsetType.DW_MODEL_LIST:
				method.invoke(o, objectList);
				break;
			}
		}
	}

	/**
	 * 加载子集Object
	 *
	 * @param modelList        modelList
	 * @param result           结果集
	 * @param fieldName        字段名
	 * @param subsetTypeInfo   子集类型信息
	 * @param subsetSelectInfo 子集加载条件
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private void setSubsetToModelList_objects(List<? extends ModelBase> modelList, Map<String,List<Map<String,Object>>> result, String fieldName, SubsetTypeInfo subsetTypeInfo,
			SubsetSelectInfo subsetSelectInfo) throws IntrospectionException, InvocationTargetException, IllegalAccessException, ParseException
	{
		if (result == null)
		{
			return;
		}
		Collection<String> vs = subsetSelectInfo.getUnionFilterMap().values();
		for (Object o : modelList)
		{
			String key = "";
			for (String v : vs)
			{
				PropertyDescriptor pd = new PropertyDescriptor(v, o.getClass());
				Method method = pd.getReadMethod();
				Object value = method.invoke(o);
				if (key.length() > 0)
				{
					key += "@";
				}
				key += value.toString();
			}
			List<Map<String,Object>> mapList = result.get(key);
			if (mapList == null || mapList.size() == 0)
			{
				continue;
			}
			PropertyDescriptor pd = new PropertyDescriptor(fieldName, o.getClass());
			Method method = pd.getWriteMethod();
			switch (subsetTypeInfo.getSubsetType())
			{
			case SubsetType.OBJECT:
				Object value = TypeUtil.typeConversion(mapListToObjectArray2(mapList)[0][0], subsetTypeInfo.getType());
				method.invoke(o, value);
				break;
			case SubsetType.OBJECT_ARRAY:
				method.invoke(o, mapListToObjectArray2(mapList)[0]);
				break;
			case SubsetType.OBJECT_ARRAY_2:
				method.invoke(o, mapListToObjectArray2(mapList));
				break;
			case SubsetType.LIST_MAP:
				method.invoke(o, mapList);
				break;
			case SubsetType.MAP:
				method.invoke(o, mapList.get(0));
				break;
			}
		}
	}

	/**
	 * 加载子集数据
	 *
	 * @param db               DBSession
	 * @param models           modelList
	 * @param subsetTypeInfo   子集类型信息
	 * @param subsetSelectInfo 子集加载条件
	 * @return 子集数据集
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 */
	private Map<String,List<Object>> loadModels(Database db, List<? extends ModelBase> models, SubsetTypeInfo subsetTypeInfo, SubsetSelectInfo subsetSelectInfo)
			throws IllegalAccessException, IntrospectionException, InvocationTargetException
	{
		Class modelCls = subsetTypeInfo.getModelClass();
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getLoadListParamsBySelectInfo(models, subsetSelectInfo, paramGetter);
		List<Object> resultModels = null;
		switch (subsetTypeInfo.getSubsetType())
		{
		case SubsetType.DW_MODEL:
		case SubsetType.DW_MODEL_LIST:
			switch (subsetSelectInfo.getSelectType())
			{
			case SubsetSelect.FILTER:
				resultModels = ObjectProxyFactory.queryObjects(db, modelCls, filter, paramGetter);
				break;
			case SubsetSelect.SQL:
			case SubsetSelect.SQL_FILTER:
				String sql = subsetSelectInfo.getSql();
				if (StrUtil.isNotStrTrimNull(filter))
				{
					sql += " where " + filter;
				}
				if (StrUtil.isNotStrTrimNull(subsetSelectInfo.getOrdBy()))
				{
					sql += " order by " + subsetSelectInfo.getOrdBy();
				}
				resultModels = ObjectProxyFactory.queryObjectsBySql(db, modelCls, sql, paramGetter);
				break;
			}
		}
		if (resultModels != null && resultModels.size() > 0)
		{
			Map<String,List<Object>> resultListMap = new HashMap<>();
			Set<String> keys = subsetSelectInfo.getUnionFilterMap().keySet();
			for (Object o : resultModels)
			{
				String key = "";
				for (String k : keys)
				{
					PropertyDescriptor pd = new PropertyDescriptor(k, o.getClass());
					Method method = pd.getReadMethod();
					Object value = method.invoke(o);
					if (key.length() > 0)
					{
						key += "@";
					}
					key += value.toString();
				}
				if (resultListMap.get(key) == null)
				{
					resultListMap.put(key, new ArrayList<>());
				}
				resultListMap.get(key).add(o);
			}
			return resultListMap;
		}
		return null;
	}

	/**
	 * 懒加载数据加载——Object加载
	 *
	 * @param db               DBSession
	 * @param models           modelList
	 * @param subsetTypeInfo   子集类型信息
	 * @param subsetSelectInfo 子集加载条件
	 * @return
	 */
	private Map<String,List<Map<String,Object>>> loadObjects(Database db, List<? extends ModelBase> models, SubsetTypeInfo subsetTypeInfo, SubsetSelectInfo subsetSelectInfo)
			throws IllegalAccessException, IntrospectionException, InvocationTargetException
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getLoadListParamsBySelectInfo(models, subsetSelectInfo, paramGetter);
		String sql = subsetSelectInfo.getSql();
		if (StrUtil.isNotStrTrimNull(filter))
		{
			sql += " where " + filter;
		}
		if (StrUtil.isNotStrTrimNull(subsetSelectInfo.getOrdBy()))
		{
			sql += " order by " + subsetSelectInfo.getOrdBy();
		}
		switch (subsetTypeInfo.getSubsetType())
		{
		case SubsetType.LIST_MAP:
		case SubsetType.MAP:
		case SubsetType.OBJECT:
		case SubsetType.OBJECT_ARRAY:
		case SubsetType.OBJECT_ARRAY_2:
			switch (subsetSelectInfo.getSelectType())
			{
			case SubsetSelect.SQL:
			case SubsetSelect.SQL_FILTER:
				Set<String> keyCols = subsetSelectInfo.getUnionFilterMap().keySet();
				return db.queryMapListMap(sql, paramGetter, keyCols.toArray(new String[keyCols.size()]));
			default:
				return null;
			}
		}
		return null;
	}

	/**
	 * 通过对象和子集加载条件生成过滤参数
	 *
	 * @param o                Model对象
	 * @param subsetSelectInfo 子集加载条件
	 * @return 过滤数据集
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private Map<String,Object> getLoadOneParamsBySelectInfo(Object o, SubsetSelectInfo subsetSelectInfo) throws IntrospectionException, InvocationTargetException, IllegalAccessException
	{
		Map<String,Object> paramGetter = new HashMap<>();
		if (subsetSelectInfo.isNeedParams())
		{
			Map<String,String> unionMaps = subsetSelectInfo.getUnionFilterMap();
			Set<String> ks = unionMaps.keySet();
			for (String k : ks)
			{
				String v = unionMaps.get(k);
				PropertyDescriptor pd = new PropertyDescriptor(v, o.getClass());
				Method method = pd.getReadMethod();
				Object value = method.invoke(o);
				paramGetter.put(v, value);
			}
		}
		return paramGetter;
	}

	/**
	 * 通过对象列表获取加载子集列表的过滤参数
	 *
	 * @param objectList       modelList
	 * @param subsetSelectInfo 子集加载条件
	 * @param paramGetter      过滤数据集
	 * @return 过滤条件
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private String getLoadListParamsBySelectInfo(List<? extends ModelBase> objectList, SubsetSelectInfo subsetSelectInfo, Map<String,Object> paramGetter)
			throws IntrospectionException, InvocationTargetException, IllegalAccessException
	{
		String sqlFilter = "";
		if (subsetSelectInfo.isNeedParams())
		{
			Map<String,String> unionMaps = subsetSelectInfo.getUnionFilterMap();
			Set<String> ks = unionMaps.keySet();
			for (String k : ks)
			{
				String v = unionMaps.get(k);
				List<Object> inFilterObjects = new ArrayList<>();
				for (Object o : objectList)
				{
					PropertyDescriptor pd = new PropertyDescriptor(v, o.getClass());
					Method method = pd.getReadMethod();
					Object value = method.invoke(o);
					inFilterObjects.add(value);
				}
				sqlFilter = SqlFilterUtil.addFilter(sqlFilter, "and", SqlFilterUtil.getInFilter(k, inFilterObjects, paramGetter));
			}
			if (StrUtil.isNotStrTrimNull(subsetSelectInfo.getLoadFilter()))
			{
				sqlFilter = SqlFilterUtil.addFilter(sqlFilter, "and", subsetSelectInfo.getLoadFilter());
			}
		}
		return sqlFilter;
	}

	/**
	 * MapList的Value转Object数组
	 *
	 * @param mapList MapList
	 * @return 二维数组
	 */
	private Object[][] mapListToObjectArray2(List<Map<String,Object>> mapList)
	{
		Object[][] result = new Object[mapList.size()][];
		int i = 0;
		for (Map<String,Object> map : mapList)
		{
			Object os[] = new Object[map.size()];
			Iterator<Map.Entry<String,Object>> entries = map.entrySet().iterator();
			int index = 0;
			while (entries.hasNext())
			{
				Map.Entry<String,Object> entry = entries.next();
				os[index] = entry.getValue();
				index++;
			}
			result[i] = os;
			i++;
		}
		return result;
	}
}
