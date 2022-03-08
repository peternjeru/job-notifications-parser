package ke.co.proxyapi.jobnotificationparser.utils;

import ke.co.proxyapi.jobnotificationparser.models.JobAdvertModel;
import ke.co.proxyapi.jobnotificationparser.repositories.JobAdvertRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
public class AppUtils
{
	@Autowired
	private JobAdvertRepository jobAdvertRepository;

	@Autowired
	private ProducerTemplate template;

	public void sendToTelegram(String urlStr)
	{
		log.info("Found URL: " + urlStr);
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
