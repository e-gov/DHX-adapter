![](EL_Regionaalarengu_Fond_horisontaalne.jpg)


# DHX adapterserveri paigaldusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

## Sissejuhatus

DHX adapterserver on tarkvara, mis hõlbustab [DHX](https://e-gov.github.io/DHX/) dokumendivahetuse protokolli kasutusele võtmist.

DHX adapterserveri toimimise loogika on kirjeldatud [DHX adapterserveri kasutusjuhendis](https://github.com/e-gov/DHX-adapter/blob/master/docs/adapter-server-kasutusjuhend.md). 

##Tarkvara nõuded (baastarkvara eeldused)

* **Java SE 8** või **Java SE 7**. Käivitamiseks on vajalik [Java SE 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (või uuem) versioon.
* **Apache Tomcat 7**. Tarkvara käivitamiseks on vajalik [Apache Tomcat 7](http://tomcat.apache.org/download-70.cgi) või uuem versioon.
* **PostgreSQL 9.6** või **Oracle 11g**. Andmebaasi serverina on soovituslik kasutada [PostgreSQL 9.6](https://www.postgresql.org/) või [Oracle 11g](http://www.oracle.com/technetwork/database/index.html) (kaasa arvatud 11g Express Edition) versioone.
* Operatsioonisüsteem - Java poolt [toetatud süsteem](https://www.java.com/en/download/help/sysreq.xml).

Märkus (muud andmebaasid):
> Tõenäoliselt toimib tarkvara ka muude [Hibernate ORM](http://hibernate.org/orm/documentation/5.0/) poolt toetatud SQL andmebaasi [serveritel](https://docs.jboss.org/hibernate/orm/5.0/manual/en-US/html/ch03.html#configuration-optional-dialects), nagu [MySQL](https://www.mysql.com/) ja [Microsoft SQL Server](https://www.microsoft.com/en-us/sql-server/).
>
> **NB!** DHX adapterserveri töötamine on testitud ainult [PostgreSQL 9.6](https://www.postgresql.org/) ja [Oracle 11g](http://www.oracle.com/technetwork/database/index.html) andmebaasi serveri versioonidega.
>
> Seega muude andmebaasi serverite kasutamine toimub omal riisikol ja ei pruugi töötada.
>
> Paigalduspaketina alla laetav WAR fail sisaldab ainult PostgreSQL ja Oracle andmebaasiga suhtlemise [JDBC](https://en.wikipedia.org/wiki/Java_Database_Connectivity) draivereid. Kui soovitakse kasutatakse muud andmebaasi, siis tuleb dhx-adapter-serveri WAR fail ise uuesti ehitada, muutes `pom.xml` failis sõltuvusi. 

Märkus (muud Java web serverid):
> Apache Tomcat tarkvara võib asendada mõne muu [Java Web konteiner](https://en.wikipedia.org/wiki/Web_container) tarkvaraga. 
> Näiteks [GlassFish](https://glassfish.java.net/),  [WildFly (JBoss)](http://wildfly.org/) või [Jetty](http://www.eclipse.org/jetty/).
>
> Paigalduspaketina alla laetav WAR fail sisaldab ainult Apache Tomcat serverisse paigaldamise `spring-boot-starter` [mooduleid](https://spring.io/blog/2014/03/07/deploying-spring-boot-applications). Kui soovitakse kasutatakse muud Java serverit, siis tuleb dhx-adapter-serveri WAR fail ise uuesti ehitada, muutes `pom.xml` failis sõltuvusi.

##Riistvara nõuded (eeldused)
 
Minimaalsed nõuded riistvarale on järgmised:
* Muutmälu 2GB
* Kõvaketas 70 Gb
* Protsessor 2 GHz x 2 tuuma

Optimaalsed riistvara nõuded sõltuvad asutuse poolt saadetavate dokumentide arvust ja suurusest. 
Samuti sellest kas andmebaasi server paigaldatakse samasse masinasse või eraldi. Soovitav on minimaalsed nõuded kahega korrutada. 

##Paigaldamine

### Olemasoleva paigalduspaketiga (WAR) - Tomcat ja PostgeSQL

#### PostgreSQL 9.6

Laadida alla ja installeerida PostgreSQL andmebaasi versioon [9.6.x](https://www.postgresql.org/download/).

#### Java 8 SE

Laadida alla ja installeerida [Java 8 SE Runtime environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).


#### Apache Tomcat 7

1) Laadida alla ja installeerida (pakkida lahti) [Apache Tomcat 7](https://tomcat.apache.org/download-70.cgi)

2) Vajadusel muuta operatsioonisüsteemi keskkonna muutuja "JRE_HOME" home väärtuseks installeeritud Java 8 SE JRE kataloogitee. 
Näiteks Windows keskkonnas JRE_HOME=`C:\Program Files\jre1.8.0_112`. 

Luua setenv.bat või setenv.sh
https://tomcat.apache.org/tomcat-8.0-doc/monitoring.html

3) Vajadusel muuta ümber Tomcat pordi number (vaikimisi 8080) failis `apache-tomcat-7.x.x/conf/server.xml`.
```xml
    <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />
```

4) Käivitada Tomcat skriptiga `apache-tomcat-7.x.x/bin/startup.bat` (windows) või `apache-tomcat-7.x.x/bin/startup.sh` (Linux jt).

#### DHX adapterserver WAR

1) Laadida alla DHX adapterserveri WAR fail `dhx-adapter-server.war`.

2) Kopeerida see Tomcat `apache-tomcat-7.x.x/webapps` alamkataloogi
(Näiteks Windows keskkonnas `C:\Program Files\apache-tomcat-7.0.73\webapps\` alamkataloogi).

3) Tomcat püüab seejärel automaatselt WAR faili avada ja paigaldada (deploy).

4) Tekib uus alamkataloog `webapps/dhx-adapter-server`.
Aga kuna WAR fail sisaldab valesid andmebaasi ühenduse parameetreid, siis Tomcat konsoolile/logisse kuvatakse viga.

#### Muuta dhx-application.properties

1) Avada fail `webapps/dhx-adapter-server/WEB-INF/classes/dhx-application.properties` ja muuta seal õigeks andmebaasi ühenduse, X-tee turvaserveri ja asutuse registrikoodi parameetrid

```properites
soap.security-server=http://10.0.13.198
soap.xroad-instance=ee-dev
soap.member-class=GOV
soap.member-code=40000001

documents.folder=C:\\dhx_docs\\

spring.datasource.url=jdbc:postgresql://localhost:5432/dhx_adapter_dev
spring.datasource.username=postgres
spring.datasource.password=123456
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQL94Dialect
```
Teha muudetud `dhx-application.properties` failist backup koopia kuhugi mujale kataloogi.

2) Teha Tomcati restart.

Stop `apache-tomcat-7.x.x/bin/shutdown.bat` (windows) või `apache-tomcat-7.x.x/bin/shutdown.sh` (Linux jt).

Start `apache-tomcat-7.x.x/bin/startup.bat` (windows) või `apache-tomcat-7.x.x/bin/startup.sh` (Linux jt).

Märkus:
> Andmebaasi ühenduse esmakordsel avamisel kontrollitakse kas vajalikud andmbaasi tabelid on juba olemas. 
> Kui tabeleid ei ole, siis luuakse need automaatselt (eraldi SQL skriptiga ei ole vaja andmebaasi tabeleid luua)
>
> Selle määrab `dhx-application.properties` failis parameeter `spring.jpa.hibernate.ddl-auto=update` .
> Vaata [Spring boot juhendist](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-database-initialization.html#howto-initialize-a-database-using-hibernate).

3) Vaadata kas Tomcat konsoolis või logis esineb veel vigu (ei tohiks esineda)

4) Paigaldada Tomcat Windows Servicena või Linux deemonina. Selleks saab kasutada skripti `apache-tomcat-7.x.x/bin/service.bat` (Windows) või `apache-tomcat-7.x.x/bin/daemon.sh` (Linux jt)

### Olemasoleva paigalduspaketiga (WAR) - Tomcat ja Oracle 11g Express edition

#### Oracle 11g Express Edition

1) Laadida alla ja installeerida [Oracle 11g XE](http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html).

2) Logida oracle andmebaasi SYS kasutaja ja luua uus kasutaja (schema):
```sql
create user dhxadapter
  identified by dhxadapter123
  DEFAULT TABLESPACE USERS;

grant connect to dhxadapter;
grant resource to dhxadapter;
grant unlimited tablespace to dhxadapter;
```

#### Java 8 SE

Vaata [eespoolt](#java-8-se)


#### Apache Tomcat 7

Vaata [eespoolt](#apache-tomcat-7) 

#### DHX adapterserver WAR

Vaata [eespoolt](#dhx-adapterserver-war) 

#### Muuta dhx-application.properties

1) Avada fail `webapps/dhx-adapter-server/WEB-INF/classes/dhx-application.properties` ja muuta seal õigeks andmebaasi ühenduse, X-tee turvaserveri ja asutuse registrikoodi parameetrid

```properites
soap.security-server=http://10.0.13.198
soap.xroad-instance=ee-dev
soap.member-class=GOV
soap.member-code=40000001

documents.folder=C:\\dhx_docs\\

spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.jpa.database-platform=org.hibernate.dialect.Oracle10gDialect
spring.datasource.username=dhxadapter
spring.datasource.password=dhxadapter123
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.type=oracle.jdbc.pool.OracleDataSource
```
Teha muudetud `dhx-application.properties` failist backup koopia kuhugi mujale kataloogi.

2) Teha Tomcati restart.

Stop `apache-tomcat-7.x.x/bin/shutdown.bat` (windows) või `apache-tomcat-7.x.x/bin/shutdown.sh` (Linux jt).

Start `apache-tomcat-7.x.x/bin/startup.bat` (windows) või `apache-tomcat-7.x.x/bin/startup.sh` (Linux jt).

Märkus:
> Andmebaasi ühenduse esmakordsel avamisel kontrollitakse kas vajalikud andmbaasi tabelid on juba olemas. 
> Kui tabeleid ei ole, siis luuakse need automaatselt (eraldi SQL skriptiga ei ole vaja andmebaasi tabeleid luua)
>
> Selle määrab `dhx-application.properties` failis parameeter `spring.jpa.hibernate.ddl-auto=update` .
> Vaata [Spring boot juhendist](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-database-initialization.html#howto-initialize-a-database-using-hibernate).

3) Vaadata kas Tomcat konsoolis või logis esineb veel vigu (ei tohiks esineda)

4) Paigaldada Tomcat Windows Servicena või Linux deemonina. Selleks saab kasutada skripti `apache-tomcat-7.x.x/bin/service.bat` (Windows) või `apache-tomcat-7.x.x/bin/daemon.sh` (Linux jt)

### Paigalduspaketi ise ehitamine (mitte Tomcat või mitte PostgreSQL/Oracle) 

Kui soovitakse tarkvara paigalda mingisse muuse Java Web serverisse (mitte Tomcat), siis tuleb WAR fail ise uuesti ehitada, muutes eelnevalt `/DHX-adapter/dhx-adapter-server/pom.xml` failis sõltuvusi.


Create a deployable war file
http://docs.spring.io/spring-boot/docs/current/reference/html/howto-traditional-deployment.html
 

Vanemasse Java Servlet serveritesse paigaldamisel tuleb häälestus teha [Web.xml](http://docs.spring.io/spring-boot/docs/current/reference/html/howto-traditional-deployment.html#howto-create-a-deployable-war-file-for-older-containers) kaudu.

Selleks vaata täpsemalt [DHX Java teegi kasutusjuhend](java-teegid-kasutusjuhend.md#teegi-laadimise-h%C3%A4%C3%A4lestamine-webxml-ja-applicationcontextxml).


##Teadaolevad probleemid (sõltuvuste konfliktid)

Kui dhx-adpater-server soovitakse paigalda samasse Java/Tomcat serverisse, kus töötab mõni muu Java serveri tarkvara moodul (WAR), siis peab arvestama et võivad esineda sõltuvuste konfliktid.   

Vaata [DHX Java teegi kasutusjuhend](java-teegid-kasutusjuhend.md#teadaolevad-probleemid-s%C3%B5ltuvuste-konfliktid).


##Häälestus fail (dhx-application.properties)

Põhilised häälestus failis esinevad parameetrid on toodud  [DHX Java teegi kasutusjuhendis](java-teegid-kasutusjuhend.md#h%C3%A4%C3%A4lestus-fail-dhx-applicationproperties).

Lisaks neile tuleb täiendavalt lisada parameetrid

Parameeter | Vaikimisi väärtus | Näite väärtus | Kirjeldus
------------ | ------------- | ------------- | -------------
dhx.server.send-to-dhx | | */20 * * * * ? | Mitme sekundi järel käivitub Sisemise liidese kaudu vastu võetud dokumentide DHX-i edasi saatmise taustatöö. [Crontab formaat](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html) kujul: `<second> <minute> <hour> <day> <month> <weekday>`. Väärtus `*/20` tähendab igal 20-nendal ühikul. Seega `*/20 * * * * ?` tähendab iga 20 sekundi järel.
dhx.server.special-orgnisations |  | adit,kovtp,rt,eelnoud | DVK alamsüsteemide erandid, millele korral võib DVK teenusest kasutada ainult nime (ei ole vaja organistatsiooni koodi)
dhx.server.delete-old-documents |  | delete-all | "delete-all" määrab et nii dokumendi metaandmed kui ka sisu (fail) kustutatakse perioodilise puhastus protsessi poolt. "delete-content" määrab et ainult sisu (fail) kustutatakse. Muu väärtus jätab kõik alles.
dhx.server.delete-old-documents-freq | | */20 * * * * ? | Vanade dokumentide kustutamise taustatöö käivitamise periood. Kustutatakse ainult dokumendid, mis on vanemad kui alljärgnevate parameetritega määratud päevade arv (30 päeva). [Crontab formaat](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html) kujul: `<second> <minute> <hour> <day> <month> <weekday>`. Väärtus `*/20` tähendab igal 20-nendal ühikul. Seega `*/20 * * * * ?` tähendab iga 20 sekundi järel.
dhx.server.received-document-lifetime | | 30 | Määrab päevade arvu, kui kauaks jäetakse andmebaasi alles, õnnelikult vastu võetud ja edastatud dokument. Kustutamine sõltub ka parameetri "dhx.server.delete-old-documents" väärtusest.
dhx.server.failed-document-lifetime | | 30 | Määrab päevade arvu, kui kauaks jäetakse andmebaasi alles, probleemselt (veaga) edastatud dokument. Kustutamine sõltub ka parameetri "dhx.server.delete-old-documents" väärtusest. 
dhx.resend.timeout| | 1500 | Ajaperiood (minutites, 1500 min=25 tundi), pärast mida proovitakse uuesti saatmisel staatusesse jäänud dokumente saata. Peaks olema suurem kui "document-resend-template" parameetris määratud aegade summa. Kasutatakse reaaalselt satmisel ainult erijuhul kui server kukkus maha või serveri töö peatati sunnitult.
documents.folder | | `C:\\dhx_docs\\` | Kataloog kuhu salvestatakse vastu võetud (edastamist ootavate) dokumentide Kapslid. Linux korral kasutada formaati `/tmp/kataloog`. Selle kataloogi failisüsteemis peab olema piisavalt vaba ruumi (10-50Gb). Dokumendid kustutatakse teatud perioodi (30 päeva) järel (parameetrid `dhx.server.received-document-lifetime` ja `dhx.server.failed-document-lifetime`)
spring.jpa.hibernate.ddl-auto | | update| Määrab et esimesel serveri käivitamisel (kui andmebaasi ühenduse parameetrid on õigeks muudetud) luuakse andmebaasi tabelid automaatselt.

PostgreSQL korral tuleb muuta järgmiste parameetrite väärtused.

Parameeter | Vaikimisi väärtus | Näite väärtus | Kirjeldus
------------ | ------------- | ------------- | -------------
spring.datasource.url | | jdbc:postgresql://localhost:5432/dhx-adapter| Postgres andmebaasi hosti nimi8 (localhost), port (5432) ja andmebaasi nimi (dhx-adapter)
spring.datasource.username | | postgres | Postgres andmebaasi kasutajanimi
spring.datasource.password | | 1*2*3 | Posgres andmebaasi kasutaja parool 
spring.datasource.driver-class-name | | org.postgresql.Driver| Määrab et kasutame Postgres JDBC draiverit
spring.jpa.properties.hibernate.dialect | | org.hibernate.dialect.PostgreSQL94Dialect| Määrab et kasutame Posgres 9.4, 9.5 või 9.6 versiooni.

Oracle 11G kasutamise korral tuleb muuta järgmiste parameetrite väärtused.

Parameeter | Vaikimisi väärtus | Näite väärtus | Kirjeldus
------------ | ------------- | ------------- | -------------
spring.datasource.url | | jdbc:oracle:thin:@localhost:1521:xe | Oracle andmebaasi host, port ja SID.
spring.jpa.database-platform | | org.hibernate.dialect.Oracle10gDialect | Määrab et kasutame Oracle andmebaasi.
spring.datasource.username | | dhxadapter | Oracle andmebaasi kasutajanimi
spring.datasource.password | | dhxadapter123 | Oracle andmebaasi kasutaja parool 
spring.datasource.driver-class-name | | oracle.jdbc.OracleDriver | Määrab et kasutame Oracle JDBC draiverit.
spring.datasource.type | | oracle.jdbc.pool.OracleDataSource | Määrab et ksutame Oracle data source

Märkus:
> Enamikel juhtudel piisab kui muuta ainult järgmiste parameetrite väärtused
>
>  ```
>  soap.security-server=http://10.0.13.198
>  soap.xroad-instance=ee
>  soap.member-class=GOV
>  soap.member-code=40000001
>  documents.folder = C:\\dhx_docs\\
>  spring.datasource.url=jdbc:postgresql://localhost:5432/dhx-adapter
>  spring.datasource.username=postgres
>  spring.datasource.password=1*2*3
>  spring.datasource.driver-class-name=org.postgresql.Driver
>  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQL94Dialect
>  ```
>
> Ülejäänud parameetrid võib jätta samaks, nagu vaikimisi määratud. 


##Klastrisse paigaldamine (Failover/Load balancing)

DHX adapterserveri võib paigaldada klastrisse (ehk mitmele serverikobara sõlmele). 

Sellisel paigaldamisel tuleb arvestada et ühised (jagatud) ressursid on 
* Andmebaas, kus puhverdatakse metaandmeid
* Failisüsteemi kataloog, kus puhverdatakse Kapsli dokumendi faile

Näiteks üks võimalik paigalduse variant oleks järgmine

![](dhx-adapter-cluster1.png)

* X-read Security server - X-tee turvaserver mille kaudu suheldakse turvaliselt teiste asutustega
* Failover/Loadbalancer - Kas tarkvaraline (näiteks [Apache HTTPD](https://httpd.apache.org/docs/2.4/howto/reverse_proxy.html)) või riistvaraline (näiteks [F5 big-ip](https://f5.com/), [loadbalancer.org](http://loadbalancer.org/) või [Citrix NetScaler](https://www.citrix.com/products/netscaler-adc/)) failover ja/või load-balancer komponent.  
* dhx-adapter-server (Node A) - Klastri esimene server, mis pakub SOAP teenuseid.
* dhx-adapter-server (Node B) - Klastri teine server, mis pakub SOAP teenuseid.
* Shared Filesystem - jagatud (võrgu) failisüsteem, millesse ajutiselt salvestatakse edastatavate dokumentide (Kapsli) failid
* Database Server (shared database) - jagatud andmebaas, millesse ajutiselt salvestatakse edastatavate dokumentide metaandmed
* Document management System (DHS) - Asutuse dokumendihaldusssüsteem (näiteks Delta, Amphora vms).
 






