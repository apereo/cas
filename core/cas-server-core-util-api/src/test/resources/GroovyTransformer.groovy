def run(final Object... args) {
    def providedUsername = args[0]
    def logger = args[1]

    return providedUsername.substring(0, 3).concat("-person")
}
