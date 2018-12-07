package dw.spring.listener;

import dw.spring.service.DwDbCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class DwDbSpringListener implements ApplicationListener<ContextRefreshedEvent>
{
	@Autowired
	DwDbCommonService dwDbCommonService;
	@Value("${dw.auto.db:false}")
	Boolean           openAutoDB;
	@Value("${dw.auto.code:false}")
	Boolean           openAutoCode;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		try
		{
			if (openAutoDB)
			{
				dwDbCommonService.updateDB();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (openAutoCode)
		{
			dwDbCommonService.autoCode();
		}
	}
}
