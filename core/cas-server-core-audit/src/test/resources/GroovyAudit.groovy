who: ${who},
what: ${what},
when: ${when},
ip: ${
    org.apereo.cas.util.spring.ApplicationContextProvider
        .getApplicationContext()
        .getBean(org.apereo.cas.authentication.adaptive.geo.GeoLocationService.BEAN_NAME,
                org.apereo.cas.authentication.adaptive.geo.GeoLocationService.class)
            .locate(clientIpAddress)?.build()
}
