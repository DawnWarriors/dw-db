package dw.db.sql;

import dw.common.util.str.StrUtil;

import java.util.List;
import java.util.Map;

/**
 * 常用SQL过滤条件拼装工具
 */
public class SqlFilterUtil
{
	/**
	 * 根据字段名和值的集合，获取in过滤条件
	 *
	 * @param fldName 字段名
	 * @param objs    字段值数组
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getInFilter(String fldName, T[] objs, Map<String,Object> params)
	{
		return getInFilter(fldName, null, objs, params);
	}

	/**
	 * 根据字段名和值的集合，获取in过滤条件
	 *
	 * @param fldName   列名
	 * @param aliasName 列别名
	 * @param objs      字段值数组
	 * @param params    查询条件参数Map集合
	 * @param <T>       任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getInFilter(String fldName, String aliasName, T[] objs, Map<String,Object> params)
	{
		return getInFilter(fldName, aliasName, objs, params, false);
	}

	/**
	 * 根据字段名和值的集合，获取in过滤条件
	 *
	 * @param fldName   列名
	 * @param aliasName 列别名
	 * @param valList   字段值列表
	 * @param params    查询条件参数Map集合
	 * @param <T>       任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getInFilter(String fldName, String aliasName, List<T> valList, Map<String,Object> params)
	{
		return getInFilter(fldName, aliasName, valList, params, false);
	}

	/**
	 * 根据字段名和值的集合，获取in过滤条件
	 *
	 * @param fldName 列名
	 * @param valList 字段值列表
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getInFilter(String fldName, List<T> valList, Map<String,Object> params)
	{
		return getInFilter(fldName, null, valList, params);
	}

	/**
	 * 根据字段名和值的集合，获取in过滤条件
	 *
	 * @param fldName   列名
	 * @param aliasName 列别名
	 * @param valList   字符串类型，逗号分隔值集合
	 * @param params    查询条件参数Map集合
	 * @return 返回过滤条件
	 */
	public static String getInFilter(String fldName, String aliasName, String valList, Map<String,Object> params)
	{
		if (StrUtil.isStrTrimNull(valList))
		{
			return " 1=2 ";
		}
		return getInFilter(fldName, aliasName, valList.split(","), params);
	}

	/**
	 * 根据字段名和值的集合，获取in过滤条件
	 *
	 * @param fldName 列名
	 * @param valList 字符串类型，逗号分隔值集合
	 * @param params  查询条件参数Map集合
	 * @return 返回过滤条件
	 */
	public static String getInFilter(String fldName, String valList, Map<String,Object> params)
	{
		if (StrUtil.isStrTrimNull(valList))
		{
			return " 1=2 ";
		}
		return getInFilter(fldName, valList.split(","), params);
	}

	/**
	 * 添加过滤条件
	 *
	 * @param oldFilter 需扩展的Filter
	 * @param linkStr   or,and
	 * @param filter    新加入的filter
	 * @return 返回过滤条件
	 */
	public static String addFilter(String oldFilter, String linkStr, String filter)
	{
		if (StrUtil.isStrTrimNull(oldFilter))
		{
			if (linkStr.trim().equals("or"))
			{
				//原先没有过滤条件的话，or 后面的过滤条件就是无效的
				return "";
			} else
			{
				return filter;
			}
		}
		if (StrUtil.isStrTrimNull(filter))
		{
			//如果新过滤条件不存在的话，依然使用原有过滤条件
			return oldFilter;
		}
		return oldFilter + " " + linkStr + " " + filter;
	}

