import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.*;

public class EvalJMeterTestResults {
    public static void main(final String[] args) throws Exception {
        var content = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
        System.out.format("JMeter results from %s: ", args[0]);
        System.out.println(content);

        var pattern = Pattern.compile("summary\\s=.+Err:\\s*(\\d+)");

        var matcher = pattern.matcher(content);
        if (matcher.find()) {
            var count = Integer.parseInt(matcher.group(1));
            if (count > 0) {
                System.out.format("JMeter tests contain %d error(s)s.\n", count);
                System.exit(1);
            }
        }
        System.out.println("JMeter tests pass successfully.");
        System.exit(0);
    }
}
