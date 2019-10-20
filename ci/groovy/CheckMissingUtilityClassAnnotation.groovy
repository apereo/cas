import groovy.io.FileType

static void main(String[] args) {
    def directory = new File(".")
    def failBuild = false

    directory.eachFileRecurse(FileType.FILES) { file ->
        if (file.name.endsWith("Utils.java")) {
            def txt = file.text
            if (!txt.contains("@UtilityClass")) {
                println "${file.name} is a utility class and yet is missing the @UtilityClass annotation"
                failBuild = true
            }
        }
    }

    if (failBuild) {
        System.exit(1)
    }
}
