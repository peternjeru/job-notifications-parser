package ke.co.proxyapi.jobnotificationparser.services;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import ke.co.proxyapi.jobnotificationparser.models.JobAdvertModel;
import ke.co.proxyapi.jobnotificationparser.repositories.JobAdvertRepository;
import ke.co.proxyapi.jobnotificationparser.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mail.MailMessage;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.mail.Message;
import javax.mail.internet.MimeUtility;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Configuration
@Slf4j
public class EmailParser implements Processor
{
	@Autowired
	private ExecutorService executorService;

	@Autowired
	private JobAdvertRepository jobAdvertRepository;

	@Autowired
	private ProducerTemplate template;

	@Value("${app.mail.search-term-from}")
	private String searchTerm;

	@Override
	public void process(Exchange exchange) throws Exception
	{
		MailMessage mailMessage = exchange.getMessage().getBody(MailMessage.class);
		Message message = mailMessage.getMessage();

		String from = message.getFrom()[0].toString();
		String body = IOUtils.toString(
				MimeUtility.decode(
						message.getInputStream(),
						"quoted-printable"),
				StandardCharsets.UTF_8.displayName());

		executorService.submit(()
				-> parseEmail(from, body));
	}

	private void parseEmail(String from, String body)
	{
		if (!from.contains(searchTerm))
		{
			return;
		}

		UrlDetector parser = new UrlDetector(body, UrlDetectorOptions.HTML);
		List<Url> detect = parser.detect();
		detect.forEach(url -> processLink(url));
	}

	private void processLink(Url url)
	{
		if (!url.getHost().equalsIgnoreCase("www.linkedin.com"))
		{
			return;
		}

		if (!url.getPath().startsWith("/comm/jobs/view"))
		{
			return;
		}

		String urlStr = url.getScheme() + "://" + url.getHost() + url.getPath();
		log.info("URL: " + urlStr);

		String hash = StringUtils.hashString(urlStr);
		Optional<JobAdvertModel> byUrlHash = jobAdvertRepository.findByUrlHash(hash);
		if (byUrlHash.isEmpty())
		{
			JobAdvertModel advert = new JobAdvertModel()
					.setUrl(urlStr)
					.setUrlHash(hash)
					.setCreatedAt(Instant.now().getEpochSecond());
			advert = jobAdvertRepository.save(advert);
			template.asyncSendBody("direct:telegram", advert);
		}
	}
}
