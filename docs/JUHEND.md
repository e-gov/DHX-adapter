![](EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg)

ET | [EN](GUIDE.md)

# DHX-adapteri kasutusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

## Sissejuhatus

DHX adapter on Java teek, milles on realiseeritud dokumendi saatmise, vastuvõtmise ja aadressiraamatu koostamise funktsionaalsus vastavalt DHX protokolli nõuetele.

Antud juhend on mõeldud kasutamiseks tarkvara arendajatele, kes soovivad hakata oma Dokumendihaldussüsteemis (DHS) kasutama DHX protokolli.

DHX adapteri lähtekood asub aadressil https://github.com/e-gov/DHX-adapter

Selles asuvad kolm alamteeki
- [dhx-adapter-core](https://e-gov.github.io/DHX-adapter/dhx-adapter-core/doc/) – selles asuvad klassid XML (Kaplsi) ja SOAP objektide koostamiseks ja töötlemiseks,  vigade klassid ning mõned üldkasutatavad utiliit klassid
- [dhx-adapter-ws](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/) – selles asuvad klassid dokumendi saatmiseks (SOAP client), aadressiraamatu koostamiseks (SOAP client) ja dokumendi vastuvõtmiseks (SOAP Service Endpoint)
- [dhx-adapter-server](https://e-gov.github.io/DHX-adapter/dhx-adapter-server/doc/) – eraldiseisev adapter server (Variant C), mis puhverdab saabunud dokumendid vahe andmebaasis ja pakub vana [DVK liidese]((https://github.com/e-gov/DVK)) sarnaseid SOAP teenuseid

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
soap.user-id=38605150320
soap.protocol-version=4.0
soap.member-code=40000001
document-resend-template=5,10,15
address-renew-timeout=*/20 * * * * ?
```
