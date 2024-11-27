package cyou.ollama_app.document.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata", uniqueConstraints = @UniqueConstraint(columnNames = "file_hash"))
@Getter
@Setter
public class FileMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "file_hash", nullable = false, unique = true)
  private String fileHash;

  @Column(name = "upload_date", nullable = false)
  private LocalDateTime uploadDate = LocalDateTime.now();

}

