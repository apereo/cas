${logger.info("Groovy audit manager running...")}
${
    def bean = applicationContext.getBean(org.apereo.cas.audit.AuditTrailExecutionPlan.BEAN_NAME)
    logger.debug(bean.class.name)
}
who: ${who},
what: ${what},
when: ${when},
ip: ${clientIpAddress}
