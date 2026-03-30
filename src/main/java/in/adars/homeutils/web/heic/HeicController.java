package in.adars.homeutils.web.heic;

import in.adars.homeutils.utility.heic.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/utils/heic")
public class HeicController {

    private final ConversionService conversionService;

    public HeicController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("formats", ConversionService.SUPPORTED_FORMATS);
        return "heic/convert";
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convert(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "format", defaultValue = "JPEG") String format,
            @RequestParam(value = "suffix", defaultValue = "_conv") String suffix,
            @RequestParam(value = "zipPassword", required = false) String zipPassword) {

        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            return ResponseEntity.badRequest().body("No files uploaded.");
        }

        try {
            ConversionService.ConversionResponse response = conversionService.convert(files, format.toUpperCase(), suffix, zipPassword);
            MediaType contentType = response.isZip()
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.APPLICATION_OCTET_STREAM;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.fileName() + "\"")
                    .contentType(contentType)
                    .body(response.data());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Conversion failed: " + e.getMessage());
        }
    }

    private String stripExtension(String name) {
        if (name == null) return "converted";
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
