import groovy.io.FileType

import java.util.regex.Pattern

static void main(String[] args) {
    def pattern = Pattern.compile(/implementation\s+(project.+)/)
    def testPattern = Pattern.compile(/testImplementation\s+(project.+)/)

    def directory = new File(".")
    println "Starting from ${directory.absolutePath}"
    
    def failBuild = false
    directory.eachFileRecurse(FileType.FILES) { file ->
        if (file.name == "build.gradle") {
            def text = file.text
            def matcher = pattern.matcher(text)
            
            while (matcher.find()) {
                def match = matcher.group(1)
                def p2 = matcher.group().replace("(", "\\(").replace(")", "\\)")
                if (Pattern.compile(p2).matcher(text).results().count() > 1) {
                    println "\tFound duplicated configuration for ${match} at ${file.absolutePath}"
                    failBuild = true
                }

                def testImpl = "testImplementation ${match}"
                if (text.contains(testImpl)) {
                    println "\tFound duplicate test configuration for ${testImpl} at ${file.absolutePath}"
                    failBuild = true
                }
            }

            matcher = testPattern.matcher(text)
            while (matcher.find()) {
                p2 = matcher.group().replace("(", "\\(").replace(")", "\\)")
                def compiled = Pattern.compile(p2)
                if (compiled.matcher(text).results().count() > 1) {
                    println "\tFound duplicate test configuration for ${matcher.group()} at ${file.absolutePath}"
                    failBuild = true
                    text = text.replaceFirst(compiled, "")
                    file.write(text)
                }
            }
        }
    }
    if (failBuild) {
        println "Duplicate Gradle configuration found"
        System.exit(1)
    }
}
