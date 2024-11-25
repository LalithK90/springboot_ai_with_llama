package cyou.ollama_app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class LlamaAiService {


  private final VectorStore vectorStore;

//
//
//  @Override
//  public void run(String... args) throws Exception {
//    var pdfReader = new ParagraphPdfDocumentReader(marketPDF);
//    TextSplitter textSplitter = new TokenTextSplitter();
//    vectorStore.accept(textSplitter.apply(pdfReader.get()));
//    log.info("VectorStore Loaded with data!");
//  }

  private final OllamaChatModel chatModel;
  private final ChatClient chatClient;

  public LlamaAiService(VectorStore vectorStore, OllamaChatModel chatModel, ChatClient.Builder builder) {
    this.vectorStore = vectorStore;
    this.chatModel = chatModel;
    this.chatClient = builder
        .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
        .build();
  }


  public String generateResult(String prompt) {
    ChatResponse response = chatModel.call(
        new Prompt(
            prompt,
            OllamaOptions.create()
                .withModel("llama3.2-vision:latest")
        ));
    return response.getResult().getOutput().getContent();
  }

  public String processWebsite(String url) {
    return "";
  }

  public String processFile(MultipartFile file) {
    return "";
  }

  public String processPrompt(String prompt) {
    return chatClient.prompt()
        .user(prompt)
        .call()
        .content();
  }
}