package ke.co.proxyapi.jobnotificationparser.utils;

import ke.co.proxyapi.jobnotificationparser.models.JobAdvertModel;
import ke.co.proxyapi.jobnotificationparser.repositories.JobAdvertRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		//Check if url has been blacklisted
		if (hasSpamWords(urlStr))
		{
			log.info("Blacklisted URL: " + urlStr);
			return;
		}
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

	private boolean hasSpamWords(String url) {
		String description = getMetaDescription(url);
		if (description == null) {
			return false;
		}
		return isSpamDescription(description, "spam_words.txt");
	}
	public static String getMetaDescription(String url) {
		try {
			Document doc = Jsoup.connect(url).get();
			Elements metaTags = doc.getElementsByTag("meta");
			for (Element metaTag : metaTags) {
				String tagName = metaTag.attr("name");
				if (tagName.equalsIgnoreCase("description")) {
					return metaTag.attr("content");
				}
			}
		} catch (IOException ignored) {
		}
		return null;
	}
	public static boolean isSpamDescription(String description, String spamWordsFilePath) {
		String[] spamWords = readSpamWordsFromFile(spamWordsFilePath);

		for (String spamWord : spamWords) {
			Pattern pattern = Pattern.compile(spamWord, Pattern.CASE_INSENSITIVE| Pattern.LITERAL | Pattern.MULTILINE | Pattern.DOTALL);
			Matcher matcher = pattern.matcher(description);
			if (matcher.find()) {
				return true;
			}
		}
		return false;
	}
	private static String[] readSpamWordsFromFile(String spamWordsFilePath) {
		//read spam words from resources/blacklist.json
		try
		{
			ClassPathResource res = new ClassPathResource(spamWordsFilePath);
			File file = new File(res.getPath());
			String content = new String(Files.readAllBytes(file.toPath()));
			//split by new line and space. remove empty strings
			return Arrays.stream(content.split("[\\n\\s]+")).filter(s -> !s.isEmpty()).toArray(String[]::new);
		}
		catch (Exception e)
		{
			return new String[]{};
		}
	}
}
