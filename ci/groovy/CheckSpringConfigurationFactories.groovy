import groovy.io.FileType

def checkForSpringConfigurationFactories(String projectPath, String configurations) {
    //println("Project: ${projectPath}")
    def classes = configurations.split(",")
    classes.each {
        def sourcePath = "/src/main/java/".replace("/", String.valueOf(File.separator))
        def clazz = projectPath + sourcePath + it.replace(".", String.valueOf(File.separator)) + ".java"
        def configurationFile = new File(clazz)
        //println("Spring configuration file: ${configurationFile}")
        if (!configurationFile.exists()) {
            println("Spring configuration class does not exist: ${clazz}")
            return false
        }
    }
    return true
}

def checkProjectContainsSpringConfigurations(String projectPath, File springFactoriesFile) {
    if (springFactoriesFile.exists()) {
        return true
    }
    def pass = true
    new File(projectPath).eachFileRecurse(FileType.FILES) { file ->
        if (file.name.endsWith('Configuration.java') && file.text.contains("@Configuration")) {
            println("Configuration class ${file.name} is missing from ${springFactoriesFile}")
            pass = false
        }
    }
    if (!pass) {
        println("Project ${projectPath} is missing a spring.factories file at ${springFactoriesFile}")
    }
    return pass
}

static void main(String[] args) {
    def directory = new File(".")
    def count = 0

    directory.eachFileRecurse(FileType.DIRECTORIES) { dir ->
        if (dir.canonicalPath.endsWith("src/main/resources/META-INF")) {
            def projectPath = dir.parentFile.parentFile.parentFile.parentFile.canonicalPath
            def springFactoriesFile = new File(dir, "spring.factories")
            if (springFactoriesFile.exists()) {
                def properties = new Properties()
                properties.load(new FileReader(springFactoriesFile))

                if (properties.isEmpty()) {
                    println("spring.factories file ${springFactoriesFile} is empty")
                    count++
                }
                
                if (properties.containsKey("org.springframework.cloud.bootstrap.BootstrapConfiguration")) {
                    def classes = properties.get("org.springframework.cloud.bootstrap.BootstrapConfiguration")
                    if (!checkForSpringConfigurationFactories(projectPath, classes)) {
                        count++
                    }
                }
                if (properties.containsKey("org.springframework.boot.autoconfigure.EnableAutoConfiguration")) {
                    def classes = properties.get("org.springframework.boot.autoconfigure.EnableAutoConfiguration")
                    if (!checkForSpringConfigurationFactories(projectPath, classes)) {
                        count++
                    }
                }
            } else if (!checkProjectContainsSpringConfigurations(projectPath, springFactoriesFile)) {
                count++
            }
        }
    }

    if (count > 0) {
        System.exit(1)
    }
}


