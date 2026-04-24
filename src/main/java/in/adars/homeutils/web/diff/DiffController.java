package in.adars.homeutils.web.diff;

import in.adars.homeutils.utility.diff.DiffService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/utils/diff")
public class DiffController {

    private final DiffService diffService;

    public DiffController(DiffService diffService) {
        this.diffService = diffService;
    }

    @GetMapping
    public String showForm() {
        return "diff/index";
    }

    @PostMapping("/compare")
    @ResponseBody
    public ResponseEntity<?> compare(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(diffService.diff(
                body.getOrDefault("left", ""),
                body.getOrDefault("right", "")
        ));
    }
}
