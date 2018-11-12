import org.hibernate.boot.model.naming.Identifier

def run(Object[] args) {
    def identifier = args[0]
    def jdbcEnvironment = args[1]
    def applicationContext = args[2]
    def logger = args[3]
    logger.info("Mapping ${identifier.text} to table name...")
    return Identifier.toIdentifier("CasTableName")
}

