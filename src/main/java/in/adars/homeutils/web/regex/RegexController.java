package in.adars.homeutils.web.regex;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Controller
@RequestMapping("/utils/regex")
public class RegexController {

    @GetMapping
    public String showForm() {
        return "regex/index";
    }

    @PostMapping("/test")
    @ResponseBody
    public ResponseEntity<?> test(@RequestBody Map<String, Object> body) {
        String pattern = (String) body.getOrDefault("pattern", "");
        String input = (String) body.getOrDefault("input", "");
        @SuppressWarnings("unchecked")
        List<String> flags = (List<String>) body.getOrDefault("flags", List.of());
        String replacement = (String) body.get("replacement");

        int flagBits = 0;
        if (flags.contains("i")) flagBits |= Pattern.CASE_INSENSITIVE;
        if (flags.contains("m")) flagBits |= Pattern.MULTILINE;
        if (flags.contains("s")) flagBits |= Pattern.DOTALL;
        if (flags.contains("x")) flagBits |= Pattern.COMMENTS;
        if (flags.contains("u")) flagBits |= Pattern.UNICODE_CASE;

        try {
            Pattern p = Pattern.compile(pattern, flagBits);
            Matcher m = p.matcher(input);
            List<Map<String, Object>> matches = new ArrayList<>();
            while (m.find()) {
                Map<String, Object> match = new LinkedHashMap<>();
                match.put("match", m.group());
                match.put("start", m.start());
                match.put("end", m.end());
                List<String> groups = new ArrayList<>();
                for (int i = 1; i <= m.groupCount(); i++) {
                    groups.add(m.group(i));
                }
                match.put("groups", groups);
                matches.add(match);
            }
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("matches", matches);
            out.put("count", matches.size());
            if (replacement != null) {
                try {
                    out.put("replaced", p.matcher(input).replaceAll(replacement));
                } catch (Exception e) {
                    out.put("replaceError", e.getMessage());
                }
            }
            return ResponseEntity.ok(out);
        } catch (PatternSyntaxException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid regex: " + e.getDescription() + " at index " + e.getIndex()));
        }
    }
}
