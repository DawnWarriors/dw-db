package dw.db;

import com.alibaba.druid.sql.PagerUtils;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import dw.db.config.DBConfig;
import dw.db.model.SqlParameter;
import dw.db.util.DBUtil;
import dw.common.util.date.DateUtil;
import dw.common.util.num.IntUtil;
import dw.common.util.str.StrUtil;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 曹燕飞
 */
//TODO 指定列名的update操作
//查询分页：只需在参数paramGetter中设置以下参数即可：
//		SQL.PAGE.OFFSET:偏移量
//		SQL.PAGE.SIZE:每页大小
//TODO 查询以及更新的缓存操作
public class Database
{
	Connection	con			= null;
	DataSource	dataSource	= null;
	String		dbType		= null;

	/**
	 * 通过DatabaseManager获取实例
	 */
	protected Database()
	{
	}

	protected void init(Connection con, DataSource dataSource, String dbType)
	{
		this.con = con;
		this.dataSource = dataSource;
		this.dbType = dbType;
	}

	/**
	 * 获取数据库类型
	 * @return
	 */
	public String getDbType()
	{
		return this.dbType;
	}

	/**
	 * 查询结果放入Map<String,List<Map<String,Object>>>
	 * 外层Map的key是指定列的值，如果有多个列，则列值以“@”连接
	 * @param sql
	 * @param paramGetter
	 * @param keyCols
	 * @return
	 */
	public Map<String,List<Map<String,Object>>> queryMapListMap(String sql, Map<String,Object> paramGetter, String keyCols[])
	{
		List<Map<String,Object>> listMap = queryListMap(sql, paramGetter);
		Map<String,List<Map<String,Object>>> mapListMap = new HashMap<>();
		for (Map<String,Object> map : listMap)
		{
			String key = "";
			for (int i = 0; i < keyCols.length; i++)
			{
				if (i > 0)
				{
					key += "@";
				}
				key += map.get(keyCols[i]);
			}
			if (mapListMap.get(key) == null)
			{
				mapListMap.put(key, new ArrayList<Map<String,Object>>());
			}
			mapListMap.get(key).add(map);
		}
		return mapListMap;
	}

