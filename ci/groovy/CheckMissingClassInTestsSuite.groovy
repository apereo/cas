import groovy.io.FileType

import java.util.regex.Pattern

static void main(String[] args) {
    def directory = new File(".")
    def failBuild = false
    def count = 0

    directory.eachFileRecurse(FileType.DIRECTORIES) { dir ->
        if (dir.canonicalPath.endsWith("src/test/java")) {
            //println "Running checks on ${dir.path}"
            def testSuites = new FileNameByRegexFinder()
                    .getFileNames(dir.canonicalPath, /.*TestsSuite\.java/)
            def testClasses = new FileNameByRegexFinder()
                    .getFileNames(dir.canonicalPath, /.*Tests\.java/, /.*(Base|Abstract).+Tests\.java/)

            if (testClasses.size() > 1 && testSuites.isEmpty()) {
                println("Project ${dir.path} is missing a TestsSuite class, while it contains ${testClasses.size()} tests")
                failBuild = true
            }

            if (testSuites.size() > 1) {
                println("Project ${dir.path} has more than one TestsSuite")
                failBuild = true
            }

            if (!testSuites.isEmpty()) {
                def testSuiteFile = new File(testSuites.get(0))
                //println "Test suite file is ${testSuiteFile.path}"

                def testSuiteFileBody = testSuiteFile.text
                def missingClasses = testClasses.findAll({
                    def testClass = new File(it).name.replace(".java", ".class")
                    //println "Looking for test class ${testClass}"

                    def patternMulti = Pattern.compile("\\s+${testClass}")
                    def patternSingle = Pattern.compile("@SelectClasses\\(${testClass}\\)")
                    if (!patternMulti.matcher(testSuiteFileBody).find() && !patternSingle.matcher(testSuiteFileBody).find()) {
                        //println "Test class ${testClass} is missing"
                        return true
                    }
                    //println "\tTest class ${testClass} is NOT missing"
                    return false
                })
                if (!missingClasses.isEmpty()) {
                    println "\n${testSuiteFile.name} of ${dir.getName()} does not include:"
                    println missingClasses
                            .sort()
                            .collect({ new File(it).name.replace(".java", ".class") })
                            .join("\t,\n")

                    count += missingClasses.size()
                    failBuild = true
                }
            }
        }
    }

    if (failBuild) {
        if (count > 0) {
            println("\nFound ${count} missing test class(es) in test suites\n")
        }
        System.exit(1)
    }
}
