def run(final Object... args) {
    def (attributes, logger, principal, service) = args
    return [displayName: ["CAS ${principal.id}".toString()], faculty: attributes["groupMembership3"]]
}
