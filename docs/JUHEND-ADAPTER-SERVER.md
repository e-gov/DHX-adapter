![](EL_Regionaalarengu_Fond_horisontaalne.jpg)

ET | [EN](GUIDE.md)

# DHX adapteri serveri kasutusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

## Sissejuhatus
DHX adapter server on  tarkvara, mis hõlbustab DHX dokumendivahetuse protokolli kasutusele võtmist.

![](dhx-adapter-server.png)

DHX adapter server pakub kahte erinevat [SOAP](https://www.w3.org/TR/2000/NOTE-SOAP-20000508/) veebiteenuste liidest:
* Väline DHX liides (pildil kollane). DHX liides on suunatud väljapoole (teiste asutustega suhtlemiseks). DHX liides implementeerib DHX operatsiooni sendDocument. Vahendaja korral ka operatsiooni representationList (ei ole lihtsuse mõttes pildil kuvatud).
* Sisemine liides (pildil roheline). See liides on suunatud sissepoole (asutuse lokaalvõrku). Seda liidest kasutab asutuse dokumendihaldussüsteem (DHS) dokumentide saatmiseks ja vastuvõtmiseks. See liides implementeerib operatsioonid sendDocuments, receiveDocuments, markDocumentsReceived, getSendStatus ja getSendingOptions (need on väga sarnaselt vanale [DVK liidesele](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md)).

DHX adapter server käitub puhver serverina, võttes mõlema liidese kaudu vastu dokumente, salvestades kõigepealt need enda lokaalses andmebaasis/failisüsteemis, selleks et need hiljem addressaadile edastada.

## Sisemine liides



##Välised sõltuvused ja baasplatvorm

Kompileerimiseks ja käivitamiseks on vajalik [Java SE](https://en.wikipedia.org/wiki/Java_Platform,_Standard_Edition) 1.7 (või uuem) versioon.

Lokaalse andmebaasi serverina võib kasutada [Spring-Data](http://projects.spring.io/spring-data/) ja [Spring-Data-JPA] (http://projects.spring.io/spring-data-jpa/) poolt toetatud SQL andmebaasi servereid.
**NB!** DHX adapter serveri töötamine on testitud [PostgreSQL](https://www.postgresql.org/) ja [Oracle 11g](http://www.oracle.com/technetwork/database/index.html) andmebaasi serveri versioonidega.   

Põhilised välised sõltuvused on toodud [DHX-adapteri Java teegi kasutusjuhendis](https://github.com/e-gov/DHX-adapter/blob/master/docs/JUHEND.md#v%C3%A4lised-s%C3%B5ltuvused-ja-baasplatvorm).

Lisaks neile on täiendavad sõltuvused peamiselt andmbaasiga suhtlemise moodulitest:

Grupp | Moodul | Versioon | Märkused
------------ | ------------- | ------------- | -------------
org.springframework.data | spring-data-commons | 1.12.5.RELEASE | Spring Data Commons
org.springframework.boot | spring-boot-starter-data-jpa | 1.4.2.RELEASE | Spring data JPA starter
org.springframework.data | spring-data-jpa | XXXXX | Spring Data JPA
org.hibernate | hibernate-core | XXXX | Hibernate ORM Core
org.hibernate | hibernate-entitymanager | XXXX | Hibernate ORM Entity manager 
org.springframework.boot  | spring-boot-starter-jdbc | XXXX | Spring starter JDBC
javax.transaction | javax.transaction-api | XXXXX | Java transaction API
org.postgresql | postgresql | 9.4.1212 | PostgreSQL (juhul kui kasutatakse Postgre andmebaasi)


##Paigaldamine

### Paigalduspakett (WAR) - Tomcat ja PostgreSQL 

Create a deployable war file
http://docs.spring.io/spring-boot/docs/current/reference/html/howto-traditional-deployment.html

### Paigalduspaketi ise ehitamine (mitte Tomcat või PostgreSQL) 

Kui soovitakse kasutada 

Vanemasse Java Servlet serveritesse paigaldamisel tuleb häälestus teha [Web.xml](http://docs.spring.io/spring-boot/docs/current/reference/html/howto-traditional-deployment.html#howto-create-a-deployable-war-file-for-older-containers) kaudu.

Selleks vaata täpsemalt [DHX-adapteri Java teegi kasutusjuhend](https://github.com/e-gov/DHX-adapter/blob/master/docs/JUHEND.md#teegi-laadimise-h%C3%A4%C3%A4lestamine-webxml-ja-applicationcontextxml).


##Teadaolevad probleemid (sõltuvuste konfliktid)

Vaata [DHX-adapteri Java teegi kasutusjuhend](https://github.com/e-gov/DHX-adapter/blob/master/docs/JUHEND.md#teadaolevad-probleemid-s%C3%B5ltuvuste-konfliktid).


##Häälestus fail (dhx-application.properties)

Põhilised häälestus failis esinevad parameetrid on toodud  [DHX-adapteri Java teegi kasutusjuhendis](https://github.com/e-gov/DHX-adapter/blob/master/docs/JUHEND.md#h%C3%A4%C3%A4lestus-fail-dhx-applicationproperties).

Lisaks neile tuleb täiendavalt lisada parameetrid

Parameeter | Vaikimisi väärtus | Näite väärtus | Kirjeldus
------------ | ------------- | ------------- | -------------
dhx.server.special-orgnisations |  | adit,kovtp,rt,eelnoud | DVK alamsüsteemide erandid, millele korral võib DVK teenusest kasutada ainult nime (ei ole vaja organistatsiooni koodi)
dhx.server.delete-old-documents |  | delete-all | "delete-all" määrab et nii dokumendi metaandmed kui ka sisu (fail) kustutatakse perioodilise puhastus protsessi poolt. "delete-content" määrab et ainult sisu (fail) kustutatakse. Muu väärtus jätab kõik alles.
dhx.server.delete-old-documents-freq | | */20 * * * * ? | Vanade dokumentide kustutamise taustatöö käivitamise periood. Kustutatakse ainult dokumendid, mis on vanemad kui alljärgnevate parameetritega määratud päevade arv (30 päeva). [Crontab formaat](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html) kujul: `<second> <minute> <hour> <day> <month> <weekday>`. Väärtus `*/20` tähendab igal 20-nendal ühikul. Seega `*/20 * * * * ?` tähendab iga 20 sekundi järel.
dhx.server.received-document-lifetime | | 30 | Määrab päevade arvu, kui kauaks jäetakse andmebaasi alles, õnnelikult vastu võetud ja edastatud dokument. Kustutamine sõltub ka parameetri "dhx.server.delete-old-documents" väärtusest.
dhx.server.failed-document-lifetime | | 30 | Määrab päevade arvu, kui kauaks jäetakse andmebaasi alles, probleemselt (veaga) edastatud dokument. Kustutamine sõltub ka parameetri "dhx.server.delete-old-documents" väärtusest. 
dhx.resend.timeout| | 1500 | Ajaperiood (minutites, 1500 min=25 tundi), pärast mida proovitakse uuesti saatmisel staatusesse jäänud dokumente saata. Peaks olema suurem kui "document-resend-template" parameetris määratud aegade summa. Kasutatakse reaaalselt satmisel ainult erijuhul kui server kukkus maha või serveri töö peatati sunnitult.    
spring.jpa.hibernate.ddl-auto | | update|
spring.datasource.url | | jdbc:postgresql://localhost:5432/dhx-adapter| Postgres andmbaasi hosti nimi8 (localhost), port (5432) ja andmbaasi nimi (dhx-adapter)
spring.datasource.username | | postgres | Postgres andmbaasi kasutajanimi
spring.datasource.password | | 1*2*3 | Posgres andmebaasi kasutaja parool 
spring.datasource.driver-class-name | | org.postgresql.Driver| Määrab et kasutame Postgres andmbaasi
spring.jpa.properties.hibernate.dialect | | org.hibernate.dialect.PostgreSQL94Dialect| 

