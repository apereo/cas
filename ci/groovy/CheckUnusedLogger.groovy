import groovy.io.FileType

static void main(String[] args) {
    def directory = new File("/Users/Misagh/Workspace/GitWorkspace/cas-server")
    def failBuild = false
    directory.eachFileRecurse(FileType.FILES) { file ->
        if (file.name.endsWith(".java")) {
            def text = file.text
            if (text.contains("@Slf4j") && !text.contains("LOGGER")) {
                //text = text.replace("@Slf4j\n", "")
                //file.write(text)
                println file.absolutePath
                //System.exit(1)
            }
        }
    }
    if (failBuild) {
        System.exit(1)
    }
}
