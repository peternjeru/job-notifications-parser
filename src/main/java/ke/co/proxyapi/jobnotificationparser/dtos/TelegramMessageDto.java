package ke.co.proxyapi.jobnotificationparser.dtos;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TelegramMessageDto
{
	@SerializedName("message_id")
	private Long messageID;

	@SerializedName("sender_chat")
	private String senderChat;

	@SerializedName("date")
	private Long date;

	@SerializedName("text")
	private String text;
}
