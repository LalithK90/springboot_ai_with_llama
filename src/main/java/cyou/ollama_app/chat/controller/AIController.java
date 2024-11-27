package cyou.ollama_app.chat.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@Controller
@AllArgsConstructor
public class AIController {

  @GetMapping("/")
  public String chatWindow(Model model) {
    model.addAttribute("chatUrl",
        MvcUriComponentsBuilder
            .fromMethodName(ChatRestController.class, "chat", "").toUriString());
    model.addAttribute("chatUrl");
    model.addAttribute("fileUploadUrl",
        MvcUriComponentsBuilder
            .fromMethodName(ChatRestController.class, "fileUpload", "").toUriString());
    model.addAttribute("webUploadUrl",
        MvcUriComponentsBuilder
            .fromMethodName(ChatRestController.class, "webUpload", "").toUriString());
    return "result";
  }

}