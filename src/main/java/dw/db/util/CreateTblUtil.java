package dw.db.util;

import com.alibaba.druid.util.JdbcConstants;

import java.util.List;

public class CreateTblUtil
{
	/**
	 * 由于不同数据库所支持的数据类型不同，比如小数类型在不同数据库建表时类型关键字就不一样
	 * 所以需要格局数据库类型做一个适配
	 * 最终返回对应的类型信息
	 * @param fldtype
	 * @param fldlen
	 * @param flddecimal
	 * @return
	 */
	public static String getTypeInfo(String dbtype, String fldtype, String fldlen, String flddecimal)
	{
		if (fldtype.equals("number"))
		{
			if (JdbcConstants.ORACLE.equals(dbtype))
			{
				return " " + fldtype + "(" + fldlen + "," + flddecimal + ")";
			} else
			{
				return " float(" + fldlen + "," + flddecimal + ")";
			}
		}
		else if (fldtype.equals("date"))
		{
			return " datetime ";
		} else if (fldtype.equals("varchar"))
		{
			if ("0".equals(fldlen))//最大长度
			{
				if (JdbcConstants.MYSQL.equals(dbtype))
				{
					return " text ";
				} else
				{
					return " " + fldtype + "(max)";
				}
			}
		}
		if(!"0".equals(flddecimal))
		{
			return " " + fldtype + "(" + fldlen + "," + flddecimal + ")";
		}else{
			return " " + fldtype + "(" + fldlen + ")";
		}
	}

	/**
	 * 主要用于处理特殊值，比如null和0
	 * @param defaultVal
	 * @return
	 */
	public static String getDefaultInfo(String dbtype, String defaultVal)
	{
		if (defaultVal == null || defaultVal.equals("") || defaultVal.toLowerCase().equals("null"))
		{
			return "";
		}
		if (dbtype.equals(JdbcConstants.MYSQL))
		{
			return " default " + defaultVal + "";
		}
		return " default(" + defaultVal + ")";
	}

	/**
	 * 根据列限制属性，判断是否非空
	 * @param fldattr
	 * @return
	 */
	public static String getNotNullInfo(String dbtype, String fldattr)
	{
		if (fldattr.charAt(5) == '1')
		{
			return " not null";
		}
		return null;
	}

	/**
	 * 根据列限制属性，判断是否是主键
	 * @param fldattr
	 * @return
	 */
	public static boolean isPrimaryKey(String fldattr)
	{
		if (fldattr.charAt(4) == '1')
		{
			return true;
		}
		return false;
	}

	/**
	 * 根据列限制属性，判断是否唯一
	 * @param dbtype
	 * @param fldattr
	 * @return
	 */
	public static String getUniqueInfo(String dbtype, String fldattr)
	{
		if (fldattr.charAt(3) == '1')
		{
			return " unique";
		}
		return null;
	}

	/**
	 * 获取设置主键语句
	 * @return
	 */
	public static String getPrimaryKeyExpr(String dbtype, String tblName, List<String> keyCols)
	{
		if (keyCols == null || keyCols.size() == 0)
		{
			throw new RuntimeException("表" + tblName + "未设置主键!");
		}
		String expr = "primary key(";
		for (int i = 0; i < keyCols.size(); i++)
		{
			if (i > 0)
			{
				expr += ",";
			}
			expr += keyCols.get(i);
		}
		expr += ")";
		return expr;
	}

	public static String getUpdateColExpr(String dbType, String tblname, StringBuffer newFldSqlBuf)
	{
		StringBuffer updateFldSqlBuf = new StringBuffer();
		updateFldSqlBuf.append("alter table " + tblname);
		if (JdbcConstants.MYSQL.equals(dbType) || JdbcConstants.ORACLE.equals(dbType))
		{
			updateFldSqlBuf.append(" modify ");
			updateFldSqlBuf.append(newFldSqlBuf);
		} else if (JdbcConstants.MYSQL.equals(dbType))
		{
			updateFldSqlBuf.append(" alter column ");
			updateFldSqlBuf.append(newFldSqlBuf);
		}
		return updateFldSqlBuf.toString();
	}
}
