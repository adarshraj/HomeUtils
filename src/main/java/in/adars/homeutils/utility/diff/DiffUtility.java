package in.adars.homeutils.utility.diff;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class DiffUtility implements Utility {
    public String getId() { return "diff"; }
    public String getName() { return "Text Diff"; }
    public String getDescription() { return "Compare two texts side-by-side with line-level added/removed highlights."; }
    public String getRoute() { return "/utils/diff"; }
    public String getIconClass() { return "bi-file-diff"; }
}
