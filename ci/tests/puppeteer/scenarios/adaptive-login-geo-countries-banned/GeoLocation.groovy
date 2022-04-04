import org.apereo.cas.authentication.adaptive.geo.*
import org.springframework.context.*

GeoLocationResponse locateByAddress(final Object... args) {
    def address = args[0] as InetAddress
    def appContext = args[1] as ApplicationContext
    def logger = args[2]
    logger.info("Checking for ${address.hostAddress}")
    return GeoLocationResponse.builder()
            .latitude(90)
            .longitude(20)
            .build()
            .addAddress("Russia")
}

GeoLocationResponse locateByCoordinates(final Object... args) {
    def latitude = args[0]
    def longitude = args[1]
    def appContext = args[2] as ApplicationContext
    def logger = args[3]
    logger.info("Checking for ${latitude}:${longitude}")
    return GeoLocationResponse.builder()
            .latitude(latitude)
            .longitude(longitude)
            .build()
            .addAddress("Russia")
}
