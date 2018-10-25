# dw-db
Dw-db是一款基于Druid实现的ORM框架，结合spring-boot2实现了数据库操作的全面代理。通过对以往工作经验的总结，以及对Ruby语言风格的借鉴，最终实现了一种不同以往的轻量级数据库操作框架。
该框架的设计初衷，是为了更方便更简洁地进行程序开发，尽量简化DAO层和Model层在程序开发时的操作，之后将持续维护和更新。欢迎进行测试、使用、提BUG，也欢迎志同道合的朋友加入。
# 功能简介

    所有的配置文件位置均在resources/dw/文件夹下，以下所有文件及目录均以该目录为根目录进行介绍

## 1.数据表自动生成和更新

    根据表结构定义文件，可以再第一次项目启动时自动建表，也可以在表结构文件变化时及时更新数据库中实际表结构

### 1.1 表结构定义文件

>dw_tbl_base.txt(内容示例) 
```
//dw_tbl_base.txt文件为表结构入口文件，该文件中可以引用多个表结构文件，方便进行业务层级的表拆分归集（例如按照：订单、用户、商品等拆分）

#include(db/user_info.txt)
#include(db/student.txt)
```

>db/user_info.txt（示例）
```
//表结构文件中可以包含多张表，支持“//”注释

#define IDSIZE 64

#tbldef(tblid,tblname,tblname_zh,cacheflags)
02010    sys_user                   系统管理员          0
02020    platform_member            平台会员            0

###fldattr （从右往左：不能为空，主键，唯一，……）
#flddef(tblname,fldid,fldname,fldname_zh,fldtype,fldlen,flddecimal,fldattr,flddefault)
#02010    sys_user            系统管理员
sys_user        10         id              ID            varchar        IDSIZE        0        000011
sys_user        20         account         帐号          varchar        32            0        000001
sys_user        30         password        密码          varchar        32            0        000000
sys_user        40         username        用户名称      varchar        32            0        000000

#02020    platform_member            平台会员
platform_member        10        id              ID            varchar        IDSIZE        0        000011
platform_member        20        username        账号          varchar        32            0        000000
platform_member        30        password        密码          varchar        32            0        000000
platform_member        50        state           状态          varchar        4             0        000000       10
platform_member        60        last_login_date 上次登陆时间  date           8             0        000000
platform_member        70        login_times     登陆次数      int            8             0        000000
platform_member        80        integral        积分          float          8             2        000000
platform_member        90        balance         余额          decimal        16            4        000000
```

>db/student.txt（示例）
```
#define IDSIZE 64

#tbldef(tblid,tblname,tblname_zh,cacheflags)
03020    student            学生            0

###fldattr （从右往左：不能为空，主键，唯一，……）
#flddef(tblname,fldid,fldname,fldname_zh,fldtype,fldlen,flddecimal,fldattr,flddefault)
#03020    student            学生
student        10         id              ID            varchar        IDSIZE        0        000011
student        20         no              学号          varchar        32            0        000001
student        30         username        姓名          varchar        32            0        000000
```

### 1.2 表结构文件从上到下的结构为：

>长度宏定义
```
    #define IDSIZE 64    
    //定义*IDSIZE*代表的长度为64，当然，宏定义可以放在入口文件中(位置应该在include区块之前)
```

>表信息定义
```
    #tbldef(tblid,tblname,tblname_zh,cacheflags)    
    //四列分别对应：表ID，表名，表中文名，是否是缓存表
    //是否是缓存表：当前未做实现，作为预留
```

>表字段定义
```
    #flddef(tblname,fldid,fldname,fldname_zh,fldtype,fldlen,flddecimal,fldattr,flddefault)
    //九列分别对应：表名，字段序号，字段名，字段中文名，字段类型，字段长度，小数位长度，字段属性，默认值
    //字段属性：共六位0/1表示（从右往左：不能为空，主键，唯一，……），前三位目前作为预留，后续更新将会用到
```

## 2.DAO层和Model层代码自动生成

    建议在项目中提供spring-boot环境配置以识别当前是否为开发环境(DEV)，如果没有配置该选项，则无法正确生成code文件

### 2.1 Dao和Model包路径配置文件

>dw_auto_code.yml

```
dwModelDirDef:          #model包路径
  com.system.model:
    - sys_user          #表名
    - platform_member
  com.student.model:
    - student

dwDaoDirDef:            #dao包路径
  com.system.dao:
    - sys_user          #表名
    - platform_member
  com.student.dao:
    - student
```

    开发环境项目启动时，会根据**dw_auto_code.yml**配置去生成model和dao代码java文件，为避免覆盖自定义属性和方法，所以如果某文件已存在，生成器将不会再对该文件有操作

## 3.事务管理

    事务管理在Service层进行，通过在方法上注解 **@ServiceAutoTrans** 的形式实现，该注解有两个参数：isNeedNewDbSession和isNeedTrans

> isNeedNewDbSession 是否会开启新的 DB session，默认为false；

     isNeedNewDbSession=true 表示将会开启一个新的 DB session；

     isNeedNewDbSession=false 如果当前没有DB session，则会自动打开一个，如果有，则用现有的；

> isNeedTrans 是否需要事务提交，默认为false；

     isNeedTrans = true 表示本次开启的session需要事务提交操作，如果程序中间抛出异常，该session的事务操作将会回滚；

## 4.数据持久化

    通过Dao查出来的对象均为持久化对象；
    
    如果新new的Model对象已做insert()操作,那么也会由非持久化变为持久化对象；

# 使用示例
### 1.新建Spring-boot2测试工程，在POM文件中引入dw-db
```
<dependency>
    <groupId>org.dw.db</groupId>
    <artifactId>dw-db</artifactId>
    <version>0.0.1-SNAPSHOT</version>
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
	private int		curPage				= 0;
	private int		pageCount			= 0;
	private int		pageNumBegin		= 0;
	private int		pageNumEnd			= 0;
	private boolean	hasNext				= false;
	private boolean	hasPre				= false;
	private int		dataCount			= 0;
	private int		curPageDataCount	= 0;
	private int		pageSize			= 0;
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
    通过DaoBase.getPageInfo()方法即可获取到分页对象，通过setUrlParams()方法，将页码点击链接传入，即可实现页码链接的自动拼接，可减少在前端书写分页时的编码工作；
    
## 2.DAO层扩展
    
    通过在Dao方法上加@DaoAutoDatabase注解，即可实现对方法参数Database实现注入，注入对象为当前事务Session，如果当前没有事务Session，则会新建一个，并且自动开启事务，如果中间抛出异常，则自动回滚；
    通过Database对象，可以直接执行SQL语句，或者其他不需要得到Model形式结果的操作；
    
> 扩展案例
    
```java
@DwDao
@Component
public class SysUserDao extends DaoBase
{
	public SysUserDao()
	{
		super("sys_user",SysUserModel.class);
	}

	@DaoAutoDatabase
	public void test(Database db)
	{
		db.execute("select count(id) from student");
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
    
    所以使用者通过对temp表的管理，结合调用CreateTbl.work()方法，可以实现数据库表结构的在线管理；
    
    但是如果使用者接管了表结构的管理，需要注意及时更新表结构定义文件；

# 待完善功能

#### 1.支持多库操作

#### 2.优化表结构文件内容结构

#### 3.数据库索引定义的支持

#### 4.数据库视图定义的支持