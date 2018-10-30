package dw.db.trans;

import com.alibaba.druid.sql.PagerUtils;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import dw.db.sql.SqlParameter;
import dw.common.util.date.DateUtil;
import dw.common.util.num.IntUtil;

import javax.sql.DataSource;
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
//查询分页：只需在参数paramGetter中设置以下参数即可：
//		SQL.PAGE.OFFSET:偏移量
//		SQL.PAGE.SIZE:每页大小
//TODO 查询以及更新的缓存操作
public class Database
{
	Connection con        = null;
	DataSource dataSource = null;
	String     dbType     = null;

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
	 * 获取数据库类型，参考：com.alibaba.druid.util.JdbcConstants
	 *
	 * @return 数据库类型
	 */
	public String getDbType()
	{
		return this.dbType;
	}

	/**
	 * 查询结果放入Map&lt;String,List&lt;Map&lt;String,Object&gt;&gt;&gt;
	 * 外层Map的key是指定列的值，如果有多个列，则列值以“@”连接
	 *
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 * @param keyCols     用来作为结果集分组的列名数组
	 * @return 返回分组结果集
	 */
	public Map<String,List<Map<String,Object>>> queryMapListMap(String sql, Map<String,Object> paramGetter, String keyCols[])
	{
		List<Map<String,Object>> listMap = queryListMap(sql, paramGetter);
		Map<String,List<Map<String,Object>>> mapListMap = new HashMap<>();
		for (Map<String,Object> map : listMap)
		{
			String key = "";
			for (int i = 0 ; i < keyCols.length ; i++)
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
	 * 查询结果放入MapList
	 *
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 * @return 查询结果列表
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
	 * 查询结果放入MapList
	 *
	 * @param sql 查询SQL，调用者拼装的SQL语句
	 * @return 查询结果列表
	 */
	public List<Map<String,Object>> queryListMap(String sql)
	{
		return queryListMap(sql, null);
	}

	/**
	 * 以主键值为key，将结果集放入map
	 *
	 * @param tableName   表名 数据表名
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 * @return 以主键值为key的结果集合
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
	 * 以主键值为key，将结果集放入map
	 *
	 * @param tableName 表名
	 * @param sql       查询SQL
	 * @return 以主键值为key的结果集合
	 */
	public Map<String,Map<String,Object>> queryMapMap(String tableName, String sql)
	{
		return queryMapMap(tableName, sql, null);
	}

	/**
	 * 查询结果放入Map
	 *
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 * @return 结果Map
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
	 * 查询结果放入Map
	 *
	 * @param sql 查询SQL
	 * @return 结果Map
	 */
	public Map<String,Object> queryMap(String sql)
	{
		return queryMap(sql, null);
	}

	/**
	 * 查询结果放入Object二维数组
	 *
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 * @return 结果数组
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
	 * 查询结果放入Object二维数组
	 *
	 * @param sql 查询SQL
	 * @return 结果数组
	 */
	public Object[][] queryObject2(String sql)
	{
		return queryObject2(sql, null);
	}

	/**
	 * 查一行，查询结果放入Object一维数组
	 *
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 * @return 结果数组
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
	 * 查一行，查询结果放入Object一维数组
	 *
	 * @param sql 查询SQL
	 * @return 结果数组
	 */
	public Object[] queryObject1Row(String sql)
	{
		return queryOjbect1Row(sql, null);
	}

	/**
	 * 查一列，获取指定列数据
	 *
	 * @param sql         查询SQL
	 * @param colName     列名
	 * @param paramGetter 查询参数封装Map
	 * @return 结果数组
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
	 * 查一列，获取指定列数据
	 *
	 * @param sql     查询SQL
	 * @param colName 列名
	 * @return 结果数组
	 */
	public Object[] queryObject1Col(String sql, String colName)
	{
		return queryObject1Col(sql, colName, null);
	}

	/**
	 * 查询结果为一个Object
	 *
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 * @return 查询结果
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
	 * 查询结果为一个Object
	 *
	 * @param sql 查询SQL
	 * @return 查询结果
	 */
	public Object queryObject(String sql)
	{
		return queryObject(sql, null);
	}

	/**
	 * @param sql         SQL语句
	 * @param paramGetter 查询参数封装Map
	 * @return 更新影响行数
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
	 *
	 * @param tableName   表名
	 * @param paramGetter 查询参数封装Map
	 * @return 更新影响行数
	 */
	public int update2(String tableName, Map<String,Object> paramGetter)
	{
		String sql = DBUtil.getUpdateSql(dataSource, tableName, paramGetter, DatabaseConstant.getIdNames(tableName));
		SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
		return executeUpdate(sqlParameter);
	}

	/**
	 * 执行数据插入
	 *
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 */
	public void insert1(String sql, Map<String,Object> paramGetter)
	{
		SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
		executeOther(sqlParameter);
	}

	/**
	 * 执行数据插入
	 *
	 * @param tableName 表名
	 * @param params    需要更新的列名与值对应的Map集合
	 * @param <T> 列值参数对象
	 */
	public <T> void insert2(String tableName, Map<String,T> params)
	{
		String sql = DBUtil.getInsertSql(tableName, params);
		SqlParameter sqlParameter = DBUtil.parseParamSql(sql, (Map<String,Object>) params);
		executeOther(sqlParameter);
	}

	/**
	 * 执行删除
	 *
	 * @param tableName 表名
	 * @param params    删除数据参数设置
	 */
	public void delete(String tableName, Map<String,Object> params)
	{
		String sql = DBUtil.getDeleteSql(tableName);
		SqlParameter sqlParameter = DBUtil.parseParamSql(sql, params);
		executeOther(sqlParameter);
	}

	/**
	 * SQL执行
	 *
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 */
	public void execute(String sql, Map<String,Object> paramGetter)
	{
		SqlParameter sqlParameter = DBUtil.parseParamSql(sql, paramGetter);
		executeOther(sqlParameter);
	}

	/**
	 * SQL执行
	 *
	 * @param sql 查询SQL
	 */
	public void execute(String sql)
	{
		execute(sql, null);
	}

	/**
	 * 获取查询数据总数
	 *
	 * @param sql         查询SQL
	 * @param paramGetter 查询参数封装Map
	 * @return 数据条数
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
	 *
	 * @param sql 查询SQL
	 * @return 数据条数
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
	 *
	 * @param isNeedRollback 是否回滚事务
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
	 *
	 * @param parameter SQL参数对象
	 * @return 查询结果集
	 */
	private ResultSet executeSelect(SqlParameter parameter)
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
	 *
	 * @param parameter SQL参数对象
	 * @return 更新影响行数
	 */
	private int executeUpdate(SqlParameter parameter)
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
	 *
	 * @param parameter SQL参数对象
	 * @return 执行是否成功标志
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
	 *
	 * @param parameter SQLParameter对象
	 * @param option    1：查询	2：更新	3：其他
	 * @return 执行结果
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
				for (int i = 0, len = keys.size() ; i < len ; i++)
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
			return result;
		} catch (SQLException | IllegalArgumentException | SecurityException e)
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
	 *
	 * @return 数据库关闭状态
	 * @throws SQLException 数据库异常
	 */
	public boolean isClosed() throws SQLException
	{
		return con.isClosed();
	}

	/**
	 * 关闭连接
	 *
	 * @throws SQLException 数据库异常
	 */
	public void close() throws SQLException
	{
		if (!isClosed())
		{
			con.close();
		}
	}
}
