package dw.db.base;

import dw.common.util.map.MapUtil;
import dw.common.util.str.StrUtil;
import dw.db.annotation.DwDbFld;
import dw.db.annotation.DwDbSub;
import dw.db.annotation.DwDbTbl;
import dw.db.constant.DwDbConstant;
import dw.db.proxy.DwModelProxyFactory;
import dw.db.trans.Database;
import dw.db.util.DBSubsetUtil;
import dw.db.util.DwSpringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
		return queryObjectBySql(db, cls, sql, paramGetter);
	}

	/**
	 * 自定义SQL查询对象
	 *
	 * @param db
	 * @param cls
	 * @param sql
	 * @param paramGetter
	 * @param <T>
	 * @return
	 */
	static <T extends ModelBase> T queryObjectBySql(Database db, Class<T> cls, String sql, Map<String,Object> paramGetter)
	{
		DBModelInfo dbModelInfo = getDBModelInfo(cls);
		Map<String,Object> dataMap = db.queryMap(sql, paramGetter);
		if (dataMap == null)
		{
			return null;
		}
		//T objT = MapUtil.mapToObject(dataMap, cls);
		//此处进行代理
		T objT = MapUtil.mapToObject(dataMap, DwModelProxyFactory.getProxyObject(cls));
		//T objT = DwModelProxyFactory.getProxyObject(MapUtil.mapToObject(dataMap, cls));
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
		//处理非懒加载的子表加载
		DwSubsetLoadService dwSubsetLoadService = (DwSubsetLoadService) DwSpringUtil.getBean("dwSubsetLoadService");
		Map<String,SubsetInfo> subsetInfoMap = dbModelInfo.getSubsetInfoMap();
		Set<String> subsetFieldNames = subsetInfoMap.keySet();
		for (String subsetFieldName : subsetFieldNames)
		{
			DwDbConstant.SubsetSelectInfo subsetSelectInfo = subsetInfoMap.get(subsetFieldName).getSubsetSelectInfo();
			if (!subsetSelectInfo.isLazy())
			{
				dwSubsetLoadService.loadToModel(objT, subsetFieldName);
			}
		}
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
		return queryObjectsBySql(db, cls, sql, paramGetter);
	}

	static <T extends ModelBase> List<T> queryObjectsBySql(Database db, Class<T> cls, String sql, Map<String,Object> paramGetter)
	{
		DBModelInfo dbModelInfo = getDBModelInfo(cls);
		List<Map<String,Object>> dataMapList = db.queryListMap(sql, paramGetter);
		List<T> objList = new ArrayList<>();
		for (Map<String,Object> dataMap : dataMapList)
		{
			//T objT = MapUtil.mapToObject(dataMap, cls);
			T objT = MapUtil.mapToObject(dataMap, DwModelProxyFactory.getProxyObject(cls));
			//T objT = DwModelProxyFactory.getProxyObject(MapUtil.mapToObject(dataMap, cls));
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
		DwSubsetLoadService dwSubsetLoadService = (DwSubsetLoadService) DwSpringUtil.getBean("dwSubsetLoadService");
		Map<String,SubsetInfo> subsetInfoMap = dbModelInfo.getSubsetInfoMap();
		Set<String> subsetFieldNames = subsetInfoMap.keySet();
		for (String subsetFieldName : subsetFieldNames)
		{
			DwDbConstant.SubsetSelectInfo subsetSelectInfo = subsetInfoMap.get(subsetFieldName).getSubsetSelectInfo();
			if (!subsetSelectInfo.isLazy())
			{
				dwSubsetLoadService.loadToModelList(objList, subsetFieldName);
			}
		}
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
		DBModelInfo dbModelInfo = getDBModelInfo(modelBase.getClass());
		Set<String> keyFlds = dbModelInfo.getKeys();
		//是否有数据主键，没有数据主键则抛出错误
		if (keyFlds == null || keyFlds.size() == 0)
		{
			throw new RuntimeException("未检测到数据主键：" + dbModelInfo.getClassPath());
		}
		//检测更新数据中是否包含主键，如果包含主键则抛出错误
		for (String key : keyFlds)
		{
			if (updateDatas.containsKey(key))
			{
				throw new RuntimeException("数据主键被修改，请检查是否有BUG！");
			}
		}
		StringBuffer sql = new StringBuffer();
		List<FldInfo> fldInfoList = dbModelInfo.getFldInfoList();
		Set<String> updateFldNames = updateDatas.keySet();
		sql.append("update " + dbModelInfo.getTblName() + " set ");
		int i = 0;
		for (FldInfo fldInfo : fldInfoList)
		{
			if (fldInfo.isRealField())
			{
				String fldName = fldInfo.getFldName();
				if (updateFldNames.contains(fldName))
				{
					if (i > 0)
					{
						sql.append(",");
					}
					sql.append(fldName + "=:" + fldName);
					i++;
				}
			}
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
			sql.append(key + "=:" + key);
			//取key值，作为paramGetter
			updateDatas.put(key, modelBase.get_oldDataMap_().get(key));
		}
		//执行数据库更新
		db.update1(sql.toString(), updateDatas);
		//更新oldValue
		Set<String> fldNames = dbModelInfo.getFldNames();
		for (String fldName : fldNames)
		{
			modelBase._setOldValue(fldName, updateDatas.get(fldName));
		}
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
		Class<?> cls = modelBase.getClass();
		DBModelInfo dbModelInfo = getDBModelInfo(cls);
		//表名
		String tblName = dbModelInfo.getTblName();
		//该model bean下的所有属性的数据
		Map<String,Object> dataMap = MapUtil.objectToMap(modelBase);
		//要处理的更新字段
		List<FldInfo> fldInfoList = dbModelInfo.getFldInfoList();
		Set<String> fldNames = dbModelInfo.getFldNames();
		//构造插入数据
		Map<String,Object> insertDatas = new HashMap<>();
		for (FldInfo fldInfo : fldInfoList)
		{
			if (fldInfo.isRealField())
			{
				String fldName = fldInfo.getFldName();
				insertDatas.put(fldName, dataMap.get(fldName));
			}
		}
		insertDatas.put("_create_date", modelBase.get_create_date());
		insertDatas.put("_update_date", modelBase.get_update_date());
		insertDatas.put("_delete_flag", modelBase.get_delete_flag());
		db.insert2(tblName, insertDatas);
		//更新oldValue
		for (String fldName : fldNames)
		{
			modelBase._setOldValue(fldName, insertDatas.get(fldName));
		}
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
	private static String getSelectSql(DBModelInfo dbModelInfo)
	{
		String tblName = dbModelInfo.getTblName();
		List<FldInfo> fldInfoList = dbModelInfo.getFldInfoList();
		if (StrUtil.isStrTrimNull(tblName) || fldInfoList == null || fldInfoList.size() == 0)
		{
			throw new RuntimeException("Model Error, tblName or fldName is Null:" + dbModelInfo.getClassPath());
		}
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		int i = 0;
		for (FldInfo fldInfo : fldInfoList)
		{
			if (i > 0)
			{
				sql.append(",");
			}
			//如果别名和原名一致，则不需要再使用别名
			String fldName = fldInfo.getFldName();
			String fldRealName = fldInfo.getRealName();
			if (fldName.equals(fldRealName))
			{
				sql.append(fldName);
			} else
			{
				sql.append(fldRealName + " as " + fldName + " ");
			}
			i++;
		}
		sql.append(" from " + tblName);
		return sql.toString();
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
		DBAnnotationModel annotationModel = getAnnotationModel(cls);
		//DBModelInfo填充
		dbModelInfo = getDbModelInfoByCls(cls);
		dbModelInfos.put(cls.getName(), dbModelInfo);
		return dbModelInfo;
	}

	private static DBModelInfo getDbModelInfoByCls(Class<?> cls)
	{
		DBAnnotationModel dbAnnotationModel = getAnnotationModel(cls);
		DBModelInfo dbModelInfo = new DBModelInfo();
		//classPath;//model class path
		dbModelInfo.setClassPath(cls.getName());
		//tblName;//table name
		DwDbTbl dwDbTbl = dbAnnotationModel.getDwDbTbl();
		dbModelInfo.setTblName(dwDbTbl.tblName());
		//fldNames;//field names
		//keys;//主键
		//fldInfoList;//字段属性信息
		Map<String,DwDbFld> dwDbFldMap = dbAnnotationModel.getDwDbFldMap();
		Set<String> fldNames = dwDbFldMap.keySet();
		Set<String> keys = new HashSet<>();
		List<FldInfo> fldInfoList = new ArrayList<>();
		if (fldNames != null)
		{
			for (String fldName : fldNames)
			{
				DwDbFld dwDbFld = dwDbFldMap.get(fldName);
				FldInfo fldInfo = getFldInfoByDwDbFld(fldName, dwDbFld);
				if (fldInfo.isKey())
				{
					keys.add(fldInfo.getFldName());
				}
				fldInfoList.add(fldInfo);
			}
		}
		//处理默认字段
		dealDefaultFldInfo(fldInfoList);
		dbModelInfo.setFldNames(fldNames);
		dbModelInfo.setKeys(keys);
		dbModelInfo.setFldInfoList(fldInfoList);
		//subInfoList;//级联属性信息
		Map<String,DwDbSub> dwDbSubMap = dbAnnotationModel.getDwDbSubMap();
		Set<String> subNames = dwDbSubMap.keySet();
		Map<String,SubsetInfo> subsetInfoMap = new HashMap<>();
		if (subNames != null)
		{
			for (String subName : subNames)
			{
				DwDbSub dwDbSub = dwDbSubMap.get(subName);
				SubsetInfo subsetInfo = getSubInfoByDwDbSub(cls, subName, dwDbSub);
				subsetInfoMap.put(subName, subsetInfo);
			}
		}
		dbModelInfo.setSubsetInfoMap(subsetInfoMap);
		//selectSql;//select sql
		dbModelInfo.setSelectSql(getSelectSql(dbModelInfo));
		return dbModelInfo;
	}

	/**
	 * 获取DBModel的注解信息
	 *
	 * @param cls
	 * @return
	 */
	private static DBAnnotationModel getAnnotationModel(Class<?> cls)
	{
		DBAnnotationModel annotationModel = new DBAnnotationModel();
		DwDbTbl fwDbTbl = cls.getAnnotation(DwDbTbl.class);
		annotationModel.setDwDbTbl(fwDbTbl);
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields)
		{
			String fldName = field.getName();
			DwDbFld fwDbFld = field.getAnnotation(DwDbFld.class);
			if (fwDbFld != null)
			{
				annotationModel.addDwDbFld(fldName, fwDbFld);
				continue;
			}
			DwDbSub dwDbSub = field.getAnnotation(DwDbSub.class);
			if (dwDbSub != null)
			{
				annotationModel.addDwDbSub(fldName, dwDbSub);
				continue;
			}
		}
		return annotationModel;
	}

	/**
	 * 处理默认字段信息
	 *
	 * @param fldInfos
	 */
	private static void dealDefaultFldInfo(List<FldInfo> fldInfos)
	{
		fldInfos.add(new FldInfo(false, "_update_date", "_update_date"));
		fldInfos.add(new FldInfo(false, "_create_date", "_create_date"));
		fldInfos.add(new FldInfo(false, "_delete_flag", "_delete_flag"));
	}

	/**
	 * 根据字段注解，获取字段信息
	 *
	 * @param fldName
	 * @param dwDbFld
	 * @return
	 */
	private static FldInfo getFldInfoByDwDbFld(String fldName, DwDbFld dwDbFld)
	{
		FldInfo fldInfo = new FldInfo();
		//是否主键
		fldInfo.setKey(dwDbFld.isKey());
		//属性名
		fldInfo.setFldName(fldName);
		//真实字段名
		String realName = dwDbFld.fldName();
		realName = StrUtil.isStrTrimNull(realName) ? fldName : realName;
		fldInfo.setRealName(realName);
		return fldInfo;
	}

	private static SubsetInfo getSubInfoByDwDbSub(Class cls, String fldName, DwDbSub dwDbSub)
	{
		SubsetInfo subInfo = new SubsetInfo();
		subInfo.setFldName(fldName);
		try
		{
			subInfo.setSubsetTypeInfo(DBSubsetUtil.getSubsetTypeInfoByField(cls.getDeclaredField(fldName)));
		} catch (NoSuchFieldException e)
		{
			subInfo.setSubsetTypeInfo(null);
		}
		subInfo.setSubsetSelectInfo(DBSubsetUtil.getSubsetSelectInfoByDwDbSub(dwDbSub));
		return subInfo;
	}

	/**
	 * Model info 封装
	 */
	@Data
	protected static class DBModelInfo
	{
		private String                 classPath;//model class path
		private String                 tblName;//table name
		private String                 selectSql;//select sql
		private Set<String>            fldNames;//field names
		private Set<String>            keys;//主键
		private List<FldInfo>          fldInfoList;//字段属性信息
		private Map<String,SubsetInfo> subsetInfoMap;//级联属性信息
	}
	@Data
	private static class DBAnnotationModel
	{
		Map<String,DwDbFld> dwDbFldMap = new HashMap<>();
		Map<String,DwDbSub> dwDbSubMap = new HashMap<>();
		DwDbTbl             dwDbTbl    = null;

		public void addDwDbFld(String fldName, DwDbFld dwDbFld)
		{
			dwDbFldMap.put(fldName, dwDbFld);
		}

		public void addDwDbSub(String fldName, DwDbSub dwDbSub)
		{
			dwDbSubMap.put(fldName, dwDbSub);
		}
	}
	@Data
	private static class FldInfo
	{
		private boolean isKey;
		private String  fldName;
		private String  realName;

		FldInfo()
		{
		}

		FldInfo(boolean isKey, String fldName, String realName)
		{
			this.isKey = isKey;
			this.fldName = fldName;
			this.realName = realName;
		}

		/**
		 * 是否是真实字段，根据配置的字段内容和字段名是否一致进行判断
		 *
		 * @return
		 */
		boolean isRealField()
		{
			return fldName.equals(realName);
		}
	}
	@Data
	protected static class SubsetInfo
	{
		protected String                        fldName;
		protected DwDbConstant.SubsetTypeInfo   subsetTypeInfo;
		protected DwDbConstant.SubsetSelectInfo subsetSelectInfo;
	}
}
