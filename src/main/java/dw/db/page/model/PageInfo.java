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

	public PageInfo(int curPage_, int pageCount_)
	{
		this.curPage = curPage_;
		this.pageCount = pageCount_;
		int begin = 0;
		if (curPage_ - 5 > 0)
		{
			begin = curPage_ - 5;
		} else
		{
			begin = 1;
		}
		int end = 0;
		if (curPage_ + 5 < pageCount_)
		{
			end = curPage_ + 5;
		} else
		{
			end = pageCount_;
		}
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
		this.pageNumBegin = begin;
		this.pageNumEnd = end;
	}

	public void setUrlParams(String baseUrl, String ortherParamStr)
	{
		baseUrl = baseUrl + "?";
		if (StrUtil.isNotStrNull(ortherParamStr))
		{
			baseUrl += "&" + ortherParamStr;
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
}