package dw.db.base;

import dw.common.util.date.DateUtil;
import dw.common.util.map.MapUtil;
import dw.common.util.str.StrUtil;
import dw.db.trans.Database;
import dw.db.annotation.DwDbFld;
import dw.db.annotation.DwDbTbl;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.*;

class ObjectProxyFactory
{
	private static final Map<String,DBModelInfo> dbModelInfos = new HashMap<>();

	static String getTblName(Class<?> cls)
	{
		DBModelInfo dbModelInfo = getDBModelInfo(cls);
		return dbModelInfo.getTblName();
	}

	/**
	 * 根据Class获取注解配置的数据对象信息
	 *
	 * @param cls
	 * @return
	 */
	static DBModelInfo getDBModelInfo(Class<?> cls)
	{
		DBModelInfo dbModelInfo = dbModelInfos.get(cls.getName());
		if (dbModelInfo != null)
		{
			return dbModelInfo;
		} else
		{
			return putDBModelInfo(cls);
		}
	}

	/**
	 * 查询对象
	 *
	 * @param db
	 * @param cls
	 * @param filter
	 * @param paramGetter
	 * @param <T>
	 * @return
	 */
	static <T extends ModelBase> T queryObject(Database db, Class<T> cls, String filter, Map<String,Object> paramGetter)
	{
		return queryObject(db, cls, filter, paramGetter, false);
	}

	/**
	 * @param db
	 * @param cls
	 * @param filter
	 * @param paramGetter
	 * @param <T>
	 * @return
	 */
	static <T extends ModelBase> T queryObject_updateLock(Database db, Class<T> cls, String filter, Map<String,Object> paramGetter)
	{
		return queryObject(db, cls, filter, paramGetter, true);
	}

