package ee.ria.dhx

import ee.ria.dhx.database.Postgres
import io.restassured.RestAssured
import io.restassured.internal.RequestSpecificationImpl
import io.restassured.internal.http.HTTPBuilder
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
    def postgresDb = new Postgres(url: System.getenv("POSTGRES_TEST_DATABASE_URL") ?: getConf("dhx.test.postgresUrl"),
            username: System.getenv("POSTGRES_TEST_DATABASE_USER") ?: getConf("dhx.test.postgresUser"),
            password: System.getenv("POSTGRES_DATABASE_PASSWORD") ?: getConf("dhx.test.postgresPassword"),
            driver: "org.postgresql.Driver")

    def setupSpec() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

        //Not sure why this is needed, but overriding in Steps.multipartGiven does not work without this
        RequestSpecificationImpl.metaClass.registerRestAssuredEncoders = { HTTPBuilder http ->
            throw new MissingMethodException()
        }
    }

    private String getConf(confCode) {
        Properties properties = new Properties()
        File propertiesFile = new File("src/test/resources/application.properties")
        propertiesFile.withInputStream {
            properties.load(it)
        }
        return properties."$confCode".toString()
    }
}
