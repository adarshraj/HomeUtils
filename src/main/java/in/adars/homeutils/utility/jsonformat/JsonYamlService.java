package in.adars.homeutils.utility.jsonformat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class JsonYamlService {

    private final ObjectMapper jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final ObjectMapper yamlMapper = new ObjectMapper(
            new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

    public record FormatResult(String output, String converted, String error) {}
    public record DiffResult(String html, int added, int removed, String error) {}

    /** Parse input (auto-detect JSON or YAML), return pretty JSON + pretty YAML. */
    public FormatResult format(String input) {
        try {
            JsonNode node = parse(input.trim());
            String prettyJson = jsonMapper.writeValueAsString(node);
            String prettyYaml = yamlMapper.writeValueAsString(node);
            // Return same format as input, converted to the other
            boolean isYaml = !input.trim().startsWith("{") && !input.trim().startsWith("[");
            return new FormatResult(
                    isYaml ? prettyYaml : prettyJson,
                    isYaml ? prettyJson : prettyYaml,
                    null);
        } catch (Exception e) {
            return new FormatResult(null, null, e.getMessage());
        }
    }

    /** Compute a unified HTML diff between two JSON/YAML documents. */
    public DiffResult diff(String left, String right) {
        try {
            JsonNode nodeA = parse(left.trim());
            JsonNode nodeB = parse(right.trim());
            String prettyA = jsonMapper.writeValueAsString(nodeA);
            String prettyB = jsonMapper.writeValueAsString(nodeB);

            List<String> linesA = Arrays.asList(prettyA.split("\n"));
            List<String> linesB = Arrays.asList(prettyB.split("\n"));

            Patch<String> patch = DiffUtils.diff(linesA, linesB);

            StringBuilder html = new StringBuilder("<pre class='diff-output mb-0'>");
            int added = 0, removed = 0;

            // Build a line-by-line annotated view
            int[] posA = {0};
            for (AbstractDelta<String> delta : patch.getDeltas()) {
                // unchanged lines before this delta
                while (posA[0] < delta.getSource().getPosition()) {
                    html.append("<span class='diff-ctx'>").append(escape(linesA.get(posA[0]))).append("\n</span>");
                    posA[0]++;
                }
                for (String line : delta.getSource().getLines()) {
                    html.append("<span class='diff-del'>- ").append(escape(line)).append("\n</span>");
                    posA[0]++;
                    removed++;
                }
                for (String line : delta.getTarget().getLines()) {
                    html.append("<span class='diff-add'>+ ").append(escape(line)).append("\n</span>");
                    added++;
                }
            }
            while (posA[0] < linesA.size()) {
                html.append("<span class='diff-ctx'>").append(escape(linesA.get(posA[0]))).append("\n</span>");
                posA[0]++;
            }
            html.append("</pre>");
            return new DiffResult(html.toString(), added, removed, null);
        } catch (Exception e) {
            return new DiffResult(null, 0, 0, e.getMessage());
        }
    }

    private JsonNode parse(String input) throws Exception {
        if (input.startsWith("{") || input.startsWith("[")) {
            return jsonMapper.readTree(input);
        }
        return yamlMapper.readTree(input);
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
