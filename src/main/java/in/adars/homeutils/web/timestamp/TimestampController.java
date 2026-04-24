package in.adars.homeutils.web.timestamp;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/utils/timestamp")
public class TimestampController {

    @GetMapping
    public String showForm() {
        return "timestamp/index";
    }

    @PostMapping("/from-epoch")
    @ResponseBody
    public ResponseEntity<?> fromEpoch(@RequestBody Map<String, String> body) {
        try {
            String raw = body.getOrDefault("value", "").trim();
            String unit = body.getOrDefault("unit", "s");
            String zone = body.getOrDefault("zone", "UTC");
            long v = Long.parseLong(raw);
            Instant instant = switch (unit) {
                case "ms" -> Instant.ofEpochMilli(v);
                case "us" -> Instant.ofEpochSecond(v / 1_000_000L, (v % 1_000_000L) * 1_000L);
                default -> Instant.ofEpochSecond(v);
            };
            ZonedDateTime zdt = instant.atZone(ZoneId.of(zone));
            Map<String, String> out = new LinkedHashMap<>();
            out.put("iso", zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            out.put("utc", instant.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            out.put("local", zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
            out.put("rfc1123", zdt.format(DateTimeFormatter.RFC_1123_DATE_TIME));
            out.put("epochSeconds", String.valueOf(instant.getEpochSecond()));
            out.put("epochMillis", String.valueOf(instant.toEpochMilli()));
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input: " + e.getMessage()));
        }
    }

    @PostMapping("/from-date")
    @ResponseBody
    public ResponseEntity<?> fromDate(@RequestBody Map<String, String> body) {
        try {
            String raw = body.getOrDefault("value", "").trim();
            String zone = body.getOrDefault("zone", "UTC");
            Instant instant;
            try {
                instant = ZonedDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
            } catch (Exception ignore) {
                instant = java.time.LocalDateTime.parse(raw.replace(' ', 'T'))
                        .atZone(ZoneId.of(zone)).toInstant();
            }
            Map<String, String> out = new LinkedHashMap<>();
            out.put("epochSeconds", String.valueOf(instant.getEpochSecond()));
            out.put("epochMillis", String.valueOf(instant.toEpochMilli()));
            out.put("iso", instant.atZone(ZoneId.of(zone)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            out.put("utc", instant.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not parse date. Try ISO 8601 (e.g. 2026-04-23T10:30:00) or epoch input."));
        }
    }

    @GetMapping("/now")
    @ResponseBody
    public ResponseEntity<?> now() {
        Instant i = Instant.now();
        return ResponseEntity.ok(Map.of(
                "epochSeconds", i.getEpochSecond(),
                "epochMillis", i.toEpochMilli()
        ));
    }
}
