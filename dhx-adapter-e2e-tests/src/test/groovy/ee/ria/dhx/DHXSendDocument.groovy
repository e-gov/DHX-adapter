package ee.ria.dhx

import io.restassured.RestAssured
import io.restassured.builder.MultiPartSpecBuilder
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration

import static io.restassured.config.MultiPartConfig.multiPartConfig
import static org.hamcrest.CoreMatchers.notNullValue

@ContextConfiguration(
        classes = [TestConfiguration.class],
        initializers = ConfigFileApplicationContextInitializer.class)
class DHXSendDocument extends Spec {

    def "Valid request returns receipt"() {
        given:
        String xml = Steps.sendDocumentRequest(['DHXVersion'        : '1.0',
                                                'documentAttachment': 'cid:doc',
                                                'consignmentId'     : UUID.randomUUID()])
        String kapsel = Steps.getKapsel('10391131', '10391131').bytes.encodeBase64()

        RestAssured.given().config(RestAssured.config()
                .multiPartConfig(multiPartConfig().defaultSubtype("related")))
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
                        .charset("UTF-8").build())
                .when().post(Steps.conf.test.dhxServiceUrl).then()
                .statusCode(200)
                .root("Envelope.Body.sendDocumentResponse")
                .body("receiptId", notNullValue())

    }
}
