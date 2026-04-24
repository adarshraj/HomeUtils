package in.adars.homeutils.utility.worldclock;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class WorldClockUtility implements Utility {
    public String getId() { return "worldclock"; }
    public String getName() { return "World Clock"; }
    public String getDescription() { return "Compare times across cities side-by-side with a scheduler-style hourly grid."; }
    public String getRoute() { return "/utils/worldclock"; }
    public String getIconClass() { return "bi-globe2"; }
}
