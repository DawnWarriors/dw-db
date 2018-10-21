package dw.db.util;

import dw.db.Database;
import dw.db.DatabaseConstant;
import dw.db.config.DBConfig;
import dw.common.util.num.IntUtil;
import dw.common.util.str.StrUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//	 如果后期需要更换ID生成策略，在这里修改即可
//	 目前仅支持单主键
public class DBIDUtil
{
	/**
	 * 生成制指定数的新的ID
	 * @param eveMap
	 * @param tableName
	 * @param count	生成ID个数
	 * @return
	 */
	public static String[] createIDs(Map<String,Object> eveMap, String tableName, int count)
	{
//		String cacheKey = getIDCacheKey(eveMap, tableName);
//		String oldId = CacheUtil.getString(cacheKey);
//		String idLike = getIDLike(eveMap, tableName);
//		//要处理ID格式发生变化的情况，所以要拿oldID和IDlike进行匹配，如果不匹配，也是要重新生成新缓存
//		if (oldId == null || oldId.length() == 0 || !isIdMatch(oldId, idLike))
//		{
//			String idNames[] = DatabaseConstant.getIdNames(tableName);
//			//仅支持单主键
//			String idName = idNames[0];
//			String sql = "select max(" + idName + ") from " + tableName + " where " + idName + " like '" + idLike + "'";
//			Database db = (Database) eveMap.get("DB");
//			Object obj = db.queryObject(sql);
//			if (obj == null)
//			{
//				oldId = idLike.replaceAll("_", "0");
//			} else
//			{
//				oldId = (String) obj;
//			}
//		}
//		String idPrefix = DBConfig.getIDPrefix(eveMap, tableName);
//		int idLength = DBConfig.getIDLength(eveMap);
//		String newIds[] = StrUtil.numStrIncrease(oldId, idPrefix.length(), idLength, count, 1);
//		CacheUtil.putString(cacheKey, newIds[count - 1]);
//		return newIds;
		//TODO 使用通用Redis缓存解决方案
		return null;
	}

	/**
	 * 生成制指定数的新的ID
	 * @param db
	 * @param tableName
	 * @param count	生成ID个数
	 * @return
	 */
	public static String[] createIDs(Database db, String tableName, int count)
	{
		Map<String,Object> eveMap = new HashMap<>();
		eveMap.put("DB", db);
		return createIDs(eveMap, tableName, count);
	}

	/**
	 * 生成一个ID
	 * @param eveMap
	 * @param tableName
	 * @return
	 */
	public static String createID(Map<String,Object> eveMap, String tableName)
	{
		String[] newIds = createIDs(eveMap, tableName, 1);
		return newIds[0];
	}

	/**
	 * 生成一个ID
	 * @param db
	 * @param tableName
	 * @return
	 */
	public static String createID(Database db, String tableName)
	{
		String[] newIds = createIDs(db, tableName, 1);
		return newIds[0];
	}

	/**
	 * 可供客户端调用的生成单个ID的方法
	 * @param eveMap
	 * @param params
	 * @return
	 */
	public static String createID(Map<String,Object> eveMap, Map<String,Object> params)
	{
		String tableName = (String) params.get("tableName");
		String[] newIds = createIDs(eveMap, tableName, 1);
		return newIds[0];
	}

	/**
	 * 可供客户端调用的生成多个ID的方法
	 * @param eveMap
	 * @param params
	 * @return
	 */
	public static String[] createIDs(Map<String,Object> eveMap, Map<String,Object> params)
	{
		String tableName = (String) params.get("tableName");
		int count = IntUtil.objToInt(params.get("count"));
		String[] newIds = createIDs(eveMap, tableName, count);
		return newIds;
	}

	/**
	 * 进行oldID和ID格式的匹配验证
	 * @param oldId
	 * @param idLike
	 * @return
	 */
	private static boolean isIdMatch(String oldId, String idLike)
	{
		if (oldId == null || oldId.length() != idLike.length())
		{
			return false;
		}
		idLike = idLike.replaceAll("_", "\\\\d");
		return oldId.matches(idLike);
	}

	/**
	 * 根据表名获取对应的ID缓存key
	 * @param eveMap
	 * @param tableName
	 * @return
	 */
	private static String getIDCacheKey(Map<String,Object> eveMap, String tableName)
	{
		//TODO 多系统共用缓存服务器的解决方案
		String sysName = "";
		String sysCode = "";
		return "TBLID:" + sysName + ":" + sysCode + ":" + tableName + ":";
	}

	/**
	 * 根据表名获取对应的ID的格式
	 * @param eveMap
	 * @param tableName
	 * @return
	 */
	private static String getIDLike(Map<String,Object> eveMap, String tableName)
	{
		String idLike = "";
		String prefix = DBConfig.getIDPrefix(eveMap, tableName);
		int length = DBConfig.getIDLength(eveMap);
		if (!StrUtil.isStrTrimNull(prefix))
		{
			idLike += prefix;
			length = length - prefix.length();
		}
		idLike += StrUtil.createStr(length, '_');
		//idLike = idLike.replaceAll("@", "[0-9]");
		return idLike;
	}

	/**
	 * 通过UUID创建32位的数据ID
	 *
	 * @return
	 */
	public static String createUUId() {
		return UUID.randomUUID().toString().substring(0, 32);
	}

	public static void main(String[] args)
	{
		String oldId = "abc1234";
		String idLike = "c___";
		System.out.println(isIdMatch(oldId, idLike));
		System.out.println(idLike);
	}
}
