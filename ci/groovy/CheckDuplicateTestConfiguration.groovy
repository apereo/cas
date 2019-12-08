import groovy.io.FileType

import java.util.regex.Pattern

static void main(String[] args) {
    def pattern = Pattern.compile("@SpringBootTest\\(classes\\s*=\\s*\\{(.*?)\\}", Pattern.DOTALL)
    def directory = new File(".")
    def failBuild = false
    def duplicatesInTestClass = new TreeSet();
    directory.eachFileRecurse(FileType.FILES) { file ->
        if (file.name.endsWith("Tests.java")) {
            duplicatesInTestClass.clear();
            def matcher = pattern.matcher(file.text)
            if (matcher.find()) {
                def match = matcher.group(1)
                def classes = match.split(",")
                classes.each {
                    def className = it.trim()
                    if (!duplicatesInTestClass.add(className)) {
                        println "Duplicate found: ${className} in ${file.name}"
                        failBuild = true
                    }
                }
            }
        }
    }
    if (failBuild) {
        System.exit(1)
    }
}
