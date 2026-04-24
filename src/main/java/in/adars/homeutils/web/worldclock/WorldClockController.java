package in.adars.homeutils.web.worldclock;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/utils/worldclock")
public class WorldClockController {

    @GetMapping
    public String page(Model model) {
        List<String> zones = ZoneId.getAvailableZoneIds().stream()
                .filter(z -> z.contains("/") && !z.startsWith("Etc/") && !z.startsWith("SystemV/"))
                .sorted()
                .collect(Collectors.toList());
        model.addAttribute("zones", zones);
        return "worldclock/index";
    }

    @GetMapping("/zones")
    @ResponseBody
    public List<String> zones() {
        return ZoneId.getAvailableZoneIds().stream()
                .filter(z -> z.contains("/") && !z.startsWith("Etc/") && !z.startsWith("SystemV/"))
                .sorted()
                .collect(Collectors.toList());
    }
}
