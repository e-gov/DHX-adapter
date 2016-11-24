![](EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg)

ET | [EN](GUIDE.md)

# DHX-adapteri kasutusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

## Sissejuhatus

DHX adapter on Java teek, milles on realiseeritud dokumendi saatmise, vastuvõtmise ja aadressiraamatu koostamise funktsionaalsus vastavalt [DHX protokolli](https://e-gov.github.io/DHX/) nõuetele.

Antud juhend on mõeldud kasutamiseks tarkvara arendajatele (DHX rakendajatele), kes soovivad hakata oma Dokumendihaldussüsteemis (DHS) kasutama DHX protokolli.

DHX adapteri lähtekood asub aadressil https://github.com/e-gov/DHX-adapter

Selles asuvad kolm alamteeki
- [dhx-adapter-core](https://e-gov.github.io/DHX-adapter/dhx-adapter-core/doc/) – selles asuvad klassid XML (Kapsli) ja SOAP objektide koostamiseks ja töötlemiseks,  vigade klassid ning mõned üldkasutatavad utiliit klassid
- [dhx-adapter-ws](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/) – selles asuvad klassid dokumendi saatmiseks (SOAP client), aadressiraamatu koostamiseks (SOAP client) ja dokumendi vastuvõtmiseks (SOAP Service Endpoint)
- [dhx-adapter-server](https://e-gov.github.io/DHX-adapter/dhx-adapter-server/doc/) – eraldiseisev adapter server (Variant C), mis puhverdab saabunud dokumendid vahe andmebaasis ja pakub vana [DVK liidese](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md) sarnaseid SOAP teenuseid

DHS-iga otse liidestamiseks tuleb kasutada 2 esimest teeki **dhx-adapter-core** ja **dhx-adapter-ws**.

**dhx-adapter-server** on vajalik ainult neile, kes ei soovi kasutada otse liidestust, vaid plaanivad paigaldada vahepealse puhverserveri, selleks et kasutada edasi vana DVK SOAP liidesele sarnast liidest.

##Välised sõltuvused ja baasplatvorm

DHX adapteri teekide kasutamisel tuleb arvestada et DHX adapter sõltub allpool toodud komponentidest.

Kompileerimiseks ja käivitamiseks on vajalik [Java SE](https://en.wikipedia.org/wiki/Java_Platform,_Standard_Edition) 1.7 (või uuem) versioon.

Kuna DHX adapteri teek pakub väljapoole veebiteenust (ei ole ainult teenuse klient), siis sõltutakse J2EE [Java Servlet API](https://en.wikipedia.org/wiki/Java_servlet) teegist, läbi [Spring Web Services](http://projects.spring.io/spring-ws/) mooduli).

XML töötluseks kasutatakse [Java Architecture for XML Binding - JAXB](https://docs.oracle.com/javase/7/docs/api/javax/xml/bind/package-summary.html) teeki, mis on Java SE 7 osa.

DHX adapteri Java teek baseerub Spring Framework arhitektuuril, kasutades selle mooduleid:
- Häälestamiseks ja laadimiseks (Spring AOP, Spring Context, jne)
- HTTP SOAP kliendina päringute tegemiseks (Spring WS Client, Apache HttpClient)
- HTTP SOAP veebiteenuse pakkumiseks (Spring WS Server Endpoint, Java Servlet API)

DHX adapteri otsesed ja kaudsed välised sõltuvused on järgmised:

Grupp | Moodul | Versioon | Märkused
------------ | ------------- | ------------- | -------------
org.springframework | spring-core | 4.3.3.RELEASE | Spring Core
org.springframework | spring-aop | 4.2.7.RELEASE | Spring AOP
org.springframework | spring-beans | 4.2.7.RELEASE | Spring Beans
org.springframework | spring-context | 4.2.7.RELEASE | Spring Context
org.springframework | spring-expression | 4.2.7.RELEASE | Spring Expression Language (SpEL)
org.springframework | spring-oxm | 4.2.7.RELEASE | Spring Object/XML Marshalling
org.springframework | spring-web | 4.2.7.RELEASE | Spring Web
org.springframework | spring-webmvc | 4.2.7.RELEASE | Spring Web MVC
org.springframework.ws | spring-ws-core   | 2.4.0.RELEASE | Spring WS Core
org.springframework.ws | spring-xml | 2.4.0.RELEASE | Spring XML
org.springframework.boot | spring-boot-starter-log4j2 | 1.3.5.RELEASE | Spring Boot starter Log4j2
commons-logging | commons-logging | 1.1.3, 1.2 | Apache commons login pakett logimiseks
commons-codec | commons-codec | 1.9 | Apache Commons Codec
aopalliance | aopalliance | 1.0 | AOP alliance
org.hamcrest | hamcrest-core | 1.3 | Hamcrest Core
org.apache.httpcomponents | httpclient | 4.5.2 | Apache HttpClient
org.apache.httpcomponents | httpcore | 4.4.4 | Apache HttpCore
org.slf4j | jcl-over-slf4j | 1.7.21 | JCL 1.1.1 implemented over SLF4J
org.slf4j | jul-to-slf4j | 1.7.21 | JUL to SLF4J bridge
org.apache.logging.log4j | log4j-api | 2.4.1 | Apache Log4j API
org.apache.logging.log4j | log4j-core | 2.4.1 | Apache Log4j Core
org.apache.logging.log4j | log4j-slf4j-impl | 2.4.1 | Apache Log4j SLF4J Binding
org.projectlombok | lombok | 1.16.6 | Project Lombok
org.slf4j | slf4j-api | 1.7.12 | SLF4J API Module
wsdl4j | wsdl4j | 1.6.3 | WSDL4J
javax.activation | activation | 1.1 | JavaBeans Activation Framework (JAF)
javax.mail | mail | 1.4 | JavaMail API
javax.servlet | javax.servlet-api | 3.0.1 | Java Servlet API
javax.validation | validation-api | 1.1.0.Final | Bean Validation API
org.aspectj | aspectjrt | 1.8.2 | AspectJ runtime
com.jcabi | jcabi-aspects | 0.19 | jcabi-aspects
com.jcabi | jcabi-log | 0.15 | jcabi-log
org.mockito | mockito-all | 1.10.19 | Mockito
junit | junit | 4.12 | JUnit

##Ehitamine

Alljärgnevalt on toodud näide, kuidas kaasata DHX adapteri teegid olemasoleva tarkvara sisse, kasutades ehitamiseks [Apache Maven](https://maven.apache.org/) ehitus-tarkvara.

Ülaltoodud välised sõltuvused laetakse Maveni kasutamise korral automaatselt alla.

Lisada oma DHS tarkvara ehitamise Maven pom.xml sisse järgmised sõltuvused:
```xml
		<dependency>
			<groupId>ee.ria.dhx</groupId>
			<artifactId>dhx-adapter-core</artifactId>
			<version>1.0.0</version>
			<scope>compile</scope>
		</dependency>
		<dependency>
			<groupId>ee.ria.dhx</groupId>
			<artifactId>dhx-adapter-ws</artifactId>
			<version>1.0.0</version>
			<scope>compile</scope>
		</dependency>
```
##Teadaolevad probleemid (sõltuvuste konfliktid)

**axiom-dom** ja **axis2-saaj** teekide kasutamisel Java classpathis ei tööta korrektselt XML objektide marshallimine/unmrashallimine (JAXB probleem). Nimelt manused jäävad tühjaks.

Soovitatav on eemaldada need teegid Java classpath seest. 

Maven-ga, juhul kui mingi muu kasutatav teek (näiteks axis2-codegen) sõltub nendest teekides, siis võib seda eemaldada järgmiselt:
```xml
	<dependency>
		<groupId>org.apache.axis2</groupId>
		<artifactId>axis2-codegen</artifactId>
		<version>1.4</version>
		<exclusions>
			<exclusion>
				<groupId>org.apache.ws.commons.axiom</groupId>
				<artifactId>axiom-dom</artifactId>
			</exclusion>
			<exclusion>
				<groupId>org.apache.axis2</groupId>
				<artifactId>axis2-saaj</artifactId>
			</exclusion>
		</exclusions>
	</dependency>
```

##Teegi laadimise häälestamine (web.xml ja applicationContext.xml)

Kõige lihtsam on DHX adapteri teeke kasutada Web (Servlet) Container tarkvara (Tomcat, Jetty, jne) sees, kasutades laadimiseks SpringFramework klasse [ContextLoaderListener](http://docs.spring.io/spring/docs/4.2.7.RELEASE/spring-framework-reference/html/beans.html#beans-java-instantiating-container-web) ja [MessageDispatcherServlet](http://docs.spring.io/spring-ws/site/reference/html/server.html#message-dispatcher-servlet).

Selleks tuleb `web.xml` häälestusfaili lisada sektsioonid:
```xml
  <listener>
      <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>	
```

```xml
<servlet>
  <servlet-name>spring-ws-dhx</servlet-name>
  <servlet-class>org.springframework.ws.transport.http.MessageDispatcherServlet</servlet-class>
  <init-param>
     <param-name>transformWsdlLocations</param-name>
     <param-value>true</param-value>
  </init-param>
  <init-param>
    <param-name>contextClass</param-name>
    <param-value>
      org.springframework.web.context.support.AnnotationConfigWebApplicationContext
    </param-value>
  </init-param>
  <init-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>ee.ria.dhx.ws.beanconfig.DhxEndpointConfig</param-value>
  </init-param>
  <load-on-startup>1</load-on-startup>
</servlet>
```

```xml
<servlet-mapping>
   <servlet-name>spring-ws-dhx</servlet-name>
   <url-pattern>/ws/*</url-pattern>
</servlet-mapping>
```

Lisaks tuleb ehitatava WAR-i sisse lisada fail `/WEB-INF/applicationContext.xml`
Selle sisu peaks olema järgmine:

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:task="http://www.springframework.org/schema/task"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context-2.5.xsd
	http://www.springframework.org/schema/task  
    http://www.springframework.org/schema/task/spring-task-3.0.xsd">
    
        <context:annotation-config />
        <context:component-scan base-package="ee.ria.dhx.*" />
        <task:annotation-driven scheduler="myScheduler" />
        <task:scheduler id="myScheduler" pool-size="10" />
        <context:property-placeholder location="WEB-INF/classes/dhx-application.properties" />
</beans>
```

##Häälestus fail (dhx-application.properties)

Servleti laadimisel otsitakse Servleti classpathist faili nimega `dhx-application.properties`.

Ehitamisel on soovitav see näiteks paigalda WAR-i sisse `/WEB-INF/classes` alamataloogi.

Selle näide on toodud failis [dhx-application.properties](https://github.com/e-gov/DHX-adapter/blob/master/src/main/resources/conf/development/ws/dhx-application.properties)

Selle sisu on näiteks:
```properites
soap.security-server=http://10.0.13.198
soap.xroad-instance=ee-dev
soap.member-class=GOV
soap.member-code=40000001

document-resend-template=30,120,1200
address-renew-timeout=*/20 * * * * ?
```

Allpool tabelis on toodud `dhx-application.properties` võimalike parameetrite kirjeldused. 
Neid parameetreid, millel on vaikimisi väärtus, ei pea properties faili lisama, juhul kui vaikimisi väärtust muuta ei soovita.

Parameeter | Vaikimisi väärtus | Näite väärtus | Kirjeldus
------------ | ------------- | ------------- | -------------
**soap.security-server** |  | http://10.0.13.198 | Turvaserveri aadress
**soap.xroad-instance** |  | ee | `ee-dev` arenduses, `ee` toodangus. Määratakse saatmisel X-tee päise `Header/client/xRoadInstance` väärtuseks
**soap.member-class** |  | GOV | Asutuse enda X-tee kuuluvuse klass (`COM` või `GOV`). Määratakse saatmisel X-tee päise `Header/client/memberClass` väärtuseks
**soap.member-code** |  | 40000001 | Asutuse enda registrikood. Määratakse saatmisel X-tee päise `Header/client/memberCode` väärtuseks
soap.default-subsystem | DHX |  | Asutuse enda X-tee DHX alamsüssteem. Määratakse saatmisel X-tee päise `Header/client/subsystemCode` väärtuseks. Näiteks ADIT kasutab alamsüsteemi, ning kui ta saadab dokumente välja, siis ta peaks selleks väärtustama `DHX.adit`
soap.security-server-appender | /cgi-bin/consumer_proxy |  | Turvaserveri URL-i path 
soap.targetnamespace | `http://dhx.x-road.eu/producer` |  | SOAP X-tee päringute nimeruum
soap.protocol-version | 4.0 |  | X-tee protokolli versioon. Määratakse saatmisel X-tee päise `Header/protocolVersion` väärtuseks.
soap.global-conf-location | verificationconf |  | Määrab millisest X-tee serveri URL-i kataloogist laetakse alla X-tee Globaalkonfiguratsioon. Üldjuhul asub see `/verificationconf/ee/shared-params.xml`
soap.global-conf-filename | shared-params.xml |  | Määrab millisest X-tee serveri URL-i failist laetakse alla X-tee Globaalkonfiguratsioon. Üldjuhul asub see `/verificationconf/ee/shared-params.xml`
soap.dhx-representation-group-name | DHX vahendajad |  | Määrab DHX vahendajate grupi nime, mille järgi otsitakse vahendajaid X-tee globaalkonfiguratsioonist
soap.accepted-subsystems | DHX |  | Määrab milliste alamsüsteemidega võtab asutus dokumente vastu. Komaga eraldatud list. Näiteks kui RIA omab mitut alamsüsteemi DHX.dvk ja DHX.adit, ning kui ta sooviks mõlema alamsüsteemi dokumente vastu võtta ühe serveri teenuse kaudu, siis ta peaks väärtustama `soap.accepted-subsystems=DHX.dvk,DHX.adit`
soap.send-document-service-code | sendDocument |  | Teenuse nimi. DHX protokoll nõuab et see peab olema alati `sendDocument`. Määratakse dokumendi saatmisel X-tee päise `Header/service/serviceCode` väärtuseks
soap.send-document-service-version | v1 |  | Määratakse dokumendi saatmisel X-tee päise `Header/service/serviceVersion` väärtuseks.
soap.representatives-service-code | representationList |  | Määratakse vahendatavate nimekirja päringu saatmisel X-tee päise `Header/service/serviceCode` väärtuseks.
soap.representatives-service-version | v1 |  | Määratakse vahendatavate nimekirja päringu saatmisel X-tee päise `Header/service/serviceVersion` väärtuseks.
soap.connection-timeout | 60000 |  | SOAP päringute tegemisel kasutatav HTTP ühenduse avamise timeout väärtus millisekundites. Vaikimisi 1 minut
soap.read-timeout | 120000 |  | SOAP päringute tegemisel kasutatav HTTP päringu vastuse ootamise timeout väärtus millisekundites. Vaikimisi 2 minutit. Kui saadetavad failid on suured, siis võib suurendada.
soap.dhx-subsystem-prefix | DHX |  | DHX Alamsüsteemide prefiks, mille järgi otsitakse X-tee globaalkonfiguratsioonist DHX adressaate. DHX protokoll nõuab et see oleks alati konstant `DHX`
dhx.capsule-validate | true |  | Määrab kas valideerida saabunud ja saadetava dokumendi XML kapsel XSD schema vastu või mitte. Kui dokument ei valideeru, siis vastatakse saatjale veaga [DHX.Validation](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#veakoodid). [Kapsli 2.1 Schema](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-core/src/main/resources/Dvk_kapsel_vers_2_1_eng_est.xsd)
dhx.parse-capsule | true |  | Määrab kas parsida saabunud või saadetava dokumendi Kapsli XML fail lahti Java objektideks
dhx.check-recipient | true |  | Määrab kas kontrollida saabunud dokumendi adressaadi korrektsust. Kontrollitakse kas Kapsli sees olev adressaat vastab vastuvõtja registrikoodile. Kui adressaat on vale, siis vastatakse saatjale veaga [DHX.InvalidAddressee](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#veakoodid)
dhx.check-sender | false |  | Määrab kas kontrollida saabunud dokumendi saatja korrektsust. Kas Kapsli XML-is asuv saatja vastab X-tee client päises määratletud saatja andmetele.
dhx.check-duplicate | true |  | Määrab kas kontrollida saabunud saadetise topelt saabumist. Kui true, siis kutsutakse välja `DhxImplementationSpecificService` [isDuplicatePackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#isDuplicatePackage-ee.ria.dhx.types.InternalXroadMember-java.lang.String-). Topelt korral väljastatakse saatjale viga [DHX.Duplicate](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#veakoodid)
**dhx.document-resend-template** | 30,120,1200 |  | Määrab uuesti saatmise ürituste arvu ja oote ajad [Crontab pattern](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html) formaadis. Kasutatakse ainult asünkroonsel saatmisel. Antud näide määrab, et kokku tehakse 4 saatmisüritust. Uuesti saatmist üritatakse kõigepealt 30 sekundi järel, seejärel 120 sekundi (2 minuti) järel ning seejärel 1200 sekundi (20 minuti) järel. Kui ka viimane saatmine ebaõnnestus, siis lõpetatakse üritamine.
dhx.wsdl-file | dhx.wsdl |  | Asutuse poolt pakutava DHX teenuse [WSDL faili](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-ws/src/main/resources/dhx.wsdl) nimi. Selle nimelist faili otsitakse käivitamisel Java Classpathist. WSDL fail on kõikide DHX rakendajate jaoks konstantne ja seda muuta ei ole vaja
dhx.protocol-version | 1.0 |  | DHX protokolli versiooni number, mis saadetakse `sendDocument` päringu [DHXVersion](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#p%C3%A4ringu-sisend) parameetrina. 
dhx.check-dhx-version | true |  | Kas dokumendi saabumisel kontrollida ka saatja poolt määratud DHX versiooni vastavust. Kui versioon ei ole õige siis tagastatakse saatjale DHX viga [DHX.UnsupportedVersion](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#veakoodid)
dhx.accepted-dhx-protocol-versions | 1.0 |  | Milliseid protokolli versioone toetatakse dokumendi vastuvõtmisel. Komaga eraldatud list. näiteks tulevikus võib see olla `1.0,2.0`. Töötab koos eelmise parameetriga `dhx.check-dhx-version`
dhx.marshall-context | `ee.ria.dhx. types.ee.riik.schemas. deccontainer.vers_2_1: ee.ria.dhx.types.eu. x_road.dhx.producer: ee.ria.dhx.types.eu. x_road.xsd.identifiers: ee.ria.dhx.types.eu. x_road.xsd. representation: ee.ria.dhx.types.eu. x_road.xsd.xroad` |  | Määrab millistes Java pakettides asuvaid XML tüüpide objekte püütakse JAXB parseriga töödelda. Kui SOAP päringus ja/või Kapsli XML-is saadetakse lisaandmeid kolmandatest nimeruumidest, siis võib siia lisada uusi tüüpe. Kaspli sees saab laiendatud elemente saata [RecordTypeSpecificMetadata](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-core/src/main/resources/Dvk_kapsel_vers_2_1_eng_est.xsd#L426) elmendi sees (lubatud `<xs:any namespace="##any">`)
dhx.xsd.capsule-xsd-file21 | jar://Dvk_kapsel_vers_ 2_1_eng_est.xsd |  | Määrab kust otsitakse Kapsli 2.1 versiooni XSD schema faili. Üldjuhul võetakse see `dhx-adapter-core` JAR-i seest.
**dhx.renew-address-list-on-startup** | true |  | Määrab kas Java serveri startimise järel käivitatakse adressaatide nimekirja uuendamine. Adressaatide nimekirja uuendamine võib erijuhtudel võtta kaua aega (näiteks kui mõne vahendaja server on maas). Seepärast on DHX adapteri teegi kasutamisel mõistlik see puhverdada andmebaasis ja implementeerida `DhxImplementationSpecificService` [getAdresseeList](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#getAdresseeList--). Sel juhul tuleks väärtustada `dhx.renew-address-list-on-startup=false`
**address-renew-timeout** |  | 0 */20 * * * ? | Määrab [adressaatide nimekirja](https://e-gov.github.io/DHX/#74-lokaalne-aadressiraamat) uuendamise sageduse. [Crontab formaat](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html) kujul: `<second> <minute> <hour> <day> <month> <weekday>`. Väärtus `*/20` tähendab igal 20-nendal ühikul. Seega `0 */20 * * * ?` tähendab iga 20 minuti järel. Võib soovi korral muuta, näiteks iga päeva kell 7:00 on `0 0 7 * * *`

##Funktsionaalsuse üldpõhimõtted

DHX adapteri Java teegi põhifunktsionaalsus on dokumendi saatmine, dokumendi vastuvõtmine ja [lokaalse aadressiraamatu](https://e-gov.github.io/DHX/#74-lokaalne-aadressiraamat) koostamine.
 
Põhiline osa, mis DHX adapteri teegi kasutajat (arendajat) huvitab, asub pakettides
- [ee.ria.dhx.ws.service](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/package-summary.html) – java teenuste liidesed (service interfaces)
- [ee.ria.dhx.ws.service.impl](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/impl/package-summary.html) – java teenuste realisatsioonid (service interface implementations)

Nendes pakettides asuvad teenused on

Klass/liides | Kirjeldus 
------------ | -------------
[AddressService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AddressService.html) | Teenus aadressiraamatu koostamiseks ja uuendamiseks
[DhxPackageService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxPackageService.html) | Teenused dokumendi vastuvõtmiseks ja sünkroonseks saatmiseks
[AsyncDhxPackageService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AsyncDhxPackageService.html) | Teenus dokumendi asünkroonseks saatmiseks
[DhxMarshallerService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxMarshallerService.html) | Teenus XML objektide (Kapsli) koostamiseks 
[DhxPackageProviderService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxPackageProviderService.html) | Teenus SOAP päringu XML objekti koostamiseks
[DhxImplementationSpecificService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html) | Realisatsiooni spetsiifilised callback liidesed. Selle liidese implementatsiooni peaks DHX adapterit kasutav arendaja ise realiseerima.

Arendaja jaoks kõige tähtsam neist on [DhxImplementationSpecificService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html), mille meetodid peab arendaja peab ise realiseerima.

Näiteks:

```java
package com.example.service.impl;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;

@Service("dhxImplementationSpecificService")
public class CustomDhxImplementationSpecificService 
                implements DhxImplementationSpecificService {
   . . . 
}
```

Siin `@Service` tag määrab, et DHX adapteri seest kasutav teenus `dhxImplementationSpecificService` on nüüd ülekirjutatud omatehtud klassiga. 
Seega nüüd kasutab dokumendi vastuvõtmise ja saatmise automaatloogika „callback“ liidesena arendaja enda loodud klassi `CustomDhxImplementationSpecificService`.

##Aadressiraamatu koostamise ja kasutamise liides

DHX adresseerimisel tuleb silmas pidada, et ainuüksi asutuse registrikoodi kasutamine ei taga korrektset adresseerimist. 
Üheseks adresseerimiseks tuleb kasutada kombinatsiooni `registrikood + alamsüsteem`. Näiteks kui dokument adresseeritakse Lääne Ringkonnaprokuratuurile, siis adresseerimiseks piisab kombinatsioonist `code=70000906 + subsystem=DHX.laane`.
Kui adresseeritakse Lõuna Ringkonnaprokuratuurile, siis adresseerimiseks piisab `code=70000906 + subsystem=DHX.louna`.

Aadressiraamatu koostamiseks ja küsimiseks tuleb kasutada liidest [AddressService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AddressService.html).

Sellel on kolm meetodit. 

- Meetod [getAdresseeList](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AddressService.html#getAdresseeList--) tagastab puhverdatud (eelnevalt koostatud) lokaalse aadressiraamatu.

- Meetod [renewAddressList](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AddressService.html#renewAddressList--) käivitab uuesti aadressiraamatu koostamise algoritmi. Viimane käivitatakse soovi korral timer jobi poolt perioodiliselt (perioodi määrab `dhx-application.properties` parameeter `address-renew-timeout=0 */20 * * * ?`).

- Meetod [getClientForMemberCode](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AddressService.html#getClientForMemberCode-java.lang.String-java.lang.String-) hõlbustab dokumendi saatmiseks vajaliku adressaadi tehniliste andmete leidmist asutuse unikaalse kombinatsiooni  `registrikood + alamsüsteem ` järgi.

Aadressiraamatu pikemaajaliseks säilitamiseks (näiteks andmebaasis või failisüsteemis) võib üle kirjutada DhxImplementationSpecificService meetodid [saveAddresseeList](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#saveAddresseeList-java.util.List-) ja [getAdresseeList](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#getAdresseeList--).
Kui neid kasutatakse, siis need peaks salvestama ja tagastama kõik [InternalXroadMember](https://e-gov.github.io/DHX-adapter/dhx-adapter-core/doc/ee/ria/dhx/types/InternalXroadMember.html) atribuudid (mis on allpool tabelis kirjas).

Näide
```java
@Service("dhxImplementationSpecificService")
public class CustomDhxImplementationSpecificService 
                implements DhxImplementationSpecificService {
  @Override
  public List<InternalXroadMember> getAdresseeList() {
    // retrieve from database or filesystem
  }

  @Override
  public void saveAddresseeList(List<InternalXroadMember> members) {
    // store to database or filesystem
  }

  ...
}
```

Adressaati [InternalXroadMember](https://e-gov.github.io/DHX-adapter/dhx-adapter-core/doc/ee/ria/dhx/types/InternalXroadMember.html) identifitseerivad DHX protokollis järgmised väljad

Väli | Näide | Kirjeldus
------------ | ------------- | -------------
xroadInstance | EE | Riik EE
memberClass | GOV | GOV- Valitsus, COM - eraettevõte
memberCode | 70000001 | Asutuse registrikood
subsystemCode | DHX või DHX.adit | Alamsüsteemi kood. Peab algama DHX prefiksiga. Üldjuhul lihtsalt DHX
Name | Riigi infosüsteemide keskus | Asutuse või alamsüsteemi nimi
representee.representeeCode | 70012121 | Esindatava registrikood
representee.representeeSystem |  DHX.subsystem | Üldjuhul tühi, aga erijuhul kui esindataval on mitu alamsüsteemi, siis alamsüsteemi kood
representee.representeeName | Lasteaed Pallipõnn | Esindatava nimi või esindatava alamsüsteemi nimi

Meetod [getAdresseeList](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AddressService.html#getAdresseeList--) tagastab [InternalXroadMember](https://e-gov.github.io/DHX-adapter/dhx-adapter-core/doc/ee/ria/dhx/types/InternalXroadMember.html) objektide massiivi, mis on kõikide DHX adressaatide nimekiri.

Adressaate on mitut tüüpi
- Otse DHX võimekusega (alamsüsteem DHX)
- Otse DHX võimekusega (mitu alamsüsteemi `DHX.subsystem1` ja `DHX.subsystem2` jne)
- Vahendaja kaudu DHX võimekusega (alamsüsteem on üldjuhul tühi)
- Vahendaja kaudu DHX võimekusega (mitu alamsüsteemi `DHX.subsystem1` ja `DHX.subsystem2` jne)

**NB!** Dokumendi saatmisel, saadetise loomisel allpool toodud [getOutgoingPackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxPackageProviderService.html) meetoditega, tuleb kindlasti ette anda kõik `InternalXroadMember` eksemplari atribuudid. 
See tähendab, et kui adressaat omab `subsystemCode` väärtust, siis see tuleb kindlasti määratleda ka `getOutgoingPackage()` väljakutsel.
Kui adressaat omab `representeeCode` väärtust, siis see tuleb kindlasti määratleda ka `getOutgoingPackage()` väljakutsel.

**Kõide kindlam** on kasutada [getOutgoingPackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxPackageProviderService.html#getOutgoingPackage-java.io.File-java.lang.String-ee.ria.dhx.types.InternalXroadMember-) variatsioone, kus sisendis on parameeter  `InternalXroadMember recipient `.

Vajaliku **eelväärtustatud `InternalXroadMember` objekti leidmiseks** saab kasutada `AddressService` meetodit  [getClientForMemberCode](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AddressService.html#getClientForMemberCode-java.lang.String-java.lang.String-), mis leiab lokaalsest aadressiraamatust korrektse kirje, kasutades sisendparameetriteks kombinatsiooni `registrikood` + `subsystem`.


##Dokumendi saatmine (sünkroonselt)

Dokumendi sünkroonseks saatmiseks tuleb välja kutsuda teenuse `ee.ria.dhx.ws.service.DhxPackageService` meetodit [sendPackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxPackageService.html#sendPackage-ee.ria.dhx.types.OutgoingDhxPackage-). 

**NB!** Dokumendi sünkroonselt saatmine saadab dokumendi ainult üks kord, oodates ära saatmise tulemuse (success/fail). Üldjuhul tuleks dokumendi saatmiseks kasutada asünkroonset saatjat [AsyncDhxPackageService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AsyncDhxPackageService.html), mis teostab mitu saatmisüritust.

Saadetava XML paketi koostamiseks tuleb kasutada teenuse  `ee.ria.dhx.ws.service.DhxPackageProviderService ` meetodeid [getOutgoingPackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxPackageProviderService.html#getOutgoingPackage-java.io.File-java.lang.String-).


Näide
```java
package com.example.service;
import ee.ria.dhx.ws.service.DhxPackageService;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.InternalXroadMember;


public class Sender { 
  @Autowired
  DhxPackageService dhxPackageService;

  @Autowired
  AddressService addressService;

  @Autowired
  DhxPackageProviderService dhxPackageProviderService;

  public void sendExample() throws DhxException {
     // leiame adressaadi tehnilised andmed
     InternalXroadMember recipient = addressService.getClientForMemberCode(
        "70000001",  // adressaadi registrikood
        "DHX"); // adressaadi alamsüsteem on üldjuhul DHX (erandjuhul DHS.sybsystem)

     // genereerime saadetise
     OutgoingDhxPackage dhxPackage = dhxPackageProviderService.getOutgoingPackage(
         new File("saadetav-dokumendi-kapsel.xml"),
         UUID.randomUUID().toString(), // unikaalne ise genereeritud saadetise id
         recipient);

    // saadame dokumendi üle X-tee ja ootame sünkroonselt vastust
    DhxSendDocumentResult result = dhxPackageService.sendPackage(dhxPackage);

    // check result error
    if (result.occuredException !=  null 
       || result.getResponse().getFault() != null) {
      // saatmisel ilmnes viga
    }
  }
}
```

Kui soovitakse sama kapslit saata korraga mitme DHX adressaadile, siis tuleb see igale adressaadile saata eraldi. Selle lihtsustamiseks on loodud `ee.ria.dhx.ws.service.DhxPackageService` meetod [sendMultiplePackages](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxPackageService.html#sendMultiplePackages-java.util.List-). 

##Dokumendi saatmine (asünkroonselt)

Dokumendi asünkroonselt saatmise liides on sarnane sünkroonselt saatmise liidesele.
 
Erinevus on selles et liidese [AsyncDhxPackageService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AsyncDhxPackageService.html) meetod [sendPackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AsyncDhxPackageService.html#sendPackage-ee.ria.dhx.types.OutgoingDhxPackage-) käivitatakse asünkroonselt (eraldi threadis) ja käivitavas threadis selle meetodi väljakutse lõppeb/tagastab koheselt (ei jää vastust ära ootama).

Asünkroonselt käivitamine püüab tehnilise vea korral uuesti saatmist (saatmisürituste arv ja sagedus on määratud `dhx-application.properties` parameeteriga `document-resend-template=30,120,1200`).

Pärast saatmise õnnestumist (või kui viimane lõplik saatmisüritus ebaõnnestus) kutsutakse välja [DhxImplementationSpecificService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html) klassi meetod [saveSendResult](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#saveSendResult-ee.ria.dhx.types.DhxSendDocumentResult-java.util.List-). 
Selle meetodi peab arendaja ise implementeerima, salvestades vastuse näiteks oma DHS andmebaasi vms.

Callback liidese meetodi `saveSendResult` realisatsiooni näide
```java
@Service("dhxImplementationSpecificService")
public class CustomDhxImplementationSpecificService 
                implements DhxImplementationSpecificService {

  @Override
  public void saveSendResult(DhxSendDocumentResult finalResult,
      List<AsyncDhxSendDocumentResult> retryResults) {
     if (finalResult.occuredException !=  null 
        || finalResult.getResponse().getFault() != null) {
      // saatmisel ilmnes viga
     } else {
       // success
       String id = finalResult.getResponse().getReceiptId();
     }
  }

  ...
}
```

Asünkroonse saatmise meetodi [sendPackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AsyncDhxPackageService.html#sendPackage-ee.ria.dhx.types.OutgoingDhxPackage-) väljakutsumise näide
```java
package com.example.service;
import ee.ria.dhx.ws.service.AsyncDhxPackageService;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;
import ee.ria.dhx.types.InternalXroadMember;

public class Sender { 

  @Autowired
  AsyncDhxPackageService asyncDhxPackageService;

  @Autowired
  AddressService addressService;

  @Autowired
  DhxPackageProviderService dhxPackageProviderService;

  public void sendExample() throws DhxException {
     // leiame adressaadi tehnilised andmed
     InternalXroadMember recipient = addressService.getClientForMemberCode(
        "70000001",  // adressaadi registrikood
        "DHX"); // adressaadi alamsüsteem on üldjuhul DHX (erandjuhul DHS.sybsystem)

     // genereerime saadetise
     OutgoingDhxPackage dhxPackage = dhxPackageProviderService.getOutgoingPackage(
         new File("saadetav-dokumendi-kapsel.xml"),
         UUID.randomUUID().toString(), // unikaalne ise genereeritud saadetise id
         recipient);

    // saadame dokumendi üle X-tee 
    // tulemust kohe ei saa, vaid meetodi väljakutse tagastab tööjärje koheselt kehtivale threadile  
    // siis kui saatmine on tehtud kutsutakse asünkroonses threadis välja    
    // DhxImplementationSpecificService.saveSendResult
    asyncDhxPackageService.sendPackage(dhxPackage);
  }
  
}
```

Kui soovitakse sama kapslit saata korraga mitme DHX adressaadile, siis tuleb see igale adressaadile saata eraldi. Selle lihtsustamiseks on loodud `ee.ria.dhx.ws.service.AsyncDhxPackageService` meetod [sendMultiplePackages](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/AsyncDhxPackageService.html#sendMultiplePackages-java.util.List-).


##Dokumendi vastuvõtmise liides

Ülaltoodud `web.xml` häälestuse kasutamisel registreeritakse serverisse automaatselt web service endpoint.  
Selle aadress on `http://<hostname>:<port>/ws/dhx.wsdl`

Sellelt aadressilt pakutav DHX sendDocument jt teenused tuleb registreerida X-tee turvaserveris.

Arendaja poolt tuleb dokumendi vastuvõtmiseks ja andmebaasi salvestamiseks realiseerida [DhxImplementationSpecificService](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html) meetodid [isDuplicatePackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#isDuplicatePackage-ee.ria.dhx.types.InternalXroadMember-java.lang.String-) ja [receiveDocument](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#receiveDocument-ee.ria.dhx.types.IncomingDhxPackage-org.springframework.ws.context.MessageContext-).

Dokumendi serverisse saabumisel kutsutakse kõigepealt välja [isDuplicatePackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#isDuplicatePackage-ee.ria.dhx.types.InternalXroadMember-java.lang.String-), millega kontrollitakse kas see on topelt saatmine (sama dokumendi saadetis on DHS-ile saabunud teist või enamat korda).
Kui on topelt saatmine, siis tagastatakse saatjale SOAP päringu vastuse viga [DHX.Duplicate](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#veakoodid).
Kui ei olnud topelt saatmine siis kutsutakse välja meetod [receiveDocument](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#receiveDocument-ee.ria.dhx.types.IncomingDhxPackage-org.springframework.ws.context.MessageContext-), mis peaks salvestama dokumendi DHS andmebaasi (näiteks "Saabunud dokumendid" kausta).

Näide
```java
@Service("dhxImplementationSpecificService")
public class CustomDhxImplementationSpecificService 
                implements DhxImplementationSpecificService {
  @Override
  public boolean isDuplicatePackage(InternalXroadMember from,
      String consignmentId) {
    // check for duplicate: same consignmentId from the same sender (from)  
  }

  @Override
  public String receiveDocument(IncomingDhxPackage document,
      MessageContext context) throws DhxException {
    String receiptId = UUID.randomUUID().toString();
    // get document Capsule XML
    DataHandler kapsel = document.getDocumentFile();
    // store Capsule to database
     ...
    return receiptId;
  }
  
  ...
}
```

