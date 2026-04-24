package in.adars.homeutils.web.urlcodec;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequestMapping("/utils/urlcodec")
public class UrlCodecController {

    @GetMapping
    public String showForm() {
        return "urlcodec/index";
    }

    @PostMapping("/encode")
    @ResponseBody
    public ResponseEntity<?> encode(@RequestBody Map<String, String> body) {
        String input = body.getOrDefault("input", "");
        String encoded = URLEncoder.encode(input, StandardCharsets.UTF_8);
        return ResponseEntity.ok(Map.of("result", encoded));
    }

    @PostMapping("/decode")
    @ResponseBody
    public ResponseEntity<?> decode(@RequestBody Map<String, String> body) {
        try {
            String input = body.getOrDefault("input", "");
            return ResponseEntity.ok(Map.of("result", URLDecoder.decode(input, StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid URL-encoded input: " + e.getMessage()));
        }
    }
}
