import groovy.io.FileType

import java.util.regex.Pattern

static void main(String[] args) {
    def pattern = Pattern.compile(/implementation\s+(project.+)/)
    def directory = new File(".")
    def failBuild = false
    directory.eachFileRecurse(FileType.FILES) { file ->
        if (file.name == "build.gradle") {
            def text = file.text
            def matcher = pattern.matcher(text)
            def foundDuplicate = false
            while (matcher.find()) {
                def match = matcher.group(1)
                def testPattern = "testImplementation ${match}"
                if (text.contains(testPattern)) {
                    println "\tFound duplicate test configuration for ${testPattern} at ${file.absolutePath}"
                    failBuild = true
                    foundDuplicate = true
                }
            }
            if (foundDuplicate) {
                file.write(text)
            }
        }
    }
    if (failBuild) {
        println "Duplicate Gradle configuration found"
        System.exit(1)
    }
}
