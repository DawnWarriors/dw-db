package dw.db;

public class DatabaseConstant
{
	//表定义表中的字段
	public static final String	tbldef_cols[]			= { "tblid", "tblname", "tblname_zh", "cacheflags" };
	//表定义表中可修改的字段
	public static final String	tbldef_editable_cols[]	= { "tblid", "tblname_zh", "cacheflags" };
	//字段定义表中的字段
	public static final String	flddef_cols[]			= { "tblname", "fldid", "fldname", "fldname_zh", "fldtype", "fldlen", "flddecimal", "fldattr", "flddefault", "fldreftbl", "fldreffld" };
	//字段定义表中可修改的字段
	public static final String	flddef_editable_cols[]	= { "fldid", "fldname_zh", "fldtype", "fldlen", "flddecimal", "fldattr", "flddefault", "fldreftbl", "fldreffld" };
	//tbldef表主键
	private static final String	tbldefIDNames[]			= { "tblname" };
	//flddef表主键
	private static final String	flddefIDNames[]			= { "tblname", "fldname" };
	//table和sql 组成key的集合的缓存的key
	public static final String	tableSqlCacheKey		= "TABLE_SQL_CACHE_KEY:";

	/**
	 * 根据表名获取主键字段名称
	 * @param tblName
	 * @return
	 */
	public static String[] getIdNames(String tblName)
	{
		switch (tblName)
		{
		case "dw_tbl_def":
			return tbldefIDNames;
		case "dw_fld_def":
			return flddefIDNames;
		default:
			//默认主键名
			return new String[] { "id" };
		}
	}
}
