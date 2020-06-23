import groovy.io.FileType

import java.util.regex.Pattern

static void main(String[] args) {
    def directory = new File(".")
    def failBuild = false

    def patternBeanMethods = Pattern.compile("public\\s\\w+(<\\w+>)*\\s(\\w+)\\(")
    def results = []

    directory.eachFileRecurse(FileType.FILES) { file ->
        if (file.name.endsWith('Configuration.java') && file.text.contains("@Configuration")) {
            def canProxyBeans = true
            def matcher = patternBeanMethods.matcher(file.text)
            results.clear()
            while (matcher.find()) {
                results += matcher.group(2)
            }
            for (def r in results) {
                def patternMethodCall = Pattern.compile(r + "\\(");
                def matcher2 = patternMethodCall.matcher(file.text)
                def count = 0
                while (matcher2.find()) {
                    count++
                }
                if (count > 1) {
                    canProxyBeans = false
                    break
                }
            }
            if (canProxyBeans) {
                def proxyPattern = Pattern.compile("@Configuration\\(value\\s*=\\s*\"(\\w+)\",\\s*proxyBeanMethods\\s*=\\s*(false|true)\\)")
                        .matcher(file.text)
                if (!proxyPattern.find()) {
                    println("Configuration class ${file.name} should be marked with proxyBeanMethods = false")
                    failBuild = true
                }
            }
        }
    }

    if (failBuild) {
        System.exit(1)
    }
}


