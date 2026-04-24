package in.adars.homeutils.utility.pdf;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class PdfSplitUtility implements Utility {
    public String getId() { return "pdfsplit"; }
    public String getName() { return "PDF Split"; }
    public String getDescription() { return "Split a PDF into individual single-page files, delivered as a ZIP."; }
    public String getRoute() { return "/utils/pdfsplit"; }
    public String getIconClass() { return "bi-scissors"; }
}
