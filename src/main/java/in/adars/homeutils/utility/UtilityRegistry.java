package in.adars.homeutils.utility;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UtilityRegistry {

    private final List<Utility> utilities;

    public UtilityRegistry(List<Utility> utilities) {
        this.utilities = utilities;
    }

    public List<Utility> getAll() {
        return utilities;
    }
}
