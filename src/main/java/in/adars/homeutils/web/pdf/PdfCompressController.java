package in.adars.homeutils.web.pdf;

import in.adars.homeutils.utility.pdf.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/utils/pdfcompress")
public class PdfCompressController {

    private final PdfService pdfService;

    public PdfCompressController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping
    public String showForm() {
        return "pdf/pdfcompress";
    }

    @PostMapping("/compress")
    public ResponseEntity<?> compress(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "dpi", defaultValue = "120") int dpi,
                                      @RequestParam(value = "quality", defaultValue = "0.6") float quality) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("No file uploaded.");
        int safeDpi = Math.min(300, Math.max(50, dpi));
        float safeQuality = Math.min(1.0f, Math.max(0.1f, quality));
        try {
            byte[] compressed = pdfService.compress(file, safeDpi, safeQuality);
            String base = file.getOriginalFilename() == null ? "compressed" :
                    file.getOriginalFilename().replaceAll("\\.pdf$", "");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + base + "_compressed.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(compressed);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }
}
