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
     * Database 参数索引,用于适配多库操作
     * @return
     */
    int value() default 0;

    /**
     * 是否需要事务
     * @return
     */
    boolean isNeedTrans() default false;

    /**
     * 是否新开事务
     */
    boolean isNeedNewDbSession() default false;
}
