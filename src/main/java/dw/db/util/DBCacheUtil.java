package dw.db.util;

import dw.db.Database;
import dw.db.DatabaseConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

///TODO 更新缓存的操作可以用一个独立的线程去做，不影响请求处理效率
//添加缓存时，要先判断这个表是不是有缓存标志
//！！注意：当该类路径发生变化的时候，要同时修改DB中的程序；
public class DBCacheUtil
{
	/**
	 * 初始化数据库缓存，缓存那些配置有缓存标志的表
	 * Map<String,Map<String,Map<String,Object>>> 的形式
	 * 外层Map的key是表名
	 * 第二层Map的key是主键值
	 * 最里层Map的key是列名
	 * 
	 * 先缓存tbldef和flddef,发sql去查,然后根据查到的内容去处理数据缓存
	 */
	public static void initDBCache(Map<String,Object> eveMap)
	{
	}

	/**
	 * 判断一个表是不是缓存表
	 * @param eveMap
	 * @param tableName
	 * @return
	 */
	public static boolean isCacheTable(Map<String,Object> eveMap, String tableName)
	{
		return false;
	}

	/**
	 * 更新整张表的缓存
	 * @param eveMap
	 * @param tableName
	 */
	public static void updateDBCache(Map<String,Object> eveMap, String tableName)
	{
	}

	/**
	 * 更新单条记录的缓存
	 * 在更新操作事务提交之后去处理
	 * @param eveMap
	 * @param tableName
	 * @param id
	 */
	public static void updateDBCache(Map<String,Object> eveMap, String tableName, String id)
	{
	}

	/**
	 * 获取整张表数据
	 * @param eveMap
	 * @param tableName
	 * @return
	 */
	public static List<Map<String,Object>> getCachedData(Map<String,Object> eveMap, String tableName)
	{
		return null;
	}

	/**
	 * 获取单条记录的缓存
	 * 如果获取失败，则先更新缓存再次获取，再次获取依然失败则说明数据不存在，抛一个RuntimeException
	 * @param eveMap
	 * @param tableName
	 * @param params 包含主键值的参数集合
	 * @return
	 */
	public static Map<String,Object> getCachedData(Map<String,Object> eveMap, String tableName, Map<String,Object> params)
	{
		String sql = "select * from " + tableName + " where ";
		String keyCols[] = DatabaseConstant.getIdNames(tableName);
		//将sql拼装成完整的sql，把id值从params中取出来并拼进去，作为缓存sql使用
		for (int i = 0; i < keyCols.length; i++)
		{
			String key = keyCols[i];
			if (i > 0)
			{
				sql += " and ";
			}
			sql += key + "='" + params.get(key) + "'";
		}
		List<Map<String,Object>> results = getDBCacheBySql(eveMap, tableName, sql);
		if (results == null || results.size() == 0)
		{
			return null;
		}
		return results.get(0);
	}

	/**
	 * 根据表名和sql去获取缓存
	 * 注意：这里的sql不支持参数写法，必须是拼接好的可以直接执行的sql
	 * 如果sql获取不到则执行sql,并缓存结果
	 * @param eveMap
	 * @param tableName
	 * @param sql
	 */
	public static List<Map<String,Object>> getDBCacheBySql(Map<String,Object> eveMap, String tableName, String sql)
	{
//		String sqlCacheKey = getSqlCacheKey(eveMap, tableName, sql);
//		List<Map<String,Object>> cacheData = (List<Map<String,Object>>) CacheUtil.getListMap(sqlCacheKey);
//		//如果缓存中没有取到数据则执行SQL查询，并缓存结果
//		//如果没有缓存则执行查询，如果有缓存则不管缓存的结果是不是有数据都不再查询
//		if (cacheData == null)//|| cacheData.size() == 0)
//		{
//			Database db = (Database) eveMap.get("DB");
//			List<Map<String,Object>> result = db.queryListMap(sql);
//			//CacheUtil.put(sqlCacheKey, result);
//			CacheUtil.putListMap(sqlCacheKey, result);
//			return result;
//		} else
//		{
//			return cacheData;
//		}
		//TODO 使用通用Redis缓存解决方案
		return null;
	}

	/**
	 * 根据表名和SQL获取对应的sql缓存key
	 * 缓存中key的格式：tableName:sql，这个key也是需要管理的，在更新操作时，要清空对应表的SQL缓存
	 * @param tableName
	 * @param sql
	 */
	private static String getSqlCacheKey(Map<String,Object> eveMap, String tableName, String sql)
	{
		//缓存中key的格式：tableName:sql
//		String sqlCacheKey = tableName + ":" + sql;
//		//当前表下的所有sql缓存的key集合
//		List<String> sqlCacheKeys = CacheUtil.getList(DatabaseConstant.tableSqlCacheKey + tableName, String.class);
//		if (sqlCacheKeys == null)
//		{
//			sqlCacheKeys = new ArrayList<>();
//		}
//		if (!sqlCacheKeys.contains(sqlCacheKey))
//		{
//			sqlCacheKeys.add(sqlCacheKey);
//			//覆盖掉已有
//			CacheUtil.putList(DatabaseConstant.tableSqlCacheKey + tableName, sqlCacheKeys);
//		}
//		return sqlCacheKey;
		//TODO 使用通用Redis缓存解决方案
		return  null;
	}

	/**
	 * 清空指定表的所有的sql缓存，在表数据更新后调用
	 * @param tableName
	 */
	public static void emptyTableSqlCache(String tableName)
	{
//		List<String> sqlCacheKeys = CacheUtil.getList(DatabaseConstant.tableSqlCacheKey + tableName, String.class);
//		if (sqlCacheKeys != null)
//		{
//			for (String sqlCacheKey : sqlCacheKeys)
//			{
//				CacheUtil.delete(sqlCacheKey);
//			}
//			CacheUtil.delete(DatabaseConstant.tableSqlCacheKey + tableName);
//		}
		//TODO 使用通用Redis缓存解决方案
	}
}