	/**
	 * 根据字段名和值的集合，获取not in过滤条件
	 *
	 * @param fldName 字段名
	 * @param objs    字段值数组
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getNotInFilter(String fldName, T[] objs, Map<String,Object> params)
	{
		return getInFilter(fldName, null, objs, params, true);
	}

	/**
	 * 根据字段名和值的集合，获取not in过滤条件
	 *
	 * @param fldName 字段名
	 * @param valList 字段值列表
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getNotInFilter(String fldName, List<T> valList, Map<String,Object> params)
	{
		return getInFilter(fldName, null, valList, params, true);
	}

	/**
	 * 根据字段名和值的集合，获取not in过滤条件
	 *
	 * @param fldName   字段名
	 * @param aliasName 列别名
	 * @param valList   字符串类型，逗号分隔值集合
	 * @param params    查询条件参数Map集合
	 * @return 返回过滤条件
	 */
	public static String getNotInFilter(String fldName, String aliasName, String valList, Map<String,Object> params)
	{
		if (StrUtil.isStrTrimNull(valList))
		{
			return "";
		}
		return getInFilter(fldName, aliasName, valList.split(","), params, true);
	}

	/**
	 * 根据字段名和值的集合，获取not in过滤条件
	 *
	 * @param fldName 字段名
	 * @param valList 字符串类型，逗号分隔值集合
	 * @param params  查询条件参数Map集合
	 * @return 返回过滤条件
	 */
	public static String getNotInFilter(String fldName, String valList, Map<String,Object> params)
	{
		if (StrUtil.isStrTrimNull(valList))
		{
			return "";
		}
		return getInFilter(fldName, null, valList.split(","), params, true);
	}

	/**
	 * 左匹配过滤条件
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @return 返回过滤条件
	 */
	public static String getLeftLikeFilter(String fldName, String val, Map<String,Object> params)
	{
		return getLikeFilter(fldName, val, params, -1);
	}

	/**
	 * 右匹配过滤条件
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @return 返回过滤条件
	 */
	public static String getRightLikeFilter(String fldName, String val, Map<String,Object> params)
	{
		return getLikeFilter(fldName, val, params, 1);
	}

	/**
	 * 左右匹配过滤条件
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @return 返回过滤条件
	 */
	public static String getLikeFilter(String fldName, String val, Map<String,Object> params)
	{
		return getLikeFilter(fldName, val, params, 0);
	}

	/**
	 * 自定义匹配过滤条件
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @return 返回过滤条件
	 */
	public static String getLikeFilterDIY(String fldName, String val, Map<String,Object> params)
	{
		return getLikeFilter(fldName, val, params, 2);
	}

	/**
	 * 小于过滤
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getLtFilter(String fldName, T val, Map<String,Object> params)
	{
		return getCompareFilter(fldName, val, params, -2);
	}

	/**
	 * 小于等于过滤
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getLtEqFilter(String fldName, T val, Map<String,Object> params)
	{
		return getCompareFilter(fldName, val, params, -1);
	}

	/**
	 * 等于过滤
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getEqFilter(String fldName, T val, Map<String,Object> params)
	{
		return getCompareFilter(fldName, val, params, 0);
	}

	/**
	 * 大于等于过滤
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getGtEqFilter(String fldName, T val, Map<String,Object> params)
	{
		return getCompareFilter(fldName, val, params, 1);
	}

	/**
	 * 大于过滤
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getGtFilter(String fldName, T val, Map<String,Object> params)
	{
		return getCompareFilter(fldName, val, params, 2);
	}

	/**
	 * between 过滤，具体的是否包含上下限需要看对应的数据库支持情况
	 *
	 * @param fldName 字段名
	 * @param val1    起始值
	 * @param val2    结束值
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getBetweenFilter(String fldName, T val1, T val2, Map<String,Object> params)
	{
		return getBetweenFilter(fldName, val1, val2, params, 1);
	}

	/**
	 * not between 过滤，具体的是否包含上下限需要看对应的数据库支持情况
	 *
	 * @param fldName 字段名
	 * @param val1    起始值
	 * @param val2    结束值
	 * @param params  查询条件参数Map集合
	 * @param <T>     任意类型对象
	 * @return 返回过滤条件
	 */
	public static <T> String getNotBetweenFilter(String fldName, T val1, T val2, Map<String,Object> params)
	{
		return getBetweenFilter(fldName, val1, val2, params, -1);
	}

