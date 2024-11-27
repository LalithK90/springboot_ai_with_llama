package cyou.ollama_app.util.aspect;

import cyou.ollama_app.chat.web_scraping.WebScraperService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Aspect
@Component
public class WebUploadAspect {

  @Autowired
  private WebScraperService webScraperService;

  @AfterReturning("execution(* cyou.ollama_app.chat.controller.ChatRestController.webUpload(..))")
  public void runAfterWebUpload(JoinPoint joinPoint){
  var url =   (String) joinPoint.getArgs()[0];
    try {
      if (!url.isEmpty()) {
        webScraperService.scrapeWebsite(url);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error scraping website: " + e.getMessage(), e);
    }
  }
}
