package in.adars.homeutils.utility.timecalc;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class TimeCalcUtility implements Utility {
    public String getId() { return "timecalc"; }
    public String getName() { return "Time Calculator"; }
    public String getDescription() { return "Add or subtract durations, and compute the difference between two dates."; }
    public String getRoute() { return "/utils/timecalc"; }
    public String getIconClass() { return "bi-calculator"; }
}
