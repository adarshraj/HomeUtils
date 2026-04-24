package in.adars.homeutils.web;

import in.adars.homeutils.utility.Utility;
import in.adars.homeutils.utility.UtilityCategorizer;
import in.adars.homeutils.utility.UtilityRegistry;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UtilityRegistry registry;

    public GlobalModelAdvice(UtilityRegistry registry) {
        this.registry = registry;
    }

    @ModelAttribute("sidebarGroups")
    public List<Map<String, Object>> sidebarGroups() {
        Map<String, List<Utility>> byCat = registry.getAll().stream()
                .collect(Collectors.groupingBy(UtilityCategorizer::categoryOf, LinkedHashMap::new, Collectors.toList()));
        List<Map.Entry<String, List<Utility>>> entries = new ArrayList<>(byCat.entrySet());
        entries.sort(Comparator.comparingInt(e -> UtilityCategorizer.orderOf(e.getKey())));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, List<Utility>> e : entries) {
            List<Utility> items = new ArrayList<>(e.getValue());
            items.sort(Comparator.comparing(Utility::getName, String.CASE_INSENSITIVE_ORDER));
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("name", e.getKey());
            group.put("items", items);
            out.add(group);
        }
        return out;
    }
}
