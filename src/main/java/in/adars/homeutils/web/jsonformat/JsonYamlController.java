package in.adars.homeutils.web.jsonformat;

import in.adars.homeutils.utility.jsonformat.JsonYamlService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/utils/jsonformat")
public class JsonYamlController {

    private final JsonYamlService service;

    public JsonYamlController(JsonYamlService service) {
        this.service = service;
    }

    @GetMapping
    public String showForm() {
        return "jsonformat/index";
    }

    @PostMapping("/format")
    @ResponseBody
    public ResponseEntity<?> format(@RequestBody Map<String, String> body) {
        String input = body.getOrDefault("input", "");
        if (input.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Input is empty."));
        JsonYamlService.FormatResult result = service.format(input);
        if (result.error() != null) return ResponseEntity.badRequest().body(Map.of("error", result.error()));
        return ResponseEntity.ok(Map.of("output", result.output(), "converted", result.converted()));
    }

    @PostMapping("/diff")
    @ResponseBody
    public ResponseEntity<?> diff(@RequestBody Map<String, String> body) {
        String left  = body.getOrDefault("left", "");
        String right = body.getOrDefault("right", "");
        if (left.isBlank() || right.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Both inputs are required."));
        JsonYamlService.DiffResult result = service.diff(left, right);
        if (result.error() != null) return ResponseEntity.badRequest().body(Map.of("error", result.error()));
        return ResponseEntity.ok(Map.of("html", result.html(), "added", result.added(), "removed", result.removed()));
    }
}
