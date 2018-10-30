package dw.spring.service;

import dw.code.model.AutoCodeInfoModel;
import dw.code.util.CreateDaoUtil;
import dw.code.util.CreateModelUtil;
import dw.db.annotation.ServiceAutoTrans;
import dw.db.create.CreateTbl;
import dw.db.create.CreateTblDefBaseTbl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

@Service
public class DwDbCommonService
{
	@Autowired
	ApplicationContext context;

	@ServiceAutoTrans
	public void autoCode()
	{
		String[] profiles = context.getEnvironment().getActiveProfiles();
		if (profiles == null || profiles.length == 0 || "dev".equals(context.getEnvironment().getActiveProfiles()[0]))
		{
			//代码自动构建
			Yaml yaml = new Yaml();
			AutoCodeInfoModel dirInfo = yaml.loadAs(CreateModelUtil.class.getResourceAsStream("/dw/dw_auto_code.yml"), AutoCodeInfoModel.class);
			Map<String,String[]> modelClassInfo = new CreateModelUtil().work(dirInfo.getDwModelDirDef());
			new CreateDaoUtil().work(dirInfo.getDwDaoDirDef(), modelClassInfo);
		}
	}

	@ServiceAutoTrans(isNeedNewDbSession = true, isNeedTrans = true)
	public void updateDB() throws Exception
	{
		//执行数据库初始化
		new CreateTblDefBaseTbl().work();
		//执行数据表结构更新
		new CreateTbl().work();
	}
}
