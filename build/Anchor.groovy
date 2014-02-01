import java.awt.Container;
import java.awt.GraphicsConfiguration.DefaultBufferCapabilities;
import java.lang.invoke.DirectMethodHandle.StaticAccessor;
import java.util.logging.Logger;

import groovy.swing.impl.DefaultAction;
import groovy.util.logging.Log
import static groovy.io.FileType.FILES

/*
 * A quick groovy script to generate anchor tags for section headers.
 * @author: Misagh Moayyed
 */
@Log
class Anchor {
    static main(args) {
        def cli = new CliBuilder(usage: 'Anchor.groovy --dir <value> --help')
        cli.width = 100

        cli.with {
            h longOpt: 'help', 'Show usage information'
            f longOpt: 'file', args:1, argName: 'file', 'A single markdown file to anchorize'
            d longOpt: 'dir', args:1, argName: 'value', 'The starting directory to search for markdown files'
        }

        def options = cli.parse(args)
        if (!options || args.size() == 0 || options.h || (!options.d && !options.f)) {
            cli.usage()
            return
        }
        println()

        log.info("Using project directory: " + options.d)

        if (options.d) {
            new File(options.d).eachFileRecurse(FILES) { addAnchorsToMarkdownFile(it) }
        } else if (options.f) {
            addAnchorsToMarkdownFile(new File(options.f))
        }
        log.info("Done!")
    }

    static boolean shouldIgnoreTitle(def title) {
        title.contains(".") || title.contains(":") || title.contains("?") || 
        title.contains("/") || title.contains("*") || title.contains("(")
    }


    static void addAnchorsToMarkdownFile(def it) {
        if (it.name.endsWith('.md')) {
            log.info("Looking at markdown file: " + it.name)
            def builder = ""
            def contents = it.getText("UTF-8")
            def lineNumber = 0
            def codeBlockCount = 0
            Set anchorsSet = []

            it.eachLine { line ->
                def addLine = true
                lineNumber++

                if (line.startsWith("```") || line.startsWith("{%")) {
                    if (codeBlockCount > 0) {
                        codeBlockCount = 0
                    } else {
                        codeBlockCount++
                    }
                }

                if (line.startsWith('#') && codeBlockCount == 0) {
                    def title = line.substring(line.lastIndexOf('#') + 1).trim().replaceAll("`| ", "")


                    log.info("Found section header: " + title)
                    def anchor = String.format('<a name="%s">  </a>\n%s', title, line)

                    if (contents.contains(anchor) || anchorsSet.contains(title.toLowerCase()) ||
                                shouldIgnoreTitle(title)) {
                        log.info("Ignoring tag for header [" + title + "]...")
                    } else {
                        log.info("Reformatted section header with anchor: " + anchor)
                        builder <<= anchor

                        addLine = false
                    }

                    anchorsSet.add(title.toLowerCase())
                }

                if (addLine) {
                    builder <<= line
                }

                builder <<= "\n"
            }

            it.write(builder.toString(), "UTF-8")
        }
    }
}