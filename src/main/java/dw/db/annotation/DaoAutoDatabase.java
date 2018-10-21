package dw.db.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
///是否需要事务由上层service决定,Dao本身不进行事务处理
public @interface DaoAutoDatabase
{
	/**
	 * TODO
	 * Database 参数索引,用于适配多库操作
	 *
	 * @return
	 */
	int value() default 0;
}
