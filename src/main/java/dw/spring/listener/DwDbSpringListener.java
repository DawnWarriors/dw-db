package dw.spring.listener;

import dw.db.create.CreateDao;
import dw.db.create.CreateModel;
import dw.db.create.CreateTbl;
import dw.db.create.CreateTblDefBaseTbl;
import dw.db.model.AutoCodeInfoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class DwDbSpringListener implements ApplicationListener<ContextRefreshedEvent>
{
	@Autowired
	ApplicationContext context;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		try
		{
			//执行数据库初始化
			CreateTblDefBaseTbl.work();
			//执行数据表结构更新
			CreateTbl.work();
			String[] profiles = context.getEnvironment().getActiveProfiles();
			if (profiles == null || profiles.length == 0 || "dev".equals(context.getEnvironment().getActiveProfiles()[0]))
			{
				//代码自动构建
				Yaml yaml = new Yaml();
				AutoCodeInfoModel dirInfo = yaml.loadAs(CreateModel.class.getResourceAsStream("/dw/dw_auto_code.yml"), AutoCodeInfoModel.class);
				Map<String,String[]> modelClassInfo = new CreateModel().work(dirInfo.getDwModelDirDef());
				new CreateDao().work(dirInfo.getDwDaoDirDef(), modelClassInfo);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
