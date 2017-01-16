![](EL_Regionaalarengu_Fond_horisontaalne.jpg)


# DHX adapteri serveri kasutusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

## Sissejuhatus
DHX adapter server on  tarkvara, mis hõlbustab [DHX](https://e-gov.github.io/DHX/) dokumendivahetuse protokolli kasutusele võtmist.

Tarkvara paigaldamine on kirjeldatud eraldi dokumendis [DHX adapteri serveri paigaldusjuhend](https://github.com/e-gov/DHX-adapter/blob/master/docs/PAIGALDUS-ADAPTER-SERVER.md).

![](dhx-adapter-server.png)

DHX adapter server pakub kahte erinevat [SOAP](https://www.w3.org/TR/2000/NOTE-SOAP-20000508/) veebiteenuste liidest:
* Väline DHX liides (pildil kollane). DHX liides on suunatud väljapoole (teiste asutustega suhtlemiseks). DHX liides implementeerib DHX operatsiooni [sendDocument](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md). Vahendaja korral ka operatsiooni [representationList](https://github.com/e-gov/DHX/blob/master/files/representationList.md) (ei ole lihtsustamise eesmärgil pildil näidatud).
* Sisemine liides (pildil roheline). See liides on suunatud sissepoole (asutuse lokaalvõrku). Seda liidest kasutab asutuse dokumendihaldussüsteem (DHS) dokumentide saatmiseks ja vastuvõtmiseks. See liides implementeerib operatsioonid sendDocuments, receiveDocuments, markDocumentsReceived, getSendStatus ja getSendingOptions.

DHX adapter server käitub puhver serverina, võttes mõlema liidese kaudu vastu dokumente, salvestades kõigepealt need enda lokaalses andmebaasis/failisüsteemis, selleks et need hiljem addressaadile edastada.

Välise DHX liidese toimimise loogikast arusaamine ei ole dhx-adapter-serveri kasutajale hädavajalik.

## WSDL asukohad

Välise DHX liidese [WSDL](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-ws/src/main/resources/dhx.wsdl) asub dhx-adapter-serveris aadressil `http://<HOST>:<PORT>/dhx-adapter-server/ws/dhx.wsdl`.
Välise DHX liidese SOAP päringud tuleb teha vastu aadressi `http://<HOST>:<PORT>/dhx-adapter-server/ws`.

Sisemisel liidese [WSDL]() asub dhx-adapater-serveris aadressil `http://<HOST>:<PORT>/dhx-adapter-server/wsServer/dhxServer.wsdl`. 
Sisemise liidese SOAP päringud tuleb teha vastu aadressi `http://<HOST>:<PORT>/dhx-adapter-server/wsServer`.
  
## Sisemine liides

Sisemist liidest kasutab asutuse DHS tarkvara dokumentide saatmiseks ja vastuvõtmiseks.

Sisemise liidese kasutamisel käitub DHS tarkvara SOAP kliendina (DHS tarkvara ei pea ise ühtegi teenust pakkuma).

Märkused vana DVK X-tee liidese kasutajale: 
> Sisemist liidese operatsioonid on projekteeritud väga sarnaselt vanale [DVK liidesele](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md). 
> Sisemise liidese SOAP teenuste XML nimeruumid ja implementeeritud operatsioonide struktuur on täpselt samad nagu vanas DVK liideses.
> 
> Enamasti peaks saama vanalt DVK X-tee liideselt üle minna uuele DHX protokollile, hakates kasutama uut dhx-adapter-server tarkvara, muutes DHS sees ümber DVK veebiteenuse võrguaadressi (endpoint URI aadressi).
> Kui varem pakkus seda teenust X-tee turvaserver, siis selle asemel pakub seda dhx-adapter-serveri sisemine liides.
> 
> Sisemises liideses on implementeeritud ainult hädavajalikud DVK liidese operatsioonide versioonid.
>
> Lisaks tuleb silmas pidada, et esineb mõningaid väiksemaid sisulisi loogika erinevusi võrreldes DVK liidesega. Need on välja toodud [allpool](JUHEND-ADAPTER-SERVER.md#erinevused-vana-dvk-liidese-ja-adapteri-sisemise-liidese-toimimise-loogikas). 

Järgnevalt kirjeldatakse lühidalt kuidas toimub dhx-adpater-serveri sisemise liidese kasutamine dokumentide saatmiseks ja vastuvõtmiseks. 

### sendDocuments (sisemine liides)

SOAP operatsiooni `sendDocuments.v4` kasutatakse dokumentide saatmiseks teisel asutusele.
Dokumendid peavad olema Kapsli [2.1](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-core/src/main/resources/Dvk_kapsel_vers_2_1_eng_est.xsd) versioonis (vanemad Kapsli versioonid ei ole toetatud).

dhx-adapter-server võtab dokumendi vastu, salvestab enda andmebaasi ja vastab SOAP päringule koheselt. 
Dokumendi edasine DHX addresaadile saatmine teostatakse asünkroonselt (taustatöö poolt).
Dokumendi saatmise staatuse küsimiseks tuleb kasutada operatsiooni `getSendStatus`

Märkused vana DVK X-tee liidese kasutajale:
> Võrreldes DVK sendDocuments liidestega on dhx-adpater-serveris realiseeritud on ainult sendDocuments operatsioonide [v4](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv4) versioon, mis eeldab et dokumendi Kapsel on 2.1 formaadis.
> Vanemaid DVK sendDocuments versioone [v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv1), [v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv2), [v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv3) dhx-adpater-serveri ei paku.

### getSendStatus (sisemine liides)

SOAP operatsiooni `getSendStatus.v2` kasutatakse saadetud dokumendi staatuse ja saatmisel ilmnenud vea info (fault) küsimiseks.
Võimalikud staatused on:
* `saatmisel` – dokumenti üritatakse veel antud saajale edastada
* `saadetud` – dokument sai edukalt antud saajale saadetud
* `katkestatud` – dokumenti ei õnnestunud antud saajale saata.

Staatuste kohta vaata täpselt [DVK dokumentatsioonist](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#edastatud-dokumentide-staatuse-kontroll).

Märkused vana DVK X-tee liidese kasutajale:
> Võrreldes DVK getSendStatus liidestega on dhx-adpater-serveris realiseeritud on ainult getSendStatus operatsioonide [v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendstatusv2) versioon.
> Vanemat DVK sendDocuments versiooni [v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendstatusv1) dhx-adpater-serveri ei paku.


## Erinevused vana DVK liidese ja Adapteri Sisemise liidese toimimise loogikas



##Välised sõltuvused ja baasplatvorm

Kompileerimiseks ja käivitamiseks on vajalik [Java SE](https://en.wikipedia.org/wiki/Java_Platform,_Standard_Edition) 1.7 (või uuem) versioon.

Lokaalse andmebaasi serverina võib kasutada [Spring-Data](http://projects.spring.io/spring-data/) ja [Spring-Data-JPA] (http://projects.spring.io/spring-data-jpa/) poolt toetatud SQL andmebaasi servereid.
**NB!** DHX adapter serveri töötamine on testitud [PostgreSQL](https://www.postgresql.org/) ja [Oracle 11g](http://www.oracle.com/technetwork/database/index.html) andmebaasi serveri versioonidega.   

Põhilised välised sõltuvused on toodud [DHX-adapteri Java teegi kasutusjuhendis](https://github.com/e-gov/DHX-adapter/blob/master/docs/JUHEND.md#v%C3%A4lised-s%C3%B5ltuvused-ja-baasplatvorm).

Lisaks neile on täiendavad sõltuvused peamiselt andmebaasiga suhtlemise moodulitest:

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


