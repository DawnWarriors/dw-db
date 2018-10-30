package dw.db.trans;

import dw.db.sql.SqlParameter;
import dw.common.util.str.StrUtil;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class DBUtil
{
	/**
	 * 将指定行指定列数据读入Object[]中
	 *
	 * @param rs 结果集
	 * @param columns 列信息
	 * @param row 当前行号
	 * @return 一行数据组成的数组
	 * @throws SQLException 数据库异常
	 */
	static Object[] getData1RowFromResultSet(ResultSet rs, String columns, int row) throws SQLException
	{
		rs.last();
		int rowCount = rs.getRow();
		rs.beforeFirst();
		if (rowCount == 0)
		{
			return new Object[] {};
		}
		rs.absolute(row);
		String[] column = columns.split(",");
		Object[] result = new Object[column.length];
		for (int i = 0 ; i < column.length ; i++)
		{
			result[i] = getObjectFormRS(rs, column[i]);
		}
		rs.beforeFirst();
		return result;
	}

	/**
	 * 将指定行指定列数据读入Object[]中
	 *
	 * @param rs 结果集
	 * @param row 行号
	 * @return 一行数据组成的数组
	 * @throws SQLException 数据库异常
	 */
	static Object[] getData1RowFormResultSet(ResultSet rs, int row) throws SQLException
	{
		return getData1RowFromResultSet(rs, getColumnNames(rs), row);
	}

	/**
	 * 获取所有行指定列的全部数据
	 *
	 * @param rs 结果集
	 * @param columns 列名
	 * @return 结果集二维数组
	 * @throws SQLException 数据库异常
	 */
	static Object[][] getData2FormResultSet(ResultSet rs, String columns) throws SQLException
	{
		rs.last();
		int rowCount = rs.getRow();
		rs.beforeFirst();
		Object[][] result = new Object[rowCount][];
		for (int i = 1 ; i <= rowCount ; i++)
		{
			result[i - 1] = getData1RowFromResultSet(rs, columns, i);
		}
		return result;
	}

	/**
	 * 获取指定列数据
	 *
	 * @param rs 结果集
	 * @param column 列名
	 * @return 一列数据
	 * @throws SQLException 数据库异常
	 */
	static Object[] getData1ColFromResultSet(ResultSet rs, String column) throws SQLException
	{
		Object[][] objs = getData2FormResultSet(rs, column);
		Object[] result = new Object[objs.length];
		for (int i = 0 ; i < objs.length ; i++)
		{
			result[i] = objs[i][0];
		}
		return result;
	}

	/**
	 * 将结果集读入Object数组
	 *
	 * @param rs 结果集
	 * @return 结果集二维数组
	 * @throws SQLException 数据库异常
	 */
	static Object[][] getData2FromResultSet(ResultSet rs) throws SQLException
	{
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		rs.last();
		int rowCount = rs.getRow();
		Object[][] result = new Object[rowCount][columnCount];
		rs.beforeFirst();
		for (int i = 0 ; i < rowCount ; i++)
		{
			rs.next();
			for (int j = 0 ; j < columnCount ; j++)
			{
				result[i][j] = getObjectFormRS(rs, j + 1);
			}
		}
		rs.beforeFirst();
		return result;
	}

	/**
	 * 将结果集读入MapList中
	 *
	 * @param rs 结果集
	 * @param columns 列名信息
	 * @return MapList
	 * @throws SQLException 数据库异常
	 */
	static List<Map<String,Object>> getMapFormResultSet(ResultSet rs, String columns) throws SQLException
	{
		String column[] = columns.split(",");
		List<Map<String,Object>> result = new ArrayList<>();
		while (rs.next())
		{
			Map<String,Object> recordMap = new HashMap<>();
			for (int i = 0 ; i < column.length ; i++)
			{
				recordMap.put(column[i], getObjectFormRS(rs, column[i]));
			}
			result.add(recordMap);
		}
		rs.beforeFirst();
		return result;
	}

	/**
	 * 未知列名的情况下，将结果集读入MapList中
	 *
	 * @param rs 结果集
	 * @return MapList
	 * @throws SQLException 数据库异常
	 */
	static List<Map<String,Object>> getMapFromResultSet(ResultSet rs) throws SQLException
	{
		return getMapFormResultSet(rs, getColumnNames(rs));
	}

	/**
	 * 将查询结果封装成以主键值为 KEY 的 Map
	 *
	 * @param dataSource 数据源
	 * @param tableName 表名
	 * @param rs 结果集
	 * @return 以主键值为 KEY 的 Map
	 * @throws SQLException 数据库异常
	 */
	static Map<String,Map<String,Object>> getMapMapFromResultSet(DataSource dataSource, String tableName, ResultSet rs) throws SQLException
	{
		Map<String,Map<String,Object>> result = new HashMap<>();
		String[] keys = DatabaseConstant.getIdNames(tableName);
		List<Map<String,Object>> listMap = getMapFromResultSet(rs);
		for (Map<String,Object> map : listMap)
		{
			String idValue = "";
			for (int i = 0 ; i < keys.length ; i++)
			{
				if (i > 0)
				{
					idValue += "@";
				}
				idValue += (String) map.get(keys[i]);
			}
			result.put(idValue, map);
		}
		return result;
	}

	/**
	 * 获取rs中所有的列名
	 *
	 * @param rs 结果集
	 * @return 结果集中所有列名，逗号分隔
	 * @throws SQLException 数据库异常
	 */
	static String getColumnNames(ResultSet rs) throws SQLException
	{
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		String columnNames = "";
		for (int i = 0 ; i < columnCount ; i++)
		{
			if (i > 0)
			{
				columnNames += ",";
			}
			//获取列原名
			//columnNames += metaData.getColumnName(i + 1);
			//获取列别名
			columnNames += metaData.getColumnLabel(i + 1);
		}
		return columnNames;
	}

	/**
	 * 将ParamSQL解析成可直接执行的组装方式
	 *
	 * @param sql SQL
	 * @param params 参数Map
	 * @return SQL参数对象
	 */
	static SqlParameter parseParamSql(String sql, Map<String,Object> params)
	{
		if (!sqlHasParameter(sql))
		{
			return new SqlParameter(sql, null, params);
		}
		int sqlLen = sql.length();
		boolean isInQuote = false;
		StringBuffer newSql = new StringBuffer();
		List<String> paramNames = new ArrayList<>();
		for (int i = 0 ; i < sqlLen ; i++)
		{
			char ch = sql.charAt(i);
			if (!isInQuote)
			{
				if (ch == ':')
				{
					if (i < sqlLen - 1 && Character.isJavaIdentifierStart(sql.charAt(i + 1)))
					{
						char chAry[] = new char[128];
						int chAryIndex = 0;
						for (i++; i < sqlLen ; i++)
						{
							ch = sql.charAt(i);
							if (!Character.isJavaIdentifierPart(ch))
							{
								break;
							} else
							{
								chAry[chAryIndex++] = ch;
								ch = ' ';
							}
						}
						newSql.append("?");
						String paramName = new String(chAry, 0, chAryIndex);//.toLowerCase();----取消转小写
						//参数名称转换为小写
						paramNames.add(paramName);
					}
				}
			}
			if (ch == '\'')
			{
				isInQuote = !isInQuote;
			}
			newSql.append(ch);
			ch = ' ';
		}
		sql = newSql.toString();
		switchParamContent(paramNames, params);
		return new SqlParameter(sql, paramNames, params);
	}

	/**
	 * 简单判断是否是参数格式的SQL
	 *
	 * @param sql sql语句
	 * @return 是否是参数格式的SQL
	 */
	private static boolean sqlHasParameter(String sql)
	{
		if (sql == null)
			return false;
		boolean isInQuote = false;
		int lsqLen = sql.length();
		for (int i = 0 ; i < lsqLen ; i++)
		{
			char ch = sql.charAt(i);
			if (ch == '\'')
				isInQuote = !isInQuote;
			else if (!isInQuote && ch == ':')
				return true;
		}
		return false;
	}

	/**
	 * 根据更新语句，返回所更新表的表名(这里更新指的是：增删改)
	 *
	 * @param sql sql语句
	 * @return 表名
	 */
	static String getUpdateTableName(String sql)
	{
		if (sql == null || sql.trim().length() == 0)
		{
			return null;
		}
		String sql_temp = sql.replaceAll("\\s+", " ");
		String sqlParams[] = sql_temp.trim().split(" ");
		String updateType = sqlParams[0].toLowerCase();
		if (sqlParams.length > 1)
		{
			if ("update".equals(updateType))
			{
				return sqlParams[1].toLowerCase();
			} else if ("insert".equals(updateType) || "delete".equals(updateType))
			{
				return sqlParams[2].toLowerCase();
			}
		}
		return null;
	}

	/**
	 * 根据update params获取update SQL语句
	 *
	 * @param dataSource 数据源
	 * @param tableName 表名
	 * @param params 参数Map
	 * @param keyColNames 考虑多主键情况
	 * @return SQL语句
	 */
	static String getUpdateSql(DataSource dataSource, String tableName, Map<String,Object> params, String keyColNames[])
	{
		String sql = "";
		if (params.size() > 1)
		{
			sql += "update " + tableName + " set ";
			Set<String> keySet = params.keySet();
			boolean notFirst = false;
			for (String key : keySet)
			{
				if (!StrUtil.isStrInStrAry(keyColNames, key))
				{
					if (notFirst)
					{
						sql += ",";
					}
					sql += " " + key + "=:" + key + " ";
					notFirst = true;
				}
			}
			sql += " where ";
			for (int i = 0 ; i < keyColNames.length ; i++)
			{
				if (i > 0)
				{
					sql += " and ";
				}
				sql += keyColNames[i] + "=:" + keyColNames[i];
			}
		}
		return sql;
	}

	/**
	 * 根据insert params 获取insert SQL语句
	 *
	 * @param tableName 表名
	 * @param params 参数Map
	 * @param <T> 任意对象
	 * @return SQL语句
	 */
	static <T> String getInsertSql(String tableName, Map<String,T> params)
	{
		String sql = "";
		sql += "insert into " + tableName;
		Set<String> keySet = params.keySet();
		boolean notFirst = false;
		String values = "";
		String columns = "";
		for (String key : keySet)
		{
			if (notFirst)
			{
				columns += ",";
				values += ",";
			}
			columns += " " + key + " ";
			values += " :" + key + " ";
			notFirst = true;
		}
		sql += " ( " + columns + " ) ";
		sql += " values ";
		sql += " ( " + values + " ) ";
		return sql;
	}

	/**
	 * 根据表名获取删除语句
	 * 表数据删除，只能根据主键去删除
	 *
	 * @param tableName 表名
	 * @return SQL语句
	 */
	static String getDeleteSql(String tableName)
	{
		String idNames[] = DatabaseConstant.getIdNames(tableName);
		String sql = "delete from " + tableName + " where ";
		for (int i = 0 ; i < idNames.length ; i++)
		{
			if (i > 0)
			{
				sql += " and ";
			}
			sql += idNames[i] + "=:" + idNames[i];
		}
		return sql;
	}

	/**
	 * 在这里进行相关类型的转换，转换成程序方便处理的类型
	 *
	 * @param rs 结果集
	 * @param column 列对象
	 * @return 列值对象
	 * @throws SQLException 数据库异常
	 */
	private static Object getObjectFormRS(ResultSet rs, Object column) throws SQLException
	{
		Object objValue = null;
		if (column instanceof String)
		{
			objValue = rs.getObject((String) column);
		} else if (column instanceof Integer)
		{
			objValue = rs.getObject((Integer) column);
		} else
		{
			return null;
		}
		Object objValueTurned = objValue;
		//数据库日期类型Timestamp===>java.util.Date
		if (objValue instanceof Timestamp)
		{
			objValueTurned = new Date(((Timestamp) objValue).getTime());
		}
		return objValueTurned;
	}

	/**
	 * 参数Map部分值处理
	 * @param paramNames 参数列名集合
	 * @param params 参数Map
	 */
	private static void switchParamContent(List<String> paramNames, Map<String,Object> params)
	{
		if (paramNames == null || paramNames.size() == 0)
		{
			return;
		}
		for (String paramName : paramNames)
		{
			Object obj = params.get(paramName);
			if (obj == null)
			{
				continue;
			}
			//日期转换，统一格式 yyyy-mm-dd hh:ii:ss
			if (obj instanceof Date)
			{
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				params.put(paramName, dateFormat.format((Date) obj));
			}
		}
	}
}
