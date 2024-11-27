package cyou.ollama_app.chat.service;

import cyou.ollama_app.document.service.FileMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

@Service
@Slf4j
public class LlamaAiService {

  private final VectorStore vectorStore;
  private final FileMetadataService fileMetadataService;

  public LlamaAiService(VectorStore vectorStore, FileMetadataService fileMetadataService) {
    this.vectorStore = vectorStore;
    this.fileMetadataService = fileMetadataService;
  }

  public String processFileTikaReader(MultipartFile file) {
    try {
      var x = fileDuplicateChecker(file);
      if (x != null) return x;

      // Step 1: Save the uploaded file to a temporary location
      File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        fos.write(file.getBytes());
      }

      // Step 2: Convert the temporary file to a Spring `Resource`
      Resource resource = new UrlResource(tempFile.toPath().toUri());

      // Step 3: Pass the resource to TikaDocumentReader
      TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);

      // Step 4: Extract content
//      String content = tikaDocumentReader.read();

      TextSplitter textSplitter = new TokenTextSplitter();
      vectorStore.accept(textSplitter.apply(tikaDocumentReader.read()));

      // Step 5: (Optional) Delete the temporary file after processing
      tempFile.delete();

      // Step 6: Return or log the extracted content
//      System.out.println("Extracted Content: " + content);
      return "File processed successfully with content: ";
    } catch (Exception e) {
      e.printStackTrace();
      return "Failed: " + e.getMessage();
    }
  }

  private String fileDuplicateChecker(MultipartFile file) {
    // Ensure the file is not empty
    if (file.isEmpty()) {
      return "Failed: File is empty!";
    }
    // Check for duplicate file
    if (fileMetadataService.isDuplicateFile(file)) {
      return "File already exists!";
    }

    // Save metadata
    fileMetadataService.saveFileMetadata(file);
    return null;
  }

  public String processWebsite(String url) {
    return "";
  }

}