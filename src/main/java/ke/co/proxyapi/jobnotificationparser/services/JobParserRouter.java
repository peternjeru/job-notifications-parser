package ke.co.proxyapi.jobnotificationparser.services;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JobParserRouter extends RouteBuilder
{
	@Autowired
	private EmailParser emailParser;

	@Autowired
	private TelegramService telegramService;

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

	@Override
	public void configure()
	{
		String url = "imaps://" + host + ":" + port
				+ "?username=" + username
				+ "&password=" + password
				+ "&delay=" + pollDelay
				+ "&fetchSize=" + fetchSize
				+ "&connectionTimeout=" + connectionTimeoutMillis
				+ "&debugMode=" + debug
				+ "&unseen=" + unseen;

		from(url)
				.process(emailParser)
				.end();

		from("direct:telegram")
				.throttle(1)    //no. of requests...
				.timePeriodMillis(5000)             //...per this time period
				.process(telegramService);
	}
}
