import groovy.io.FileType

import java.util.regex.Pattern

static void main(String[] args) {
    def directory = new File("/Users/Misagh/Workspace/GitWorkspace/cas-server")
    def failBuild = false
    def duplicatesInTestClass = new TreeSet();
    long count = 0;
    directory.eachFileRecurse(FileType.FILES) { file ->
        if (file.name.endsWith("Tests.java")) {
            duplicatesInTestClass.clear();
            def text = file.text
            if (text.contains("@EnabledIfContinuousIntegration")) {
                if (!text.contains("@EnabledIfPortOpen")) {
                    println "Found test class: " + file.name
                    count++;
                } else {
//                    def results = text.replace("@EnabledIfContinuousIntegration\n", "")
//                    file.write(results)
                }
            }
        }
    }
    println "Total ${count}"
    if (failBuild) {
        System.exit(1)
    }
}
