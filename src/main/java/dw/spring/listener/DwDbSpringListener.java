package dw.spring.listener;

import dw.spring.service.DwDbCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class DwDbSpringListener implements ApplicationListener<ContextRefreshedEvent>
{
	@Autowired
	DwDbCommonService dwDbCommonService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		try
		{
			dwDbCommonService.updateDB();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		dwDbCommonService.autoCode();
	}
}
