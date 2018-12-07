package dw.spring.service;

import dw.code.model.AutoCodeInfoModel;
import dw.code.util.CreateDaoUtil;
import dw.code.util.CreateModelUtil;
import dw.db.annotation.ServiceAutoTrans;
import dw.db.create.CreateTbl;
import dw.db.create.CreateTblDefBaseTbl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

@Service
@Slf4j
public class DwDbCommonService
{
	@Autowired
	ApplicationContext context;

	@ServiceAutoTrans
	public void autoCode()
	{
		//代码自动构建
		Yaml yaml = new Yaml();
		AutoCodeInfoModel dirInfo = yaml.loadAs(CreateModelUtil.class.getResourceAsStream("/dw/dw_auto_code.yml"), AutoCodeInfoModel.class);
		Map<String,String[]> modelClassInfo = new CreateModelUtil().work(dirInfo.getDwModelDirDef());
		new CreateDaoUtil().work(dirInfo.getDwDaoDirDef(), modelClassInfo);
		log.info("代码自动化生成完成");
	}

	@ServiceAutoTrans(isNeedNewDbSession = true, isNeedTrans = true)
	public void updateDB() throws Exception
	{
		//执行数据库初始化
		new CreateTblDefBaseTbl().work();
		log.info("数据库初始化检测完成");
		//执行数据表结构更新
		new CreateTbl().work();
		log.info("执行数据表结构更新完成");
	}
}
