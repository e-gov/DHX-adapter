![](EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg)

[ET](JUHEND.md) | EN

# DHX-adapter usage guide

![](DHX.PNG)  ![](X-ROAD.PNG)

## Introduction

DHX adpater is Java software library that implements [DHX protocol](https://e-gov.github.io/DHX/EN.html) functionality for [sending documents](https://e-gov.github.io/DHX/EN.html#7-saatmine), [receiving documents](https://e-gov.github.io/DHX/EN.html#8-vastuv%C3%B5tmine) and generating [local address book](https://e-gov.github.io/DHX/EN.html#74-lokaalne-aadressiraamat). 

This guide is intended for software developers (DHX implementers), who wish to use DHX protocol in their document management system (DMS).

Source code of DHX Adapter is located in the url https://github.com/e-gov/DHX-adapter

It contains three sub-packages
- [dhx-adapter-core](https://e-gov.github.io/DHX-adapter/dhx-adapter-core/doc/) – contains classes for creating and parsing XML objects (Capsule and SOAP), exception classes and some general utility classes
- [dhx-adapter-ws](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/) – contains classes for sending document (SOAP client), for generating address book (SOAP client) and for receiving documents (SOAP Service endpoint)
- [dhx-adapter-server](https://e-gov.github.io/DHX-adapter/dhx-adapter-server/doc/) – a separately used adapter server (Variant C), that caches received documents in local database and offers SOAP interface similar to old [DVK interface](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md)

The two first packages **dhx-adapter-core** and **dhx-adapter-ws** are for direct (java) interfacing.

**dhx-adapter-server** is intended for them, who cannot use direct (java) interfacing, but plan to deploy separate mediator server, in order to continue to use old style [DVK SOAP interface](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md).

##Base platform and external dependencies

DHX adapter packages depends on the components shown below.

[Java SE](https://en.wikipedia.org/wiki/Java_Platform,_Standard_Edition) 1.7 (or newer) version is needed for compiling and running the DHX adapter. 

As DHX adapter package provides web services (is not only web client), it depends on J2EE [Java Servlet API](https://en.wikipedia.org/wiki/Java_servlet), by using [Spring Web Services](http://projects.spring.io/spring-ws/).

[Java Architecture for XML Binding - JAXB](https://docs.oracle.com/javase/7/docs/api/javax/xml/bind/package-summary.html) API is used for XML marshalling (is part of Java SE 7).

DHX adapter Java package is based on Spring Framework architecture by using extensively its sub-modules:
- For configuraton and initializing (Spring AOP, Spring Context, etc)
- For making HTTP SOAP client request (Spring WS Client, Apache HttpClient)
- For providing HTTP SOAP web service (Spring WS Server Endpoint, Java Servlet API)

Direct and indirect external references (dependencies) of DHX adapter are following

Group | Package | Version | Notes
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

##Building

The following example is given on how to involve the DHX adapter package in existing (or new) software project, by using [Apache Maven](https://maven.apache.org/).

The above external dependencies are downloaded automatically by Maven.

The following dependencies should be appended into Maven pom.xml
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

##Known issues (dependency conflicts)

In case **axiom-dom** or **axis2-saaj** are in Java classpath, the XML marshalling/unmarshalling will not work properly (attachments remain empty). It is known JAXB issue.

It is recommended to remove these libraries from Java classpath. 

With Maven, if third party library (for axample axis2-codegen) depends on these, it could be removed like this:

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

##Loading setup (web.xml and applicationContext.xml)

The simpliest way is to use DHX adapter inside Web (Servlet) Container (Tomcat, Jetty, etc), by using Spring Framework classes [ContextLoaderListener](http://docs.spring.io/spring/docs/4.2.7.RELEASE/spring-framework-reference/html/beans.html#beans-java-instantiating-container-web) and [MessageDispatcherServlet](http://docs.spring.io/spring-ws/site/reference/html/server.html#message-dispatcher-servlet).

The `web.xml` must be supplemented with sections:
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

Also `/WEB-INF/applicationContext.xml` must be placed inside WAR. 
It should look like the following:

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
        <task:scheduler id="myScheduler" pool-size="3" />
        <context:property-placeholder location="WEB-INF/classes/dhx-application.properties" />
</beans>
```

##Configuration properties (dhx-application.properties)

On servlet initialization, the file named `dhx-application.properties` is searched from Servlet classpath. 

Building processs should include it into WAR `/WEB-INF/classes` directory.

Example is in file [dhx-application.properties](https://github.com/e-gov/DHX-adapter/blob/master/src/main/resources/conf/development/ws/dhx-application.properties)

Example content:
```properites
soap.security-server=http://10.0.13.198
soap.xroad-instance=ee-dev
soap.member-class=GOV
soap.member-code=40000001

document-resend-template=30,120,1200
address-renew-timeout=*/20 * * * * ?
```

Possible `dhx-application.properties` property names are described in table below. 
The parameters with default value do not have to be added into proprties file (if you do not want to change the value).

Parameter | Default value | Example value | Description
------------ | ------------- | ------------- | -------------
**soap.security-server** |  | http://10.0.13.198 | X-road security server network address
**soap.xroad-instance** |  | ee | Country code. `ee-dev` in develepment, `ee` in production. Assigned to X-road header `Header/client/xRoadInstance` value in SOAP request. 
**soap.member-class** |  | GOV | Organization X-road member class (`COM` or `GOV`). Assigned to X-road header `Header/client/memberClass` value in SOAP request.
**soap.member-code** |  | 40000001 | Organization registration code. Assigned to X-road header `Header/client/memberCode` value in SOAP request.
soap.default-subsystem | DHX |  | Organization X-road DHX subsystem code. Assigned to X-road header `Header/client/subsystemCode` value in SOAP request. If organization has multiple DHX subsystems, it could be different (like `DHX.adit`)
soap.security-server-appender | /cgi-bin/consumer_proxy |  | X-road security server URL path 
soap.targetnamespace | `http://dhx.x-road.eu/producer` |  | SOAP X-road namespace
soap.protocol-version | 4.0 |  | X-road protocol version. Assigned to X-road header `Header/protocolVersion` value in SOAP request.
soap.global-conf-location | verificationconf |  | The path prefix, that determines X-road Global configuration download URL. In general it is `/verificationconf/ee/shared-params.xml`
soap.global-conf-filename | shared-params.xml |  | The filename suffix, that determines X-road Global configuration download URL. In general it is `/verificationconf/ee/shared-params.xml`
soap.dhx-representation-group-name | DHX vahendajad |  | DHX intermediary group name. Used for searching mediators from X-road global configuration.
soap.accepted-subsystems | DHX |  | Specifies organization DHX sub-systems (for document receival). Comma separated list. Example: In case organization has several DHX sub-systems DHX.dvk and DHX.adit (served by single DHX endpoint), it could be assigned as `soap.accepted-subsystems=DHX.dvk,DHX.adit`
soap.send-document-service-code | sendDocument |  | Service operation name. DHX protocol requires constant `sendDocument`. Assigned to X-road header `Header/service/serviceCode` value in SOAP request.
soap.send-document-service-version | v1 |  | Assigned to X-road header `Header/service/serviceVersion` value in SOAP request.
soap.representatives-service-code | representationList |  | Representee list operation name. Assigned to X-road header `Header/service/serviceCode` value in SOAP request.
soap.representatives-service-version | v1 |  | Representee list operation version. Assigned to X-road header `Header/service/serviceVersion` value in SOAP request.
soap.connection-timeout | 60000 |  | HTTP connection opening timeout (when making SOAP requests). Milliseconds. Default is 1 minute.
soap.read-timeout | 120000 |  | HTTP response waiting timeout (when making SOAP requests). Milliseconds. Default is 2 minutes. Could be increased in case document files are large. 
soap.dhx-subsystem-prefix | DHX |  | DHX sub-system prefix. Used for searching DHX addressees from X-road global configuration. DHX protocol requires it to be constant `DHX`
dhx.capsule-validate | true |  | Specifies whether to validate the document (both received and sended) Capsule XML against its XSD schema. If document does not validate, then respond with error [DHX.Validation](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#veakoodid) to the sender. [Capsule 2.1 XSD Schema](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-core/src/main/resources/Dvk_kapsel_vers_2_1_eng_est.xsd)  
dhx.parse-capsule | true |  | Specifies whether to marshall (parse) the incoming document Capsule XML into Java objects. 
dhx.check-recipient | true |  | Specifies whether to validate incoming document addressees. Validation checks whether addressee inside Capsule XML is the same as receiver (our own) organization code. If addressee is invalid, then respond with error [DHX.InvalidAddressee](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#veakoodid) to the sender.
dhx.check-sender | false |  | Specifies whether to check sender of the incoming document.  Validation checks whether the sender inside Capsule XML is the same as sender (client) in X-road header.
dhx.check-duplicate | true |  | Specifies whether to execute duplication checks on incoming documents consignments.  If true, the  `DhxImplementationSpecificService` [isDuplicatePackage](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#isDuplicatePackage-ee.ria.dhx.types.InternalXroadMember-java.lang.String-) is called. If it is duplicate consingment, then respond with error [DHX.Duplicate](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#veakoodid) to the sender.
**dhx.document-resend-template** | 30,120,1200 |  | Specifies sending re-attempting count and wait times. Used only when sending asynchronously. This example determines that total 4 sending attepts are made. Re-attempt are made at first after 30 seconds, then after 120 seconds (2 minutes) and finally after 1200 sconds (20 minutes). If final attempt fails, then finish.
dhx.wsdl-file | dhx.wsdl |  | DHX web service [WSDL file](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-ws/src/main/resources/dhx.wsdl) name. This wsdl file is searched from Java Classpath on server restart. WSDL file is same for all DHX implementers and needs no changes.
dhx.protocol-version | 1.0 |  | DHX protocol version number. Sended inside `sendDocument` request as paramater [DHXVersion](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#p%C3%A4ringu-sisend) value. 
dhx.check-dhx-version | true |  | Specifies whether to check DHXVersion validity on document arrival. If version is not right, then respond with error [DHX.UnsupportedVersion](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md#veakoodid) to the sender.
dhx.accepted-dhx-protocol-versions | 1.0 |  | Specifies what DHX protocol versions we accept on document arrival. Comma sparated list. In future it might be `1.0,2.0`. Works together with previous `dhx.check-dhx-version` parameter.
dhx.marshall-context | `ee.ria.dhx. types.ee.riik.schemas. deccontainer.vers_2_1: ee.ria.dhx.types.eu. x_road.dhx.producer: ee.ria.dhx.types.eu. x_road.xsd.identifiers: ee.ria.dhx.types.eu. x_road.xsd. representation: ee.ria.dhx.types.eu. x_road.xsd.xroad` |  | Specifies Java package names that contain XML type objects (marshalled/unmarshalled by JAXB). If SOAP request or Capsule XML contains additional extension elements from third party namespaces, then new types could be added. Capsule XML contains extenison point inside [RecordTypeSpecificMetadata](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-core/src/main/resources/Dvk_kapsel_vers_2_1_eng_est.xsd#L426) element (`<xs:any namespace="##any">`)
dhx.xsd.capsule-xsd-file21 | jar://Dvk_kapsel_vers_ 2_1_eng_est.xsd |  | Specifies location to search for Capsule XSD schema file. In general it is inside `dhx-adapter-core` jar.
**dhx.renew-address-list-on-startup** | true |  | Specifies whether to start adress book renewal job on server restart. Address book renewal could take long time in special case (if a DHX mediator has its servers down). Therefore it is reasonable to cache address list in local database and implement `DhxImplementationSpecificService` method [getAdresseeList](https://e-gov.github.io/DHX-adapter/dhx-adapter-ws/doc/ee/ria/dhx/ws/service/DhxImplementationSpecificService.html#getAdresseeList--). In this case use `dhx.renew-address-list-on-startup=false`
**address-renew-timeout** |  | 0 */20 * * * ? | Specifies [local adress boook](https://e-gov.github.io/DHX/#74-lokaalne-aadressiraamat) renewal frequency. [Crontab](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html) format: `<second> <minute> <hour> <day> <month> <weekday>`. Value `*/20` means on every 20-th unit. Therefore `0 */20 * * * ?` means after every 20 minutes. Every day at 7:00 a clock is `0 0 7 * * *`

