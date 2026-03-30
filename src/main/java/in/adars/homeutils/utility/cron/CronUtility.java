package in.adars.homeutils.utility.cron;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class CronUtility implements Utility {
    public String getId() { return "cron"; }
    public String getName() { return "Cron Helper"; }
    public String getDescription() { return "Parse cron expressions — human-readable description and next run times."; }
    public String getRoute() { return "/utils/cron"; }
    public String getIconClass() { return "bi-clock"; }
}
