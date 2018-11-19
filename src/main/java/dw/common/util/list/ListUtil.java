package dw.common.util.list;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListUtil
{
	/**
	 * 返回两个集合的运算结果
	 *
	 * @param list1  列表1
	 * @param list2  列表2
	 * @param option 1:(list1-list2)在list1中不在list2中
	 *               0:(list1-list2)+(list2-list1)在list1和list2合集减去公共部分
	 *               -1:(list2-list1)在list2中不在list1中
	 * @param <T>    泛型对象
	 * @return 集合运算结果
	 */
	public static <T> List<T> getDiffSet(List<T> list1, List<T> list2, int option)
	{
		List<T> result = new ArrayList<>();
		//在list1中，不再list2中
		if (option == 1)
		{
			result = list1.stream().filter((t) -> !list2.contains(t)).collect(Collectors.toList());
		} else if (option == -1)
		{
			result = list2.stream().filter((t) -> !list1.contains(t)).collect(Collectors.toList());
		} else
		{
			List<T> r1 = getDiffSet(list1, list2, 1);
			List<T> r2 = getDiffSet(list1, list2, -1);
			result.addAll(r1);
			result.addAll(r2);
		}
		return result;
	}

	/**
	 * 将from中得元素全部添加到to中
	 *
	 * @param to 添加至
	 * @param from 添加来源
	 * @param <T>    泛型对象
	 * @return 合并后的List
	 *
	 */
	public static <T> List<T> mergeList(List<T> to, List<T> from)
	{
		if (from == null)
		{
			return to;
		}
		if (to == null)
		{
			to = new ArrayList<>();
		}
		to.addAll(from);
		return to;
	}

	/**
	 * 使用指定字符串连接strList，生成最终连接后的字符串
	 *
	 * @param strList 字符串列表
	 * @param linkStr 连接字符串
	 * @return 连接后的字符串
	 */
	public static String linkStrList(List<String> strList, String linkStr)
	{
		if (strList == null || strList.size() == 0)
		{
			return "";
		}
		StringBuffer linkedStr = new StringBuffer();
		for (int i = 0 ; i < strList.size() ; i++)
		{
			if (i > 0)
			{
				linkedStr.append(linkStr);
			}
			linkedStr.append(strList.get(i));
		}
		return linkedStr.toString();
	}
}
