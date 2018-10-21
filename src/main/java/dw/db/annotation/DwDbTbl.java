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
	 * 表名
	 * @return
	 */
	public String tblName() default "";

	/**
	 * 字段名是否统一转换为小写
	 * @return
	 */
	public boolean isAllFldLowerCase() default false;
}
