package in.adars.homeutils.utility.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfService {

    /** Combine multiple uploaded images into a single PDF, one image per page. */
    public byte[] imagesToPdf(List<MultipartFile> files) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            for (MultipartFile file : files) {
                BufferedImage image;
                try (InputStream in = file.getInputStream()) {
                    image = ImageIO.read(in);
                }
                if (image == null) continue;

                // Convert to RGB (PDFBox LosslessFactory requires RGB or ARGB)
                if (image.getType() != BufferedImage.TYPE_INT_RGB &&
                    image.getType() != BufferedImage.TYPE_INT_ARGB) {
                    BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                    rgb.createGraphics().drawImage(image, 0, 0, null);
                    image = rgb;
                }

                PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                doc.addPage(page);
                PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());
                }
            }

            doc.save(baos);
            return baos.toByteArray();
        }
    }

    /** Render each page of a PDF as a PNG. Returns raw PNG if single page, ZIP otherwise. */
    public PdfToImagesResult pdfToImages(MultipartFile file, int dpi) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();

            if (pageCount == 1) {
                BufferedImage image = renderer.renderImageWithDPI(0, dpi, ImageType.RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", baos);
                String name = stripExtension(file.getOriginalFilename()) + ".png";
                return new PdfToImagesResult(name, baos.toByteArray(), false);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                String base = stripExtension(file.getOriginalFilename());
                for (int i = 0; i < pageCount; i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                    ByteArrayOutputStream pageBaos = new ByteArrayOutputStream();
                    ImageIO.write(image, "PNG", pageBaos);
                    zos.putNextEntry(new ZipEntry(base + "_page" + (i + 1) + ".png"));
                    zos.write(pageBaos.toByteArray());
                    zos.closeEntry();
                }
            }
            String name = stripExtension(file.getOriginalFilename()) + "_pages.zip";
            return new PdfToImagesResult(name, baos.toByteArray(), true);
        }
    }

    public record PdfToImagesResult(String fileName, byte[] data, boolean isZip) {}

    /** Merge multiple PDFs into a single PDF, preserving order. */
    public byte[] merge(List<MultipartFile> files) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        merger.setDestinationStream(baos);
        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;
            merger.addSource(new RandomAccessReadBuffer(f.getBytes()));
        }
        merger.mergeDocuments(null);
        return baos.toByteArray();
    }

    /** Split a PDF into individual single-page PDFs packaged as a ZIP. */
    public byte[] splitToZip(MultipartFile file) throws IOException {
        try (PDDocument src = Loader.loadPDF(file.getBytes())) {
            String base = stripExtension(file.getOriginalFilename());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                int pageCount = src.getNumberOfPages();
                for (int i = 0; i < pageCount; i++) {
                    try (PDDocument page = new PDDocument()) {
                        page.addPage(src.getPage(i));
                        ByteArrayOutputStream pageOut = new ByteArrayOutputStream();
                        page.save(pageOut);
                        zos.putNextEntry(new ZipEntry(base + "_page" + (i + 1) + ".pdf"));
                        zos.write(pageOut.toByteArray());
                        zos.closeEntry();
                    }
                }
            }
            return baos.toByteArray();
        }
    }

    /**
     * Compress a PDF by rasterizing each page to JPEG at the given DPI and quality.
     * Trade-off: text becomes non-selectable; great for image-heavy/scanned PDFs.
     */
    public byte[] compress(MultipartFile file, int dpi, float jpegQuality) throws IOException {
        try (PDDocument src = Loader.loadPDF(file.getBytes());
             PDDocument out = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDFRenderer renderer = new PDFRenderer(src);
            int pageCount = src.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                PDImageXObject pdImage = JPEGFactory.createFromImage(out, image, jpegQuality);

                PDRectangle original = src.getPage(i).getMediaBox();
                PDPage newPage = new PDPage(new PDRectangle(original.getWidth(), original.getHeight()));
                out.addPage(newPage);
                try (PDPageContentStream cs = new PDPageContentStream(out, newPage)) {
                    cs.drawImage(pdImage, 0, 0, original.getWidth(), original.getHeight());
                }
            }
            out.save(baos);
            return baos.toByteArray();
        }
    }

    private String stripExtension(String name) {
        if (name == null) return "output";
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
