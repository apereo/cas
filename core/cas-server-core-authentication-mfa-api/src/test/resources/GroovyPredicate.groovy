import groovy.transform.TupleConstructor
import org.apereo.cas.authentication.principal.*
import java.util.function.Predicate

@TupleConstructor
class GroovyPredicate implements Predicate {
    Object service
    Object principal
    Object providers
    Object logger

    @Override
    boolean test(final Object o) {
        return true
    }
}
