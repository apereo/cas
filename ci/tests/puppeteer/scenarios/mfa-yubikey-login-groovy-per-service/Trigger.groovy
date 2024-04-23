def run(final Object... args) {
    def authentication = args[0]
    def registeredService = args[1]
    def httpRequest = args[2]
    def service = args[3]
    def applicationContext = args[4]
    def logger = args[5]

    def memberOf = authentication.principal.attributes['memberOf']
    def foundMatch = memberOf.find {
        def attrValue = it.toString()
        println "MFA: Checking memberOf attribute value ${attrValue}"
        attrValue.toLowerCase().startsWith("yubi-")
    }
    println "Matching attribute value found: ${foundMatch}"
    return foundMatch != null ? "mfa-yubikey" : null
}
