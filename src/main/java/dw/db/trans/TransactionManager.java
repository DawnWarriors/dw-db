package dw.db.trans;

import java.util.ArrayList;
import java.util.List;

public class TransactionManager
{
	private static ThreadLocal<List<Database>> databaseListThreadLocal    = new ThreadLocal<>();
	private static ThreadLocal<List<Boolean>>  isNeedTransListThreadLocal = new ThreadLocal<>();
	private static ThreadLocal<Integer>        indexThreadLocal           = new ThreadLocal<>();

	/**
	 * 为当前线程开启一个DB session，创建完成后将会执行进栈操作
	 *
	 * @param isNeedTrans 是否需要事务提交
	 */
	static void createNewDatabase(boolean isNeedTrans)
	{
		//这句首先执行，避免由于DB创建失败引起本地线程内容错乱
		Database db = DatabaseManager.createDatabase();
		initLocalThread();
		addIndex();
		List<Database> databaseList = databaseListThreadLocal.get();
		List<Boolean> isNeedTransList = isNeedTransListThreadLocal.get();
		isNeedTransList.add(isNeedTrans);
		databaseList.add(db);
		if (isNeedTrans)
		{
			db.beginTrans();
		}
	}

	/**
	 * 供使用者调用，来获取当前线程当前所处的DB session
	 *
	 * @return DB session
	 */
	public static Database getCurrentDBSession()
	{
		int index = getIndex();
		if (index == -1)
		{
			throw new RuntimeException("当前线程无可用DB");
		}
		List<Database> databaseList = databaseListThreadLocal.get();
		return databaseList.get(index);
	}

	/**
	 * 获取当前线程当前所处的DB session
	 *
	 * @return DB session
	 */
	static Database getCurrentDatabase()
	{
		int index = getIndex();
		if (index == -1)
		{
			return null;
		}
		List<Database> databaseList = databaseListThreadLocal.get();
		return databaseList.get(index);
	}

	/**
	 * 移除当前线程所处的DB session，移除之前将会做事务提交操作，移除执行出栈
	 *
	 * @param isNeedRollback 是否需要回滚事务
	 */
	static void removeCurrentDatabase(boolean isNeedRollback)
	{
		int index = getIndex();
		//数据库Session已关闭，避免创建时发生的异常，再次引发新的异常
		if(index==-1)
		{
			return;
		}
		Database db = getCurrentDatabase();
		List<Database> databaseList = databaseListThreadLocal.get();
		List<Boolean> isNeedTransList = isNeedTransListThreadLocal.get();
		Boolean isNeedTrans = isNeedTransList.get(index);
		if (isNeedTrans)
		{
			db.commitTrans(isNeedRollback);
		}
		DatabaseManager.closeDatabase(db);
		databaseList.remove(index);
		isNeedTransList.remove(index);
		index = subIndex();
		if (index == -1)
		{
			databaseListThreadLocal.remove();
			isNeedTransListThreadLocal.remove();
			indexThreadLocal.remove();
		}
	}

	private static int addIndex()
	{
		int index = indexThreadLocal.get();
		index++;
		indexThreadLocal.set(index);
		return index;
	}

	private static int subIndex()
	{
		int index = indexThreadLocal.get();
		index--;
		indexThreadLocal.set(index);
		return index;
	}

	private static int getIndex()
	{
		Integer index = indexThreadLocal.get();
		return index == null ? -1 : index;
	}

	private static void initLocalThread()
	{
		if (indexThreadLocal.get() == null)
		{
			databaseListThreadLocal.set(new ArrayList<Database>());
			isNeedTransListThreadLocal.set(new ArrayList<Boolean>());
			indexThreadLocal.set(-1);
		}
	}
}
