package cyou.ollama_app.chat.controller;

import cyou.ollama_app.util.service.CommonService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@Controller
@AllArgsConstructor
public class AIController {
private final CommonService commonService;
  @GetMapping("/")
  public String chatWindow(Model model) {
    model.addAttribute("chatUrl",
        commonService.adjustUrlSchemeHttpToHttps(MvcUriComponentsBuilder
            .fromMethodName(ChatRestController.class, "chat", "").toUriString()));
    model.addAttribute("chatUrl");
    model.addAttribute("fileUploadUrl",
        commonService.adjustUrlSchemeHttpToHttps(MvcUriComponentsBuilder
            .fromMethodName(ChatRestController.class, "fileUpload", "").toUriString()));
    model.addAttribute("webUploadUrl",
        commonService.adjustUrlSchemeHttpToHttps(MvcUriComponentsBuilder
            .fromMethodName(ChatRestController.class, "webUpload", "").toUriString()));
    return "result";
  }

}