	/**
	 * 查询对象
	 *
	 * @param cls
	 * @return
	 */
	static <T extends ModelBase> T queryObject(Database db, Class<T> cls, String filter, Map<String,Object> paramGetter, boolean isLock)
	{
		String before_time_begin = DateUtil.getCurDateTimeCS();
		long before_begin = System.currentTimeMillis();
		DBModelInfo dbModelInfo = getDBModelInfo(cls);
		String sql = dbModelInfo.getSelectSql();
		if (!StrUtil.isStrTrimNull(filter))
		{
			sql += " where " + filter;
		}
		if (isLock)
		{
			sql += " for update";
		}
		String before_time_end = DateUtil.getCurDateTimeCS();
		long before_end = System.currentTimeMillis();
		//Logger.info("对象查询SQL生成:", before_time_begin + "——" + before_time_end + "(ST:" + (before_end - before_begin) + "ms)");
		//TODO 通用日志记录方案
		Map<String,Object> dataMap = db.queryMap(sql, paramGetter);
		if (dataMap == null)
		{
			return null;
		}
		Map<String,String> childTblInfo = dbModelInfo.getChildTblInfos();
		if (childTblInfo != null && childTblInfo.size() > 0)
		{
			Set<String> childFldNames = childTblInfo.keySet();
			for (String childFldName : childFldNames)
			{
				if (childFldName.endsWith("$$FILTER"))
				{
					continue;
				}
				String classPath = childTblInfo.get(childFldName);
				String childFilter = childTblInfo.get(childFldName + "$$FILTER");
				try
				{
					Class<T> childCls = (Class<T>) Class.forName(classPath);
					List<T> childObjs = queryObjects(db, childCls, childFilter, dataMap);
					dataMap.put(childFldName, childObjs);
				} catch (ClassNotFoundException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		String after_time_begin = DateUtil.getCurDateTimeCS();
		long after_begin = System.currentTimeMillis();
		T objT = MapUtil.mapToObject(dataMap, cls);
		objT.set_create_date((Date) dataMap.get("_create_date"));
		objT.set_update_date((Date) dataMap.get("_update_date"));
		objT.set_delete_flag((Integer) dataMap.get("_delete_flag"));
		//设置oldValue
		Set<String> fldNames = dbModelInfo.getFldNames();
		ModelBase modelBase = (ModelBase) objT;
		for (String fldName : fldNames)
		{
			modelBase._setOldValue(fldName, dataMap.get(fldName));
		}
		String after_time_end = DateUtil.getCurDateTimeCS();
		long after_end = System.currentTimeMillis();
		//Logger.info("单对象封装:", after_time_begin + "——" + after_time_end + "(ST:" + (after_begin - after_end) + "ms)");
		//TODO 通用日志记录方案
		return objT;
	}

	/**
	 * 查询多个对象
	 *
	 * @param db
	 * @param cls
	 * @param filter
	 * @param paramGetter
	 * @return
	 */
	static <T extends ModelBase> List<T> queryObjects(Database db, Class<T> cls, String filter, Map<String,Object> paramGetter)
	{
		return queryObjects(db, cls, filter, paramGetter, 0, 0, null);
	}

	/**
	 * 查询多个对象，有排序
	 *
	 * @param db
	 * @param cls
	 * @param filter
	 * @param paramGetter
	 * @param ordBy
	 * @return
	 */
	static <T extends ModelBase> List<T> queryObjects(Database db, Class<T> cls, String filter, Map<String,Object> paramGetter, String ordBy)
	{
		return queryObjects(db, cls, filter, paramGetter, 0, 0, ordBy);
	}

	/**
	 * 根据分页信息获取指定页数据
	 *
	 * @param db
	 * @param cls
	 * @param filter
	 * @param paramGetter
	 * @param pageNum     要取第几页数据
	 * @param pageSize    每页数据条数
	 * @param ordBy       排序字段和方式
	 * @return
	 */
	static <T extends ModelBase> List<T> queryObjects(Database db, Class<T> cls, String filter, Map<String,Object> paramGetter, int pageNum, int pageSize, String ordBy)
	{
		String before_time_begin = DateUtil.getCurDateTimeCS();
		long before_begin = System.currentTimeMillis();
		if (pageNum != 0 && pageSize != 0)
		{
			if (paramGetter == null)
			{
				paramGetter = new HashMap<>();
			}
			paramGetter.put("SQL.PAGE.OFFSET", (pageNum - 1) * pageSize);
			paramGetter.put("SQL.PAGE.SIZE", pageSize);
		}
		DBModelInfo dbModelInfo = getDBModelInfo(cls);
		String sql = dbModelInfo.getSelectSql();
		if (!StrUtil.isStrTrimNull(filter))
		{
			sql += " where " + filter;
		}
		if (!StrUtil.isStrTrimNull(ordBy))
		{
			sql += " order by " + ordBy;
		}
		String before_time_end = DateUtil.getCurDateTimeCS();
		long before_end = System.currentTimeMillis();
		//Logger.info("列表查询SQL生成:", before_time_begin + "——" + before_time_end + "(ST:" + (before_end - before_begin) + "ms)");
		//TODO 通用日志记录方案----用于debug模式下，sql执行效率检测
		List<Map<String,Object>> dataMapList = db.queryListMap(sql, paramGetter);
		Map<String,String> childTblInfo = dbModelInfo.getChildTblInfos();
		if (childTblInfo != null && childTblInfo.size() > 0)
		{
			Set<String> childFldNames = childTblInfo.keySet();
			for (Map<String,Object> dataMap : dataMapList)
			{
				for (String childFldName : childFldNames)
				{
					if (childFldName.endsWith("$$FILTER"))
					{
						continue;
					}
					String classPath = childTblInfo.get(childFldName);
					String childFilter = childTblInfo.get(childFldName + "$$FILTER");
					try
					{
						Class<T> childCls = (Class<T>) Class.forName(classPath);
						List<T> childObjs = queryObjects(db, childCls, childFilter, dataMap);
						dataMap.put(childFldName, childObjs);
					} catch (ClassNotFoundException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		}
		String after_time_begin = DateUtil.getCurDateTimeCS();
		long after_begin = System.currentTimeMillis();
		List<T> objList = new ArrayList<>();
		for (Map<String,Object> dataMap : dataMapList)
		{
			T objT = MapUtil.mapToObject(dataMap, cls);
			objT.set_create_date((Date) dataMap.get("_create_date"));
			objT.set_update_date((Date) dataMap.get("_update_date"));
			//设置oldValue
			Set<String> fldNames = dbModelInfo.getFldNames();
			ModelBase modelBase = (ModelBase) objT;
			for (String fldName : fldNames)
			{
				modelBase._setOldValue(fldName, dataMap.get(fldName));
			}
			objList.add(objT);
		}
		String after_time_end = DateUtil.getCurDateTimeCS();
		long after_end = System.currentTimeMillis();
		//Logger.info("对象列表封装:", after_time_begin + "——" + after_time_end + "(ST:" + (after_end - after_begin) + "ms)");
		//TODO 通用日志记录方案----用于debug模式下，sql执行效率检测
		return objList;
	}

	/**
	 * 根据查询条件获取总共有多少条数据
	 *
	 * @param db
	 * @param cls
	 * @param filter
	 * @param paramGetter
	 * @return
	 */
	static long queryObjectCount(Database db, Class<?> cls, String filter, Map<String,Object> paramGetter)
	{
		DBModelInfo dbModelInfo = getDBModelInfo(cls);
		String sql = dbModelInfo.getSelectSql();
		if (!StrUtil.isStrTrimNull(filter))
		{
			sql += " where " + filter;
		}
		long count = db.getCount(sql, paramGetter);
		return count;
	}

	/**
	 * 根据DBModel进行数据库更新
	 * 注意！！ 不支持子表数据同步更新
	 *
	 * @param db
	 * @param modelBase
	 */
	static void update(Database db, ModelBase modelBase)
	{
		if (modelBase.get_oldDataMap_().size() == 0)
		{
			throw new RuntimeException("数据来源不支持，不可执行更新操作！");
		}
		Map<String,Object> updateDatas = getUpdateDatas(modelBase);
		if (updateDatas == null || updateDatas.size() == 0)
		{
			return;
		}
		String before_time_begin = DateUtil.getCurDateTimeCS();
		long before_begin = System.currentTimeMillis();
		DBModelInfo dbModelInfo = getDBModelInfo(modelBase.getClass());
		Map<String,String> fldNameMap = dbModelInfo.getFldNameInfos();
		Map<String,String> keyInfo = dbModelInfo.getKeys();
		//是否有数据主键，没有数据主键则抛出错误
		if (keyInfo == null || keyInfo.size() == 0)
		{
			throw new RuntimeException("未检测到数据主键：" + dbModelInfo.getClassPath());
		}
		//检测更新数据中是否包含主键，如果包含主键则抛出错误
		Set<String> keyFlds = keyInfo.keySet();
		for (String key : keyFlds)
		{
			if (updateDatas.containsKey(key))
			{
				throw new RuntimeException("数据主键被修改，请检查是否有BUG！");
			}
		}
		StringBuffer sql = new StringBuffer();
		Set<String> updateFldNames = updateDatas.keySet();
		sql.append("update " + dbModelInfo.getTblName() + " set ");
		int i = 0;
		for (String fldName : updateFldNames)
		{
			if (i > 0)
			{
				sql.append(",");
			}
			sql.append(fldNameMap.get(fldName) + "=:" + fldName);
			i++;
		}
		sql.append(" where ");
		//过滤条件
		i = 0;
		for (String key : keyFlds)
		{
			if (i > 0)
			{
				sql.append(" and ");
			}
			sql.append(keyInfo.get(key) + "=:" + key);
			//取key值，作为paramGetter
			updateDatas.put(key, modelBase.get_oldDataMap_().get(key));
		}
		String before_time_end = DateUtil.getCurDateTimeCS();
		long before_end = System.currentTimeMillis();
		//Logger.info("更新SQL生成:", before_time_begin + "——" + before_time_end + "(ST:" + (before_end - before_begin) + "ms)");
		//TODO 通用日志记录方案----用于debug模式下，sql执行效率检测
		//执行数据库更新
		db.update1(sql.toString(), updateDatas);
		//更新oldValue
		String after_time_begin = DateUtil.getCurDateTimeCS();
		long after_begin = System.currentTimeMillis();
		Set<String> fldNames = dbModelInfo.getFldNames();
		for (String fldName : fldNames)
		{
			modelBase._setOldValue(fldName, updateDatas.get(fldName));
		}
		String after_time_end = DateUtil.getCurDateTimeCS();
		long after_end = System.currentTimeMillis();
		//Logger.info("更新后处理:", after_time_begin + "——" + after_time_end + "(ST:" + (after_end - after_begin) + "ms)");
		//TODO 通用日志记录方案----用于debug模式下，sql执行效率检测
	}

	/**
	 * 执行数据库插入
	 * 注意！！ 不支持子表数据同步插入
	 *
	 * @param db
	 * @param modelBase
	 */
	static void insert(Database db, ModelBase modelBase)
	{
		String before_time_begin = DateUtil.getCurDateTimeCS();
		long before_begin = System.currentTimeMillis();
		Class<?> cls = modelBase.getClass();
		DBModelInfo dbModelInfo = getDBModelInfo(cls);
		//表名
		String tblName = dbModelInfo.getTblName();
		//该model bean下的所有属性的数据
		Map<String,Object> dataMap = MapUtil.objectToMap(modelBase);
		//要处理的更新字段
		Map<String,String> fldNameInfos = dbModelInfo.getFldNameInfos();
		Set<String> fldNames = dbModelInfo.getFldNames();
		//构造插入数据
		Map<String,Object> insertDatas = new HashMap<>();
		for (String fldName : fldNames)
		{
			insertDatas.put(fldNameInfos.get(fldName), dataMap.get(fldName));
		}
		insertDatas.put("_create_date", modelBase.get_create_date());
		insertDatas.put("_update_date", modelBase.get_update_date());
		insertDatas.put("_delete_flag", modelBase.get_delete_flag());
		String before_time_end = DateUtil.getCurDateTimeCS();
		long before_end = System.currentTimeMillis();
		//Logger.info("插入SQL生成:", before_time_begin + "——" + before_time_end + "(ST:" + (before_end - before_begin) + "ms)");
		//TODO 通用日志记录方案----用于debug模式下，sql执行效率检测
		db.insert2(tblName, insertDatas);
		String after_time_begin = DateUtil.getCurDateTimeCS();
		long after_begin = System.currentTimeMillis();
		//更新oldValue
		for (String fldName : fldNames)
		{
			modelBase._setOldValue(fldName, insertDatas.get(fldName));
		}
		String after_time_end = DateUtil.getCurDateTimeCS();
		long after_end = System.currentTimeMillis();
		//Logger.info("插入后处理:", after_time_begin + "——" + after_time_end + "(ST:" + (after_end - after_begin) + "ms)");
		//TODO 通用日志记录方案----用于debug模式下，sql执行效率检测
	}

	/**
	 * 根据对象获取要更新的字段集合
	 *
	 * @param modelBase
	 * @return
	 */
	private static Map<String,Object> getUpdateDatas(ModelBase modelBase)
	{
		Map<String,Object> updateDatas = new HashMap<>();
		Class<?> cls = modelBase.getClass();
		Map<String,Object> oldDataMap = modelBase.get_oldDataMap_();
		Field[] flds = cls.getDeclaredFields();
		try
		{
			for (Field fld : flds)
			{
				String fldName = fld.getName();
				if (oldDataMap.containsKey(fldName))
				{
					Object newValue;
					if (fld.isAccessible())
					{
						newValue = fld.get(modelBase);
					} else
					{
						fld.setAccessible(true);
						newValue = fld.get(modelBase);
						fld.setAccessible(false);
					}
					Object oldValue = oldDataMap.get(fldName);
					if (newValue == null)
					{
						if (oldValue != null)
						{
							updateDatas.put(fldName, newValue);
						}
					} else if (!newValue.equals(oldValue))
					{
						updateDatas.put(fldName, newValue);
					}
				}
			}
			updateDatas.put("_update_date", modelBase.get_update_date());
			updateDatas.put("_delete_flag", modelBase.get_delete_flag());
			return updateDatas;
		} catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * 在DBModelInfo中，存入查询sql
	 *
	 * @param dbModelInfo
	 */
	private static void setSelectSql(DBModelInfo dbModelInfo)
	{
		String tblName = dbModelInfo.getTblName();
		Map<String,String> fldNameInfos = dbModelInfo.getFldNameInfos();
		if (StrUtil.isStrTrimNull(tblName) || fldNameInfos == null || fldNameInfos.size() == 0)
		{
			throw new RuntimeException("Model Error, tblName or fldName is Null:" + dbModelInfo.getClassPath());
		}
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		Set<String> keys = fldNameInfos.keySet();
		int i = 0;
		for (String key : keys)
		{
			if (i > 0)
			{
				sql.append(",");
			}
			//如果别名和原名一致，则不需要再使用别名
			String fldRealName = fldNameInfos.get(key);
			if (key.equals(fldRealName))
			{
				sql.append(key);
			} else
			{
				sql.append(fldNameInfos.get(key) + " as " + key + " ");
			}
			i++;
		}
		sql.append(" from " + tblName);
		dbModelInfo.setSelectSql(sql.toString());
	}

	/**
	 * 存入注解信息
	 *
	 * @param cls
	 * @return
	 */
	private synchronized static DBModelInfo putDBModelInfo(Class<?> cls)
	{
		DBModelInfo dbModelInfo = dbModelInfos.get(cls.getName());
		if (dbModelInfo != null)
		{
			return dbModelInfo;
		}
		dbModelInfo = new DBModelInfo();
		DBAnnotationModel annotationModel = getAnnotationModel(cls);
		dbModelInfo.setTblName(getTblName(annotationModel));
		dbModelInfo.setFldNameInfos(getFldNames(annotationModel));
		dbModelInfo.setKeys(getKeyMap(annotationModel));
		dbModelInfo.setChildTblInfos(getChildFldClsInfo(annotationModel));
		dbModelInfo.setClassPath(cls.getName());
		setSelectSql(dbModelInfo);
		dbModelInfos.put(cls.getName(), dbModelInfo);
		return dbModelInfo;
	}

	/**
	 * 获取DBModel的注解信息
	 * @param cls
	 * @return
	 */
	private static DBAnnotationModel getAnnotationModel(Class<?> cls)
	{
		DBAnnotationModel annotationModel = new DBAnnotationModel();
		DwDbTbl fwDbTbl = cls.getAnnotation(DwDbTbl.class);
		annotationModel.setDwDbTbl(fwDbTbl);
		Field[] fileds = cls.getDeclaredFields();
		for (Field field : fileds)
		{
			String fldName = field.getName();
			DwDbFld fwDbFld = field.getAnnotation(DwDbFld.class);
			if (fwDbFld != null)
			{
				annotationModel.addDwDbFld(fldName, fwDbFld);
			}
		}
		return annotationModel;
	}

	/**
	 * 根据注解信息，获取DBModel配置的表名
	 * @param annotationModel
	 * @return
	 */
	private static String getTblName(DBAnnotationModel annotationModel)
	{
		DwDbTbl fwDbTbl = annotationModel.getDwDbTbl();
		return fwDbTbl.tblName();
	}

	/**
	 * 根据注解信息，获取DBModel配置的属性名和字段名的映射Map
	 * @param annotationModel
	 * @return
	 */
	private static Map<String,String> getFldNames(DBAnnotationModel annotationModel)
	{
		Map<String,String> fldNameMap = new HashMap<>();
		DwDbTbl fwDbTbl = annotationModel.getDwDbTbl();
		Map<String,DwDbFld> fwDbFldMap = annotationModel.getDwDbFlds();
		Set<String> keys = fwDbFldMap.keySet();
		for (String key : keys)
		{
			DwDbFld fwDbFld = fwDbFldMap.get(key);
			//未配置子表类路径
			if ("".equals(fwDbFld.childTblClsPath()) && fwDbFld.isFld())
			{
				String fldName = fwDbFld.fldName();
				if ("".equals(fldName))
				{
					fldName = key;
					//如果设置了转换小写
					if (fwDbTbl.isAllFldLowerCase() || fwDbFld.isLowerCase())
					{
						fldName = fldName.toLowerCase();
					}
				}
				fldNameMap.put(key, fldName);
			}
		}
		fldNameMap.put("_update_date", "_update_date");
		fldNameMap.put("_create_date", "_create_date");
		fldNameMap.put("_delete_flag", "_delete_flag");
		return fldNameMap;
	}

	/**
	 * 根据注解信息获取子表类路径信息
	 * @param annotationModel
	 * @return
	 */
	private static Map<String,String> getChildFldClsInfo(DBAnnotationModel annotationModel)
	{
		Map<String,String> fldNameCls = new HashMap<>();
		Map<String,DwDbFld> fwDbFldMap = annotationModel.getDwDbFlds();
		Set<String> keys = fwDbFldMap.keySet();
		for (String key : keys)
		{
			DwDbFld fwDbFld = fwDbFldMap.get(key);
			//配置子表类路径，且配置了子表加载过滤条件
			if (!"".equals(fwDbFld.childTblClsPath()) && !"".equals(fwDbFld.childTblLoadFilter()))
			{
				String clsPath = fwDbFld.childTblClsPath();
				String loadFilter = fwDbFld.childTblLoadFilter();
				fldNameCls.put(key, clsPath);
				fldNameCls.put(key + "$$FILTER", loadFilter);
			}
		}
		return fldNameCls;
	}

	/**
	 * 根据注解信息，获取配置的表的主键
	 * @param annotationModel
	 * @return
	 */
	private static Map<String,String> getKeyMap(DBAnnotationModel annotationModel)
	{
		Map<String,String> keyMap = new HashMap<>();
		Map<String,DwDbFld> fwDbFldMap = annotationModel.getDwDbFlds();
		Set<String> keys = fwDbFldMap.keySet();
		for (String key : keys)
		{
			DwDbFld fwDbFld = fwDbFldMap.get(key);
			//未配置子表类路径
			if (fwDbFld.isKey())
			{
				String fldName = fwDbFld.fldName();
				if ("".equals(fldName))
				{
					fldName = key;
				}
				keyMap.put(key, fldName);
			}
		}
		return keyMap;
	}

	/**
	 * Model info 封装
	 */
	@Data
	private static class DBModelInfo
	{
		private String             classPath;
		private String             tblName;
		private Map<String,String> fldNameInfos;
		private Map<String,String> childTblInfos;
		private Map<String,String> keys;
		private String             selectSql;
		private Set<String>        fldNames;

		public void setFldNameInfos(Map<String,String> fldNameInfos)
		{
			this.fldNames = fldNameInfos.keySet();
			this.fldNameInfos = fldNameInfos;
		}
	}

	@Data
	private static class DBAnnotationModel
	{
		Map<String,DwDbFld> dwDbFldMaps = new HashMap<>();
		DwDbTbl             dwDbTbl     = null;

		public Map<String,DwDbFld> getDwDbFlds()
		{
			return dwDbFldMaps;
		}

		public void addDwDbFld(String fldName, DwDbFld dwDbFld)
		{
			dwDbFldMaps.put(fldName, dwDbFld);
		}
	}
}
