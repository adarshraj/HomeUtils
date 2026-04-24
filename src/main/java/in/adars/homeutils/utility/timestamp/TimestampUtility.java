package in.adars.homeutils.utility.timestamp;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class TimestampUtility implements Utility {
    public String getId() { return "timestamp"; }
    public String getName() { return "Timestamp Converter"; }
    public String getDescription() { return "Convert between Unix epoch timestamps and human-readable dates across timezones."; }
    public String getRoute() { return "/utils/timestamp"; }
    public String getIconClass() { return "bi-clock-history"; }
}
