package in.adars.homeutils.web.docconvert;

import in.adars.homeutils.utility.docconvert.DocConvertService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequestMapping("/utils/docconvert")
public class DocConvertController {

    private final DocConvertService service;

    public DocConvertController(DocConvertService service) {
        this.service = service;
    }

    @GetMapping
    public String showForm() {
        return "docconvert/index";
    }

    @PostMapping("/md-to-html")
    @ResponseBody
    public ResponseEntity<?> mdToHtml(@RequestBody Map<String, String> body) {
        String md = body.getOrDefault("markdown", "");
        if (md.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Markdown content is empty."));
        String html = service.markdownToHtml(md);
        return ResponseEntity.ok(Map.of("html", html));
    }

    @PostMapping("/md-to-pdf")
    public ResponseEntity<byte[]> mdToPdf(@RequestBody Map<String, String> body) {
        try {
            String md = body.getOrDefault("markdown", "");
            if (md.isBlank()) return ResponseEntity.badRequest().build();
            byte[] pdf = service.markdownToPdf(md);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/md-to-docx")
    public ResponseEntity<byte[]> mdToDocx(@RequestBody Map<String, String> body) {
        try {
            String md = body.getOrDefault("markdown", "");
            if (md.isBlank()) return ResponseEntity.badRequest().build();
            byte[] docx = service.markdownToDocx(md);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.docx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(docx);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/docx-to-md")
    @ResponseBody
    public ResponseEntity<?> docxToMd(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded."));
            String md = service.docxToMarkdown(file.getBytes());
            return ResponseEntity.ok(Map.of("markdown", md));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to convert: " + e.getMessage()));
        }
    }

    @PostMapping("/docx-to-pdf")
    public ResponseEntity<byte[]> docxToPdf(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().build();
            byte[] pdf = service.docxToPdf(file.getBytes());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
