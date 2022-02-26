package ke.co.proxyapi.jobnotificationparser.dtos;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TelegramMessageRequestDto
{
	@SerializedName("chat_id")
	private String chatID;

	@SerializedName("text")
	private String text;

	@SerializedName("disable_web_page_preview")
	private Boolean disableWebPagePreview;

	@SerializedName("disable_notification")
	private Boolean disableNotification;
}
