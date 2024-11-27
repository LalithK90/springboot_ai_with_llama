package cyou.ollama_app.chat.web_scraping;

import cyou.ollama_app.chat.service.LlamaAiService;
import cyou.ollama_app.chat.web_scraping.service.VisitLogger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class WebScraperService {
  private final VectorStore vectorStore;
  private final LlamaAiService llamaAiService;
  private final VisitLogger visitLogger;  // Logger to handle visited URLs

  private Set<String> visitedLinks = new HashSet<>();

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;  // 10 MB
  private static final int RATE_LIMIT_DELAY_MS = 2000;  // 2 seconds delay between requests

  public WebScraperService(VectorStore vectorStore, LlamaAiService llamaAiService, VisitLogger visitLogger) {
    this.vectorStore = vectorStore;
    this.llamaAiService = llamaAiService;
    this.visitLogger = visitLogger;
  }

  public String scrapeWebsite(String url) throws IOException {
    if (!isCrawlable(url)) {
      System.out.println("Skipping URL due to robots.txt restrictions: " + url);
      return "";
    }

    // Check if the URL has been visited and if its content has changed
    if (visitLogger.isVisitedAndUpdated(url)) {
      System.out.println("No update found for URL: " + url);
      return "";
    }

    Document document = Jsoup.connect(url)
        .timeout(5000)
        .get();
    visitedLinks.add(url);

    StringBuilder scrapedContent = new StringBuilder();

    Elements paragraphs = document.select("p");
    Elements headers = document.select("h1, h2, h3, h4, h5, h6");

    for (Element paragraph : paragraphs) {
      scrapedContent.append(paragraph.text()).append("\n");
    }
    for (Element header : headers) {
      scrapedContent.append(header.text()).append("\n");
    }

    // Process PDF links
    Elements links = document.select("a[href]");
    for (Element link : links) {
      String href = link.absUrl("href");

      if (href.endsWith(".pdf") && isPdfFileValid(href)) {
        processPdfLink(href);
      }
    }

    // Now, scrapedContent is a StringBuilder that contains all the text content
    // Convert StringBuilder to String
    String content = scrapedContent.toString();

    // Pass the string content to the TextSplitter
    TextSplitter textSplitter = new TokenTextSplitter();

    // Assuming vectorStore is a predefined object for processing the split content
    vectorStore.accept(textSplitter.apply(Collections.singletonList(new org.springframework.ai.document.Document(content))));

    // Log the visited URL and content hash to the database
    visitLogger.logVisitedUrl(url, content);

    // Sleep between requests to avoid rate limiting
    sleepBetweenRequests();

    return scrapedContent.toString();
  }

  // Check if the PDF file size is below the specified limit
  private boolean isPdfFileValid(String pdfUrl) {
    try {
      URL url = new URL(pdfUrl);
      long fileSize = url.openConnection().getContentLengthLong();
      return fileSize <= MAX_FILE_SIZE;
    } catch (IOException e) {
      return false;
    }
  }

  private void processPdfLink(String pdfUrl) throws IOException {
    URL url = new URL(pdfUrl);
    File tempFile = File.createTempFile("uploaded-", ".pdf");

    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      url.openStream().transferTo(fos);
    }

    CustomMultipartFile multipartFile = new CustomMultipartFile(tempFile);
    llamaAiService.processFileTikaReader(multipartFile);

    tempFile.delete();
  }

  // Check if the URL is allowed by robots.txt
  private boolean isCrawlable(String url) {

    try {
      URI uri = new URI(url);
      String domain = uri.getHost();
      String robotsUrl = "https://" + domain + "/robots.txt";

      Document robotsDocument = Jsoup.connect(robotsUrl).get();
      String robotsText = robotsDocument.text();
      return !robotsText.contains("Disallow: " + url);
    } catch (IOException e) {
      return true;
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  // Sleep between requests
  private void sleepBetweenRequests() {
    try {
      Thread.sleep(RATE_LIMIT_DELAY_MS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
