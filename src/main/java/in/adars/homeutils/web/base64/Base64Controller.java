package in.adars.homeutils.web.base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Controller
@RequestMapping("/utils/base64")
public class Base64Controller {

    @GetMapping
    public String showForm() {
        return "base64/index";
    }

    @PostMapping("/encode-text")
    @ResponseBody
    public ResponseEntity<?> encodeText(@RequestBody Map<String, String> body) {
        String input = body.getOrDefault("input", "");
        String encoded = Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
        return ResponseEntity.ok(Map.of("result", encoded));
    }

    @PostMapping("/decode-text")
    @ResponseBody
    public ResponseEntity<?> decodeText(@RequestBody Map<String, String> body) {
        try {
            String input = body.getOrDefault("input", "").trim();
            byte[] decoded = Base64.getDecoder().decode(input);
            return ResponseEntity.ok(Map.of("result", new String(decoded, StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid Base64: " + e.getMessage()));
        }
    }

    @PostMapping("/encode-file")
    @ResponseBody
    public ResponseEntity<?> encodeFile(@RequestParam("file") MultipartFile file) {
        try {
            String encoded = Base64.getEncoder().encodeToString(file.getBytes());
            return ResponseEntity.ok(Map.of("result", encoded, "filename", file.getOriginalFilename()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/decode-file")
    public ResponseEntity<byte[]> decodeFile(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "outputName", defaultValue = "decoded_file") String outputName) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8).trim();
            byte[] decoded = Base64.getDecoder().decode(content);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
                    .body(decoded);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
