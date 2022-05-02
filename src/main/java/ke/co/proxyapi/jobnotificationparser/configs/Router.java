package ke.co.proxyapi.jobnotificationparser.configs;

import ke.co.proxyapi.jobnotificationparser.services.JobsCleaner;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Router extends RouteBuilder
{
	@Autowired
	private RoutesConfig routesConfig;

	@Autowired
	private JobsCleaner jobsCleaner;

	@Value("${app.cron-job}")
	private String cronJob;

	@Override
	public void configure()
	{
		routesConfig.getRoutes()
				.forEach((s, routeDto) ->
				{
					if (routeDto.getRequests() != null
							&& routeDto.getTimePeriodMillis() != null
							&& routeDto.getRequests() > 0
							&& routeDto.getTimePeriodMillis() > 0)
					{
						from(routeDto.getRoute())
								.throttle(routeDto.getRequests())         // this no. of requests...
								.timePeriodMillis(routeDto.getTimePeriodMillis())   // ...per this time period in millis
								.process(routeDto.getProcessor())
								.end();
					}
					else
					{
						from(routeDto.getRoute())
								.process(routeDto.getProcessor())
								.end();
					}
				});

		from(cronJob)
				.setBody()
				.constant("event")
				.process(jobsCleaner)
				.end();
	}
}
