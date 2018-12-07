package dw.spring.configuration;

import dw.db.base.DwSubsetLoadService;
import dw.db.trans.ServiceDatabasePoint;
import dw.db.util.DwSpringUtil;
import dw.spring.listener.DwDbSpringListener;
import dw.spring.service.DwDbCommonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DwDbSpringConfiguration
{
	@Bean
	public DwDbSpringListener dwDbSpringListener()
	{
		return new DwDbSpringListener();
	}

	@Bean
	public ServiceDatabasePoint serviceDatabasePoint()
	{
		return new ServiceDatabasePoint();
	}

	@Bean
	public DwDbCommonService dwDbCommonService()
	{
		return new DwDbCommonService();
	}

	@Bean
	public DwSubsetLoadService dwSubsetLoadService()
	{
		return new DwSubsetLoadService();
	}

	@Bean
	public DwSpringUtil dwSpringUtil()
	{
		return new DwSpringUtil();
	}
}
