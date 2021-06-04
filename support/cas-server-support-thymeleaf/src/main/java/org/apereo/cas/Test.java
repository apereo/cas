package org.apereo.cas;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * This is {@link Test}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class Test {
    public static void main(String[] args) throws Exception {
        var r = new HashMap<>();
        var file = new ClassPathResource("cas-theme-default.properties").getFile();
        var lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
        var it = lines.iterator();

        var pattern = Pattern.compile("#*\\s*(cas.+)=(\\S+)*");
        var comments = new StringBuilder();
        while (it.hasNext()) {
            var ln = it.next();
            var matcher = pattern.matcher(ln);
            if (matcher.find()) {
                var prop = matcher.group(1);
                var value = matcher.group(2);
                var comm = comments.toString().stripLeading().trim();

                System.out.println(comm);
                System.out.println(prop + " " + value);
                System.out.println("---------------");
                
                comments = new StringBuilder();
            } else {
                ln = ln.replace("# ", " ");
                comments.append(ln);
            }
            it.remove();
        }
    }
}
