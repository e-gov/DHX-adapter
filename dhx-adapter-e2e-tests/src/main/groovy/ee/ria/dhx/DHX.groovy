package ee.ria.dhx

import groovy.xml.MarkupBuilder
import io.qameta.allure.Step
import io.restassured.RestAssured
import io.restassured.builder.MultiPartSpecBuilder
import io.restassured.response.Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DHX {

    static Configuration conf


    @Autowired
    DHX(Configuration conf) {
        this.conf = conf
    }

    static Map sendDocumentHeaderData() {
        return [requestHash    : "29KTVbZf83XlfdYrsxjaSYMGoxvktnTUBTtA4BmSrh1e\n" +
                "gtRtvR9VY8QycYaVdsKtGJIh/8CpucYWPbWfaIgJDQ==",
                protocolVersion: '4.0',
                id             : UUID.randomUUID(),
                userId         : "TEST",
                service        : [xRoadInstance : 'ee-dev',
                                  memberClass   : 'COM',
                                  memberCode    : '10391131',
                                  subsystemCode : 'DHX',
                                  serviceCode   : 'sendDocuments',
                                  serviceVersion: 'v1'],
                client         : [xRoadInstance: 'ee-dev',
                                  memberClass  : 'COM',
                                  memberCode   : '10391131',
                                  subsystemCode: 'DHX']
        ]
    }

    static String sendDocumentRequest(Map data, Map headerData = sendDocumentHeaderData()) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.'soapenv:Envelope'('xmlns:iden': 'http://x-road.eu/xsd/identifiers', 'xmlns:prod': 'http://dhx.x-road.eu/producer', 'xmlns:soapenv': 'http://schemas.xmlsoap.org/soap/envelope/', 'xmlns:xro': 'http://x-road.eu/xsd/xroad.xsd') {
            'soapenv:Header'() {
                'xro:requestHash'(algorithmId: 'http://www.w3.org/2001/04/xmlenc#sha512', headerData.requestHash)
                headerData.findAll {
                    !["requestHash", "service", "client"].contains(it.key) && it.value != null
                }.each { key, value ->
                    "xro:${key}" "${value}"
                }
                'xro:service'('iden:objectType': 'SERVICE') {
                    headerData.service.findAll { it.value != null }.each { key, value ->
                        "iden:${key}" "${value}"
                    }
                }
                'xro:client'('iden:objectType': 'SUBSYSTEM') {
                    headerData.client.findAll { it.value != null }.each { key, value ->
                        "iden:${key}" "${value}"
                    }
                }
            }
            'soapenv:Body'() {
                'prod:sendDocument'() {
                    data.findAll { it.value != null }.each { key, value ->
                        "prod:${key}" "${value}"
                    }

                }
            }

        }
        return writer.toString()
    }

    @Step("DHX sendDocument request")
    static Response dhxSendDocument(String xml, String attachment, String url) {
        return Steps.multipartGiven()
                .filter(new MultipartAllureFilter())
                .multiPart(new MultiPartSpecBuilder(xml)
                        .fileName("binaryContent2450448932417715564.tmp")
                        .controlName("foobar")
                        .mimeType("text/xml")
                        .charset("UTF-8").build())

                .multiPart(new MultiPartSpecBuilder(attachment)
                        .fileName("binaryContent2450448932417715564.tmp")
                        .controlName("text")
                        .mimeType("text/xml")
                        .header("Content-ID", "<doc>")
                        .header("Content-Transfer-Encoding", "base64")
                        .charset("UTF-8").build())

                .when().post(url).then()
        //TODO: .root("Envelope.Body.sendDocumentResponse") ?
    }

}
