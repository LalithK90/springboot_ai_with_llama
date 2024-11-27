package cyou.ollama_app.chat.web_scraping.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class VisitLog {

  @Id
  private String url; // URL as the unique identifier
  private String contentHash; // Hash of the content to detect changes


}
