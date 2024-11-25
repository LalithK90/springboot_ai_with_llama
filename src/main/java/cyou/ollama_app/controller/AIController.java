package cyou.ollama_app.controller;

import cyou.ollama_app.service.LlamaAiService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
public class AIController {

  private final LlamaAiService aiService;

  @GetMapping("/chatWindow")
  public String chatWindow(Model model) {
    model.addAttribute("chatUrl",
        MvcUriComponentsBuilder
            .fromMethodName(ChatController.class, "chat","").toUriString());
    return "result";
  }

  @PostMapping("/process")
  public String processInput(
      @RequestParam("type") String type,
      @RequestParam(value = "url", required = false) String url,
      @RequestParam(value = "file", required = false) MultipartFile file,
      RedirectAttributes redirectAttributes
  ) {
    String result;
    try {
      result = switch (type) {
        case "url" -> aiService.processWebsite(url);
        case "file" -> aiService.processFile(file);
        default -> throw new IllegalArgumentException("Unsupported input type");
      };

      redirectAttributes.addAttribute("result", result);
    } catch (Exception e) {
      redirectAttributes.addAttribute("error", "Error processing input: " + e.getMessage());
    }
    return "redirect:/chatWindow";
  }

}