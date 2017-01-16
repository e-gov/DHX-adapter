![](EL_Regionaalarengu_Fond_horisontaalne.jpg)


# DHX adapteri serveri paigaldusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

## Sissejuhatus

DHX adapter server on  tarkvara, mis hõlbustab [DHX](https://e-gov.github.io/DHX/) dokumendivahetuse protokolli kasutusele võtmist.

dhx-adapter-serveri toimimise loogika on kirjeldatud [DHX adapteri serveri kasutusjuhendis](https://github.com/e-gov/DHX-adapter/blob/master/docs/JUHEND-ADAPTER-SERVER.md). 

##Tarkvara nõuded (baasplatvormi eeldused)

Kompileerimiseks ja käivitamiseks on vajalik [Java SE](https://en.wikipedia.org/wiki/Java_Platform,_Standard_Edition) 1.7 (või uuem) versioon.

Lokaalse andmebaasi serverina võib kasutada [Spring-Data](http://projects.spring.io/spring-data/) ja [Spring-Data-JPA] (http://projects.spring.io/spring-data-jpa/) poolt toetatud SQL andmebaasi servereid.

**NB!** DHX adapter serveri töötamine on testitud ainult [PostgreSQL 9.4](https://www.postgresql.org/) ja [Oracle 11g](http://www.oracle.com/technetwork/database/index.html) andmebaasi serveri versioonidega. 
Muude andmebaasi serverite kasutamine toimub omal riisikol (kuigi võiks töötada ka [MySQL](https://www.mysql.com/) ja [Microsoft SQL Server](https://www.microsoft.com/en-us/sql-server/) andmebaasidega).

##Riistvara nõuded (eeldused)

* Mälu 1GB 
* Ketas 100Gb ?
* Protsessor


##Paigaldamine


### Paigalduspakett (WAR) - Tomcat ja PostgreSQL 

Create a deployable war file
http://docs.spring.io/spring-boot/docs/current/reference/html/howto-traditional-deployment.html

### Paigalduspaketi ise ehitamine (mitte Tomcat või PostgreSQL) 

Kui soovitakse kasutada 

Vanemasse Java Servlet serveritesse paigaldamisel tuleb häälestus teha [Web.xml](http://docs.spring.io/spring-boot/docs/current/reference/html/howto-traditional-deployment.html#howto-create-a-deployable-war-file-for-older-containers) kaudu.

Selleks vaata täpsemalt [DHX-adapteri Java teegi kasutusjuhend](https://github.com/e-gov/DHX-adapter/blob/master/docs/JUHEND.md#teegi-laadimise-h%C3%A4%C3%A4lestamine-webxml-ja-applicationcontextxml).


##Teadaolevad probleemid (sõltuvuste konfliktid)

Kui dhx-adpater-server soovitakse paigalda samasse Java/Tomcat serverisse, kus töötab mõni muu Java serveri tarkvara moodul (WAR), siis peab arvestama et võivad esineda sõltuvuste konfliktid.   

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
spring.datasource.url | | jdbc:postgresql://localhost:5432/dhx-adapter| Postgres andmebaasi hosti nimi8 (localhost), port (5432) ja andmebaasi nimi (dhx-adapter)
spring.datasource.username | | postgres | Postgres andmebaasi kasutajanimi
spring.datasource.password | | 1*2*3 | Posgres andmebaasi kasutaja parool 
spring.datasource.driver-class-name | | org.postgresql.Driver| Määrab et kasutame Postgres andmebaasi
spring.jpa.properties.hibernate.dialect | | org.hibernate.dialect.PostgreSQL94Dialect| 

Üldjuhul piisab kui muuta ära ainult järgmised parameetrid:
*  

