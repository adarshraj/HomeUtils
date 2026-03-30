package in.adars.homeutils.utility.cron;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CronService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z");

    public record CronResult(String description, List<String> nextRuns, String error) {}

    public CronResult describe(String expression, String cronType, int nextCount) {
        try {
            CronType type = switch (cronType.toUpperCase()) {
                case "QUARTZ" -> CronType.QUARTZ;
                case "SPRING" -> CronType.SPRING;
                default -> CronType.UNIX;
            };
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(type));
            Cron cron = parser.parse(expression.trim());
            cron.validate();

            String description = CronDescriptor.instance(Locale.ENGLISH).describe(cron);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            ZonedDateTime now = ZonedDateTime.now();

            List<String> nextRuns = new ArrayList<>();
            ZonedDateTime cursor = now;
            for (int i = 0; i < nextCount; i++) {
                Optional<ZonedDateTime> next = executionTime.nextExecution(cursor);
                if (next.isEmpty()) break;
                nextRuns.add(next.get().format(FORMATTER));
                cursor = next.get();
            }
            return new CronResult(description, nextRuns, null);
        } catch (Exception e) {
            return new CronResult(null, List.of(), e.getMessage());
        }
    }
}
