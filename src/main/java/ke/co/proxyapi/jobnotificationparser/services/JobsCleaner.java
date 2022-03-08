package ke.co.proxyapi.jobnotificationparser.services;

import ke.co.proxyapi.jobnotificationparser.repositories.JobAdvertRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class JobsCleaner implements Processor
{
	@Autowired
	private ExecutorService executorService;

	@Autowired
	private JobAdvertRepository jobAdvertRepository;

	@Override
	public void process(Exchange exchange)
	{
		log.info("Cleaner called");

		//delete anything older than a month
		executorService.submit(() ->
		{
			long oldCreatedAt = Instant.now().getEpochSecond() - 2628000;
			jobAdvertRepository.deleteAllByCreatedAtLessThan(oldCreatedAt);
		});
	}
}
