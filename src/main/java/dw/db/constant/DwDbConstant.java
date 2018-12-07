package dw.db.constant;

import lombok.Data;

import java.lang.reflect.Type;
import java.util.Map;

public class DwDbConstant
{
	/**
	 * 数据类型
	 */
	public static class SubsetType
	{
		/**
		 * model
		 */
		public static final int DW_MODEL       = 10;
		/**
		 * modelList
		 */
		public static final int DW_MODEL_LIST  = 11;
		/**
		 * object
		 */
		public static final int OBJECT         = 20;
		/**
		 * object[]
		 */
		public static final int OBJECT_ARRAY   = 21;
		/**
		 * object[][]
		 */
		public static final int OBJECT_ARRAY_2 = 22;
		/**
		 * Map[String,Object]
		 */
		public static final int MAP            = 30;
		/**
		 * List[Map[String,Object]]
		 */
		public static final int LIST_MAP       = 40;
	}
	/**
	 * 加载查询方式
	 */
	public static class SubsetSelect
	{
		//sql
		public static final int SQL        = 4;
		//sql+条件
		public static final int SQL_FILTER = 6;
		//条件
		public static final int FILTER     = 2;
	}
	/**
	 * SubsetType信息
	 */
	@Data
	public static class SubsetTypeInfo
	{
		//类型
		Integer subsetType;
		//ModelClass
		Class   modelClass;
		//TypeClass
		Class<?>    type;
	}
	/**
	 * 加载查询方式信息
	 */
	@Data
	public static class SubsetSelectInfo
	{
		//Sql
		String             sql;
		//loadFilter
		String             loadFilter;
		//KV存储字段对应
		Map<String,String> unionFilterMap;
		//order By
		String             ordBy;
		//是否需要参数
		boolean            isNeedParams;
		//查询方式
		int                selectType;
		//final sql 包含sql和过滤条件拼接
		String             finalSql;
		//final filter 包含loadFilter 和 union filter
		String  finalFilter = "";
		boolean isLazy      = false;
	}
}
