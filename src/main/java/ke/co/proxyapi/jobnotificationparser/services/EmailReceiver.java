package ke.co.proxyapi.jobnotificationparser.services;

import ke.co.proxyapi.jobnotificationparser.dtos.EmailDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mail.MailMessage;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailReceiver implements Processor
{
	@Autowired
	private ProducerTemplate template;

	@Value("${app.mail.search-terms}")
	private String[] searchTerms;

	@Override
	public void process(Exchange exchange) throws Exception
	{
		MailMessage mailMessage = exchange.getMessage().getBody(MailMessage.class);
		Message message = mailMessage.getMessage();

		InternetAddress internetAddress = (InternetAddress) message.getFrom()[0];
		String emailRoute = internetAddress.getAddress();

		String body = IOUtils.toString(
				MimeUtility.decode(
						message.getInputStream(),
						"quoted-printable"),
				StandardCharsets.UTF_8.displayName());

		EmailDto emailDto = new EmailDto()
				.setAddress(emailRoute)
				.setBody(body);

		for (String email: searchTerms)
		{
			if (emailRoute.equalsIgnoreCase(email))
			{
				template.asyncSendBody("direct:" + emailRoute, emailDto);
			}
		}
	}
}
