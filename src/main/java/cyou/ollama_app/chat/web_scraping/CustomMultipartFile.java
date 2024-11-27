package cyou.ollama_app.chat.web_scraping;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class CustomMultipartFile implements MultipartFile {
  private final String name;
  private final String originalFilename;
  private final byte[] content;

  public CustomMultipartFile(File file) throws IOException {
    this.name = file.getName();
    this.originalFilename = file.getName();
    this.content = loadFileContent(file);
  }

  private byte[] loadFileContent(File file) throws IOException {
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      return fileInputStream.readAllBytes();
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOriginalFilename() {
    return originalFilename;
  }

  @Override
  public String getContentType() {
    return "application/pdf"; // You can adjust the content type based on the file type
  }

  @Override
  public boolean isEmpty() {
    return content.length == 0;
  }

  @Override
  public long getSize() {
    return content.length;
  }

  @Override
  public byte[] getBytes() throws IOException {
    return content;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(content);
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    try (FileOutputStream fos = new FileOutputStream(dest)) {
      fos.write(content);
    }
  }
}
