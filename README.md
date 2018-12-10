# dw-db
Dw-db是一款基于Druid实现的ORM框架，结合spring-boot2实现了数据库操作的全面代理。通过对以往工作经验的总结，以及对Ruby语言风格的借鉴，最终实现了一种不同以往的轻量级数据库操作框架。
该框架的设计初衷，是为了更方便更简洁地进行程序开发，尽量简化DAO层和Model层在程序开发时的操作，之后将持续维护和更新。欢迎进行测试、使用、提BUG，也欢迎志同道合的朋友加入。

# 功能简介

## 1.数据表自动生成和结构更新
## 2.DAO和POJO代码自动生成
## 3.事务管理
## 4.数据持久化

# 使用示例
### 1.新建Spring-boot2测试工程，在POM文件中引入dw-db
```
<dependency>
    <groupId>com.dawnwarriors</groupId>
    <artifactId>dw-db</artifactId>
    <version>1.0.3</version>
</dependency>
```
### 2.创建新的数据库，并在resources/dw文件夹下编写druid.properties配置文件
### 3.编写数据库脚本文件
### 4.编写Model和Dao生成包路径配置文件
### 5.启动项目

    即可看到数据库中表已经自动生成，且工程目录中对应的model和dao已经生成

### 6.Service 示例

```java
@Service
public class StudentService
{
    @ServiceAutoTrans(isNeedNewDbSession = true, isNeedTrans = true)
    public void addStudent(String username, String no)
    {
        StudentModel studentModel = new StudentModel();
        studentModel.setId(DBIDUtil.createUUId());
        studentModel.setUsername(username);
        studentModel.setNo(no);
        studentModel.insert();
    }
	
    @ServiceAutoTrans(isNeedNewDbSession = true)
    public List<StudentModel> getAllStudent()
    {
        return studentDao.getList();
    }

    @ServiceAutoTrans(isNeedNewDbSession = true)
    public StudentModel getStudentByName(String userName, String no)
    {
        return studentDao.get(new String[]{"username","no"},new Object[]{userName,no});
    }
}
```

# 其他功能

## 1.分页

 > 分页对象
    
```java
@Data
public class PageInfo
{
	private String	url_first			= "";		//首页链接
	private String	url_last			= "";		//末页链接
	private String	url_pre				= "";		//上一页
	private String	url_next			= "";		//下一页
	private String	url_size_10			= "";		//调整分页大小-10
	private String	url_size_20			= "";		//调整分页大小-20
	private String	url_size_30			= "";		//调整分页大小-30
	private String	url_size_50			= "";		//调整分页大小-50
	private String	url_num				= "";		//根据页面访问链接前缀，后边拼上页码即可
	
	public void setUrlParams(String baseUrl, String ortherParamStr){}
}
```
## 2.DAO层扩展
    
    通过Database对象，可以直接执行SQL语句，或者其他不需要得到Model形式结果的操作；
    
> 扩展案例
    
```java
@Component
public class SysUserDao extends DaoBase
{
	public SysUserDao()
	{
		super("sys_user",SysUserModel.class);
	}

	public void test()
    {
        Database database = TransactionManager.getCurrentDBSession();
        database.execute("select count(1) from student");
    }
}
```

# 应用扩展

    "dw_tbl_def" ：数据表定义表
    "dw_tbl_def_temp" ：数据表定义临时表
    "dw_fld_def" ：字段定义表
    "dw_fld_def_temp" ：字段定义临时表

    目前数据库的生成和更新主要依靠这四张表去完成，所有的更改都在临时表中，定义表中为实际结构；
    
    表维护程序通过临时表和定义表的对照来完成数据库表结构的维护；
    
    所以使用者可以实现数据库表结构的在线管理；
    
    但是如果使用者接管了表结构的管理，需要注意及时更新表结构定义文件；

# 待完善功能

#### 1.支持多库操作

#### 2.优化表结构文件内容结构

#### 3.数据库索引定义的支持

#### 4.数据库视图定义的支持

# 版本历史

#### 版本号约定：三位数字的为开发版本，两位数字的是发布版本，两位数字以奇数结尾的为公测发布版本，两位数且以偶数结尾的是稳定发布版本

#### 1.0.0 版本：完成基础功能发布
#### 1.0.1 版本：加入Service事务管理，完善代码生成内容
#### 1.0.2 版本：修复部分BUG
#### 1.0.3 版本：使用GCLib动态代理，完成级联查询和懒加载功能
