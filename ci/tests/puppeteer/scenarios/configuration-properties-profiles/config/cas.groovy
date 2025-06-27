profiles {
    custom1 {
        cas.custom.properties.groovy1="custom1"
    }
    custom2 {
        cas.custom.properties.groovy2="custom2"
    }
}

cas.custom.properties.all="everything"
logging.config="file:${env.SCENARIO_FOLDER}/config/log4j2.xml"
