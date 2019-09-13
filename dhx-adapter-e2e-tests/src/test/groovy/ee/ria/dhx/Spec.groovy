package ee.ria.dhx


import io.restassured.RestAssured
import org.junit.Rule
import org.junit.rules.Timeout
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(
        classes = [TestConfiguration.class],
        initializers = ConfigFileApplicationContextInitializer.class)
abstract class Spec extends Specification {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(60 * 2)

    def setupSpec() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }
}
