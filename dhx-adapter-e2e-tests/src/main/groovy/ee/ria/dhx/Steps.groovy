package ee.ria.dhx


import io.qameta.allure.Step
import io.qameta.allure.restassured.AllureRestAssured
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.internal.http.BoundaryExtractor
import io.restassured.internal.http.CharsetExtractor
import io.restassured.internal.http.ContentTypeExtractor
import io.restassured.internal.http.HTTPBuilder
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.http.entity.mime.FormBodyPartBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import static io.restassured.config.MultiPartConfig.multiPartConfig
import static org.apache.commons.lang3.StringUtils.*

@Component
class Steps {

    static Configuration conf


    @Autowired
    Steps(Configuration conf) {
        this.conf = conf
    }

    static RequestSpecification multipartGiven() {
        //Override io.restassured.internal.RequestSpecificationImpl.registerRestAssuredEncoders
        RequestSpecification request = RestAssured.given().config(RestAssured.config()
                .multiPartConfig(multiPartConfig().defaultSubtype("related")))
        request.metaClass.registerRestAssuredEncoders = { HTTPBuilder http ->
            System.out.println("Override")
            // Multipart form-data
            if (multiParts.isEmpty()) {
                return;
            }

            if (hasFormParams()) {
                convertFormParamsToMultiPartParams()
            }

            def contentTypeAsString = headers.getValue(CONTENT_TYPE)
            def ct = ContentTypeExtractor.getContentTypeWithoutCharset(contentTypeAsString)
            def subType;
            if (ct?.toLowerCase()?.startsWith(MULTIPART_CONTENT_TYPE_PREFIX_WITH_SLASH)) {
                subType = substringAfter(ct, MULTIPART_CONTENT_TYPE_PREFIX_WITH_SLASH)
            } else if (ct?.toLowerCase()?.contains(MULTIPART_CONTENT_TYPE_PREFIX_WITH_PLUS)) {
                subType = substringBefore(substringAfter(ct, MULTIPART_CONTENT_TYPE_PREFIX_WITH_PLUS), "+")
            } else {
                throw new IllegalArgumentException("Content-Type $ct is not valid when using multiparts, it must start with \"$MULTIPART_CONTENT_TYPE_PREFIX_WITH_SLASH\" or contain \"$MULTIPART_CONTENT_TYPE_PREFIX_WITH_PLUS\".");
            }

            def charsetFromContentType = CharsetExtractor.getCharsetFromContentType(contentTypeAsString)
            def charsetToUse = isBlank(charsetFromContentType) ? restAssuredConfig().getMultiPartConfig().defaultCharset() : charsetFromContentType
            def boundaryFromContentType = BoundaryExtractor.getBoundaryFromContentType(contentTypeAsString)
            String boundaryToUse = boundaryFromContentType ?: restAssuredConfig().getMultiPartConfig().defaultBoundary()
            boundaryToUse = boundaryToUse ?: generateBoundary()
            if (!boundaryFromContentType) {
                removeHeader(CONTENT_TYPE) // there should only be one
                contentType(contentTypeAsString + "; boundary=\"" + boundaryToUse + "\"")
            }

            def multipartMode = httpClientConfig().httpMultipartMode()
            // For "defaultCharset" to be taken into account we need to

            http.encoders.putAt ct, { contentType, content ->
                DHXMultiPartEntity entity = new DHXMultiPartEntity(subType, charsetToUse, multipartMode, boundaryToUse);

                multiParts.each {
                    def body = it.contentBody
                    def controlName = it.controlName
                    def headers = it.headers
                    if (headers.isEmpty()) {
                        entity.addPart(controlName, body);
                    } else {
                        def builder = FormBodyPartBuilder.create(controlName, body)
                        headers.each { name, value ->
                            builder.addField(name, value)
                        }
                        entity.addPart(builder.build())
                    }
                }

                entity;
            }
        }
        return request
    }

