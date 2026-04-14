package in.adars.homeutils.utility.docconvert;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class DocConvertService {

    private final Parser mdParser;
    private final HtmlRenderer htmlRenderer;

    public DocConvertService() {
        MutableDataSet options = new MutableDataSet();
        mdParser = Parser.builder(options).build();
        htmlRenderer = HtmlRenderer.builder(options).build();
    }

    /**
     * Markdown -> HTML string
     */
    public String markdownToHtml(String markdown) {
        Node doc = mdParser.parse(markdown);
        return htmlRenderer.render(doc);
    }

    /**
     * Markdown -> PDF bytes
     */
    public byte[] markdownToPdf(String markdown) throws Exception {
        String html = markdownToHtml(markdown);
        String fullHtml = wrapHtml(html);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(fullHtml, null);
        builder.toStream(out);
        builder.run();
        return out.toByteArray();
    }

    /**
     * Markdown -> DOCX bytes
     */
    public byte[] markdownToDocx(String markdown) throws Exception {
        String html = markdownToHtml(markdown);
        WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        MainDocumentPart main = pkg.getMainDocumentPart();
        main.addAltChunk(
                org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType.Xhtml,
                wrapHtml(html).getBytes("UTF-8"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pkg.save(out);
        return out.toByteArray();
    }

    /**
     * DOCX bytes -> Markdown string (best-effort plain text extraction with basic formatting)
     */
    public String docxToMarkdown(byte[] docxBytes) throws Exception {
        WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docxBytes));
        List<Object> bodyElements = pkg.getMainDocumentPart().getContent();
        StringBuilder sb = new StringBuilder();
        for (Object obj : bodyElements) {
            if (obj instanceof P p) {
                String style = getParagraphStyle(p);
                String text = extractText(p);
                if (text.isBlank()) {
                    sb.append("\n");
                    continue;
                }
                switch (style) {
                    case "Heading1" -> sb.append("# ").append(text).append("\n\n");
                    case "Heading2" -> sb.append("## ").append(text).append("\n\n");
                    case "Heading3" -> sb.append("### ").append(text).append("\n\n");
                    case "Heading4" -> sb.append("#### ").append(text).append("\n\n");
                    case "ListParagraph" -> sb.append("- ").append(text).append("\n");
                    default -> sb.append(text).append("\n\n");
                }
            }
        }
        return sb.toString().replaceAll("\n{3,}", "\n\n").trim();
    }

    /**
     * DOCX bytes -> PDF bytes (via markdown -> HTML -> PDF)
     */
    public byte[] docxToPdf(byte[] docxBytes) throws Exception {
        String md = docxToMarkdown(docxBytes);
        return markdownToPdf(md);
    }

    private String getParagraphStyle(P p) {
        if (p.getPPr() != null && p.getPPr().getPStyle() != null) {
            return p.getPPr().getPStyle().getVal();
        }
        return "";
    }

    private String extractText(P p) {
        StringBuilder sb = new StringBuilder();
        for (Object content : p.getContent()) {
            Object unwrapped = content;
            if (content instanceof JAXBElement<?> jaxb) {
                unwrapped = jaxb.getValue();
            }
            if (unwrapped instanceof R r) {
                for (Object rc : r.getContent()) {
                    Object unwrappedRc = rc;
                    if (rc instanceof JAXBElement<?> jaxb) {
                        unwrappedRc = jaxb.getValue();
                    }
                    if (unwrappedRc instanceof Text t) {
                        sb.append(t.getValue());
                    }
                }
            }
        }
        return sb.toString();
    }

    private String wrapHtml(String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8"/>
                <style>
                body { font-family: sans-serif; font-size: 12pt; line-height: 1.6; margin: 40px; color: #333; }
                h1 { font-size: 24pt; margin-top: 20pt; }
                h2 { font-size: 20pt; margin-top: 16pt; }
                h3 { font-size: 16pt; margin-top: 12pt; }
                code { background: #f4f4f4; padding: 2px 4px; font-size: 90%%; }
                pre { background: #f4f4f4; padding: 12px; overflow-x: auto; }
                pre code { padding: 0; }
                blockquote { border-left: 3px solid #ccc; padding-left: 12px; color: #666; }
                table { border-collapse: collapse; width: 100%%; }
                th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                </style>
                </head>
                <body>
                """ + bodyHtml + """
                </body>
                </html>
                """;
    }
}
