package dw.db.util;

import dw.db.Database;
import dw.db.DatabaseConstant;
import dw.db.code.util.MakeCodeUtil;
import dw.db.model.SqlParameter;
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
	* @param rs
	* @param columns
	* @param row
	* @return
	* @throws Exception
	*/
	public static Object[] getData1RowFromResultSet(ResultSet rs, String columns, int row) throws Exception
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
		for (int i = 0; i < column.length; i++)
		{
			result[i] = getObjectFormRS(rs, column[i]);
		}
		rs.beforeFirst();
		return result;
	}

	/**
	* 将指定行指定列数据读入Object[]中
	* @param rs
	* @param row
	* @return
	* @throws Exception
	*/
	public static Object[] getData1RowFormResultSet(ResultSet rs, int row) throws Exception
	{
		return getData1RowFromResultSet(rs, getColumnNames(rs), row);
	}

	/**
	* 获取所有行指定列的全部数据
	* @param rs
	* @param columns
	* @return
	* @throws Exception
	*/
	public static Object[][] getData2FormResultSet(ResultSet rs, String columns) throws Exception
	{
		rs.last();
		int rowCount = rs.getRow();
		rs.beforeFirst();
		Object[][] result = new Object[rowCount][];
		for (int i = 1; i <= rowCount; i++)
		{
			result[i - 1] = getData1RowFromResultSet(rs, columns, i);
		}
		return result;
	}

	/**
	* 获取指定列数据
	* @param rs
	* @param column
	* @return
	* @throws Exception
	*/
	public static Object[] getData1ColFromResultSet(ResultSet rs, String column) throws Exception
	{
		Object[][] objs = getData2FormResultSet(rs, column);
		Object[] result = new Object[objs.length];
		for (int i = 0; i < objs.length; i++)
		{
			result[i] = objs[i][0];
		}
		return result;
	}

	/**
	* 将结果集读入Object数组
	* @param rs
	* @return
	* @throws SQLException
	*/
	public static Object[][] getData2FromResultSet(ResultSet rs) throws SQLException
	{
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		rs.last();
		int rowCount = rs.getRow();
		Object[][] result = new Object[rowCount][columnCount];
		rs.beforeFirst();
		for (int i = 0; i < rowCount; i++)
		{
			rs.next();
			for (int j = 0; j < columnCount; j++)
			{
				result[i][j] = getObjectFormRS(rs, j + 1);
			}
		}
		rs.beforeFirst();
		return result;
	}

	/**
	 * 将结果集读入List<map>中
	 * @param rs
	 * @param columns
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> getMapFormResultSet(ResultSet rs, String columns) throws SQLException
	{
		String column[] = columns.split(",");
		List<Map<String,Object>> result = new ArrayList<>();
		while (rs.next())
		{
			Map<String,Object> recordMap = new HashMap<>();
			for (int i = 0; i < column.length; i++)
			{
				recordMap.put(column[i], getObjectFormRS(rs, column[i]));
			}
			result.add(recordMap);
		}
		rs.beforeFirst();
		return result;
	}

	/**
	 * 未知列名的情况下，将结果集读入List<map>中
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public static List<Map<String,Object>> getMapFromResultSet(ResultSet rs) throws SQLException
	{
		return getMapFormResultSet(rs, getColumnNames(rs));
	}

	/**
	 * 将查询结果封装成以主键值为 KEY 的 Map
	 * @param dataSource
	 * @param tableName
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public static Map<String,Map<String,Object>> getMapMapFromResultSet(DataSource dataSource, String tableName, ResultSet rs) throws SQLException
	{
		Map<String,Map<String,Object>> result = new HashMap<>();
		String[] keys = DatabaseConstant.getIdNames(tableName);
		List<Map<String,Object>> listMap = getMapFromResultSet(rs);
		for (Map<String,Object> map : listMap)
		{
			String idValue = "";
			for (int i = 0; i < keys.length; i++)
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
	 * @return
	 * @throws SQLException 
	 */
	public static String getColumnNames(ResultSet rs) throws SQLException
	{
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		String columnNames = "";
		for (int i = 0; i < columnCount; i++)
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

	// SqlUtil: sql 解析 获取表名、列名、执行类型（更新，删除，查询，新加）、过滤条件、排序（、分组）
	//			将map的数据，组装成SQL语句（insert、update、delete）
	//			根据map拼装过滤条件
	/**
	 * 将ParamSQL解析成可直接执行的组装方式
	 * @param sql
	 * @param params
	 * @return
	 */
	public static SqlParameter parseParamSql(String sql, Map<String,Object> params)
	{
		if (!sqlHasParameter(sql))
		{
			return new SqlParameter(sql, null, params);
		}
		int sqlLen = sql.length();
		boolean isInQuote = false;
		StringBuffer newSql = new StringBuffer();
		List<String> paramNames = new ArrayList<>();
		for (int i = 0; i < sqlLen; i++)
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
						for (i++; i < sqlLen; i++)
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
	 * @param sql
	 * @return
	 */
	private static boolean sqlHasParameter(String sql)
	{
		if (sql == null)
			return false;
		boolean isInQuote = false;
		int lsqLen = sql.length();
		for (int i = 0; i < lsqLen; i++)
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
	 * @param sql
	 * @return
	 */
	public static String getUpdateTableName(String sql)
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
	 * @param dataSource
	 * @param tableName
	 * @param params
	 * @param keyColNames 考虑多主键情况
	 * @return
	 */
	public static String getUpdateSql(DataSource dataSource, String tableName, Map<String,Object> params, String keyColNames[])
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
			for (int i = 0; i < keyColNames.length; i++)
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
	 * @param <T>
	 * @param tableName
	 * @param params
	 * @return
	 */
	public static <T> String getInsertSql(String tableName, Map<String,T> params)
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
	 * @param tableName
	 * @return
	 */
	public static String getDeleteSql(String tableName)
	{
		String idNames[] = DatabaseConstant.getIdNames(tableName);
		String sql = "delete from " + tableName + " where ";
		for (int i = 0; i < idNames.length; i++)
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
	 * 获取查询两个表的cols这些列值不同的记录的sql
	 * :联合查询tbl1和tbl2,联合字段unionCols,查询两个表的cols这些列值不同的记录
	 * :前提是unioncols和cols这些字段在两个表中均存在
	 * @param tbl1
	 * @param tbl2
	 * @param unionCols
	 * @param cols
	 * @return
	 */
	public static String getDiffColsSql(String tbl1, String tbl2, String unionCols[], String cols[])
	{
		StringBuffer sqlBuf = new StringBuffer("select ");
		for (String s : unionCols)
		{
			sqlBuf.append(tbl1 + "." + s + " as " + s + ",");
		}
		for (int i = 0; i < cols.length; i++)
		{
			if (i > 0)
			{
				sqlBuf.append(",");
			}
			//tbl1和tbl2的数据都查出来了，为的是能够根据结果集判断修改了哪一个列属性
			sqlBuf.append(tbl1 + "." + cols[i]);
			sqlBuf.append(",");
			sqlBuf.append(tbl2 + "." + cols[i]);
		}
		sqlBuf.append(" from ");
		sqlBuf.append(tbl1 + " join " + tbl2);
		sqlBuf.append(" on ");
		for (int i = 0; i < unionCols.length; i++)
		{
			if (i > 0)
			{
				sqlBuf.append(" and ");
			}
			sqlBuf.append(tbl1 + "." + unionCols[i] + "=" + tbl2 + "." + unionCols[i]);
		}
		sqlBuf.append(" where ");
		for (int i = 0; i < cols.length; i++)
		{
			if (i > 0)
			{
				sqlBuf.append(" or ");
			}
			sqlBuf.append(tbl1 + "." + cols[i] + " <> " + tbl2 + "." + cols[i]);
		}
		return sqlBuf.toString();
	}

	/**
	 * 在这里进行相关类型的转换，转换成程序方便处理的类型
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
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

	/**
	 * 通过UUID创建32位的数据ID
	 * @return
	 */
	public static String createUUId()
	{
		return UUID.randomUUID().toString().substring(0, 32);
	}

	/**
	 * 创建一个四位的code
	 * @param db
	 * @param tableName
	 * @return
	 */
	public static String createCode(Database db, String tableName)
	{
		Map<String,Object> eveMap = new HashMap<>();
		eveMap.put("DB", db);
		return dw.db.code.util.MakeCodeUtil.getCode(eveMap, tableName, "code", "____");
	}

	/**
	 * 创建制定个数的四位code
	 * @param db
	 * @param tableName
	 * @param count
	 * @return
	 */
	public static String[] createCodes(Database db, String tableName, int count)
	{
		Map<String,Object> eveMap = new HashMap<>();
		eveMap.put("DB", db);
		return MakeCodeUtil.getCodes(eveMap, tableName, "code", "____", count);
	}

	public static void main(String[] args)
	{
		//String sql = "insert into tablename values ('123:a123', :test)";
		//parseParamSql(sql, null);
		//		String sql = "update t1 set v=1";
		//		System.out.println(getUpdateTableName(sql));
		//String tbl1 = "tbldef";
		//String tbl2 = "tbldef_old";
		//String sql = getDiffColsSql(tbl1, tbl2, new String[] { "tblname" }, DatabaseConstant.tbldef_editable_cols);
		//System.out.println(sql);
		//System.out.println(getDeleteSql("flddef"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(dateFormat.format(new Date()));
	}
}
