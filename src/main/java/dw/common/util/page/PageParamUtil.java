package dw.common.util.page;

import java.util.Map;
import java.util.Set;

/**
 * Created by xins_cyf on 2017/3/19.
 */
public class PageParamUtil
{
	/**
	 * 根据dataMap获取对应的分页携带的过滤参数
	 *
	 * @param dataMap 参数集合
	 * @return 拼接后的Url参数字符串
	 */
	public static String createPageInfoParams(Map<String,Object> dataMap)
	{
		if (dataMap == null || dataMap.size() == 0)
		{
			return null;
		} else
		{
			Set<String> keys = dataMap.keySet();
			String params = "";
			for (String key : keys)
			{
				if (key.startsWith("_"))
				{
					continue;
				}
				Object value = dataMap.get(key);
				if (value != null)
				{
					if (params.length() > 0)
					{
						params += "&";
					}
					params += key + "=" + value;
				}
			}
			return params;
		}
	}
}
