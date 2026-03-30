package in.adars.homeutils.web;

import in.adars.homeutils.utility.UtilityRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UtilityRegistry registry;

    public HomeController(UtilityRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("utilities", registry.getAll());
        return "index";
    }
}
