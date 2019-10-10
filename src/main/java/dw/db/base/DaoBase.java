package dw.db.base;

import dw.common.util.str.StrUtil;
import dw.db.sql.SqlFilterUtil;
import dw.db.trans.Database;
import dw.db.page.model.PageInfo;
import dw.db.page.util.PageUtil;
import dw.db.trans.TransactionManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xins_cyf on 2017/3/11.
 */
public abstract class DaoBase<T extends ModelBase>
{
	protected Class<T> modelClass = null;
	protected String   tblName    = null;

	protected DaoBase(String tblName, Class<T> modelClass)
	{
		this.tblName = tblName;
		this.modelClass = modelClass;
	}

	/**
	 * 根据ID获取对象该查询将忽略刪除标志
	 *
	 * @param id 数据ID值
	 * @return 结果对象
	 */
	public T getById(String id)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		paramGetter.put("id", id);
		return _getObjectIgnoreDelete("id=:id", paramGetter);
	}

	/**
	 * 更新锁查询
	 *
	 * @param id 数据ID值
	 * @return 结果对象
	 */
	public T getById_updateLock(String id)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		paramGetter.put("id", id);
		Database db = TransactionManager.getCurrentDBSession();
		T t = (T) ObjectProxyFactory.queryObject(db, modelClass, "id=:id", paramGetter, true);
		return t;
	}

	/**
	 * 根据查询条件获取一个对象
	 *
	 * @param filter      过滤条件
	 * @param paramGetter 条件参数Map
	 * @return 结果对象，如果结果不唯一，则返回列表第一条
	 */
	public T get(String filter, Map<String,Object> paramGetter)
	{
		return _getObject(filter, paramGetter);
	}

	/**
	 * 根据查询条件获取对象
	 *
	 * @param keys 过滤条件列名数组
	 * @param vals 与列名数组对应的过滤条件值
	 * @return 结果对象，如果结果不唯一，则返回列表第一条
	 */
	public T get(String keys[], Object vals[])
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getFilter(keys, vals, paramGetter);
		return get(filter, paramGetter);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param filter      过滤条件
	 * @param paramGetter 条件参数Map
	 * @return 结果对象列表
	 */
	public List<T> getList(String filter, Map<String,Object> paramGetter)
	{
		return _getObjects(filter, paramGetter);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param keys 过滤条件列名数组
	 * @param vals 与列名数组对应的过滤条件值
	 * @return 结果对象列表
	 */
	public List<T> getList(String keys[], Object vals[])
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getFilter(keys, vals, paramGetter);
		return getList(filter, paramGetter);
	}

	/**
	 * 根据无参条件查询列表
	 *
	 * @param filter 拼装的过滤条件
	 * @return 结果对象列表
	 */
	public List<T> getListByFilter(String filter)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		return getList(filter, paramGetter);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param filter      过滤条件
	 * @param paramGetter 条件参数Map
	 * @param orderBy     排序字段和排序方式
	 * @return 经过排序的结果对象列表
	 */
	public List<T> getList(String filter, Map<String,Object> paramGetter, String orderBy)
	{
		return _getObjects(filter, paramGetter, orderBy);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param keys    过滤条件列名数组
	 * @param vals    与列名数组对应的过滤条件值
	 * @param orderBy 排序字段和排序方式
	 * @return 经过排序的结果对象列表
	 */
	public List<T> getList(String keys[], Object vals[], String orderBy)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getFilter(keys, vals, paramGetter);
		return _getObjects(filter, paramGetter, orderBy);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param filter      过滤条件
	 * @param paramGetter 条件参数Map
	 * @param orderBy     排序字段和排序方式
	 * @param pageNum     页码
	 * @param pageSize    分页大小
	 * @return 经过排序和分页的结果列表
	 */
	public List<T> getList(String filter, Map<String,Object> paramGetter, String orderBy, int pageNum, int pageSize)
	{
		return _getObjects(filter, paramGetter, orderBy, pageNum, pageSize);
	}

	/**
	 * 根据查询条件获取对象列表
	 *
	 * @param keys     过滤条件列名数组
	 * @param vals     与列名数组对应的过滤条件值
	 * @param orderBy  排序字段和排序方式
	 * @param pageNum  页码
	 * @param pageSize 分页大小
	 * @return 经过排序和分页的结果列表
	 */
	public List<T> getList(String keys[], Object vals[], String orderBy, int pageNum, int pageSize)
	{
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = getFilter(keys, vals, paramGetter);
		return getList(filter, paramGetter, orderBy, pageNum, pageSize);
	}

	/**
	 * 获取分页信息
	 *
	 * @param filter      过滤条件
	 * @param paramGetter 条件参数Map
	 * @param pageNum     页码
	 * @param pageSize    分页大小
	 * @return 分页对象PageInfo
	 */
	public PageInfo getPageInfo(String filter, Map<String,Object> paramGetter, int pageNum, int pageSize)
	{
		Database db = TransactionManager.getCurrentDBSession();
		PageInfo pageInfo = PageUtil.getPageInfo(db, "select id from " + this.tblName, deletedDataFilter(filter), paramGetter, pageNum, pageSize);
		return pageInfo;
	}

	/**
	 * 获取数据总条数
	 *
	 * @param filter      过滤条件
	 * @param paramGetter 条件参数Map
	 * @return 总数据条数
	 */
	public int getCount(String filter, Map<String,Object> paramGetter)
	{
		PageInfo pageInfo = getPageInfo(filter, paramGetter, 1, 1);
		return pageInfo.getDataCount();
	}

	/**
	 * 拼接默认过滤条件，用于直接用sql的执行
	 *
	 * @param filter 原过滤条件
	 * @return 返回拼接了底层默认条件的新过滤条件，例如：逻辑删除过滤
	 */
	public String addDefaultFilter(String filter)
	{
		return deletedDataFilter(filter);
	}

	/**
	 * 获取模糊查询查询条件
	 *
	 * @param key         字段名
	 * @param value       字段值
	 * @param paramGetter 参数Map
	 * @return 查询条件
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
	 * @param key         字段名
	 * @param value       字段值
	 * @param paramGetter 参数Map
	 * @return 查询条件
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
	 * @param key         字段名
	 * @param value       字段值
	 * @param paramGetter 参数Map
	 * @return 查询条件
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
	 * @param filter 过滤条件
	 * @return 逻辑删除过滤
	 */
	private String deletedDataFilter(String filter)
	{
		if (StrUtil.isNotStrTrimNull(filter))
		{
			filter = "( " + filter + " )" + " and (__delete_flag is null or __delete_flag != 1)";
		} else
		{
			filter = "__delete_flag is null or __delete_flag != 1";
		}
		return filter;
	}

	/**
	 * 获取查询条件
	 *
	 * @param keys        字段名
	 * @param vals        字段值
	 * @param paramGetter 参数Map
	 * @return 查询条件
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
			Object value = vals[i];
			if (value.getClass().isArray())
			{
				if(((Object[]) value).length>0)
				{
					filter = SqlFilterUtil.getInFilter(keys[i], (Object[]) value, paramGetter);
				}else{
					filter += keys[i] + "=''";
				}
			} else if (value instanceof Collection)
			{
				if(((Collection<? extends Object>) value).size()>0)
				{
					filter = SqlFilterUtil.getInFilter(keys[i], (Collection<? extends Object>) value, paramGetter);
				}else{
					filter += keys[i] + "=''";
				}
			} else
			{
				filter += keys[i] + "=:" + keys[i];
				paramGetter.put(keys[i], vals[i]);
			}
		}
		return filter;
	}

	/**
	 * 查询忽略刪除标志
	 *
	 * @param filter      过滤条件
	 * @param paramGetter 参数Map
	 * @return Model对象
	 */
	private T _getObjectIgnoreDelete(String filter, Map<String,Object> paramGetter)
	{
		Database db = TransactionManager.getCurrentDBSession();
		T t = (T) ObjectProxyFactory.queryObject(db, modelClass, filter, paramGetter);
		return t;
	}

	private T _getObject(String filter, Map<String,Object> paramGetter)
	{
		Database db = TransactionManager.getCurrentDBSession();
		T t = (T) ObjectProxyFactory.queryObject(db, modelClass, deletedDataFilter(filter), paramGetter);
		return t;
	}

	public List<T> _getObjects(String filter, Map<String,Object> paramGetter)
	{
		Database db = TransactionManager.getCurrentDBSession();
		List<T> list = (List<T>) ObjectProxyFactory.queryObjects(db, modelClass, deletedDataFilter(filter), paramGetter);
		return list;
	}

	private List<T> _getObjects(String filter, Map<String,Object> paramGetter, String orderBy)
	{
		Database db = TransactionManager.getCurrentDBSession();
		List<T> list = (List<T>) ObjectProxyFactory.queryObjects(db, modelClass, deletedDataFilter(filter), paramGetter, orderBy);
		return list;
	}

	private List<T> _getObjects(String filter, Map<String,Object> paramGetter, String orderBy, int pageNum, int pageSize)
	{
		Database db = TransactionManager.getCurrentDBSession();
		List<T> list = (List<T>) ObjectProxyFactory.queryObjects(db, modelClass, deletedDataFilter(filter), paramGetter, pageNum, pageSize, orderBy);
		return list;
	}
}
