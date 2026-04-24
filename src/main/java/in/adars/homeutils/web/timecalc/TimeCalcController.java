package in.adars.homeutils.web.timecalc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/utils/timecalc")
public class TimeCalcController {

    @GetMapping
    public String page() {
        return "timecalc/index";
    }
}
