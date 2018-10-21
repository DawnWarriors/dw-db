package dw.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DwDbFld
{
	/**
	 * 字段名称
	 * @return
	 */
	public String fldName() default "";

	/**
	 * 字段名是否转换为小写
	 * @return
	 */
	public boolean isLowerCase() default false;

	/**
	 * 是否是数据库字段
	 * @return
	 */
	public boolean isFld() default true;

	/**
	 * 是否是主键
	 * @return
	 */
	public boolean isKey() default false;

	/**
	 * 子表类路径
	 * 当此值不为空时，默认该字段不为数据库字段
	 * 注意！！ 字段类型必须为List
	 * @return
	 */
	public String childTblClsPath() default "";

	/**
	 * 子表数据加载过滤条件
	 * 书写格式例子：col1=:fldName1 and col2='10'
	 * 说明：col1、col2都是子表数据库字段名，fldName是当前类的属性名
	 * @return
	 */
	public String childTblLoadFilter() default "";
}
