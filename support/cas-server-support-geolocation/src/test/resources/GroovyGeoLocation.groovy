import org.apereo.cas.authentication.adaptive.geo.*
import org.springframework.context.*

GeoLocationResponse locateByAddress(final Object... args) {
    def address = args[0] as InetAddress
    def appContext = args[1] as ApplicationContext
    def logger = args[2]
    return GeoLocationResponse.builder()
            .latitude(1234)
            .longitude(1234)
            .build()
            .addAddress("USA")
}

GeoLocationResponse locateByCoordinates(final Object... args) {
    def latitude = args[0]
    def longitude = args[1]
    def appContext = args[2] as ApplicationContext
    def logger = args[3]
    return GeoLocationResponse.builder()
            .latitude(latitude)
            .longitude(longitude)
            .build()
            .addAddress("USA")
}
