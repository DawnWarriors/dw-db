package dw.db.util;

import java.util.UUID;

public class DBIDUtil
{
	/**
	 * 创建32位UUID
	 *
	 * @return 32位UUID
	 */
	public static String createUUId()
	{
		return UUID.randomUUID().toString().substring(0, 32);
	}

	/**
	 * 创建64位UUID
	 * @return 64位UUID
	 */
	public static String createUUId64()
	{
		return UUID.randomUUID().toString().substring(0, 64);
	}
}
