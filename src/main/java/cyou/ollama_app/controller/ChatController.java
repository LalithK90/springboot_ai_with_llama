package cyou.ollama_app.controller;

import cyou.ollama_app.service.LlamaAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

  private final ChatClient chatClient;
  private final LlamaAiService aiService;

  public ChatController(ChatClient.Builder builder, VectorStore vectorStore, LlamaAiService aiService) {
    this.aiService = aiService;
    this.chatClient = builder
        .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
        .build();
  }

  @GetMapping("/chat")
  public String chat(@RequestParam(value = "prompt", required = false) String prompt) {
    return chatClient.prompt()
        .user(prompt)
        .call()
        .content();
  }


  @GetMapping("/api/v1/generate")
  public String generate(@RequestParam(value = "prompt") String promptMessage) {
    return aiService.generateResult(promptMessage);
  }
}