	/**
	 * 查询结果放入List<Map>
	 * @param sql
	 * @param paramGetter
	 * @return
	 */
	public List<Map<String,Object>> queryListMap(String sql, Map<String,Object> paramGetter)
	{
		try
		{
			SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
			ResultSet resultSet = executeSelect(sqlParameter);
			return DBUtil.getMapFromResultSet(resultSet);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param sql
	 * @return
	 */
	public List<Map<String,Object>> queryListMap(String sql)
	{
		return queryListMap(sql, null);
	}

	/**
	 * 以主键值为key，将结果集放入map
	 * @param sql
	 * @param paramGetter
	 * @return
	 */
	public Map<String,Map<String,Object>> queryMapMap(String tableName, String sql, Map<String,Object> paramGetter)
	{
		try
		{
			SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
			ResultSet resultSet = executeSelect(sqlParameter);
			return DBUtil.getMapMapFromResultSet(dataSource, tableName, resultSet);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param tableName
	 * @param sql
	 * @return
	 */
	public Map<String,Map<String,Object>> queryMapMap(String tableName, String sql)
	{
		return queryMapMap(tableName, sql, null);
	}

	/**
	 * 查询结果放入Map
	 * @param sql
	 * @param paramGetter
	 * @return
	 */
	public Map<String,Object> queryMap(String sql, Map<String,Object> paramGetter)
	{
		try
		{
			SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
			ResultSet resultSet = executeSelect(sqlParameter);
			List<Map<String,Object>> mapList = DBUtil.getMapFromResultSet(resultSet);
			if (mapList != null && mapList.size() != 0)
			{
				return mapList.get(0);
			}
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return null;
	}

	/**
	 * @param sql
	 * @return
	 */
	public Map<String,Object> queryMap(String sql)
	{
		return queryMap(sql, null);
	}

	/**
	 * 查询结果放入Object二维数组
	 * @param sql
	 * @param paramGetter
	 * @return
	 */
	public Object[][] queryObject2(String sql, Map<String,Object> paramGetter)
	{
		try
		{
			SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
			ResultSet resultSet = executeSelect(sqlParameter);
			return DBUtil.getData2FromResultSet(resultSet);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param sql
	 * @return
	 */
	public Object[][] queryObject2(String sql)
	{
		return queryObject2(sql, null);
	}

	/**
	 * 查询结果放入Object一维数组
	 * @param sql
	 * @param paramGetter
	 * @return
	 */
	public Object[] queryOjbect1Row(String sql, Map<String,Object> paramGetter)
	{
		try
		{
			SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
			ResultSet resultSet = executeSelect(sqlParameter);
			return DBUtil.getData1RowFormResultSet(resultSet, 1);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param sql
	 * @return
	 */
	public Object[] queryObject1Row(String sql)
	{
		return queryOjbect1Row(sql, null);
	}

	/**
	 * 获取指定列数据
	 * @param sql
	 * @param colName
	 * @param paramGetter
	 * @return
	 */
	public Object[] queryObject1Col(String sql, String colName, Map<String,Object> paramGetter)
	{
		try
		{
			SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
			ResultSet resultSet = executeSelect(sqlParameter);
			return DBUtil.getData1ColFromResultSet(resultSet, colName);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取指定列数据
	 * @param sql
	 * @param colName
	 * @return
	 */
	public Object[] queryObject1Col(String sql, String colName)
	{
		return queryObject1Col(sql, colName, null);
	}

	/**
	 * 查询结果为一个Object
	 * @param sql
	 * @param paramGetter
	 * @return
	 */
	public Object queryObject(String sql, Map<String,Object> paramGetter)
	{
		Object[] objs = queryOjbect1Row(sql, paramGetter);
		if (objs == null || objs.length == 0)
		{
			return null;
		}
		return objs[0];
	}

	/**
	 * @param sql
	 * @return
	 */
	public Object queryObject(String sql)
	{
		return queryObject(sql, null);
	}

	/**
	 * @param sql
	 * @param paramGetter
	 * @return
	 * @throws Exception 
	 */
	public int update1(String sql, Map<String,Object> paramGetter)
	{
		try
		{
			SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
			return executeUpdate(sqlParameter);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * 使用默认主键名更新表
	 * @param tableName
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public int update2(String tableName, Map<String,Object> params)
	{
		try
		{
			String sql = DBUtil.getUpdateSql(dataSource, tableName, params, DatabaseConstant.getIdNames(tableName));
			SqlParameter sqlParameter = DBUtil.parseParamSql(sql, params);
			return executeUpdate(sqlParameter);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param sql
	 * @param paramGetter
	 * @throws Exception
	 */
	public void insert1(String sql, Map<String,Object> paramGetter) throws Exception
	{
		SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
		executeOther(sqlParameter);
	}

	/**
	 * @param <T>
	 * @param tableName
	 * @param params
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public <T> void insert2(String tableName, Map<String,T> params)
	{
		String sql = DBUtil.getInsertSql(tableName, params);
		SqlParameter sqlParameter = DBUtil.parseParamSql(sql, (Map<String,Object>) params);
		executeOther(sqlParameter);
	}

	/**
	 * 执行删除
	 * @param tableName
	 * @param params
	 * @throws Exception
	 */
	public void delete(String tableName, Map<String,Object> params)
	{
		String sql = DBUtil.getDeleteSql(tableName);
		SqlParameter sqlParameter = DBUtil.parseParamSql(sql, params);
		executeOther(sqlParameter);
	}

	/**
	 * @param sql
	 * @param paramGetter
	 * @throws Exception
	 */
	public void execute(String sql, Map<String,Object> paramGetter)
	{
		SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
		executeOther(sqlParameter);
	}

	/**
	 * @param sql
	 * @throws Exception 
	 */
	public void execute(String sql)
	{
		execute(sql, null);
	}

	/**
	 * 获取查询数据总数
	 * @param sql
	 * @param paramGetter
	 * @return
	 * @throws Exception
	 */
	public long getCount(String sql, Map<String,Object> paramGetter)
	{
		String countSql = PagerUtils.count(sql, dbType);
		Object countObj = queryObject(countSql, paramGetter);
		if (countObj == null)
		{
			return 0;
		}
		return (Long) countObj;
	}

	/**
	 * 获取查询数据总数
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public long getCount(String sql)
	{
		return getCount(sql, null);
	}

	/*********************************** 事务处理 ***********************************/
	/**
	 * 事务开始
	 */
	public void beginTrans()
	{
		try
		{
			this.con.setAutoCommit(false);
		} catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * 事务提交
	 */
	public void commitTrans(boolean isNeedRollback)
	{
		try
		{
			if (isNeedRollback)
			{
				con.rollback();
			} else
			{
				con.commit();
			}
		} catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	/********************************* 私有方法 ***********************************/
	/**
	 * 查询执行，返回查询结果集
	 * @param parameter
	 * @return
	 * @throws Exception 
	 */
	private ResultSet executeSelect(SqlParameter parameter) throws Exception
	{
		Object obj = exceuteCommon(parameter, 1);
		if (obj == null)
		{
			return null;
		}
		return (ResultSet) obj;
	}

	/**
	 * 更新执行，返回影响行数
	 * @param parameter
	 * @return
	 * @throws Exception 
	 */
	private int executeUpdate(SqlParameter parameter) throws Exception
	{
		Object obj = exceuteCommon(parameter, 2);
		if (obj == null)
		{
			return 0;
		}
		return (int) obj;
	}

	/**
	 * 删除或插入
	 * @param parameter
	 * @return
	 * @throws Exception 
	 */
	private boolean executeOther(SqlParameter parameter)
	{
		Object obj = exceuteCommon(parameter, 3);
		if (obj == null)
		{
			return false;
		}
		return (boolean) obj;
	}

	/**
	 * SQL执行通用
	 * @param parameter
	 * @param option 1：查询	2：更新	3：其他
	 * @return
	 * @throws Exception 
	 */
	private Object exceuteCommon(SqlParameter parameter, int option)
	{
		try
		{

			List<String> keys = parameter.getParamNames();
			Map<String,Object> paramGetter = parameter.getParams();
			String sql = parameter.getSql();
			//查询时分页处理，SQL添加分页内容
			if (option == 1 && paramGetter != null && paramGetter.size() > 0)
			{
				Object offset = paramGetter.get("SQL.PAGE.OFFSET");
				Object count = paramGetter.get("SQL.PAGE.SIZE");
				if (count != null)
				{
					if (offset == null)
					{
						offset = 0;
					}
					sql = PagerUtils.limit(sql, dbType, IntUtil.objToInt(offset), IntUtil.objToInt(count));
				}
			}
			String sql_begin_time = DateUtil.getCurDateTimeCS();
			long sql_begin = System.currentTimeMillis();
			//System.out.println(DateUtil.getCurDateTimeStr() + ":" + sql);
			PreparedStatement preparedStatement = con.prepareStatement(sql);
			if (keys != null && paramGetter != null)
			{
				for (int i = 0, len = keys.size(); i < len; i++)
				{
					String key = keys.get(i);
					preparedStatement.setObject(i + 1, paramGetter.get(key));
				}
			}
			sql = sql.replaceAll("\n", " ");
			sql = sql.replaceAll("\\s{2,}", " ");
			Object result = null;
			// 查询
			if (option == 1)
			{
				result = preparedStatement.executeQuery();
				//con.close();
			}
			// 更新
			else if (option == 2)
			{
				result = preparedStatement.executeUpdate();
			}
			// 其他
			else
			{
				result = preparedStatement.execute();
			}
			String sql_end_time = DateUtil.getCurDateTimeCS();
			long sql_end = System.currentTimeMillis();
			//Logger.info("SQL:",sql);
			//Logger.info("SQL执行:",sql_begin_time+"——"+sql_end_time+"(ST:"+(sql_end-sql_begin)+"ms)");
			//TODO 通用日志记录方案----用于debug模式下，sql执行效率检测
			if (DBConfig.isUseCache())
			{

				//清除缓存
				String tableName = DBUtil.getUpdateTableName(sql);
				if (!StrUtil.isStrTrimNull(tableName))
				{
					String cache_begin_time = DateUtil.getCurDateTimeCS();
					long cache_begin = System.currentTimeMillis();
					//DBCacheUtil.emptyTableSqlCache(tableName);
					//使用反射调用
					String className = "fm.db.util.DBCacheUtil";
					String methodName = "emptyTableSqlCache";
					Class<?> cls = Class.forName(className);
					Method method = cls.getMethod(methodName, new Class[] { String.class });
					method.invoke(cls, tableName);
					String cache_end_time = DateUtil.getCurDateTimeCS();
					long cache_end = System.currentTimeMillis();
					//Logger.info("表更新时缓存处理:",cache_begin_time+"——"+cache_end_time+"(ST:"+(cache_end-cache_begin)+"ms)");
				}
			}
			return result;
		} catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | SecurityException e)
		{
			//链接失效，重新获取连接
			if (e instanceof CommunicationsException)
			{
				try
				{
					if (this.con != null && !this.con.isClosed())
					{
						this.con.close();
					}
					//获得新链接
					this.con = this.dataSource.getConnection();
					//重新执行并返回结果
					return exceuteCommon(parameter, option);
				} catch (SQLException e1)
				{
					throw new RuntimeException(e);
				}
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * 连接是否已关闭
	 * @return
	 * @throws Exception
	 */
	public boolean isClosed() throws Exception
	{
		return con.isClosed();
	}

	/**
	 * 关闭连接
	 * @throws Exception
	 */
	public void close() throws Exception
	{
		if (!isClosed())
		{
			con.close();
		}
	}
}
