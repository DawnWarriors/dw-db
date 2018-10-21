package dw.db.point;

import dw.db.annotation.ServiceAutoTrans;
import dw.db.trans.TransactionManager;
import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.Method;

/**
 * Created by xins_cyf on 2017/3/12.
 */
@Aspect
@Component
public class ServiceDatabasePoint
{
		@Pointcut("@within(org.springframework.stereotype.Service) && @annotation(dw.db.annotation.ServiceAutoTrans)")
		public void aspect()
		{
		}

		@Around("aspect()")
		public Object doAround(ProceedingJoinPoint point) throws Throwable
		{
			Object[] args = point.getArgs();
			Method m = ((MethodSignature) point.getSignature()).getMethod();
			ServiceAutoTrans autoTrans = m.getAnnotation(ServiceAutoTrans.class);
			//TODO 多库处理方案
			boolean isNeedTrans = autoTrans.isNeedTrans();
			boolean isNeedNewDbSession = autoTrans.isNeedNewDbSession();
			TransactionManager transactionManager = new TransactionManager();
			if(isNeedNewDbSession)
			{
				transactionManager.createNewDatabase(isNeedTrans);
			}
			boolean isNeedRollback = true;
			Object result = null;
			try
			{
				result = point.proceed(args);
				isNeedRollback = false;
			} catch (Throwable throwable)
			{
				throw throwable;
			} finally
			{
				if (isNeedNewDbSession)
				{
					transactionManager.removeCurrentDatabase(isNeedRollback);
				}
			}
			return result;
		}
}

