package cyou.ollama_app.chat.controller;

import cyou.ollama_app.chat.service.LlamaAiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
public class ChatRestController {

  private final ChatClient chatClient;
  private final LlamaAiService aiService;

  List<String> sensitiveWords = Collections.singletonList("fuck");

  public ChatRestController(ChatClient.Builder builder, VectorStore vectorStore, LlamaAiService aiService) {
    this.aiService = aiService;
    this.chatClient = builder
        .defaultAdvisors(
            new QuestionAnswerAdvisor(vectorStore),
            new SafeGuardAdvisor(sensitiveWords),
            new SimpleLoggerAdvisor(),
            new VectorStoreChatMemoryAdvisor(vectorStore)
        )
        .build();

  }

  @GetMapping("/chat")
  public String chat(@RequestParam(value = "prompt", required = false) String prompt) {
    return chatClient.prompt()
        .user(prompt)
        .call()
        .content();
  }

  @PostMapping("/fileUpload")
  public String fileUpload(@RequestParam(value = "file", required = false) MultipartFile file) {
    return aiService.processFileTikaReader(file);
  }
}