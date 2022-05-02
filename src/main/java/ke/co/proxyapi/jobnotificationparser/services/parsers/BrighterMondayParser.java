package ke.co.proxyapi.jobnotificationparser.services.parsers;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import ke.co.proxyapi.jobnotificationparser.dtos.EmailDto;
import ke.co.proxyapi.jobnotificationparser.utils.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BrighterMondayParser implements Processor
{
	@Autowired
	private ExecutorService executorService;

	@Autowired
	private AppUtils appUtils;

	@Override
	public void process(Exchange exchange)
	{
		EmailDto emailDto = exchange.getMessage().getBody(EmailDto.class);
		UrlDetector parser = new UrlDetector(emailDto.getBody(), UrlDetectorOptions.HTML);
		List<Url> detect = parser.detect();
		detect.forEach(url ->
				executorService.submit(() -> processLink(url)));
	}

	private void processLink(Url url)
	{
		if (!url.getHost().equalsIgnoreCase("link.brightermonday.co.ke"))
		{
			return;
		}

		if (!url.getPath().startsWith("/click"))
		{
			return;
		}

		List<String> parts = Arrays.stream(url.getPath().split("/"))
				.filter(s -> s != null && !s.isEmpty())
				.map(part ->
				{
					try
					{
						return new String(Base64.getUrlDecoder().decode(part));
					}
					catch (RuntimeException exception)
					{
						return "";
					}
				})
				.filter(s -> !s.isEmpty())
				.map(part ->
				{
					try
					{
						URI jobUri = URI.create(part);
						if (!jobUri.getHost().equalsIgnoreCase("www.brightermonday.co.ke"))
						{
							return "";
						}

						if (!jobUri.getPath().startsWith("/listings"))
						{
							return "";
						}
						return part;
					}
					catch (RuntimeException exception)
					{
						return "";
					}
				})
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());

		if (parts.isEmpty())
		{
			return;
		}

		URI uri = URI.create(parts.get(0));
		String urlStr = uri.getScheme() + "://" + uri.getHost() + uri.getPath();
		appUtils.sendToTelegram(urlStr);
	}
}
