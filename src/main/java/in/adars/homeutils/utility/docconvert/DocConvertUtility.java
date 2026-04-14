package in.adars.homeutils.utility.docconvert;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class DocConvertUtility implements Utility {
    public String getId() { return "docconvert"; }
    public String getName() { return "Document Converter"; }
    public String getDescription() { return "Convert between Markdown, DOCX, PDF, and HTML formats."; }
    public String getRoute() { return "/utils/docconvert"; }
    public String getIconClass() { return "bi-file-earmark-richtext"; }
}
