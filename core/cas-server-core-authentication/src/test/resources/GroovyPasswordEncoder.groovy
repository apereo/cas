byte[] run(final Object... args) {
    def rawPassword = args[0]
    def generatedSalt = args[1]
    def logger = args[2]
    def casApplicationContext = args[3]
    return rawPassword as byte[]
}

String encode(String rawPassword) {
    return rawPassword
}

Boolean matches(final Object... args) {
    def rawPassword = args[0]
    def encodedPassword = args[1]
    return true
}
