package in.adars.homeutils.web.image;

import in.adars.homeutils.utility.image.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/utils/image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public String showForm() {
        return "image/index";
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convert(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "format", defaultValue = "jpg") String format,
                                     @RequestParam(value = "quality", defaultValue = "0.85") float quality,
                                     @RequestParam(value = "maxWidth", required = false) Integer maxWidth) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("No file uploaded.");
        try {
            ImageService.ConversionResult result = imageService.convert(file, format, quality, maxWidth);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                    .contentType(MediaType.parseMediaType(result.mimeType()))
                    .body(result.data());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }
}
