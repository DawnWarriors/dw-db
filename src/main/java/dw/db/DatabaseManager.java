package dw.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager
{
	/**
	 * 后期可以修改为，通过配置文件中的 id 来获取指定的 DataSource，用于多个DataSource的情况
	 * 在登陆时，根据登陆情况（用户类型等），选择合适的id，获取对应的DS，存储于session中，或者与session id结合存储于缓存
	 */
	private static DruidDataSource	dataSource	= null;

	private static DruidDataSource getDataSource()
	{
		try
		{
			Properties prop = new Properties();
			String path = DatabaseManager.class.getClassLoader().getResource("").toURI().getPath();
			File file = new File(path, "dw/druid.properties");
			prop.load(new FileReader(file));
			DruidDataSource ds = (DruidDataSource) DruidDataSourceFactory.createDataSource(prop);
			return ds;
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * 创建DB实例
	 * @return
	 */
	public static synchronized Database createDatabase()
	{
		if (dataSource == null)
		{
			dataSource = getDataSource();
		}
		Database db = new Database();
		Connection con;
		try
		{
			con = dataSource.getConnection();
			String dbType = dataSource.getDbType();
			db.init(con, dataSource, dbType);
			return db;
		} catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * 关闭DB
	 * @param db
	 */
	public static void closeDatabase(Database db)
	{
		try
		{
			if (db != null && !db.isClosed())
			{
				db.close();
			}
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
