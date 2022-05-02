package ke.co.proxyapi.jobnotificationparser.configs;

import ke.co.proxyapi.jobnotificationparser.dtos.RouteDto;
import ke.co.proxyapi.jobnotificationparser.services.parsers.BrighterMondayParser;
import ke.co.proxyapi.jobnotificationparser.services.EmailReceiver;
import ke.co.proxyapi.jobnotificationparser.services.parsers.LinkedInParser;
import ke.co.proxyapi.jobnotificationparser.services.TelegramService;
import lombok.Getter;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Component
public class RoutesConfig
{
	@Autowired
	private EmailReceiver emailReceiver;

	@Autowired
	private TelegramService telegramService;

	@Autowired
	private LinkedInParser linkedInParser;

	@Autowired
	private BrighterMondayParser brighterMondayParser;

	@Value("${app.mail.host}")
	private String host;

	@Value("${app.mail.port}")
	private Integer port;

	@Value("${app.mail.username}")
	private String username;

	@Value("${app.mail.password}")
	private String password;

	@Value("${app.mail.connection-timeout-millis}")
	private Long connectionTimeoutMillis;

	@Value("${app.mail.fetch-size}")
	private Integer fetchSize;

	@Value("${app.mail.poll-delay}")
	private Long pollDelay;

	@Value("${app.mail.unseen}")
	private String unseen;

	@Value("${app.mail.debug}")
	private String debug;

	@Getter
	private final Map<String, RouteDto> routes = new HashMap<>();

	public void addRoute(@NotNull @NotEmpty String route, @NotNull Processor processor, Long requests, Long timePeriod)
	{
		routes.put(route, new RouteDto()
				.setRoute(route)
				.setProcessor(processor)
				.setRequests(requests)
				.setTimePeriodMillis(timePeriod));
	}

	@PostConstruct
	public void init()
	{
		addEmailRoute();
		addTelegramRoute();
		addLinkedInRoute();
		addBrighterMondayRoute();
	}

	private void addEmailRoute()
	{
		String url = "imaps://" + host + ":" + port
				+ "?username=" + username
				+ "&password=" + password
				+ "&delay=" + pollDelay
				+ "&fetchSize=" + fetchSize
				+ "&connectionTimeout=" + connectionTimeoutMillis
				+ "&debugMode=" + debug
				+ "&skipFailedMessage=true"
				+ "&searchTerm.fromSentDate=now-24h"
				+ "&searchTerm.unseen=" + unseen
				+ "&unseen=" + unseen;

		addRoute(url, emailReceiver, null, null);
	}

	private void addTelegramRoute()
	{
		addRoute("direct:telegram", telegramService, 1L, 20000L);
	}

	private void addLinkedInRoute()
	{
		addRoute("direct:jobalerts-noreply@linkedin.com", linkedInParser, null, null);
	}

	private void addBrighterMondayRoute()
	{
		addRoute("direct:support@brightermonday.co.ke", brighterMondayParser, null, null);
	}
}
