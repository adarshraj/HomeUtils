package in.adars.homeutils.web.pdf;

import in.adars.homeutils.utility.pdf.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/utils/pdfmerge")
public class PdfMergeController {

    private final PdfService pdfService;

    public PdfMergeController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping
    public String showForm() {
        return "pdf/pdfmerge";
    }

    @PostMapping("/merge")
    public ResponseEntity<?> merge(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.size() < 2) {
            return ResponseEntity.badRequest().body("Upload at least two PDF files.");
        }
        try {
            byte[] merged = pdfService.merge(files);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"merged.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(merged);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }
}
