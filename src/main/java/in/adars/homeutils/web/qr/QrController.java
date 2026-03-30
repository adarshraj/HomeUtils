package in.adars.homeutils.web.qr;

import in.adars.homeutils.utility.qr.QrService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@Controller
@RequestMapping("/utils/qr")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping
    public String showForm() {
        return "qr/generate";
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<?> generate(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "").trim();
        if (content.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Content is empty."));
        int size = Math.min(1024, Math.max(64, Integer.parseInt(body.getOrDefault("size", "300"))));
        try {
            byte[] png = qrService.generate(content, size);
            String dataUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
            return ResponseEntity.ok(Map.of("dataUrl", dataUrl, "pngBase64", Base64.getEncoder().encodeToString(png)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> download(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "").trim();
        int size = Math.min(1024, Math.max(64, Integer.parseInt(body.getOrDefault("size", "300"))));
        try {
            byte[] png = qrService.generate(content, size);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"qrcode.png\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(png);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
