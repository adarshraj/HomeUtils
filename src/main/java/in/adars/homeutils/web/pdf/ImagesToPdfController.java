package in.adars.homeutils.web.pdf;

import in.adars.homeutils.utility.pdf.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/utils/img2pdf")
public class ImagesToPdfController {

    private final PdfService pdfService;

    public ImagesToPdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping
    public String showForm() {
        return "pdf/img2pdf";
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convert(@RequestParam("files") List<MultipartFile> files,
                                     @RequestParam(value = "outputName", defaultValue = "combined") String outputName) {
        if (files == null || files.stream().allMatch(MultipartFile::isEmpty))
            return ResponseEntity.badRequest().body("No files uploaded.");
        try {
            byte[] pdf = pdfService.imagesToPdf(files);
            String name = outputName.isBlank() ? "combined" : outputName;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + ".pdf\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }
}
