package cyou.ollama_app.util.service;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommonService {
//
//  private final DateTimeAgeService dateTimeAgeService;
//  private final BCryptPasswordEncoder bcryptPasswordEncoder;

  @Value("${app.url.scheme}")
  private String urlScheme;

  public String escapeHtml(String input) {
    if (input == null) {
      return null;
    }
    return input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")  // Single quote
        .replace("/", "&#47;")  // Forward slash
        .replace("`", "&#96;")  // Backtick
        .replace("=", "&#61;"); // Equals sign
  }

//  public Integer numberAutoGen(String lastNumber) {
//    int newNumber;
//    int previousNumber;
//    int newNumberFirstTwoCharacters;
//
//    int currentYearLastTwoNumber = Integer
//        .parseInt(String.valueOf(dateTimeAgeService.getCurrentDate().getYear()).substring(2, 4));
//
//    if (lastNumber != null) {
//      previousNumber = Integer.parseInt(lastNumber.substring(0, 9));
//      newNumberFirstTwoCharacters = Integer.parseInt(lastNumber.substring(0, 2));
//
//      if (currentYearLastTwoNumber == newNumberFirstTwoCharacters) {
//        newNumber = previousNumber + 1;
//      } else {
//        newNumber = Integer.parseInt(currentYearLastTwoNumber + "0000000");
//      }
//
//    } else {
//      newNumber = Integer.parseInt(currentYearLastTwoNumber + "0000000");
//    }
//    return newNumber;
//  }

  public String phoneNumberLengthValidator(String number) {
    if (number.length() == 9) {
      number = "0".concat(number);
    }
    return number;
  }

  public void printAttributesInObject(Object obj) {
    System.out.println("================================================");
    Class<?> objClass = obj.getClass();
    Field[] fields = objClass.getDeclaredFields();

    for (Field field : fields) {
      field.setAccessible(true);
      String name = field.getName();
      Object value;
      try {
        value = field.get(obj);
      } catch (IllegalAccessException e) {
        value = null;
      }
      log.info("{} = {}", name, value);
    }
    System.out.println("================================================");

  }

  public boolean isValidEmail(String email) {
    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    Pattern pat = Pattern.compile(emailRegex);
    if (email == null) {
      return false;
    }
    return pat.matcher(email).matches();
  }

  public boolean isValidUrl(String urlString) {
    try {
      // Parse the URL
      URI uri = new URI(urlString);
      String scheme = uri.getScheme() != null ? uri.getScheme() : "http";
      String host = uri.getHost();

      if (host == null || host.isEmpty()) {
        return false; // Invalid URL if host is missing
      }

      // Ensure scheme is http or https
      if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
        return false;
      }

      // Convert http to https if needed
      if (scheme.equalsIgnoreCase("http")) {
        uri = new URI("https", uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
      }

      // Validate URL by checking if we can establish a connection
      URL url = uri.toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000); // 5 seconds timeout
      connection.setReadTimeout(5000);    // 5 seconds timeout

      int responseCode = connection.getResponseCode();
      return responseCode >= 200 && responseCode < 300; // Check for successful response
    } catch (URISyntaxException | IOException e) {
      System.err.println("URL is not valid: " + e.getMessage());
      return false;
    }
  }


  public String generateUniqCode(int length) {
    // Define the length of the random string
    if (length < 0) {
      length = 6;
    }
    // Define the characters to use in the random string
    String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Generate CustomAuditListener random string of the specified length
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int index = (int) (Math.random() * characters.length());
      sb.append(characters.charAt(index));
    }

    return sb.toString();
  }

//  public String generateEncryptCode(int length) {
//    return bcryptPasswordEncoder.encode(generateUniqCode(length));
//  }

  public String url_http_to_https(HttpServletRequest request) throws URISyntaxException {
    // Get the base URL from the request
    String requestUrl = request.getRequestURL().toString();
    String baseUrl = requestUrl.substring(0, requestUrl.length() - request.getRequestURI().length());

    // Parse the URL into URI
    URI uri = new URI(baseUrl);

    // Check if the scheme is http
    if (urlScheme.equals("https")) {
      // Convert http to https
      URI httpsUri = new URI("https", uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
      return httpsUri.toString();
    } else {
      return String.valueOf(uri);
    }
  }

  public String adjustUrlSchemeHttpToHttps(String url) {
    if (url != null) {
      if (urlScheme.equals("https") && url.startsWith("http://")) {
        // Change http to https
        return url.replaceFirst("http://", "https://");
      } else if (urlScheme.equals("http") && url.startsWith("https://")) {
        // Change https to http (for development or staging environments)
        return url.replaceFirst("https://", "http://");
      }
    }
    return url;  // Return the original URL if no changes are needed
  }

  public String firstLaterCapital(String name) {
    char firstChar = name.charAt(0);
    firstChar = Character.toUpperCase(firstChar);
    name = firstChar + name.substring(1);
    return name;
  }

  public void metaTag(Model model, String meta_keyword_new, String meta_content_new) {
    String meta_content = "Enhance your learning journey with our student-friendly web app. Test your knowledge and assess your progress through a wide range of multiple-choice questions (MCQs). Our intuitive platform allows teachers to effortlessly create customized test papers, while providing easy download options. Unlock your potential, boost your confidence, and achieve academic excellence with our seamless MCQ-based learning experience.";
    model.addAttribute("meta_content", meta_content + meta_content_new);
    String meta_keyword = "MCQ app, Online testing, Knowledge assessment, Exam preparation, Student evaluation,  Test creation, Academic tool, Learning platform, Test generator, Study aid Exam simulator, Progress tracking, Adaptive learning, Interactive quizzes, Student performance, Custom tests, Learning analytics, Exam readiness, Practice tool, Self-assessment";
    model.addAttribute("meta_keywords", meta_keyword + meta_keyword_new);
  }

//  public void setSession(HttpServletRequest request, List<SessionNameAndValue> sessionNameAndValues) {
//    HttpSession session = request.getSession();
//    for (SessionNameAndValue sessionNameAndValue : sessionNameAndValues) {
//      session.setAttribute(sessionNameAndValue.name(), sessionNameAndValue.value());
//    }
//
//  }

  public Object getValueFromCookies(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      // Loop through the cookies to find the one you're interested in
      for (Cookie cookie : cookies) {
        if (name.equals(cookie.getName())) {
          // Found the cookie, you can access its value
          return cookie.getValue();
        }
      }
    }
    return "";
  }

  public String getCurrentURL(HttpServletRequest request) {
    return ServletUriComponentsBuilder
        .fromCurrentRequestUri()
        .build()
        .toUriString();
  }

  public String stringEnglishFirstLatterCapital(String input) {
    String[] words = input.split(" ");
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < words.length; i++) {
      String word = words[i];
      String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
      result.append(capitalizedWord);
      if (i < words.length - 1) {
        result.append("_");
      }
    }
    return result.toString();
  }
}
