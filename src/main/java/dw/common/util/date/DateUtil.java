package dw.common.util.date;

import dw.common.util.macro.MacroUtil;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil extends org.apache.commons.lang3.time.DateUtils
{
	private static final String curDate       = "${CURYEAR}-${CURMONTH}-${CURDAY}";
	private static final String curDateTime   = "${CURYEAR2}-${CURMONTH}-${CURDAY} ${CURHOUR}:${CURMINUTE}:${CURSECOND}";
	private static final String curDateTimeCS = "${CURYEAR2}-${CURMONTH}-${CURDAY} ${CURHOUR}:${CURMINUTE}:${CURSECOND}.${CURMILLISECOND}";
	private static final String curTime       = "${CURHOUR}:${CURMINUTE}:${CURSECOND}";

	/**
	 * 获取上周一
	 *
	 * @return 上周一
	 */
	public static Calendar getLastMondayOfWeek()
	{
		Calendar c = Calendar.getInstance();
		int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (day_of_week == 0)
		{
			day_of_week = 7;
		}
		c.add(Calendar.DATE, -day_of_week - 6);
		return c;
	}

	/**
	 * 获取上周日
	 *
	 * @return 上周日
	 */
	public static Calendar getLastSundayOfWeek()
	{
		Calendar c = Calendar.getInstance();
		int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (day_of_week == 0)
		{
			day_of_week = 7;
		}
		c.add(Calendar.DATE, -day_of_week);
		return c;
	}

	/**
	 * 获取本周周1
	 *
	 * @return 本周周1
	 */
	public static Calendar getMondayOfThisWeek()
	{
		Calendar c = Calendar.getInstance();
		int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (day_of_week == 0)
		{
			day_of_week = 7;
		}
		c.add(Calendar.DATE, -day_of_week + 1);
		return c;
	}

	/**
	 * 获取本周周日
	 *
	 * @return 本周周日
	 */
	public static Calendar getSundayOfThisWeek()
	{
		Calendar c = Calendar.getInstance();
		int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (day_of_week == 0)
		{
			day_of_week = 7;
		}
		c.add(Calendar.DATE, -day_of_week + 7);
		return c;
	}

	/**
	 * 获取当前日期 yyyy-MM-dd
	 *
	 * @return 当前日期
	 */
	public static String getCurDate()
	{
		return MacroUtil.dateMacroReplace(curDate);
	}

	/**
	 * 获取当前日期时间 yyyy-MM-dd HH:mm:ss
	 *
	 * @return 当前日期时间
	 */
	public static String getCurDateTime()
	{
		return MacroUtil.dateMacroReplace(curDateTime);
	}

	/**
	 * 获取当前时间包含毫秒
	 *
	 * @return 当前时间
	 */
	public static String getCurDateTimeCS()
	{
		return MacroUtil.dateMacroReplace(curDateTimeCS);
	}

	/**
	 * 获取当前时间 HH:mm:ss
	 *
	 * @return 当前时间
	 */
	public static String getCurTime()
	{
		return MacroUtil.dateMacroReplace(curTime);
	}

	/**
	 * 获取自定义格式的时间
	 *
	 * @param timeExpr 日期格式字符串
	 * @return 格式化后的当前时间
	 */
	public static String getCurCustTime(String timeExpr)
	{
		timeExpr = timeExpr.replaceAll("yyyy", "\\$\\{CURYEAR\\}");
		timeExpr = timeExpr.replaceAll("yy", "\\$\\{CURYEAR2\\}");
		timeExpr = timeExpr.replaceAll("mm", "\\$\\{CURMONTH\\}");
		timeExpr = timeExpr.replaceAll("dd", "\\$\\{CURDAY\\}");
		timeExpr = timeExpr.replaceAll("hh", "\\$\\{CURHOUR\\}");
		timeExpr = timeExpr.replaceAll("ii", "\\$\\{CURMINUTE\\}");
		timeExpr = timeExpr.replaceAll("ss", "\\$\\{CURSECOND\\}");
		return MacroUtil.dateMacroReplace(timeExpr);
	}

	/**
	 * 根据指定格式格式化日期
	 *
	 * @param date     时间
	 * @param timeExpr 日期格式字符串
	 * @return 格式化后的时间
	 */
	public static String formatDate(Date date, String timeExpr)
	{
		timeExpr = timeExpr.replaceAll("yyyy", "\\$\\{CURYEAR\\}");
		timeExpr = timeExpr.replaceAll("yy", "\\$\\{CURYEAR2\\}");
		timeExpr = timeExpr.replaceAll("mm", "\\$\\{CURMONTH\\}");
		timeExpr = timeExpr.replaceAll("dd", "\\$\\{CURDAY\\}");
		timeExpr = timeExpr.replaceAll("hh", "\\$\\{CURHOUR\\}");
		timeExpr = timeExpr.replaceAll("ii", "\\$\\{CURMINUTE\\}");
		timeExpr = timeExpr.replaceAll("ss", "\\$\\{CURSECOND\\}");
		return MacroUtil.dateMacroReplace(date, timeExpr);
	}

	/**
	 * 格式化日期(yyyy-mm-dd)
	 *
	 * @param date 日期
	 * @return 日期字符串
	 */
	public static String formatDate_Y4MD(Date date)
	{
		return formatDate(date, "yyyy-mm-dd");
	}

	/**
	 * 格式化日期(yy-mm-dd)
	 *
	 * @param date 日期
	 * @return 日期字符串
	 */
	public static String formatDate_Y2MD(Date date)
	{
		return formatDate(date, "yy-mm-dd");
	}

	/**
	 * 格式化日期(yyyy-mm-dd hh:ii:ss)
	 *
	 * @param date 日期
	 * @return 日期字符串
	 */
	public static String formatDate_Y4MDHIS(Date date)
	{
		return formatDate(date, "yyyy-mm-dd hh:ii:ss");
	}

	/**
	 * 格式化日期(hh:ii:ss)
	 *
	 * @param date 日期
	 * @return 日期字符串
	 */
	public static String formatDate_HIS(Date date)
	{
		return formatDate(date, "hh:ii:ss");
	}

	/**
	 * 根据指定格式格式化当前日期
	 *
	 * @param timeExpr 日期格式化字符串
	 * @return 日期字符串
	 */
	public static String formatDate(String timeExpr)
	{
		return formatDate(new Date(), timeExpr);
	}

	/**
	 * 解析string为date。
	 *
	 * @param date    日期字符串
	 * @param pattern 日期格式化字符串
	 * @return 日期对象
	 * @throws ParseException 格式化错误
	 */
	public static Date parse(String date, String pattern) throws ParseException
	{
		FastDateFormat format = FastDateFormat.getInstance(pattern);// 优先从全局缓存中取，没有才会new
		return format.parse(date);
	}

	/**
	 * 解析string为date: yyyy-mm-dd
	 *
	 * @param date 日期字符串
	 * @return 日期对象
	 * @throws ParseException 格式化错误
	 */
	public static Date parse_Y4MD(String date) throws ParseException
	{
		FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
		return format.parse(date);
	}

	/**
	 * 解析string为date: yyyy-mm-dd hh:ii:ss
	 *
	 * @param date 日期字符串
	 * @return 日期对象
	 * @throws ParseException 格式化错误
	 */
	public static Date parse_Y4MDHIS(String date) throws ParseException
	{
		FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
		return format.parse(date);
	}

	/**
	 * 解析string为date: hh:ii
	 *
	 * @param date 日期字符串
	 * @return 日期对象
	 * @throws ParseException 格式化错误
	 */
	public static Date parse_HI(String date) throws ParseException
	{
		FastDateFormat format = FastDateFormat.getInstance("mm:ss");
		return format.parse(date);
	}

	/**
	 * 日期克隆
	 *
	 * @param date Date
	 * @return Date
	 */
	public static Date cloneDate(Date date)
	{
		return (Date) (date.clone());
	}

	/**
	 * @return 当前年
	 */
	public static int getYear()
	{
		return (new GregorianCalendar()).get(Calendar.YEAR);
	}

	/**
	 * @return 当前月
	 */
	public static int getMonth()
	{
		return (new GregorianCalendar()).get(Calendar.MONTH) + 1;
	}

	/**
	 * @return 当前日
	 */
	public static int getDay()
	{
		return (new GregorianCalendar()).get(Calendar.DATE);
	}

	/**
	 * 获取指定日期的年
	 *
	 * @param date 日期
	 * @return 年
	 */
	public static int getYearOfDate(Date date)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 获取指定日期的月
	 *
	 * @param date 日期
	 * @return 月
	 */
	public static int getMonthOfDate(Date date)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * 获取指定日期的日
	 *
	 * @param date 日期
	 * @return 日
	 */
	public static int getDayOfDate(Date date)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.DATE);
	}

	/**
	 * 获取指定日期的星期
	 *
	 * @param date 日期
	 * @return 星期
	 */
	public static int getWeekOfDate(Date date)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK) - 1;
	}

	/**
	 * 某年某月周末天数
	 *
	 * @param year  年
	 * @param month 月
	 * @return 周末天数
	 */
	static public int getDefaultHolidays(int year, int month)
	{
		GregorianCalendar cal = new GregorianCalendar(year, month - 1, 1);
		int x = 0;
		for (int d = 0 ; ; d++)
		{
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY)
				x |= (1 << d);
			cal.add(Calendar.DAY_OF_YEAR, 1);
			if (cal.get(Calendar.MONTH) + 1 != month)
				break;
		}
		return x;
	}

	/**
	 * 是否是周末
	 *
	 * @param year  年
	 * @param month 月
	 * @param day   日
	 * @return 是否是周末
	 */
	static public boolean isDefaultHolidays(int year, int month, int day)
	{
		GregorianCalendar cal = new GregorianCalendar(year, month - 1, day);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY;
	}

	/**
	 * 向后或向前推多少天
	 *
	 * @param date 日期
	 * @param day  +n 表示向后推 -n 表示向前推
	 * @return 计算后的日期
	 */
	static public Date incDate(Date date, int day)
	{
		if (day == 0 || date == null)
			return date;
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, day);
		return cal.getTime();
	}

	/**
	 * 两个日期相差天数
	 *
	 * @param date1 日期1
	 * @param date2 日期2
	 * @return 相差天数
	 */
	static public int diffDate(Date date1, Date date2)
	{
		return (int) ((date1.getTime() - date2.getTime()) / (24 * 60 * 60 * 1000));
	}

	/**
	 * 两个日期相差秒数
	 *
	 * @param date1 日期1
	 * @param date2 日期2
	 * @return 相差秒数
	 */
	static public long diffSeconds(Date date1, Date date2)
	{
		return (date1.getTime() - date2.getTime()) / 1000l;
	}

	/**
	 * 日期格式化为英文字符串
	 *
	 * @param date 日期
	 * @return 英文日期字符串
	 */
	static public String dateToEnglishString(Date date)
	{
		return dateToEnglishString(date, true, '-', '-');
	}

	/**
	 * 日期格式化为英文字符串
	 *
	 * @param date       日期
	 * @param shortMonth 月份是否缩写
	 * @param deliMD     月与日之间的分隔符
	 * @param deliDY     日与年之间的分隔符
	 * @return 英文日期字符串
	 */
	static public String dateToEnglishString(Date date, boolean shortMonth, char deliMD, char deliDY)
	{
		if (date == null)
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int m = calendar.get(Calendar.MONTH);
		String month;
		if (shortMonth)
			month = "JanFebMarAprMayJunJulAugSepOctNovDec".substring(m * 3, m * 3 + 3);
		else
		{
			String emonth[] = new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
			month = emonth[m];
		}
		return month + deliMD + calendar.get(Calendar.DATE) + deliDY + calendar.get(Calendar.YEAR);
	}

	/**
	 * 获取当前开始时间
	 *
	 * @return 当前开始时间
	 */
	public static Date getCurDayDate()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取指定时间的日期（去掉时分秒毫秒）
	 *
	 * @param date 日期
	 * @return 日期的当天起始时间
	 */
	public static Date getDayDate(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取当月第一天 00:00:00
	 *
	 * @return 当月第一天
	 */
	public static Date getFirstDayOfMonth()
	{
		//获取前月的第一天
		Calendar cal = Calendar.getInstance();//获取当前日期
		cal.add(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 获取当月最后一天 23:59:59
	 *
	 * @return 当月最后一天
	 */
	public static Date getLastDayOfMonth()
	{
		//获取前月的最后一天
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal.getTime();
	}

	/**
	 * 获取本周第一天
	 *
	 * @return 本周第一天
	 */
	public static Date getWeekStartDate()
	{
		Calendar cal = Calendar.getInstance();
		int d = 0;
		if (cal.get(Calendar.DAY_OF_WEEK) == 1)
		{
			d = -6;
		} else
		{
			d = 2 - cal.get(Calendar.DAY_OF_WEEK);
		}
		cal.add(Calendar.DAY_OF_WEEK, d);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 获取本周最后一天
	 *
	 * @return 本周最后一天
	 */
	public static Date getWeekEndDate()
	{
		Date weekStart = getWeekStartDate();
		return new Date(weekStart.getTime() + 7 * 24 * 60 * 60 * 1000 - 1);
	}

	/**
	 * 获取当天结束时间
	 *
	 * @return 当天结束时间
	 */
	public static Date getCurDayEnd()
	{
		Date begin = getCurDayDate();
		return new Date(begin.getTime() + 24 * 60 * 60 * 1000 - 1);
	}
}
