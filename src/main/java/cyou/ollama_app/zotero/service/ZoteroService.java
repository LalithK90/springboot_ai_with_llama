package cyou.ollama_app.zotero.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ZoteroService {

  @Value("${zotero.api.base-url}")
  private String baseUrl;

  @Value("${zotero.api.key}")
  private String apiKey;

  private final RestTemplate restTemplate;

  public ZoteroService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String fetchLibraryItems(String userId) {
    String url = String.format("%s/users/%s/items?key=%s", baseUrl, userId, apiKey);
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    if (response.getStatusCode().is2xxSuccessful()) {
      return response.getBody();
    }
    throw new RuntimeException("Failed to fetch library items");
  }
}

