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

[Java Architecture for XML Binding - JAXB](https://docs.oracle.com/javase/7/docs/api/javax/xml/bind/package-summary.html) API is used for XML marshalling teeki (is part of Java SE 7).

DHX adapter Java package is based on Spring Framework architecture, by using extensively ist modules:
- For configuraton and initializing (Spring AOP, Spring Context, etc)
- For making HTTP SOAP client request (Spring WS Client, Apache HttpClient)
- For providing HTTP SOAP web service (Spring WS Server Endpoint, Java Servlet API)

Direct and indirect external API-s used by DHX adapter are following

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

The following Example is given on how to involve the DHX adapter package in existing (or new) software project, by using [Apache Maven](https://maven.apache.org/).

The above external dependencies downloaded automatically by Maven.

The following dependencies should be appended into Maven pom.xml:
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

In case **axiom-dom** or **axis2-saaj** are used in Java classpath, the XML marshalling/unmarshalling will not work properly - attachments remain empty. It is known JAXB issue.

It is recommended to remove these libraries from within Java classpath. 

With Maven, if third library (for axample axis2-codegen) depends on these, it could be done like this:

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

The simpliest way is to use DHX adapter inside Web (Servlet) Container (Tomcat, Jetty, etc), by using SpringFramework classes [ContextLoaderListener](http://docs.spring.io/spring/docs/4.2.7.RELEASE/spring-framework-reference/html/beans.html#beans-java-instantiating-container-web) and [MessageDispatcherServlet](http://docs.spring.io/spring-ws/site/reference/html/server.html#message-dispatcher-servlet).

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












