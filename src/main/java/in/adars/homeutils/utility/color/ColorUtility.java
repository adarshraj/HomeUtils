package in.adars.homeutils.utility.color;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class ColorUtility implements Utility {
    public String getId() { return "color"; }
    public String getName() { return "Color Tools"; }
    public String getDescription() { return "Convert between HEX, RGB, and HSL, and check WCAG contrast ratios."; }
    public String getRoute() { return "/utils/color"; }
    public String getIconClass() { return "bi-palette"; }
}
