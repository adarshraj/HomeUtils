package in.adars.homeutils.utility.diff;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiffService {

    public Map<String, Object> diff(String left, String right) {
        List<String> leftLines = List.of(left.split("\n", -1));
        List<String> rightLines = List.of(right.split("\n", -1));
        Patch<String> patch = DiffUtils.diff(leftLines, rightLines);

        List<Map<String, Object>> deltas = new ArrayList<>();
        int added = 0, removed = 0, changed = 0;
        for (AbstractDelta<String> d : patch.getDeltas()) {
            Map<String, Object> delta = new LinkedHashMap<>();
            delta.put("type", d.getType().name());
            delta.put("sourceStart", d.getSource().getPosition());
            delta.put("sourceLines", d.getSource().getLines());
            delta.put("targetStart", d.getTarget().getPosition());
            delta.put("targetLines", d.getTarget().getLines());
            deltas.add(delta);
            switch (d.getType()) {
                case INSERT -> added += d.getTarget().size();
                case DELETE -> removed += d.getSource().size();
                case CHANGE -> { removed += d.getSource().size(); added += d.getTarget().size(); changed++; }
                default -> {}
            }
        }
        return Map.of(
                "deltas", deltas,
                "added", added,
                "removed", removed,
                "changed", changed,
                "leftLines", leftLines,
                "rightLines", rightLines
        );
    }
}
