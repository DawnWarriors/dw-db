package dw.spring.configuration;

import dw.db.trans.ServiceDatabasePoint;
import dw.spring.listener.DwDbSpringListener;
import dw.spring.service.DwDbCommonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DwDbSpringConfiguration
{
	@Bean
	public DwDbSpringListener getDwDbSpringListener()
	{
		return new DwDbSpringListener();
	}

	@Bean
	public ServiceDatabasePoint getServiceDatabasePoint()
	{
		return new ServiceDatabasePoint();
	}

	@Bean
	public DwDbCommonService getDwDbCommonService()
	{
		return new DwDbCommonService();
	}
}
