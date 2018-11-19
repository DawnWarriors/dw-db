package dw.common.util.macro;

import dw.common.util.str.StrUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 宏替换
 *
 * @author 曹燕飞
 */
public class MacroUtil
{
	//日期替换
	public static String CURYEAR        = "\\$\\{CURYEAR\\}";            //当前年（四位）
	public static String CURYEAR2       = "\\$\\{CURYEAR2\\}";            //当前年（两位）
	public static String CURMONTH       = "\\$\\{CURMONTH\\}";            //当前月
	public static String CURDAY         = "\\$\\{CURDAY\\}";            //当前日
	public static String CURHOUR        = "\\$\\{CURHOUR\\}";            //当前时
	public static String CURMINUTE      = "\\$\\{CURMINUTE\\}";        //当前分
	public static String CURSECOND      = "\\$\\{CURSECOND\\}";        //当前秒
	public static String CURMILLISECOND = "\\$\\{CURMILLISECOND\\}";    //当前毫秒

	/**
	 * 利用给定的数据Map去替换源字符串中设置的内容
	 *
	 * @param dataMap 数据集
	 * @param srcStr_ 包含宏定义的字符串
	 * @return 替换后的字符串
	 */
	public static String macroReplace(Map<String,Object> dataMap, String srcStr_)
	{
		String resultStr = dateMacroReplace(srcStr_);
		while (true)
		{
			int begin = 0;
			begin = resultStr.indexOf("#{", begin);
			if (begin == -1)
			{
				break;
			}
			int end = resultStr.indexOf("}", begin);
			if (end == -1)
			{
				throw new RuntimeException("格式错误：" + srcStr_);
			}
			String macroUnit = resultStr.substring(begin, end + 1);
			String replace = macroUnit.trim().substring(2, macroUnit.length() - 1);
			replace = StrUtil.objToString(dataMap.get(replace));
			if (StrUtil.isNotStrNull(replace))
			{
				resultStr = resultStr.replace(macroUnit, replace);
			} else
			{
				replace = macroUnit.replace("#", "$");
				resultStr = resultStr.replace(macroUnit, replace);
			}
		}
		return resultStr;
	}

	/**
	 * 当前日期的宏替换
	 *
	 * @param srcStr 原字符串
	 * @return 替换后的字符串
	 */
	public static String dateMacroReplace(String srcStr)
	{
		return dateMacroReplace(new Date(), srcStr);
	}

	/**
	 * 指定日期的宏替换
	 *
	 * @param date   日期
	 * @param srcStr 包含宏定义的字符串
	 * @return 替换后的字符串
	 */
	public static String dateMacroReplace(Date date, String srcStr)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int curYear = calendar.get(Calendar.YEAR);
		int curYear2 = curYear % 100;
		int curMonth = calendar.get(Calendar.MONTH) + 1;
		int curDay = calendar.get(Calendar.DATE);
		int curHour = calendar.get(Calendar.HOUR_OF_DAY);//calendar.hour 取得是当前系统设置的时间制时间，hour_of_day取得是24小时制时间
		int curMinute = calendar.get(Calendar.MINUTE);
		int curSecond = calendar.get(Calendar.SECOND);
		int curMilliSecond = calendar.get(Calendar.MILLISECOND);
		String curYearStr = "" + curYear;
		String curYear2Str = (curYear2 < 10 ? "0" : "") + curYear2;
		String curMonthStr = (curMonth < 10 ? "0" : "") + curMonth;
		String curDayStr = (curDay < 10 ? "0" : "") + curDay;
		String curHourStr = (curHour < 10 ? "0" : "") + curHour;
		String curMinuteStr = (curMinute < 10 ? "0" : "") + curMinute;
		String curSecondStr = (curSecond < 10 ? "0" : "") + curSecond;
		String curMilliSecondStr = "" + curMilliSecond;
		srcStr = srcStr.replaceAll(CURYEAR, curYearStr);
		srcStr = srcStr.replaceAll(CURYEAR2, curYear2Str);
		srcStr = srcStr.replaceAll(CURMONTH, curMonthStr);
		srcStr = srcStr.replaceAll(CURDAY, curDayStr);
		srcStr = srcStr.replaceAll(CURHOUR, curHourStr);
		srcStr = srcStr.replaceAll(CURMINUTE, curMinuteStr);
		srcStr = srcStr.replaceAll(CURSECOND, curSecondStr);
		srcStr = srcStr.replaceAll(CURMILLISECOND, curMilliSecondStr);
		return srcStr;
	}
}
