import org.ldaptive.FilterTemplate
import org.springframework.context.ApplicationContext

def run(Object[] args) {
    def filter = (FilterTemplate) args[0]
    def parameters = (Map) args[1]
    def applicationContext = (ApplicationContext) args[2]
    def logger = args[3]

    logger.info("Configuring LDAP filter for ${filter} via ${parameters} and bean count ${applicationContext.beanDefinitionCount}")
    filter.setFilter("uid=something")
}
