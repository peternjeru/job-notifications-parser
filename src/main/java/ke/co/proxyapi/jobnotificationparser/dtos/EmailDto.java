package ke.co.proxyapi.jobnotificationparser.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class EmailDto implements Serializable
{
	private String address;
	private String body;
}
