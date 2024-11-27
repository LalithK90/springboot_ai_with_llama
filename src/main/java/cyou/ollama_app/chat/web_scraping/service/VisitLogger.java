package cyou.ollama_app.chat.web_scraping.service;

import cyou.ollama_app.chat.web_scraping.entity.VisitLog;
import cyou.ollama_app.chat.web_scraping.repo.VisitLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class VisitLogger {

  @Autowired
  private VisitLogRepository visitLogRepository;

  // Check if the URL has been visited and if its content has changed
  public boolean isVisitedAndUpdated(String url) {
    VisitLog visitLog = visitLogRepository.findByUrl(url);
    return visitLog != null;
  }

  // Log the visited URL and content hash to the database
  public void logVisitedUrl(String url, String content) {
    String contentHash = getContentHash(content);

    // Check if the URL already exists in the database
    VisitLog visitLog = visitLogRepository.findByUrl(url);
    if (visitLog == null) {
      // If the URL doesn't exist, create a new entry
      visitLog = new VisitLog(url, contentHash);
    } else {
      // If the URL exists, update its content hash if it has changed
      if (!visitLog.getContentHash().equals(contentHash)) {
        visitLog.setContentHash(contentHash);
      } else {
        // No change, so we don't need to update the database
        return;
      }
    }

    // Save the VisitLog to the database
    visitLogRepository.save(visitLog);
  }

  // Generate a SHA-256 hash for the content to detect changes
  private String getContentHash(String content) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(content.getBytes());
      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        hexString.append(String.format("%02x", b));
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Failed to create hash", e);
    }
  }
}

