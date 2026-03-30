package in.adars.homeutils.web.pdf;

import in.adars.homeutils.utility.pdf.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/utils/pdf2img")
public class PdfToImagesController {

    private final PdfService pdfService;

    public PdfToImagesController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping
    public String showForm() {
        return "pdf/pdf2img";
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convert(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "dpi", defaultValue = "150") int dpi) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("No file uploaded.");
        try {
            PdfService.PdfToImagesResult result = pdfService.pdfToImages(file, Math.min(300, Math.max(72, dpi)));
            MediaType type = result.isZip() ? MediaType.APPLICATION_OCTET_STREAM : MediaType.IMAGE_PNG;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                    .contentType(type)
                    .body(result.data());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }
}
