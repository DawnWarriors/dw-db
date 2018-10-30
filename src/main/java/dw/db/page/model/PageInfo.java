package dw.db.page.model;

import dw.common.util.str.StrUtil;
import lombok.Data;

@Data
public class PageInfo
{
	private int		curPage				= 0;
	private int		pageCount			= 0;
	private int		pageNumBegin		= 0;
	private int		pageNumEnd			= 0;
	private boolean	hasNext				= false;
	private boolean	hasPre				= false;
	private int		dataCount			= 0;
	private int		curPageDataCount	= 0;
	private int		pageSize			= 0;
	private String	url_first			= "";		//首页链接
	private String	url_last			= "";		//末页链接
	private String	url_pre				= "";		//上一页
	private String	url_next			= "";		//下一页
	private String	url_size_10			= "";		//调整分页大小-10
	private String	url_size_20			= "";		//调整分页大小-20
	private String	url_size_30			= "";		//调整分页大小-30
	private String	url_size_50			= "";		//调整分页大小-50
	private String	url_num				= "";		//根据页面访问链接前缀，后边拼上页码即可

	/**
	 * 分页构造函数,默认最多显示5页
	 * @param curPage_ 当前第几页
	 * @param pageCount_ 共多少页
	 */
	public PageInfo(int curPage_, int pageCount_)
	{
		this.curPage = curPage_;
		this.pageCount = pageCount_;
		if (curPage > 1)
		{
			hasPre = true;
		} else
		{
			hasPre = false;
		}
		if (curPage < pageCount)
		{
			hasNext = true;
		} else
		{
			hasNext = false;
		}
		updatePageInfo(5);
	}

	/**
	 * 最多显示多少页，结合当前第几页来决定显示起始页和终止页
	 * @param pageRangeSize 最多显示多少页
	 */
	public void setPageRangeSize(int pageRangeSize)
	{
		updatePageInfo(pageRangeSize);
	}

	/**
	 * 设置分页页码点击请求链接
	 * @param baseUrl 基本链接，如127.0.0.1/test/userList
	 * @param otherParamStr 自定义参数，如查询参数
	 */
	public void setUrlParams(String baseUrl, String otherParamStr)
	{
		baseUrl = baseUrl + "?";
		if (StrUtil.isNotStrNull(otherParamStr))
		{
			baseUrl += "&" + otherParamStr;
		}
		url_first = baseUrl + "&pageSize=" + pageSize + "&askPage=1"; //首页链接
		url_last = baseUrl + "&pageSize=" + pageSize + "&askPage=" + pageCount; //末页链接
		url_pre = baseUrl + "&pageSize=" + pageSize + "&askPage=" + (curPage - 1); //上一页
		url_next = baseUrl + "&pageSize=" + pageSize + "&askPage=" + (curPage + 1); //下一页
		url_size_10 = baseUrl + "&pageSize=" + 10 + "&askPage=" + curPage; //调整分页大小-10
		url_size_20 = baseUrl + "&pageSize=" + 20 + "&askPage=" + curPage; //调整分页大小-20
		url_size_30 = baseUrl + "&pageSize=" + 30 + "&askPage=" + curPage; //调整分页大小-30
		url_size_50 = baseUrl + "&pageSize=" + 50 + "&askPage=" + curPage; //调整分页大小-50
		url_num = baseUrl + "&pageSize=" + pageSize + "&askPage="; //根据页面访问链接前缀，后边拼上页码即可
	}

	/**
	 * 更新页码范围
	 * @param pageRangeSize 页码范围大小
	 */
	private void updatePageInfo(int pageRangeSize)
	{
		int begin = 0;
		if (curPage - pageRangeSize > 0)
		{
			begin = curPage - pageRangeSize;
		} else
		{
			begin = 1;
		}
		int end = 0;
		if (curPage + pageRangeSize < pageCount)
		{
			end = curPage + pageRangeSize;
		} else
		{
			end = pageCount;
		}
		this.pageNumBegin = begin;
		this.pageNumEnd = end;
	}

}