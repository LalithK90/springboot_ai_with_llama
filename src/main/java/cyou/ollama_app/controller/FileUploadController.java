package cyou.ollama_app.controller;


import cyou.ollama_app.entity.PgVector;
import cyou.ollama_app.service.LlamaEmbeddingService;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RestController
public class FileUploadController {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private LlamaEmbeddingService llamaEmbeddingService;

  private final Tika tika = new Tika();

  @PostMapping("/fileUpload")
  public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    try {
      // Check for empty file
      if (file.isEmpty()) {
        return ResponseEntity.badRequest().body("File is empty");
      }

      // Get MIME type of the file
      String mimeType = tika.detect(file.getInputStream());
      if (!isValidMimeType(mimeType)) {
        return ResponseEntity.badRequest().body("Unsupported file type: " + mimeType);
      }

      // Extract content
      String content = extractContent(file);
      if (content == null || content.isEmpty()) {
        return ResponseEntity.badRequest().body("Failed to extract content from the file");
      }

      // Generate embeddings using Llama
      double[] vector = llamaEmbeddingService.generateEmbedding(content);

      // Save to pgvector database
      saveToDatabase(file.getOriginalFilename(), content, vector);

      return ResponseEntity.ok("File processed and saved successfully");

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred: " + e.getMessage());
    }
  }

  private boolean isValidMimeType(String mimeType) {
    List<String> validMimeTypes = List.of("application/pdf", "text/plain");
    return validMimeTypes.contains(mimeType);
  }

  private String extractContent(MultipartFile file) throws Exception {
    try (InputStream inputStream = file.getInputStream()) {
      BodyContentHandler handler = new BodyContentHandler(-1); // No content length limit
      ParseContext context = new ParseContext();

      String mimeType = tika.detect(inputStream);
      if ("application/pdf".equals(mimeType)) {
        new PDFParser().parse(inputStream, handler, null, context);
      } else if ("text/plain".equals(mimeType)) {
        new TXTParser().parse(inputStream, handler, null, context);
      } else {
        throw new TikaException("Unsupported file type for parsing: " + mimeType);
      }

      return handler.toString();
    }
  }

  private void saveToDatabase(String fileName, String content, double[] vector) throws SQLException {
    String insertSql = "INSERT INTO documents (name, content, embedding) VALUES (?, ?, ?)";
    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
      preparedStatement.setString(1, fileName);
      preparedStatement.setString(2, content);
      preparedStatement.setObject(3, new PgVector(vector));
      preparedStatement.executeUpdate();
    }
  }
}

