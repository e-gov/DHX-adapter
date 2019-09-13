package ee.ria.dhx


import io.restassured.RestAssured
import io.restassured.builder.MultiPartSpecBuilder
import io.restassured.response.Response
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration

import static io.restassured.config.MultiPartConfig.multiPartConfig
import static org.hamcrest.Matchers.containsString
import static org.junit.Assert.assertThat

@ContextConfiguration(
        classes = [TestConfiguration.class],
        initializers = ConfigFileApplicationContextInitializer.class)
class DVKSendDocuments extends Spec {

    def "Valid request returns dhl_id"() {
        given:
        String xml = Steps.sendDocumentsRequest(['dokumendid href="cid:doc"': null,
                                                 'kaust'                    : '/',
        ])
        String kapsel = Steps.generateDVKAttachment(Steps.getKapsel('10391131', '10391131'))

        RestAssured.config = RestAssured.config().multiPartConfig(multiPartConfig().defaultSubtype("related"));
        Response response = RestAssured.given()
                .filter(new MultipartAllureFilter())
                .multiPart(new MultiPartSpecBuilder(xml)
                        .fileName("")
                        .controlName("foobar")
                        .mimeType("text/xml")
                        .header("Content-Transfer-Encoding", "8bit")
                        .charset("UTF-8").build())
                .multiPart(new MultiPartSpecBuilder(kapsel)
                        .fileName("")
                        .mimeType("text/xml")
                        .header("Content-ID", "<doc>")
                        .header("Content-Transfer-Encoding", "base64")
                        .header("Content-Encoding", "gzip")
                        .charset("UTF-8").build())
                .when().post(Steps.conf.test.dvkServiceUrl).then().extract().response()
        response.then().statusCode(200)
        assertThat(Steps.parseDVKResponseAttachment(response.body().asString()), containsString("dhl_id"))
    }
}
