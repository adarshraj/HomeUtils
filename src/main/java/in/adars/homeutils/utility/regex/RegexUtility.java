package in.adars.homeutils.utility.regex;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class RegexUtility implements Utility {
    public String getId() { return "regex"; }
    public String getName() { return "Regex Tester"; }
    public String getDescription() { return "Test Java regular expressions with live match highlighting and group capture."; }
    public String getRoute() { return "/utils/regex"; }
    public String getIconClass() { return "bi-asterisk"; }
}
