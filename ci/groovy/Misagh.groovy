import groovy.io.FileType



static void main(String[] args) {
    def directory = new File("/Users/Misagh/Workspace/GitWorkspace/cas-server")
    def files = []

    directory.eachFileRecurse(FileType.FILES) { file ->
        if (file.name.matches(".*Tests\\.java")) {
            def txt = file.text
            if (txt.contains("@Tag(\"Simple\"")) {
//                txt = txt.replace("@Tag(\"Simple\"", "@Tag(\"RegisteredService\"")
//                file.write(txt)
                files.add(file.name)

            }
        }
    }

    files.sort().each {println it}
    
}


