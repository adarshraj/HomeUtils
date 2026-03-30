package in.adars.homeutils.utility.jsonformat;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class JsonYamlUtility implements Utility {
    public String getId() { return "jsonformat"; }
    public String getName() { return "JSON / YAML"; }
    public String getDescription() { return "Format, validate, convert between JSON and YAML, and diff two documents."; }
    public String getRoute() { return "/utils/jsonformat"; }
    public String getIconClass() { return "bi-code-square"; }
}
