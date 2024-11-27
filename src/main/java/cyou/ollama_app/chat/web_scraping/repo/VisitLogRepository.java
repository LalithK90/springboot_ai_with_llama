package cyou.ollama_app.chat.web_scraping.repo;

import cyou.ollama_app.chat.web_scraping.entity.VisitLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitLogRepository extends JpaRepository<VisitLog, String> {

  // Find a VisitLog by URL
  VisitLog findByUrl(String url);
}

