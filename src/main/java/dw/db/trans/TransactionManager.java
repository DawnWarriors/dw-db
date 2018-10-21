package dw.db.trans;

import dw.db.Database;
import dw.db.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

public class TransactionManager
{
	private static ThreadLocal<List<Database>> databaseListThreadLocal = new ThreadLocal<>();
	private static ThreadLocal<List<Boolean>>  isNeedTransListThreadLocal = new ThreadLocal<>();
	private static ThreadLocal<Integer> indexThreadLocal = new ThreadLocal<>();

	public void createNewDatabase(boolean isNeedTrans)
	{
		initLocalThread();
		addIndex();
		Database db = DatabaseManager.createDatabase();
		List<Database> databaseList = databaseListThreadLocal.get();
		List<Boolean> isNeedTransList = isNeedTransListThreadLocal.get();
		isNeedTransList.add(isNeedTrans);
		databaseList.add(db);
		if (isNeedTrans)
		{
			db.beginTrans();
		}
	}

	public Database getCurrentDatabase()
	{
		int index = getIndex();
		if (index == -1)
		{
			throw new RuntimeException("当前线程无可用DB");
		}
		List<Database> databaseList = databaseListThreadLocal.get();
		return databaseList.get(index);
	}

	public void removeCurrentDatabase(boolean isNeedRollback)
	{
		int index = getIndex();
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

	private int addIndex()
	{
		int index = indexThreadLocal.get();
		index++;
		indexThreadLocal.set(index);
		return index;
	}

	private int subIndex()
	{
		int index = indexThreadLocal.get();
		index--;
		indexThreadLocal.set(index);
		return index;
	}

	private int getIndex()
	{
		return indexThreadLocal.get();
	}

	private void initLocalThread()
	{
		if (indexThreadLocal.get()==null)
		{
			databaseListThreadLocal.set(new ArrayList<Database>());
			isNeedTransListThreadLocal.set(new ArrayList<Boolean>());
			indexThreadLocal.set(-1);
		}
	}
}
