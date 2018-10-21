package dw.db.config;

import dw.common.util.str.StrUtil;

import java.util.Map;

public class DBConfig
{
	/**
	 * 获取配置的主键长度
	 *
	 * @param eveMap
	 * @return
	 */
	public static int getIDLength(Map<String,Object> eveMap)
	{
		//		String value = Config.getSysConfig("IDLength");
		//		if (value == null)
		//		{
		//			return 32;
		//		} else
		//		{
		//			return Integer.parseInt(value);
		//		}
		//约定使用缓存主键时
		return 64;
	}

	/**
	 * 获取配置的主键前缀
	 *
	 * @param eveMap
	 * @param tableName
	 * @return
	 */
	public static String getIDPrefix(Map<String,Object> eveMap, String tableName)
	{
		//		String value = Config.getSysConfig("IDPrefix");
		//		if (value == null)
		//		{
		//			return "";
		//		} else
		//		{
		//			return value;
		//		}
		return "";
	}

	/**
	 * 获取ID中数字部分的长度
	 *
	 * @param eveMap
	 * @param tableName
	 * @return
	 */
	public static int getNumLength(Map<String,Object> eveMap, String tableName)
	{
		int idLength = getIDLength(eveMap);
		int prefixLength = getIDPrefix(eveMap, tableName).length();
		return idLength - prefixLength;
	}

	/**
	 * 是否使用缓存
	 *
	 * @return
	 */
	public static boolean isUseCache()
	{
		//		String value = Config.getSysConfig("IsUseCache");
		//		if (!StrUtil.isStrTrimNull(value))
		//		{
		//			value = value.trim();
		//			if ("yes".equals(value) || "true".equals(value) || "Y".equals(value))
		//			{
		//				return true;
		//			}
		//		}
		//TODO 该配置项需要从配置文件读取
		//不启用时，不会缓存查询结果，用于访问较少或方便测试使用，启用有助于提升性能
		return false;
	}
}
