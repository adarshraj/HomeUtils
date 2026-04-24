package in.adars.homeutils.web.pdf;

import in.adars.homeutils.utility.pdf.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/utils/pdfsplit")
public class PdfSplitController {

    private final PdfService pdfService;

    public PdfSplitController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping
    public String showForm() {
        return "pdf/pdfsplit";
    }

    @PostMapping("/split")
    public ResponseEntity<?> split(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("No file uploaded.");
        try {
            byte[] zip = pdfService.splitToZip(file);
            String base = file.getOriginalFilename() == null ? "split" :
                    file.getOriginalFilename().replaceAll("\\.pdf$", "");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + base + "_pages.zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zip);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }
}
