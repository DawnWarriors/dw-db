package dw.spring.configuration;

import dw.db.point.DaoDatabasePoint;
import dw.db.point.ServiceDatabasePoint;
import dw.spring.listener.DwDbSpringListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DwSpringConfiguration
{
	@Bean
	public DwDbSpringListener getDwDbSpringListener()
	{
		return new DwDbSpringListener();
	}

	@Bean
	public DaoDatabasePoint getDaoDatabasePoint()
	{
		return new DaoDatabasePoint();
	}

	@Bean
	public ServiceDatabasePoint getServiceDatabasePoint()
	{
		return new ServiceDatabasePoint();
	}
}
