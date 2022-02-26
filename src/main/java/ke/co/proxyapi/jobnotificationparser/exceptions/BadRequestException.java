package ke.co.proxyapi.jobnotificationparser.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RuntimeException
{
	public BadRequestException(HttpStatus statusCode, String body)
	{
		super(statusCode.getReasonPhrase());
	}
}
