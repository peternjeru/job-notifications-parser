package ke.co.proxyapi.jobnotificationparser.services;

import com.google.gson.Gson;
import ke.co.proxyapi.jobnotificationparser.dtos.TelegramMessageRequestDto;
import ke.co.proxyapi.jobnotificationparser.models.JobAdvertModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TelegramService implements Processor
{
	@Autowired
	private Gson gson;

	@Autowired
	private HttpService httpService;

	@Value("${app.telegram.api-url}")
	private String tgApiUrl;

	@Value("${app.telegram.token}")
	private String tgToken;

	@Value("${app.telegram.api-method}")
	private String tgMethod;

	@Value("${app.telegram.chat-id}")
	private String tgChatID;

	@Override
	public void process(Exchange exchange)
	{
		JobAdvertModel model = exchange.getMessage().getBody(JobAdvertModel.class);
		TelegramMessageRequestDto requestDto = new TelegramMessageRequestDto()
				.setChatID(tgChatID)
				.setText(model.getUrl())
				.setDisableNotification(false)
				.setDisableWebPagePreview(false);

		String requestJson = gson.toJson(requestDto);
		String url = tgApiUrl + "/bot" + tgToken + "/" + tgMethod;

		try
		{
			String response = httpService.post(url, requestJson);
			log.info("Response:\n" + response);
		}
		catch (RuntimeException exception)
		{
			log.error(exception.getMessage(), exception);
		}
	}
}
