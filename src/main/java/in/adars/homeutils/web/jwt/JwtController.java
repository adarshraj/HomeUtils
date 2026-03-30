package in.adars.homeutils.web.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Controller
@RequestMapping("/utils/jwt")
public class JwtController {

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z").withZone(ZoneId.systemDefault());

    @GetMapping
    public String showForm() {
        return "jwt/decode";
    }

    @PostMapping("/decode")
    @ResponseBody
    public ResponseEntity<?> decode(@RequestBody Map<String, String> body) {
        String token = body.getOrDefault("token", "").trim();
        if (token.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Token is empty."));

        String[] parts = token.split("\\.");
        if (parts.length < 2) return ResponseEntity.badRequest().body(Map.of("error", "Not a valid JWT (expected 3 dot-separated parts)."));

        try {
            String headerJson  = decodeB64(parts[0]);
            String payloadJson = decodeB64(parts[1]);

            String prettyHeader  = mapper.writeValueAsString(mapper.readTree(headerJson));
            String prettyPayload = mapper.writeValueAsString(mapper.readTree(payloadJson));

            // Extract expiry if present
            Map<?, ?> payloadMap = mapper.readValue(payloadJson, Map.class);
            String expiry = null;
            boolean expired = false;
            if (payloadMap.containsKey("exp")) {
                long exp = ((Number) payloadMap.get("exp")).longValue();
                expiry = DATE_FMT.format(Instant.ofEpochSecond(exp));
                expired = Instant.now().isAfter(Instant.ofEpochSecond(exp));
            }
            String issuedAt = null;
            if (payloadMap.containsKey("iat")) {
                long iat = ((Number) payloadMap.get("iat")).longValue();
                issuedAt = DATE_FMT.format(Instant.ofEpochSecond(iat));
            }

            return ResponseEntity.ok(Map.of(
                    "header", prettyHeader,
                    "payload", prettyPayload,
                    "signature", parts.length > 2 ? parts[2] : "",
                    "expiry", expiry != null ? expiry : "",
                    "issuedAt", issuedAt != null ? issuedAt : "",
                    "expired", expired
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to decode: " + e.getMessage()));
        }
    }

    private String decodeB64(String part) {
        // JWT uses URL-safe Base64 without padding
        byte[] bytes = Base64.getUrlDecoder().decode(pad(part));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String pad(String s) {
        return s + "=".repeat((4 - s.length() % 4) % 4);
    }
}
