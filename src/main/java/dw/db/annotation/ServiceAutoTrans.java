package dw.db.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ServiceAutoTrans
{
	/**
	 * TODO
	 *
	 * @return Database 参数索引,用于适配多库操作
	 */
	int value() default 0;

	/**
	 * @return 是否需要事务
	 */
	boolean isNeedTrans() default false;

	/**
	 * @return 是否新开事务
	 */
	boolean isNeedNewDbSession() default false;
}