    @Step("DHX-adapter Test /health request")
    static Response testHealthEndpoint() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .filter(new AllureRestAssured())
                .when()
                .get(conf.test.getHealthUrl())
    }

    @Step("DHX-adapter Test /metrics request")
    static Response testMetricsEndpoint() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .filter(new AllureRestAssured())
                .when()
                .get(conf.test.metricsUrl)
    }

    static String getKapsel(String decSender, String decRecipient) {
        return """<DecContainer xmlns="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
  <Transport>
    <DecSender>
      <OrganisationCode>${decSender}</OrganisationCode>
      <PersonalIdCode>EE38806190294</PersonalIdCode>
    </DecSender>
\t <DecRecipient>
      <OrganisationCode>${decRecipient}</OrganisationCode>
    </DecRecipient>
  </Transport>
  <RecordCreator>
    <Person>
      <Name>Lauri Tammemäe</Name>
      <GivenName>Lauri</GivenName>
      <Surname>Tammemäe</Surname>
      <PersonalIdCode>EE38806190294</PersonalIdCode>
      <Residency>EE</Residency>
    </Person>
    <ContactData>
      <Adit>true</Adit>
      <Phone>3726630276</Phone>
      <Email>lauri.tammemae@ria.ee</Email>
      <WebPage>www.hot.ee/lauri</WebPage>
      <MessagingAddress>skype: lauri.tammemae</MessagingAddress>
      <PostalAddress>
        <Country>Eesti</Country>
        <County>Harju maakond</County>
        <LocalGovernment>Tallinna linn</LocalGovernment>
        <AdministrativeUnit>Mustamäe linnaosa</AdministrativeUnit>
        <SmallPlace>Pääsukese KÜ</SmallPlace>
        <LandUnit></LandUnit>
        <Street>Mustamäe tee</Street>
        <HouseNumber>248</HouseNumber>
        <BuildingPartNumber>62</BuildingPartNumber>
        <PostalCode>11212</PostalCode>
      </PostalAddress>
    </ContactData>
  </RecordCreator>
  <RecordSenderToDec>
    <Person>
      <Name>Lauri Tammemäe</Name>
      <GivenName>Lauri</GivenName>
      <Surname>Tammemäe</Surname>
      <PersonalIdCode>EE38806190294</PersonalIdCode>
      <Residency>EE</Residency>
    </Person>
    <ContactData>
      <Adit>false</Adit>
      <Phone>3726630276</Phone>
      <Email>lauri.tammemae@ria.ee</Email>
      <WebPage>www.hot.ee/lauri</WebPage>
      <MessagingAddress>skype: lauri.tammemae</MessagingAddress>
      <PostalAddress>
        <Country>Eesti</Country>
        <County>Harju maakond</County>
        <LocalGovernment>Tallinna linn</LocalGovernment>
        <AdministrativeUnit>Mustamäe linnaosa</AdministrativeUnit>
        <SmallPlace>Pääsukese KÜ</SmallPlace>
        <LandUnit></LandUnit>
        <Street>Mustamäe tee</Street>
        <HouseNumber>248</HouseNumber>
        <BuildingPartNumber>62</BuildingPartNumber>
        <PostalCode>11212</PostalCode>
      </PostalAddress>
    </ContactData>
  </RecordSenderToDec>
  <Recipient>
    <Organisation>
      <Name>Riigi Infosüsteemi Amet</Name>
      <OrganisationCode>70006317</OrganisationCode>
      <Residency>EE</Residency>
    </Organisation>
  </Recipient>
  <RecordMetadata>
    <RecordGuid>25892e17-80f6-415f-9c65-7395632f0234</RecordGuid>
    <RecordType>Kiri</RecordType>
    <RecordOriginalIdentifier>213465</RecordOriginalIdentifier>
    <RecordDateRegistered>2012-11-11T19:18:03</RecordDateRegistered>
    <RecordTitle>Ettepanek</RecordTitle>
    <RecordLanguage>EE</RecordLanguage>
  </RecordMetadata>
  <Access>
    <AccessConditionsCode>Avalik</AccessConditionsCode>
  </Access>
  <File>
    <FileGuid>25892e17-80f6-415f-9c65-7395632f0001</FileGuid>
    <RecordMainComponent>0</RecordMainComponent>
    <FileName>Ettepanek.doc</FileName>
    <MimeType>application/msword</MimeType>
    <FileSize>211543</FileSize>
    <ZipBase64Content>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</ZipBase64Content>
  </File>
  <RecordTypeSpecificMetadata />
  <DecMetadata>
    <DecId>99999999</DecId>
    <DecFolder>/</DecFolder>
    <DecReceiptDate>2012-11-11T19:20:42</DecReceiptDate>
  </DecMetadata>
</DecContainer>"""
    }

    static String getKapsel(String decSender, String decRecipient, String content) {
        return """<DecContainer xmlns="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
  <Transport>
    <DecSender>
      <OrganisationCode>${decSender}</OrganisationCode>
      <PersonalIdCode>EE38806190294</PersonalIdCode>
    </DecSender>
\t <DecRecipient>
      <OrganisationCode>${decRecipient}</OrganisationCode>
    </DecRecipient>
  </Transport>
  <RecordCreator>
    <Person>
      <Name>Lauri Tammemäe</Name>
      <GivenName>Lauri</GivenName>
      <Surname>Tammemäe</Surname>
      <PersonalIdCode>EE38806190294</PersonalIdCode>
      <Residency>EE</Residency>
    </Person>
    <ContactData>
      <Adit>true</Adit>
      <Phone>3726630276</Phone>
      <Email>lauri.tammemae@ria.ee</Email>
      <WebPage>www.hot.ee/lauri</WebPage>
      <MessagingAddress>skype: lauri.tammemae</MessagingAddress>
      <PostalAddress>
        <Country>Eesti</Country>
        <County>Harju maakond</County>
        <LocalGovernment>Tallinna linn</LocalGovernment>
        <AdministrativeUnit>Mustamäe linnaosa</AdministrativeUnit>
        <SmallPlace>Pääsukese KÜ</SmallPlace>
        <LandUnit></LandUnit>
        <Street>Mustamäe tee</Street>
        <HouseNumber>248</HouseNumber>
        <BuildingPartNumber>62</BuildingPartNumber>
        <PostalCode>11212</PostalCode>
      </PostalAddress>
    </ContactData>
  </RecordCreator>
  <RecordSenderToDec>
    <Person>
      <Name>Lauri Tammemäe</Name>
      <GivenName>Lauri</GivenName>
      <Surname>Tammemäe</Surname>
      <PersonalIdCode>EE38806190294</PersonalIdCode>
      <Residency>EE</Residency>
    </Person>
    <ContactData>
      <Adit>false</Adit>
      <Phone>3726630276</Phone>
      <Email>lauri.tammemae@ria.ee</Email>
      <WebPage>www.hot.ee/lauri</WebPage>
      <MessagingAddress>skype: lauri.tammemae</MessagingAddress>
      <PostalAddress>
        <Country>Eesti</Country>
        <County>Harju maakond</County>
        <LocalGovernment>Tallinna linn</LocalGovernment>
        <AdministrativeUnit>Mustamäe linnaosa</AdministrativeUnit>
        <SmallPlace>Pääsukese KÜ</SmallPlace>
        <LandUnit></LandUnit>
        <Street>Mustamäe tee</Street>
        <HouseNumber>248</HouseNumber>
        <BuildingPartNumber>62</BuildingPartNumber>
        <PostalCode>11212</PostalCode>
      </PostalAddress>
    </ContactData>
  </RecordSenderToDec>
  <Recipient>
    <Organisation>
      <Name>Riigi Infosüsteemi Amet</Name>
      <OrganisationCode>70006317</OrganisationCode>
      <Residency>EE</Residency>
    </Organisation>
  </Recipient>
  <RecordMetadata>
    <RecordGuid>25892e17-80f6-415f-9c65-7395632f0234</RecordGuid>
    <RecordType>Kiri</RecordType>
    <RecordOriginalIdentifier>213465</RecordOriginalIdentifier>
    <RecordDateRegistered>2012-11-11T19:18:03</RecordDateRegistered>
    <RecordTitle>Ettepanek</RecordTitle>
    <RecordLanguage>EE</RecordLanguage>
  </RecordMetadata>
  <Access>
    <AccessConditionsCode>Avalik</AccessConditionsCode>
  </Access>
  <File>
    <FileGuid>25892e17-80f6-415f-9c65-7395632f0001</FileGuid>
    <RecordMainComponent>0</RecordMainComponent>
    <FileName>Ettepanek.doc</FileName>
    <MimeType>application/msword</MimeType>
    <FileSize>211543</FileSize>
    <ZipBase64Content>${content}</ZipBase64Content>
  </File>
  <RecordTypeSpecificMetadata />
  <DecMetadata>
    <DecId>99999999</DecId>
    <DecFolder>/</DecFolder>
    <DecReceiptDate>2012-11-11T19:20:42</DecReceiptDate>
  </DecMetadata>
</DecContainer>"""
    }
}
