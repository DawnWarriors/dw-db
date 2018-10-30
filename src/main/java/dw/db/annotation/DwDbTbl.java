package dw.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DwDbTbl
{
	/**
	 * @return 表名
	 */
	public String tblName() default "";

	/**
	 * @return 字段名是否统一转换为小写
	 */
	public boolean isAllFldLowerCase() default false;
}
