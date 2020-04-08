import groovy.io.FileType

import java.util.regex.Pattern

static void main(String[] args) {
    def directory = new File(".")
    def failBuild = false
    def parentClasses = new TreeMap<String, File>();
    directory.eachFileRecurse(FileType.FILES) { file ->
        def text = file.text
        if (file.name.endsWith("Tests.java") && text.find($/public abstract class \w+/$)) {
            parentClasses.put(getFileSimpleName(file), file)
        }
    }
    //println "Found ${parentClasses.size()} parent test classes"
    def patternClasses = Pattern.compile("@SpringBootTest\\(classes\\s*=\\s*\\{(.*?)\\}", Pattern.DOTALL)

    def pattern = Pattern.compile($/public class (\w+) extends (\w+)/$);
    directory.eachFileRecurse(FileType.FILES) { file ->
        def text = file.text
        if (file.name.endsWith("Tests.java")) {
            //println("Checking file ${file.name}")
            def matcher = pattern.matcher(text)
            if (matcher.find() && text.contains("@SpringBootTest")) {
                def group = matcher.group(2)
                if (!parentClasses.containsKey(group)) {
                    println "Unable to find ${group} as a parent class"
                    System.exit(1);
                }
                def parent = parentClasses.get(group)
                def parentText = parent.text
                if (parentText.contains("@SpringBootTest")) {
                    //println "Checking duplicate configuration in ${file.name} inherited from ${parent.name}"

                    def parentTestClasses = [];
                    def childTestClasses = [];

                    def classesMatcher = patternClasses.matcher(parentText)
                    if (classesMatcher.find()) {
                        def match = classesMatcher.group(1)
                                .replaceAll("\n", "").trim().replaceAll("\\s","")
                        parentTestClasses = new HashSet<>(Arrays.asList(match.split(",")))
                    }

                    classesMatcher = patternClasses.matcher(text)
                    if (classesMatcher.find()) {
                        def match = classesMatcher.group(1)
                                .replaceAll("\n", "").trim().replaceAll("\\s","")
                        childTestClasses = new HashSet<>(Arrays.asList(match.split(",")))
                    }

                    def foundDups = false;
                    def it = childTestClasses.iterator()
                    while (it.hasNext()) {
                        def claz = it.next().toString().trim()
                        if (parentTestClasses.contains(claz)) {
                            println "Found duplicate configuration {$claz} in ${file.name} inherited from ${parent.name}"
                            it.remove()
                            foundDups = true
                            failBuild = true
                        }
                    }
                    if (foundDups) {
                        println()
                        println """
The child class ${getFileSimpleName(file)} should be annotated as 
@SpringBootTest(classes = ${getFileSimpleName(parent)}.SharedTestConfiguration.class) 
and must only contain required configuration for the test. Shared/duplicate test 
configuration must be pushed to ${getFileSimpleName(parent)}.SharedTestConfiguration 
instead, if it's not already defined.
"""
                    }
                }
            }
        }
    }

    if (failBuild) {
        System.exit(1)
    }
}

private static String getFileSimpleName(File file) {
    file.name.replace(".java", "")
}
