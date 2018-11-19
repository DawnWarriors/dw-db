package dw.common.util.num;

import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;

/**
 * 数学运算工具类
 * 注：所有double型的做运算必须调用此工具类
 * 
 * @author leo
 * 
 */
public final class MathUtils extends NumberUtils {
	
	private MathUtils() {}

	/**
	 * 加法v1+v2
	 * @param v1 double
	 * @param v2 double
	 * @return v1+v2
	 */
	public static double add(double v1, double v2) {
		BigDecimal b1 = toBigDecimal(v1);
		BigDecimal b2 = toBigDecimal(v2);
		return b1.add(b2).doubleValue();
	}
	
	
	/**
	 * 减法v1-v2
	 * @param v1 double
	 * @param v2 double
	 * @return v1-v2
	 */
	public static double subtract(double v1, double v2) {
		BigDecimal b1 = toBigDecimal(v1);
		BigDecimal b2 = toBigDecimal(v2);
		return b1.subtract(b2).doubleValue();
	}
	
	/**
	 * 乘法v1*v2
	 * @param v1 double
	 * @param v2 double
	 * @return v1*v2
	 */
	public static double multiply(double v1, double v2) {
		BigDecimal b1 = toBigDecimal(v1);
		BigDecimal b2 = toBigDecimal(v2);
		return b1.multiply(b2).doubleValue();
	}
	
	/**
	 * 乘法v1*v2
	 * @param v1 double
	 * @param v2 double
	 * @param scale 保留小数点后几位小数
	 * @return v1*v2
	 */
	public static double multiply(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b1 = toBigDecimal(v1);
		BigDecimal b2 = toBigDecimal(v2);
		return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	 * 除法 v1/v2
	 * 如果结果出现无限循环小数会产生异常
	 * @param v1 double
	 * @param v2 double 不能为0
	 * @return v1/v2
	 */
	public static double divide(double v1, double v2) {
		BigDecimal b1 = toBigDecimal(v1);
		BigDecimal b2 = toBigDecimal(v2);
		return b1.divide(b2).doubleValue();
	}
	
	/**
	 * 除法 v1/v2
	 * 
	 * @param v1 double
	 * @param v2 double 不能为0
	 * @param scale 保留小数点后几位小数
	 * @return v1/v2
	 */
	public static double divide(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b1 = toBigDecimal(v1);
		BigDecimal b2 = toBigDecimal(v2);
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 四舍五入
	 * 
	 * @param v double
	 * @param scale 保留小数点后几位小数
	 * @return 四舍五入
	 */
	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b = toBigDecimal(v);
		return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	private static BigDecimal toBigDecimal(double d){
		return new BigDecimal(Double.toString(d));
	}

}
