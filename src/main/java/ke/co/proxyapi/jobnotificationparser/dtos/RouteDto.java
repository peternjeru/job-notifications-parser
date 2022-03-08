package ke.co.proxyapi.jobnotificationparser.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.camel.Processor;

@Getter
@Setter
@Accessors(chain = true)
public class RouteDto
{
	private String route;
	private Processor processor;
	private Long requests;
	private Long timePeriodMillis;
}
