package dw.db.annotation;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DwDbSub
{
	/**
	 * 是否懒加载 默认false,应用于级联查询
	 *
	 * @return 懒加载标志
	 */
	public boolean lazy() default false;

	/**
	 * 子表加载SQL
	 *
	 * @return 子表加载SQL
	 */
	public String loadSql() default "";

	/**
	 * 书写格式例子：col2=\"10\" and col3=20
	 * 其中：col2、col3为子表中的列
	 * @return 子表数据加载过滤条件
	 */
	public String loadFilter() default "";

	/**
	 * 子表与当前表的关联设置，如果是1对多的情况，将会有助于提高在主表批量查询时子表的查询效率
	 *
	 * 注意：unions会作为过滤条件处理，过滤条件与loadFilter以及loadSql中的条件部分关系为“and”
	 *
	 *
	 * 如果没有为子表列设置对应的主表列，则默认对应为主表ID
	 *
	 *  @return 子表与当前表的关联设置
	 */
	//书写格式：childCol1:masterCol1 & childCol2:masterCol2 & childCol3
	public String unions() default "";

	/**
	 * 排序方式,例如： code，code desc
	 *
	 * @return 排序方式
	 */
	public String ordBy() default "";
}
