def run(Object... args) {
    def (registeredService,samlConfigBean,samlProperties,criteriaSet,logger) = args
    def httpClient = HttpClients.createDefault()
    def response = null
    try {
        def httpGet = new HttpGet("http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp")
        response = httpClient.execute(httpGet)
        def entity = response.entity
        logger.info "Response status: ${response.statusLine}"
        def metadata = EntityUtils.toString(entity)
        logger.info "Response body: ${metadata}"
        return new InMemoryResourceMetadataResolver(metadata, samlConfigBean)
    } finally {
        response?.close()
        httpClient.close()
    }
    return null
}