	/**
	 * 获取模糊匹配表达式
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @param option  -1：左匹配   0：两边匹配    1：右匹配     2：自定义匹配
	 * @return 模糊匹配表达式
	 */
	private static String getLikeFilter(String fldName, String val, Map<String,Object> params, int option)
	{
		if (StrUtil.isStrTrimNull(val))
		{
			return null;
		}
		switch (option)
		{
		case -1:
			params.put(fldName, "%" + val);
			break;
		case 0:
			params.put(fldName, "%" + val + "%");
			break;
		case 1:
			params.put(fldName, val + "%");
			break;
		case 2:
			params.put(fldName, val);
			break;
		}
		return fldName + " like :" + fldName;
	}

	private static <T> String getInFilter(String fldName, String aliasName, T[] objs, Map<String,Object> params, boolean isNotIn)
	{
		if (aliasName == null)
		{
			aliasName = fldName;
		}
		StringBuffer filterBuf = new StringBuffer();
		filterBuf.append(fldName + (isNotIn ? " not" : "") + " in (");
		for (int i = 0 ; i < objs.length ; i++)
		{
			if (i > 0)
			{
				filterBuf.append(",");
			}
			filterBuf.append(":" + aliasName + i);
			params.put(fldName + i, objs[i]);
		}
		filterBuf.append(" )");
		return filterBuf.toString();
	}

	private static <T> String getInFilter(String fldName, String aliasName, List<T> valList, Map<String,Object> params, boolean isNotIn)
	{
		if (aliasName == null)
		{
			aliasName = fldName;
		}
		StringBuffer filterBuf = new StringBuffer();
		filterBuf.append(fldName + (isNotIn ? " not" : "") + " in (");
		for (int i = 0 ; i < valList.size() ; i++)
		{
			if (i > 0)
			{
				filterBuf.append(",");
			}
			filterBuf.append(":" + aliasName + i);
			params.put(fldName + i, valList.get(i));
		}
		filterBuf.append(" )");
		return filterBuf.toString();
	}

	/**
	 * 比较过滤
	 *
	 * @param fldName 字段名
	 * @param val     字段值
	 * @param params  查询条件参数Map集合
	 * @param option  -2:小于 -1：小于等于 0：等于 1：大于等于 2：大于
	 * @param <T>     任意对象
	 * @return 返回过滤条件
	 */
	private static <T> String getCompareFilter(String fldName, T val, Map<String,Object> params, int option)
	{
		if (isNull(val))
		{
			return null;
		}
		params.put(fldName, val);
		switch (option)
		{
		case -2:
			return fldName + " < :" + fldName;
		case -1:
			return fldName + " <= :" + fldName;
		case 0:
			return fldName + " = :" + fldName;
		case 1:
			return fldName + " >= :" + fldName;
		case 2:
			return fldName + " > :" + fldName;
		}
		return null;
	}

	private static <T> String getBetweenFilter(String fldName, T val1, T val2, Map<String,Object> params, int option)
	{
		if (isNull(val1))
		{
			if (isNull(val2))
			{
				return null;
			} else
			{
				return getLtEqFilter(fldName, val2, params);
			}
		} else
		{
			if (isNull(val2))
			{
				return getGtEqFilter(fldName, val1, params);
			} else
			{
				params.put(fldName + "_1", val1);
				params.put(fldName + "_2", val2);
				String filter = fldName;
				if (option == -1)
				{
					filter += " not";
				}
				filter += " between :" + fldName + "_1 AND :" + fldName + "_2";
				return filter;
			}
		}
	}

	private static <T> boolean isNull(T val)
	{
		if (val == null)
		{
			return true;
		} else if (val instanceof String)
		{
			if (StrUtil.isStrTrimNull((String) val))
			{
				return true;
			}
		}
		return false;
	}
}
