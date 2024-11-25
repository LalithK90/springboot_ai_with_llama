package cyou.ollama_app.service;



import org.springframework.stereotype.Service;

@Service
public class LlamaEmbeddingService {

  public double[] generateEmbedding(String content) {
    // Logic to interact with the Llama model and generate embeddings
    // Replace the following with actual Llama API/SDK integration

    // Example: Mock embedding based on content length (replace this logic)
    int length = Math.min(content.length(), 10);
    double[] embedding = new double[length];
    for (int i = 0; i < length; i++) {
      embedding[i] = content.charAt(i) * 0.01;
    }
    return embedding;
  }
}
