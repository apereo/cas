import groovy.io.FileType

static void main(String[] args) {
    def directory = new File(".")
    def failBuild = false

    directory.eachFileRecurse(FileType.FILES) { file ->
        if (file.name.endsWith("TestsSuite.java")) {
            def txt = file.text
            if (!txt.contains("@RunWith")) {
                println "${file.name} contains a suite of tests that is missing the @RunWith annotation"
                failBuild = true
            }
        }
    }

    if (failBuild) {
        System.exit(1)
    }
}
