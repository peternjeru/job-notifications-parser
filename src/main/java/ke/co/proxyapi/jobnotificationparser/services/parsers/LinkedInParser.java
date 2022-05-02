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

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class LinkedInParser implements Processor
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
		if (!url.getHost().equalsIgnoreCase("www.linkedin.com"))
		{
			return;
		}

		if (!url.getPath().startsWith("/comm/jobs/view"))
		{
			return;
		}

		String urlStr = url.getScheme() + "://" + url.getHost() + url.getPath();
		appUtils.sendToTelegram(urlStr);
	}
}
