package in.adars.homeutils.web.keygen;

import in.adars.homeutils.utility.keygen.KeyGenService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/utils/keygen")
public class KeyGenController {

    private final KeyGenService keyGenService;

    public KeyGenController(KeyGenService keyGenService) {
        this.keyGenService = keyGenService;
    }

    @GetMapping
    public String showForm() {
        return "keygen/index";
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<?> generate(@RequestBody Map<String, String> body) {
        String type = body.getOrDefault("type", "").trim();
        int count = Math.min(50, Math.max(1, Integer.parseInt(body.getOrDefault("count", "1"))));
        String encoding = body.getOrDefault("encoding", "hex");
        int bytes = Math.min(512, Math.max(8, Integer.parseInt(body.getOrDefault("bytes", "32"))));

        try {
            List<String> keys = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                keys.add(generateOne(type, encoding, bytes));
            }
            return ResponseEntity.ok(Map.of("keys", keys));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String generateOne(String type, String encoding, int bytes) throws Exception {
        return switch (type) {
            case "hmac-sha256" -> keyGenService.generateHmacKey("HmacSHA256");
            case "hmac-sha384" -> keyGenService.generateHmacKey("HmacSHA384");
            case "hmac-sha512" -> keyGenService.generateHmacKey("HmacSHA512");
            case "uuid" -> keyGenService.generateUuid();
            case "uuid-nodash" -> keyGenService.generateUuidNoDashes();
            case "random" -> switch (encoding) {
                case "base64" -> keyGenService.generateRandomBase64(bytes);
                case "base64url" -> keyGenService.generateRandomBase64Url(bytes);
                default -> keyGenService.generateRandomHex(bytes);
            };
            default -> throw new IllegalArgumentException("Unknown key type: " + type);
        };
    }
}
