package ke.co.proxyapi.jobnotificationparser.services;

import ke.co.proxyapi.jobnotificationparser.exceptions.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class HttpService
{
	@Autowired
	private RestTemplate restTemplate;

	public String post(String uri, String bodyStr)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(bodyStr, headers);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri, entity, String.class);
		String body = responseEntity.getBody();

		if (!responseEntity.getStatusCode().isError())
		{
			return body;
		}
		throw new BadRequestException(responseEntity.getStatusCode(), body);
	}

	public String get(String uri, Map<String, String> headersMap)
	{
		HttpHeaders headers = new HttpHeaders();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
		String body = responseEntity.getBody();

		if (!responseEntity.getStatusCode().isError())
		{
			return body;
		}
		throw new BadRequestException(responseEntity.getStatusCode(), body);
	}
}
