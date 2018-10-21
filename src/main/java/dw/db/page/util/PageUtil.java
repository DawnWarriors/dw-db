package dw.db.page.util;

import dw.db.Database;
import dw.db.page.model.PageInfo;
import dw.common.util.num.IntUtil;
import dw.common.util.str.StrUtil;

import java.util.HashMap;
import java.util.Map;

public class PageUtil
{
	public static PageInfo getPageInfo(Database db, String sql, Map<String,Object> paramGetter, int askPage, int pageSize)
	{
		Map<String,Object> pageInfoMap = getPageInfoMap(db, sql, paramGetter, askPage, pageSize);
		int curPage_ = (int) pageInfoMap.get("askPage");
		int pageCount_ = (int) pageInfoMap.get("pageCount");
		PageInfo pageInfo = new PageInfo(curPage_, pageCount_);
		int offset = IntUtil.objToInt(pageInfoMap.get("offset"));
		pageSize = IntUtil.objToInt(pageInfoMap.get("pageSize"));
		pageInfo.setDataCount(IntUtil.objToInt(pageInfoMap.get("dataCount")));
		pageInfo.setCurPageDataCount(IntUtil.objToInt(pageInfoMap.get("curPageDataCount")));
		pageInfo.setPageSize(pageSize);
		//处理分页条件
		setLimitInfo(offset, pageSize, paramGetter);
		return pageInfo;
	}

	public static PageInfo getPageInfo(Database db, String mainSql, String filter, Map<String,Object> paramGetter, int askPage, int pageSize)
	{
		if (StrUtil.isNotStrNull(filter))
		{
			mainSql += " where " + filter;
		}
		return getPageInfo(db, mainSql, paramGetter, askPage, pageSize);
	}

	/**
	 * 获取分页信息
	 * @param db
	 * @param sql
	 * @param paramGetter
	 * @param askPage
	 * @param pageSize
	 * @return
	 */
	private static Map<String,Object> getPageInfoMap(Database db, String sql, Map<String,Object> paramGetter, int askPage, int pageSize)
	{
		if (askPage <= 0)
		{
			askPage = 1;
		}
		if (pageSize <= 0)
		{
			pageSize = 10;
		}
		Map<String,Object> pageInfo = new HashMap<>();
		//总数据条数
		int dataCount = new Long(db.getCount(sql, paramGetter)).intValue();
		int offset = (askPage - 1) * pageSize;
		if (offset > dataCount)
		{
			offset = dataCount - dataCount % pageSize;
			askPage = offset / pageSize + 1;
		}
		//当前页数据条数
		int curPageDataCount = pageSize;
		//如果是最后一页
		if ((askPage + 1) * pageSize >= dataCount)
		{
			//最后一页的数据行数
			curPageDataCount = dataCount - offset;
		}
		//总页数
		int pageCount = dataCount / pageSize;
		if (dataCount % pageSize != 0)
		{
			pageCount++;
		}
		pageInfo.put("offset", offset);
		pageInfo.put("askPage", askPage);
		pageInfo.put("pageCount", pageCount);
		pageInfo.put("dataCount", dataCount);
		pageInfo.put("pageSize", pageSize);
		pageInfo.put("curPageDataCount", curPageDataCount);
		return pageInfo;
	}

	public static void setLimitInfo(int offset, int pageSize, Map<String,Object> paramGetter)
	{
		if (paramGetter == null)
		{
			paramGetter = new HashMap<>();
		}
		paramGetter.put("SQL.PAGE.OFFSET", offset);
		paramGetter.put("SQL.PAGE.SIZE", pageSize);
	}
}
