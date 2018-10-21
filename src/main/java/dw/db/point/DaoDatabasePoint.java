package dw.db.point;

import dw.db.annotation.DaoAutoDatabase;
import dw.db.trans.TransactionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class DaoDatabasePoint
{
	@Pointcut("@within(dw.db.annotation.DwDao) && @annotation(dw.db.annotation.DaoAutoDatabase) && execution(* *(dw.db.Database,..))")
	public void aspect()
	{
	}

	@Around("aspect()")
	public Object doAround(ProceedingJoinPoint point) throws Throwable
	{
		Object[] args = point.getArgs();
		Method m = ((MethodSignature) point.getSignature()).getMethod();
		DaoAutoDatabase daoAutoDatabase = m.getAnnotation(DaoAutoDatabase.class);
		//已有DB不再处理
		if(args[0]!=null)
		{
			try
			{
				return point.proceed();
			} catch (Throwable throwable)
			{
				throwable.printStackTrace();
			}
			return null;
		}
		//注入DB
		Object result = null;
		try
		{
			args[0] = new TransactionManager().getCurrentDatabase();
			result = point.proceed(args);
		} catch (Throwable throwable)
		{
			throw throwable;
		}
		return result;
	}
}
