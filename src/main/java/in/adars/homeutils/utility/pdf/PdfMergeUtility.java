package in.adars.homeutils.utility.pdf;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class PdfMergeUtility implements Utility {
    public String getId() { return "pdfmerge"; }
    public String getName() { return "PDF Merge"; }
    public String getDescription() { return "Combine multiple PDF files into a single document."; }
    public String getRoute() { return "/utils/pdfmerge"; }
    public String getIconClass() { return "bi-file-earmark-plus"; }
}
