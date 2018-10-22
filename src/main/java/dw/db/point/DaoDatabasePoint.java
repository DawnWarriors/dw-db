package dw.db.point;

import dw.db.Database;
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
		boolean isCreateNewTrans = false;
		TransactionManager transactionManager = new TransactionManager();
		if (args[0] == null)
		{
			Database db = transactionManager.getCurrentDatabase();
			//service未传递session，则新启session，并一定会开启事务
			if (db == null)
			{
				transactionManager.createNewDatabase(true);
				db = transactionManager.getCurrentDatabase();
			}
			args[0] = db;
		}
		Object result = null;
		boolean isNeedRollback = true;
		try
		{
			result = point.proceed(args);
			isNeedRollback = false;
		} catch (Throwable throwable)
		{
			throw throwable;
		} finally
		{
			if (isCreateNewTrans)
			{
				transactionManager.removeCurrentDatabase(isNeedRollback);
			}
		}
		return result;
	}
}
