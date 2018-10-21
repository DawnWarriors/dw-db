package dw.db.base;

import dw.common.util.str.StrUtil;
import dw.db.Database;
import dw.db.page.model.PageInfo;
import dw.db.page.util.PageUtil;
import dw.db.trans.TransactionManager;
import dw.db.util.DBModelUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xins_cyf on 2017/3/11.
 */
public class DaoBase
{
	protected Class  modelClass = null;
	protected String tblName    = null;

	public DaoBase(String tblName, Class modelClass)
	{
		this.tblName = tblName;
		this.modelClass = modelClass;
	}

	/**
	 * 根据ID获取对象
	 * 该查询将忽略刪除标志
	 *
	 * @param id
	 * @param <T>
	 * @return
	 */
	public <T> T getById(String id)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		paramGetter.put("id", id);
		return _getObjectIgnoreDelete("id=:id", paramGetter);
	}

	/**
	 * 锁行更新
	 *
	 * @param id
	 * @param <T>
	 * @return
	 */
	public <T> T getById_updateLock(String id)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		paramGetter.put("id", id);
		Database db = new TransactionManager().getCurrentDatabase();
		T t = (T) DBModelUtil.queryObject(db, modelClass, "id=:id", paramGetter);
		return t;
	}

	/**
	 * 根据查询条件获取一个对象
	 *
	 * @param filter
	 * @param paramGetter
	 * @param <T>
	 * @return
	 */
	public <T> T get(String filter, Map<String,Object> paramGetter)
	{
		return _getObject(filter, paramGetter);
	}

	/**
	 * 根据查询条件获取一个对象
	 *
	 * @param keys
	 * @param vals
	 * @param <T>
	 * @return
	 */
	public <T> T get(String keys[], Object vals[])
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getFilter(keys, vals, paramGetter);
		return get(filter, paramGetter);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param filter
	 * @param paramGetter
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getList(String filter, Map<String,Object> paramGetter)
	{
		return _getObjects(filter, paramGetter);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param keys
	 * @param vals
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getList(String keys[], Object vals[])
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getFilter(keys, vals, paramGetter);
		return getList(filter, paramGetter);
	}

	/**
	 * 查询全部，无过滤条件
	 *
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getList()
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = "";
		return getList(filter, paramGetter);
	}

	/**
	 * 查询列表
	 *
	 * @param filter
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getListByFilter(String filter)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		return getList(filter, paramGetter);
	}

	/**
	 * 查询全部，无过滤条件
	 *
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getList(String orderBy)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = "";
		return getList(filter, paramGetter, orderBy);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param filter
	 * @param paramGetter
	 * @param orderBy
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getList(String filter, Map<String,Object> paramGetter, String orderBy)
	{
		//        return (List<T>) DBModelUtil.queryObjects(db, modelClass, deletedDataFilter(filter), paramGetter, orderBy);
		return _getObjects(filter, paramGetter, orderBy);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param keys
	 * @param vals
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getListFromCache(String keys[], Object vals[], String orderBy)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getFilter(keys, vals, paramGetter);
		return getList(filter, paramGetter, orderBy);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param filter
	 * @param paramGetter
	 * @param orderBy
	 * @param pageNum
	 * @param pageSize
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getList(String filter, Map<String,Object> paramGetter, String orderBy, int pageNum, int pageSize)
	{
		return _getObjects(filter, paramGetter, orderBy, pageNum, pageSize);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param keys
	 * @param vals
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getList(String keys[], Object vals[], String orderBy, int pageNum, int pageSize)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getFilter(keys, vals, paramGetter);
		return getList(filter, paramGetter, orderBy, pageNum, pageSize);
	}

	/**
	 * 获取分页信息
	 *
	 * @param filter
	 * @param paramGetter
	 * @return
	 */
	public PageInfo getPageInfo(String filter, Map<String,Object> paramGetter, int pageNum, int pageSize)
	{
		Database db = new TransactionManager().getCurrentDatabase();
		PageInfo pageInfo = PageUtil.getPageInfo(db, "select id from " + this.tblName, deletedDataFilter(filter), paramGetter, pageNum, pageSize);
		return pageInfo;
	}

	/**
	 * 获取数据总条数
	 *
	 * @param filter
	 * @return
	 */
	public int getCount(String filter, Map<String,Object> paramGetter)
	{
		PageInfo pageInfo = getPageInfo(filter, paramGetter, 1, 1);
		return pageInfo.getDataCount();
	}

	/**
	 * 拼接默认过滤条件，用于直接用sql的执行
	 *
	 * @param filter
	 * @return
	 */
	public String addDefaultFilter(String filter)
	{
		return deletedDataFilter(filter);
	}

	/**
	 * 获取模糊查询查询条件
	 *
	 * @param key
	 * @param value
	 * @param paramGetter
	 * @return
	 */
	protected String getLikeFilter(String key, String value, Map<String,Object> paramGetter)
	{
		String filter = key + " like :" + key;
		paramGetter.put(key, "%" + value + "%");
		return filter;
	}

	/**
	 * 获取右匹配模糊查询查询条件
	 *
	 * @param key
	 * @param value
	 * @param paramGetter
	 * @return
	 */
	protected String getLeftLikeFilter(String key, String value, Map<String,Object> paramGetter)
	{
		String filter = key + " like :" + key;
		paramGetter.put(key, "%" + value);
		return filter;
	}

	/**
	 * 获取左匹配模糊查询查询条件
	 *
	 * @param key
	 * @param value
	 * @param paramGetter
	 * @return
	 */
	protected String getRightLikeFilter(String key, String value, Map<String,Object> paramGetter)
	{
		String filter = key + " like :" + key;
		paramGetter.put(key, value + "%");
		return filter;
	}

	/**
	 * 添加删除数据过滤
	 *
	 * @param filter
	 */
	private String deletedDataFilter(String filter)
	{
		if (StrUtil.isNotStrTrimNull(filter))
		{
			filter = "( " + filter + " )" + " and (_delete_flag is null or _delete_flag != 1)";
		} else
		{
			filter = "_delete_flag is null or _delete_flag != 1";
		}
		return filter;
	}

	/**
	 * 获取查询条件
	 *
	 * @param keys
	 * @param vals
	 * @param paramGetter
	 * @return
	 */
	private String getFilter(String keys[], Object vals[], Map<String,Object> paramGetter)
	{
		String filter = "";
		for (int i = 0 ; i < keys.length ; i++)
		{
			if (i > 0)
			{
				filter += " and ";
			}
			filter += keys[i] + "=:" + keys[i];
			paramGetter.put(keys[i], vals[i]);
		}
		return filter;
	}

	/**
	 * 查询忽略刪除标志
	 *
	 * @param filter
	 * @param paramGetter
	 * @param <T>
	 * @return
	 */
	private <T> T _getObjectIgnoreDelete(String filter, Map<String,Object> paramGetter)
	{
		Database db = new TransactionManager().getCurrentDatabase();
		T t = (T) DBModelUtil.queryObject(db, modelClass, filter, paramGetter);
		return t;
	}

	private <T> T _getObject(String filter, Map<String,Object> paramGetter)
	{
		Database db = new TransactionManager().getCurrentDatabase();
		T t = (T) DBModelUtil.queryObject(db, modelClass, deletedDataFilter(filter), paramGetter);
		return t;
	}

	public <T> List<T> _getObjects(String filter, Map<String,Object> paramGetter)
	{
		Database db = new TransactionManager().getCurrentDatabase();
		List<T> list = (List<T>) DBModelUtil.queryObjects(db, modelClass, deletedDataFilter(filter), paramGetter);
		return list;
	}

	private <T> List<T> _getObjects(String filter, Map<String,Object> paramGetter, String orderBy)
	{
		Database db = new TransactionManager().getCurrentDatabase();
		List<T> list = (List<T>) DBModelUtil.queryObjects(db, modelClass, deletedDataFilter(filter), paramGetter, orderBy);
		return list;
	}

	private <T> List<T> _getObjects(String filter, Map<String,Object> paramGetter, String orderBy, int pageNum, int pageSize)
	{
		Database db = new TransactionManager().getCurrentDatabase();
		List<T> list = (List<T>) DBModelUtil.queryObjects(db, modelClass, deletedDataFilter(filter), paramGetter, pageNum, pageSize, orderBy);
		return list;
	}
}
