![](EL_Regionaalarengu_Fond_horisontaalne.jpg)


# DHX adapterserveri haldusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

Sisukord
=================

  * [DHX adapterserveri haldusjuhend](#dhx-adapterserveri-haldusjuhend)
    * [1\. Sissejuhatus](#1-sissejuhatus)
    * [2\. Monitooring](#2-monitooring)
      * [2\.1\. DHX adapterserveri staatus (Health)](#21-dhx-adapterserveri-staatus-health)
      * [2\.2\. DHX adapterserveri mõõdikud (Metrics)](#22-dhx-adapterserveri-m%C3%B5%C3%B5dikud-metrics)
      * [2\.3\. Tomcat JMX liides](#23-tomcat-jmx-liides)
    * [3\. Logimine](#3-logimine)
    * [4\. Dokumentide edastamise vigade põhjuste analüüsimine](#4-dokumentide-edastamise-vigade-p%C3%B5hjuste-anal%C3%BC%C3%BCsimine)
      * [4\.1\. Andmebaasi mudel](#41-andmebaasi-mudel)
        * [4\.1\.1\. Asutuse nime muutmine (sisemise liidese getSendingOptions väljundis)](#411-asutuse-nime-muutmine-sisemise-liidese-getsendingoptions-väljundis)
        * [4\.1\.2\. Dokumendi määramine uuesti saatmisele](#412-dokumendi-määramine-uuesti-saatmisele)
      * [4\.2\. Kapslid lokaalses failisüsteemis](#42-kapslid-lokaalses-failis%C3%BCsteemis)

## 1. Sissejuhatus

DHX adapterserver on tarkvara, mis hõlbustab [DHX](https://e-gov.github.io/DHX/) dokumendivahetuse protokolli kasutuselevõtmist.

DHX adapterserveri toimimise loogika on kirjeldatud [DHX adapterserveri kasutusjuhendis](adapter-server-kasutusjuhend.md).

DHX adapterserveri paigaldamine on kirjeldatud [DHX adapterserveri paigaldusjuhendis](adapter-server-paigaldusjuhend.md).

DHX adapterserver on iseseisvalt toimiv komponent, mis üldjuhul ei vaja pidevat jälgmist. 

DHX adapterserver võtab dokumendi edastamiseks vastu, salvestades selle metaandmed andmebaasi ja binaarfaile sisaldava kapsli XML-i lokaalsesse failisüsteemi.
Pärast seda kui dokument on DHX kaudu edastatud või sisemise liidese kaudu alla laetud, võib selle DHX adapterserveri andmebaasist ja failisüsteemist kustutada. 
Kustutamist teostab perioodiline taustatöö.   

Haldustegevused võib jaotada kaheks:
* Serveri ressursside (mälu ja kettamaht) kasutuse jälgimine. 
* Dokumentide edastamise vigade põhjuste analüüsimine.    

## 2. Monitooring

### 2.1. DHX adapterserveri staatus (Health)
DHX adapterserver pakub andmebaasi ühenduste staatuse ja vaba kettaruumi jälgimiseks lihtsat liidest aadressil: 

http://localhost:8080/dhx-adapter-server/health

See tagastab JSON formaadis vastuse:
```json
{
 "status":"UP",
 "diskSpace":{
    "status":"UP",
    "total":486358904832,
    "free":57804505088,
    "threshold":10485760
 },
 "db":{
   "status":"UP",
   "database":"Oracle",
   "hello":"Hello"
  }
}
```

Vaata [Springframework juhendist](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html#_auto_configured_healthindicators).

### 2.2. DHX adapterserveri mõõdikud (Metrics)

Pisut parema ülevaate serveri tööst annab mõõdikute päring, mis tagastab kumulatiivsed serveri kasutuse näitajad (pärast viimast restarti).  

http://localhost:8080/dhx-adapter-server/metrics

Vastuse näide:
```json
{
	"mem":2121497,
	"mem.free":1638207,
	"processors":8,
	"instance.uptime":32055612,
	"uptime":32080833,
	"systemload.average":-1.0,
	"heap.committed":2000384,
	"heap.init":2072576,
	"heap.used":362176,
	"heap":2000384,
	"nonheap.committed":122904,
	"nonheap.init":2496,
	"nonheap.used":121113,
	"nonheap":0,
	"threads.peak":26,
	"threads.daemon":23,
	"threads.totalStarted":30,
	"threads":25,
	"classes":13739,
	"classes.loaded":13739,
	"classes.unloaded":0,
	"gc.ps_scavenge.count":7,
	"gc.ps_scavenge.time":265,
	"gc.ps_marksweep.count":3,
	"gc.ps_marksweep.time":324,
	"gauge.response.wsServer":766.0,
	"gauge.response.health":671.0,
	"gauge.response.wsServer.dhl.wsdl":131.0,
	"gauge.response.ws":24860.0,
	"gauge.response.unmapped":7.0,
	"counter.status.500.unmapped":12,
	"counter.status.200.wsServer":10,
	"counter.status.200.health":2,
	"counter.status.400.unmapped":24,
	"counter.status.200.ws":12,
	"counter.status.200.wsServer.dhl.wsdl":2
}
```
Vaata [Springframework juhendist](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-metrics.html).

### 2.3. Tomcat JMX liides

Täpsemaks Java serveri jälgimiseks saab kasutada [Java Management Extensions](https://en.wikipedia.org/wiki/Java_Management_Extensions) (JMX) liidest. 

Selleks tuleb Tomcat serveris avada eraldi port, mille kaudu saab ühenduda mingi JMX jälgijaga.

Vaata täpsemalt juhendist [Tomcat monitoring](https://tomcat.apache.org/tomcat-8.0-doc/monitoring.html).

## 3. Logimine

Serveri teenuste kasutamine logitakse [Tomcat standardsete liidestega](https://tomcat.apache.org/tomcat-7.0-doc/logging.html). 

Logi kirjutatakse `apache-tomcat-7.x.x/logs` kataloogi failidesse:
* `localhost_access_log.YYYY-MM-DD.txt` - iga pöördumise kohta "Access log", sisaldab välju: kliendi ip, juupäev ja kellaaeg, pöördumise URL vastuse staatus (200 tähendab OK, 400 või 500 tähendab viga).  
* Tomcat serveri konsoolile (standard output ja error)


Lisaks on DHX adapterserveri häälestusfailis `apache-tomcat-7.x.x/webapps/dhx-adapter-server/WEB-INF/classes/log4j2.xml` määratud et logi kirjutatakse faili: 
```xml
    <RollingFile name="RollingFile" fileName="c://logs/dhx-adapter-server.log"
```

Seal võib ka muuta logimise taseme väiksemaks (`info` või `error`). Vaata [Log4j juhend](https://logging.apache.org/log4j/2.x/manual/customloglevels.html)
```
<Logger name="ee.ria" level="debug" additivity="false">
```

## 4. Dokumentide edastamise vigade põhjuste analüüsimine

Kui dokumendi edastamisel või vastuvõtmisel esines viga, siis selle kohta kirjutatakse logi (`log4j2.xml` sees määratud) faili `c:/logs/dhx-adapter-server.log`. 

Kui dokument võeti edastamiseks vastu (SOAP päring ja kapsel olid korrektsed), siis salvestatakse metaandmed andmebaasi ja kapsel XML failisüsteemi.

Teatud juhtudel võivad dokumendid jääda edastamata, näiteks kui korduvedastuste maksimum on ületatud vms.
Selliste dokumentide mitteedastamise vea põhjuseid saab uurida logifailist ja andmebaasist.  

### 4.1. Andmebaasi mudel

Kõige lihtsam on alustada uurimist andmebaasist. Andmebaasi mudel on järgmine:

![](dhx-adapter-database.png)

Tabelite kirjeldused:
* ASUTUS - Kõikide DHX-iga liitunud adressaatide ehk asutuste andmed. See tabel täidetakse automaatselt [DHX lokaalse aadressiraamatu](https://e-gov.github.io/DHX/#74-lokaalne-aadressiraamat) koostamise algoritmiga.
* DOKUMENT - sisaldab dokumendi andmeid. Väljal SISU salvestatakse faili nimi (`c:\dhs_docs\` kataloogis).
* TRANSPORT - tabelis salvestatakse dokumendi transportimise info (kasutatakse saatja ja vastuvõtja(te)ga seostamiseks). Siin on peamine väli STAATUS_ID, mille võimalikud väärtused on: 101 (saatmisel), 102 (saadetud),  103 (katkestatud ehk ebaõnnestunud). Kui dokumendil oli mitu adressaati, siis TRANSPORT.STAATUS_ID sisaldab ühist staatust.
* SAATJA - dokumendi saatja andmed. Saatjaid on dokumendil üks.
* VASTUVOTJA - dokumendi adressaatide ehk vastuvõtja(te) andmed. Adressaate võib dokumendil olla mitu. Siin STAATUS_ID sisaldab ühele konkreetsele adressaadile saatmise (viimase saatmisürituse) staatust: 101 (saatmisel), 102 (saadetud), 103 (katkestatud ehk ebaõnnestunud). 
* STATUSE_AJALUGU - sisaldab ühe adressaadi saatmisürituste ajalugu.
* KAUST - sisaldab kaustade andmeid.

Saatmisel dokumentide leidmiseks võib kasutada SQL lauset:
```sql
SELECT d.*, t.*, v.* FROM DOKUMENT d, TRANSPORT t, VASTUVOTJA v
  WHERE t.dokument_id = d.dokument_id AND v.transport_id = t.transport_id
    AND (t.staatus_id = 101 OR v.staatus_id = 101)
```

Katkestatud (vea saanud) dokumentide leidmiseks võib kasutada SQL lauset:
```sql
SELECT d.*, t.*, v.* FROM DOKUMENT d, TRANSPORT t, VASTUVOTJA v
  WHERE t.dokument_id = d.dokument_id AND v.transport_id = t.transport_id
    AND (t.staatus_id = 103 OR v.staatus_id = 103)
```

### 4.1.1. Asutuse nime muutmine (sisemise liidese getSendingOptions väljundis)

DHS süsteem kasutab üldjuhul `getSendingOptions` väljundina tagastatud asutuste nimekirja ka enda sees. See nimekiri kuvatakse näiteks dokumendi saatmise ekraanil ripploendina, millest lõppkasutaja saab valida adressaadi.

Asutuse puhul, kellel on kasutusel mitu [DHX alamsüsteemi](https://e-gov.github.io/DHX/#55-reserveeritud-nimi-dhx), näiteks alamsüsteem `DHX.viru`, väljastatakse nimi kujul: `Asutuse nimi (DHX.viru)`. See nimi ei pruugi olla lõppkasutajale arusaadav.
 
DHS lõppkasutajale arusaadavama alamsüsteemi nime võib määrata, määrates DHX adapterserveri andmebaasis välja `ASUTUS.reaalne_nimi` väärtuseks õige nime, näiteks `Viru Ringkonnaprokuratuur`. 

### 4.1.2. Dokumendi määramine uuesti saatmisele

Juhul, kui dokumendi saatmine adressaadile lõplikult ebaõnnestub, siis märgitakse andmebaasis `VASTUVOTJA.staatus_id` väärtuseks `103` (katkestatud ehk ebaõnnestunud).

Kui uurimise käigus selgub näiteks, et välise adressaadi DHX süsteem oli pikalt maas (kogu saatmisürituste vältel), aga nüüd on see taas üleval, siis võib anda DHX adapterserverile märku, et võib uuesti algatada selle dokumendi välja saatmise.

Selleks tuleb andmebaasis väärtustada `VASTUVOTJA.dhx_internal_consignment_id=NULL` ja `VASTUVOTJA.staatus_id=101` (saatmisel).

Teades `DOKUMENT.ID` väärtust võib seda teha SQL lausega:
```sql
UPDATE vastuvotja v SET
	v.dhx_internal_consignment_id = NULL,
	v.staatus_id = 101
WHERE v.staatus_id = 103
  AND v.transport_id IN (
  	SELECT t.transport_id FROM transport t, dokument d
  	WHERE t.dokument_id = d.dokument_id
  	  AND d.dokument_id = <VALUE> 
  ) 
```

### 4.2. Kapslid lokaalses failisüsteemis

Edastamiseks salvestatakse kapsli XML lokaalses failisüsteemis (kataloogis `c:/dhx_docs`).

Faili nime formaat on `dhx_<YYYY>_<MM>_<DD>_<HHMMSS><GUID>` (näiteks `dhx_2017_01_20_1101256c7e2a4e-f467-4c32-8fa6-52bc140fe17e`). 
Selles formaadis failid sorteeritakse nime järgi kuupäevalises järjekorras.

Nende failide sisu on XML formaadis (UTF-8 kodeeringus).

Vea uurimisel saab dokumendi faili seostada andmebaasis DOKUMENT.SISU välja järgi.
```sql
SELECT d.* FROM DOKUMENT d where d.sisu = 'dhx_2017_01_20_1101256c7e2a4e-f467-4c32-8fa6-52bc140fe17e'
```


