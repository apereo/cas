package org.apereo.cas.gradle;

import org.gradle.api.artifacts.transform.CacheableTransform;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Removes the files CAS never serves from WebJars on the runtime class path of the web applications.
 *
 * <p>CAS serves a handful of files from each WebJar (see the {@code webjars.*} keys of
 * {@code cas_common_messages.properties}), while the WebJars ship complete distributions: source maps,
 * TypeScript declarations, SCSS sources, examples, icon metadata, SVG sets, alternative themes and legacy
 * font formats. Source maps and TypeScript declarations are removed from every WebJar; the other rules
 * apply to the named WebJar only. License and notice files are always kept. Jars without such entries,
 * WebJars included, pass through untouched.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@CacheableTransform
public abstract class WebjarTrimTransform implements TransformAction<TransformParameters.None> {
    private static final String WEBJARS_PREFIX = "META-INF/resources/webjars/";

    private static final Pattern NOT_SERVED_BY_ANY_WEBJAR = Pattern.compile("\\.map$|\\.d\\.m?ts$");

    private static final Map<String, Pattern> NOT_SERVED_BY_WEBJAR = Map.of(
        "font-awesome", Pattern.compile("^(js|svgs|svgs-full|sprites|sprites-full|metadata|scss|less)/"),
        "datatables", Pattern.compile("^(examples|types)/|\\.mjs$"),
        "bootstrap", Pattern.compile("^scss/|\\.gz$"),
        "mdi__font", Pattern.compile("^(scss|scripts|\\.github)/|^preview\\.html$|\\.(eot|ttf|woff)$"),
        "jquery-ui", Pattern.compile("^(ui|themes)/|^dist/themes/(?!cupertino/)"),
        "highlightjs", Pattern.compile("^languages/|^styles/(?!rainbow)"),
        "dexie", Pattern.compile("^dist/modern/|\\.mjs$"),
        "jquery", Pattern.compile("^(src|dist-module)/"),
        "sweetalert2", Pattern.compile("^src/")
    );

    private static final Pattern ALWAYS_KEPT = Pattern.compile("(^|/)(LICENSE|NOTICE|COPYING)[^/]*$", Pattern.CASE_INSENSITIVE);

    /**
     * The jar to trim.
     *
     * @return the input artifact
     */
    @InputArtifact
    @PathSensitive(PathSensitivity.NAME_ONLY)
    public abstract Provider<FileSystemLocation> getInputArtifact();

    /**
     * Register the input jar as the output when nothing is removed from it, or a trimmed copy under the same name.
     *
     * @param outputs the transform outputs
     */
    @Override
    public void transform(final TransformOutputs outputs) {
        var input = getInputArtifact().get().getAsFile();
        try {
            if (input.isFile() && hasEntriesNotServed(input)) {
                trim(input, outputs.file(input.getName()));
            } else {
                outputs.file(input);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Whether a jar entry is a WebJar file that CAS never serves.
     *
     * @param entryName the jar entry name
     * @return true if the entry can be removed
     */
    static boolean isNotServed(final String entryName) {
        if (!entryName.startsWith(WEBJARS_PREFIX) || entryName.endsWith("/")) {
            return false;
        }
        var segments = entryName.substring(WEBJARS_PREFIX.length()).split("/", 3);
        if (segments.length < 3 || ALWAYS_KEPT.matcher(segments[2]).find()) {
            return false;
        }
        var webjarRule = NOT_SERVED_BY_WEBJAR.get(segments[0]);
        return NOT_SERVED_BY_ANY_WEBJAR.matcher(segments[2]).find()
            || (webjarRule != null && webjarRule.matcher(segments[2]).find());
    }

    /**
     * Whether the jar holds any WebJar file that CAS never serves.
     *
     * @param jar the jar
     * @return true if the jar needs trimming
     * @throws IOException if the jar cannot be read
     */
    static boolean hasEntriesNotServed(final File jar) throws IOException {
        try (var zip = new ZipFile(jar)) {
            return zip.stream().anyMatch(entry -> isNotServed(entry.getName()));
        }
    }

    /**
     * Copy the jar without the WebJar files that CAS never serves, keeping entry order and timestamps.
     *
     * @param input  the jar to read
     * @param output the trimmed jar to write
     * @throws IOException if the jar cannot be read or written
     */
    static void trim(final File input, final File output) throws IOException {
        try (var zip = new ZipFile(input);
             var out = new ZipOutputStream(new FileOutputStream(output))) {
            for (var entry : zip.stream().filter(candidate -> !isNotServed(candidate.getName())).toList()) {
                var copy = new ZipEntry(entry.getName());
                copy.setTime(entry.getTime());
                out.putNextEntry(copy);
                try (var in = zip.getInputStream(entry)) {
                    in.transferTo(out);
                }
                out.closeEntry();
            }
        }
    }
}
