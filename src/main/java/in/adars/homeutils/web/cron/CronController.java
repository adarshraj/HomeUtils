package in.adars.homeutils.web.cron;

import in.adars.homeutils.utility.cron.CronService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/utils/cron")
public class CronController {

    private final CronService cronService;

    public CronController(CronService cronService) {
        this.cronService = cronService;
    }

    @GetMapping
    public String showForm() {
        return "cron/helper";
    }

    @PostMapping("/describe")
    @ResponseBody
    public ResponseEntity<?> describe(@RequestBody Map<String, String> body) {
        String expression = body.getOrDefault("expression", "").trim();
        String cronType   = body.getOrDefault("type", "UNIX");
        int nextCount     = Integer.parseInt(body.getOrDefault("count", "10"));
        if (expression.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Expression is empty."));
        CronService.CronResult result = cronService.describe(expression, cronType, nextCount);
        if (result.error() != null) return ResponseEntity.badRequest().body(Map.of("error", result.error()));
        return ResponseEntity.ok(Map.of("description", result.description(), "nextRuns", result.nextRuns()));
    }
}
