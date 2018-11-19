package dw.common.util.type;

import dw.common.util.str.StrUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by xins_cyf on 2017/8/4.
 */
public class TypeUtil
{
	/**
	 * 数据类型转换
	 *
	 * @param srcStr   String
	 * @param typeName 类型名
	 * @return 转换后的数据对象
	 * @throws ParseException 格式化异常
	 */
	public static Object typeConversion(String srcStr, String typeName) throws ParseException
	{
		if (srcStr == null)
		{
			return null;
		}
		if (StrUtil.isStrTrimNull(typeName))
		{
			return srcStr;
		}
		switch (typeName)
		{
		case "java.lang.Integer":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Integer.parseInt(srcStr);
			}
		case "java.lang.Double":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Double.parseDouble(srcStr);
			}
		case "java.lang.Float":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Float.parseFloat(srcStr);
			}
		case "java.lang.Long":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Long.parseLong(srcStr);
			}
		case "java.lang.Short":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Short.parseShort(srcStr);
			}
		case "java.lang.Byte":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Byte.parseByte(srcStr);
			}
		case "java.lang.Boolean":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return false;
			} else
			{
				return Boolean.parseBoolean(srcStr);
			}
		case "java.lang.Character":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return null;
			} else
			{
				return srcStr.charAt(0);
			}
		case "java.lang.String":
			return srcStr;
		case "java.math.BigDecimal":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return new BigDecimal(0);
			} else
			{
				return new BigDecimal(srcStr);
			}
		case "java.util.Date":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return null;
			} else
			{
				String dateExp = "";
				if (srcStr.matches("\\d{4}-\\d{2}-\\d{2}"))
				{
					dateExp = "yyyy-MM-dd";
				} else if (srcStr.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}"))
				{
					dateExp = "yyyy-MM-dd HH:mm:ss";
				} else if (srcStr.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}"))
				{
					dateExp = "yyyy-MM-dd HH:mm";
				} else if (srcStr.matches("\\d{2}-\\d{2}-\\d{2}"))
				{
					dateExp = "yy-MM-dd";
				} else if (srcStr.matches("\\d{2}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}"))
				{
					dateExp = "yy-MM-dd HH:mm:ss";
				} else if (srcStr.matches("\\d{2}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}"))
				{
					dateExp = "yy-MM-dd HH:mm";
				} else
				{
					//throw new RuntimeException("不支持的日期格式：" + srcStr + "\n");
					return null;
				}
				SimpleDateFormat dateFormat = new SimpleDateFormat(dateExp);
				return dateFormat.parse(srcStr);
			}
		case "int":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Integer.parseInt(srcStr);
			}
		case "double":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Double.parseDouble(srcStr);
			}
		case "float":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Float.parseFloat(srcStr);
			}
		case "long":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Long.parseLong(srcStr);
			}
		case "short":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Short.parseShort(srcStr);
			}
		case "byte":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return 0;
			} else
			{
				return Byte.parseByte(srcStr);
			}
		case "boolean":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return false;
			} else
			{
				return Boolean.parseBoolean(srcStr);
			}
		case "char":
			if (StrUtil.isStrTrimNull(srcStr))
			{
				return null;
			} else
			{
				return srcStr.charAt(0);
			}
		default:
			return null;
		}
	}

	/**
	 * 数据类型转换
	 *
	 * @param obj    Object
	 * @param toType Class
	 * @return 转换结果
	 * @throws ParseException 格式化异常
	 */
	public static Object typeConversion(Object obj, Class<?> toType) throws ParseException
	{
		if (obj == null)
		{
			return obj;
		}
		if (toType.isInstance(obj))
		{
			return obj;
		}
		String toTypeClassName = toType.getName();
		return typeConversion(obj.toString(), toTypeClassName);
	}
}
