package dw.common.util.str;

import org.apache.commons.lang3.StringEscapeUtils;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil
{
	/**
	 * 返回两个字符串的差集【srcStr-subStr】，默认使用逗号分隔.
	 * 测试1:
	 * 源：System.err.println(getDiffset("1,2,11,1,,", "1",","));
	 * 终端：2,11
	 *
	 * @param srcStr    源字符串
	 * @param subStr    被减字符串
	 * @param separator 字符串分隔符
	 * @return String
	 */
	public static String getDiffset(final String srcStr, final String subStr, final String separator)
	{
		if (srcStr == null || subStr == null)
		{
			return srcStr;
		}
		final StringBuilder sb = new StringBuilder();
		for (final String src : srcStr.split(separator))
		{
			if (src.length() == 0 || isStrIn(subStr, src))
			{
				continue;
			}
			if (sb.length() > 0)
			{
				sb.append(',');
			}
			sb.append(src);
		}
		return sb.toString();
	}

	/**
	 * 取字符串交集
	 *
	 * @param srcStr 源字符串集
	 * @param subStr 源字符串集
	 * @param del    分隔符
	 * @return String
	 */
	public static String getInterSet(final String srcStr, final String subStr, final String del)
	{
		if (srcStr == null || subStr == null || srcStr.length() == 0 || subStr.length() == 0)
		{
			return null;
		}
		final String[] srces = srcStr.split(del);
		final StringBuilder sb = new StringBuilder();
		for (final String src : srces)
		{
			if (src == null || src.length() == 0)
			{
				continue;
			}
			if (!isStrIn(subStr, src))
			{
				continue;
			}
			if (sb.length() > 0)
			{
				sb.append(del);
			}
			sb.append(src);
		}
		if (sb.length() > 0)
		{
			return sb.toString();
		}
		return null;
	}

	/**
	 * 判断字段串 s是否在字段串strList中
	 *
	 * @param strList 可理解为源字符串
	 * @param s       想要在源字符串中查找的字符串
	 * @return 如果存在返回true, 如果不存在返回false
	 */
	static public boolean isStrIn(final String strList, final String s)
	{
		return isStrIn(strList, s, ',');
	}

	/**
	 * 判断字段串 s是否在字段串strList中
	 *
	 * @param strList   可理解为源字符串
	 * @param s         想要在源字符串中查找的字符串
	 * @param delimiter 分隔符
	 * @return 如果存在返回true, 如果不存在返回false
	 */
	public static boolean isStrIn(final String strList, final String s, final char delimiter)
	{
		return indexOf(strList, delimiter, s, false, false) >= 0;
	}

	/**
	 * 返回子串在源字符串中的索引，如果找到则返回非负的索引，否则返回-1
	 *
	 * @param str        源字符串
	 * @param delimiter  分隔符
	 * @param sub        子串
	 * @param trim       删除前后导空白字符
	 * @param ignoreCase 忽略大小写
	 * @return int
	 */
	public static final int indexOf(String str, char delimiter, String sub, boolean trim, boolean ignoreCase)
	{
		if (str == null || sub == null)
			return -1;
		int p0 = 0;
		final int n = str.length();
		final int cmpLen = sub.length();
		int j = 0;
		for (int i = 0 ; i <= n ; i++)
		{
			if (i == n || str.charAt(i) == delimiter)
			{
				if (trim)
				{
					if (ignoreCase ? str.substring(p0, i).trim().equalsIgnoreCase(sub) : str.substring(p0, i).trim().equals(sub))
						return j;
					// str.regionMatches(p0,);
				} else
				{
					if (cmpLen == i - p0 && str.regionMatches(ignoreCase, p0, sub, 0, cmpLen))
						return j;
				}
				p0 = i + 1;
				j++;
			}
		}
		return -1;
	}

	/**
	 * 用separator作为分隔符，连接strList
	 *
	 * @param strList   字符串列表
	 * @param separator 连接分隔符
	 * @return 拼接后的字符串
	 */
	public static String linkListToString(List<String> strList, final String separator)
	{
		StringBuffer strbuf = new StringBuffer("");
		for (int i = 0 ; i < strList.size() ; i++)
		{
			if (i > 0)
			{
				strbuf.append(separator);
			}
			strbuf.append(strList.get(i));
		}
		return strbuf.toString();
	}

	/**
	 * 用separator作为分隔符，连接strArray
	 *
	 * @param strArray  字符串数组
	 * @param separator 连接分隔符
	 * @return 拼接后的字符串
	 */
	public static String linkStrArrayToString(String[] strArray, final String separator)
	{
		StringBuffer strbuf = new StringBuffer("");
		for (int i = 0 ; i < strArray.length ; i++)
		{
			if (i > 0)
			{
				strbuf.append(separator);
			}
			strbuf.append(strArray[i]);
		}
		return strbuf.toString();
	}

	/**
	 * 判断一个字符串是否在指定的字符串数组中
	 *
	 * @param strAry 字符串数组
	 * @param str    String
	 * @return boolean：str是否在strAry中
	 */
	public static boolean isStrInStrAry(String strAry[], String str)
	{
		if (strAry == null || strAry.length == 0 || str == null)
		{
			return false;
		}
		for (String s : strAry)
		{
			if (str.equals(s))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断字符串是否为空
	 * 一般用于接收前台AJAX数据时的数据判断
	 *
	 * @param str String
	 * @return 空返回true
	 */
	static public boolean isStrTrimNull(final String str)
	{
		return isStrNull(str) || str.trim().equalsIgnoreCase("null");
	}

	/**
	 * 判断字符串是否非空
	 * 一般用于接收前台AJAX数据时的数据判断
	 *
	 * @param str String
	 * @return 空返回true
	 */
	static public boolean isNotStrTrimNull(final String str)
	{
		return !isStrTrimNull(str);
	}

	/**
	 * 判断字符串是否为空
	 *
	 * @param str String
	 * @return 空返回true
	 */
	static public boolean isStrNull(final String str)
	{
		return str == null || str.trim().length() == 0;
	}

	/**
	 * 判断字符串是否非空
	 *
	 * @param str String
	 * @return 空返回false
	 */
	static public boolean isNotStrNull(final String str)
	{
		return !isStrNull(str);
	}

	/**
	 * 创建一个长度为length的由字符ch组成的字符串
	 *
	 * @param length 长度
	 * @param ch     字符
	 * @return 生成的字符串
	 */
	public static String createStr(int length, char ch)
	{
		String str = "";
		for (int i = 0 ; i < length ; i++)
		{
			str += ch;
		}
		return str;
	}

	/**
	 * 数字字符串的递增操作，count是递增次数，递增记录返回
	 *
	 * @param numStr     数字字符串
	 * @param beginIndex 开始索引
	 * @param endIndex   结束位置		与beginIndex一起用于字符串截取
	 * @param count      递增次数
	 * @param step       递增步长
	 * @return String[]
	 */
	public static String[] numStrIncrease(String numStr, int beginIndex, int endIndex, int count, int step)
	{
		String result[] = new String[count];
		String prefix = numStr.substring(0, beginIndex);
		String suffix = numStr.substring(endIndex);
		String numStr_ = numStr.substring(beginIndex, endIndex);
		BigDecimal num = new BigDecimal(numStr_);
		for (int i = 0 ; i < count ; i++)
		{
			num = num.add(new BigDecimal(1));
			String str = num.toString();
			//前方补0
			str = createStr(endIndex - beginIndex - str.length(), '0') + str;
			result[i] = prefix + str + suffix;
			if (result[i].length() > numStr.length())
			{
				throw new RuntimeException("长度超长溢出!");
			}
		}
		return result;
	}

	/**
	 * object转换成字符串，默认值空字符串
	 *
	 * @param obj Object
	 * @return String
	 */
	public static String objToString(Object obj)
	{
		return objToString(obj, "");
	}

	/**
	 * object转换成字符串，如果是NULL，则返沪默认值
	 *
	 * @param obj          Object
	 * @param defaultValue 指定的默认值
	 * @return String
	 */
	public static String objToString(Object obj, String defaultValue)
	{
		if (obj == null)
		{
			return defaultValue;
		} else if (obj instanceof Number)
		{
			return obj + "";
		} else if (obj instanceof Date)
		{
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(obj);
		} else
		{
			return obj.toString();
		}
	}

	/**
	 * 截取指定长度的字符串
	 * 如果长度超出，截取后后边加“…”
	 * 如果长度不够则返回原字符串
	 *
	 * @param str    String
	 * @param length 长度
	 * @return String
	 */
	public static String subLimitLengthStr(String str, int length)
	{
		if (StrUtil.isStrNull(str) || str.length() <= length)
		{
			return str;
		} else
		{
			return str.substring(0, length) + "…";
		}
	}

	/**
	 * 截取
	 *
	 * @param content         文本内容
	 * @param startCutKeyWord 开始截取标记
	 * @param endCutKeyWord   结束截取标记
	 * @return 截取结果
	 */
	public static String substring(String content, String startCutKeyWord, String endCutKeyWord)
	{
		int startCutIndex = content.indexOf(startCutKeyWord);
		int endCutIndex = content.indexOf(endCutKeyWord);
		String part = content.substring(startCutIndex, endCutIndex);
		return part;
	}

	/**
	 * 获取文本中的前maxLength个 超出部分使用……补充
	 *
	 * @param text      文本内容
	 * @param maxLength 最大长度
	 * @return String
	 */
	public static String head(String text, int maxLength)
	{
		if (text == null)
		{
			return null;
		}
		if (text.length() <= maxLength)
		{
			return text;
		}
		return text.substring(0, maxLength) + "……";
	}

	/**
	 * 将html转码，例如"&amp;" -&gt; "&amp;amp;"
	 *
	 * @param input String
	 * @return String
	 */
	public static String escapeHtml(String input)
	{
		return StringEscapeUtils.escapeHtml4(input);
	}

	/**
	 * 将html反转码，例如"&amp;amp;" -&gt; "&amp;"
	 *
	 * @param input String
	 * @return String
	 */
	public static String unescapeHtml(String input)
	{
		return StringEscapeUtils.unescapeHtml4(input);
	}

	/**
	 * 将字符串转码，例如"中文" -&gt; "\u4E2D\u6587"
	 *
	 * @param input String
	 * @return String
	 */
	public static String escape(String input)
	{
		return StringEscapeUtils.escapeJava(input);
	}

	/**
	 * 将字符串反转码，例如"\u4E2D\u6587" -&gt; "中文"
	 *
	 * @param input String
	 * @return String
	 */
	public static String unescape(String input)
	{
		return StringEscapeUtils.unescapeJava(input);
	}

	/**
	 * getBytes
	 *
	 * @param input   String
	 * @param charset Charset
	 * @return String
	 */
	public static byte[] getBytes(String input, Charset charset)
	{
		if (input == null)
		{
			return null;
		}
		return input.getBytes(charset);
	}

	/**
	 * 使用等号对base64补齐。
	 * （可解决base64用于补齐的等号在cookie中被截断的问题）
	 *
	 * @param base64 String
	 * @return String
	 */
	public static String ensureBase64Padding(String base64)
	{
		int length = base64.length();
		if (length % 4 != 0)
		{
			StringBuilder builder = new StringBuilder(base64);
			for (int i = 0 ; i < length % 4 ; ++i)
			{
				builder.append('=');
			}
			base64 = builder.toString();
		}
		return base64;
	}

	/**
	 * 清除base64的补齐等号。
	 *
	 * @param base64 String
	 * @return String
	 */
	public static String removeBase64Padding(String base64)
	{
		int length = base64.length();
		if (length < 4)
		{
			return base64;
		}
		int i = 0;
		for ( ; i < 3 ; i++)
		{
			if (base64.charAt(length - 1 - i) != '=')
			{
				break;
			}
		}
		return base64.substring(0, length - i);
	}

	/**
	 * 判断是否为纯数字
	 *
	 * @param str 把要判断的字符串 str 传入
	 * @return boolean
	 */
	public static boolean isNumeric(String str)
	{
		for (int i = 0 ; i < str.length() ; i++)
		{
			if (!Character.isDigit(str.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * 将以特定字符串连接的字符串，转换成List
	 *
	 * @param str       String
	 * @param separator String
	 * @return 字符串List
	 */
	public static List<String> strToList(String str, final String separator)
	{
		List<String> resultList = new ArrayList<>();
		if (isNotStrTrimNull(str))
		{
			String[] strAry = str.split(separator);
			for (String s : strAry)
			{
				resultList.add(s);
			}
		}
		return resultList;
	}

	/**
	 * 下划线转驼峰法
	 *
	 * @param line       源字符串
	 * @param smallCamel 大小驼峰,是否为小驼峰
	 * @return 转换后的字符串
	 * 转载自： https://www.cnblogs.com/javasharp/p/4622413.html
	 */
	public static String underline2Camel(String line, boolean smallCamel)
	{
		if (line == null || "".equals(line))
		{
			return "";
		}
		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile("([A-Za-z\\d]+)(_)?");
		Matcher matcher = pattern.matcher(line);
		while (matcher.find())
		{
			String word = matcher.group();
			sb.append(smallCamel && matcher.start() == 0 ? Character.toLowerCase(word.charAt(0)) : Character.toUpperCase(word.charAt(0)));
			int index = word.lastIndexOf('_');
			if (index > 0)
			{
				sb.append(word.substring(1, index).toLowerCase());
			} else
			{
				sb.append(word.substring(1).toLowerCase());
			}
		}
		return sb.toString();
	}

	/**
	 * 驼峰法转下划线
	 *
	 * @param line 源字符串
	 * @return 转换后的字符串
	 * 转载自： https://www.cnblogs.com/javasharp/p/4622413.html
	 */
	public static String camel2Underline(String line)
	{
		if (line == null || "".equals(line))
		{
			return "";
		}
		line = String.valueOf(line.charAt(0)).toUpperCase().concat(line.substring(1));
		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile("[A-Z]([a-z\\d]+)?");
		Matcher matcher = pattern.matcher(line);
		while (matcher.find())
		{
			String word = matcher.group();
			sb.append(word.toUpperCase());
			sb.append(matcher.end() == line.length() ? "" : "_");
		}
		return sb.toString();
	}
}
