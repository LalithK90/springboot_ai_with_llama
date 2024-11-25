package cyou.ollama_app.zotero.controller;

import cyou.ollama_app.zotero.service.ZoteroService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zotero")
@AllArgsConstructor
public class ZoteroController {

  private final ZoteroService zoteroService;


  @GetMapping("/items/{userId}")
  public ResponseEntity<String> getLibraryItems(@PathVariable String userId) {
    String items = zoteroService.fetchLibraryItems(userId);
    return ResponseEntity.ok(items);
  }
}

