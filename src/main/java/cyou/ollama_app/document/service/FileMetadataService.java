package cyou.ollama_app.document.service;

import cyou.ollama_app.document.entity.FileMetadata;
import cyou.ollama_app.document.repo.FileMetadataRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FileMetadataService {

  private final FileMetadataRepository repository;

  public boolean isDuplicateFile(MultipartFile file) {
    try {
      // Generate SHA-256 hash of the file
      String fileHash = generateFileHash(file);

      // Check if the file hash already exists in the database
      Optional<FileMetadata> existingFile = repository.findByFileHash(fileHash);
      return existingFile.isPresent();
    } catch (Exception e) {
      throw new RuntimeException("Failed to check file duplication", e);
    }
  }

  public FileMetadata saveFileMetadata(MultipartFile file) {
    try {
      // Generate SHA-256 hash of the file
      String fileHash = generateFileHash(file);

      // Save metadata
      FileMetadata metadata = new FileMetadata();
      metadata.setFileName(file.getOriginalFilename());
      metadata.setFileSize(file.getSize());
      metadata.setFileHash(fileHash);

      return repository.save(metadata);
    } catch (Exception e) {
      throw new RuntimeException("Failed to save file metadata", e);
    }
  }

  private String generateFileHash(MultipartFile file) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(file.getBytes());
    StringBuilder hexString = new StringBuilder();

    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }
}

