package in.adars.homeutils.web.hash;

import in.adars.homeutils.utility.hash.HashService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequestMapping("/utils/hash")
public class HashController {

    private final HashService hashService;

    public HashController(HashService hashService) {
        this.hashService = hashService;
    }

    @GetMapping
    public String showForm() {
        return "hash/index";
    }

    @PostMapping("/text")
    @ResponseBody
    public ResponseEntity<?> hashText(@RequestBody Map<String, String> body) {
        String input = body.getOrDefault("input", "");
        return ResponseEntity.ok(Map.of("hashes", hashService.hashAll(input.getBytes(StandardCharsets.UTF_8))));
    }

    @PostMapping("/file")
    @ResponseBody
    public ResponseEntity<?> hashFile(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(Map.of(
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "hashes", hashService.hashAll(file.getBytes())
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
