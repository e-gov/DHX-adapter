![](EL_Regionaalarengu_Fond_horisontaalne.jpg)


# DHX adapterserveri kasutusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

## 1. Sissejuhatus
DHX adapterserver on tarkvara, mis hõlbustab [DHX](https://e-gov.github.io/DHX/) dokumendivahetuse protokolli kasutusele võtmist.

DHX adapterserveri kasutusjuhend on vajalik eelkõige tarkvara arendajale, kes teostab Dokumendihaldus süsteemi (DHS) ja DHX adapterserveri liidestamist. 

DHX adapterserveri paigaldamine on kirjeldatud [DHX adapterserveri paigaldusjuhendis](adapter-server-paigaldusjuhend.md).

DHX adapterserveri haldamine on kirjeldatud [DHX adapterserveri haldusjuhendis](adapter-server-haldusjuhend.md).

![](dhx-adapter-server.png)

DHX adapterserver pakub kahte erinevat [SOAP](https://www.w3.org/TR/2000/NOTE-SOAP-20000508/) veebiteenuste liidest:
* Väline DHX liides (pildil kollane). DHX liides on suunatud väljapoole (teiste asutustega suhtlemiseks). DHX liides implementeerib DHX operatsiooni [sendDocument](https://github.com/e-gov/DHX/blob/master/files/sendDocument.md). Vahendaja korral ka operatsiooni [representationList](https://github.com/e-gov/DHX/blob/master/files/representationList.md) (ei ole lihtsustamise eesmärgil pildil näidatud).
* Sisemine liides (pildil roheline). See liides on suunatud sissepoole (asutuse lokaalvõrku). Seda liidest kasutab asutuse dokumendihaldussüsteem (DHS) dokumentide saatmiseks ja vastuvõtmiseks. See liides implementeerib operatsioonid sendDocuments, receiveDocuments, markDocumentsReceived, getSendStatus ja getSendingOptions. Sisemise liidese sünonüümina kasutatakse muudes dokumentides mõistet "DVK protokolli" liides.

DHX adapterserver käitub puhver serverina, võttes mõlema liidese kaudu vastu dokumente, salvestades kõigepealt need enda lokaalses andmebaasis/failisüsteemis, selleks et need hiljem addressaadile edastada.

Välise DHX liidese toimimise loogikast arusaamine ei ole DHX adapterserveri kasutajale hädavajalik.

## 2. WSDL asukohad

Välise DHX liidese [WSDL](../dhx-adapter-ws/src/main/resources/dhx.wsdl) asub DHX adapterserveris aadressil `http://<HOST>:<PORT>/dhx-adapter-server/ws/dhx.wsdl`.
Välise DHX liidese SOAP päringud tuleb teha vastu aadressi `http://<HOST>:<PORT>/dhx-adapter-server/ws`.

Sisemisel liidese [WSDL](../dhx-adapter-server/src/main/resources/dhl.wsdl) asub DHX adapaterserveris aadressil `http://<HOST>:<PORT>/dhx-adapter-server/wsServer/dhl.wsdl`. 
Sisemise liidese SOAP päringud tuleb teha vastu aadressi `http://<HOST>:<PORT>/dhx-adapter-server/wsServer`.
  
### 3. SoapUI testimine

Sisemist liidest saab soovi korral testida [SoapUI](https://www.soapui.org/) programmiga.

**1)** Avada SoapUI ja lisada uus projekt, sisestades WSDL aadressiks `http://localhost:8080/dhx-adapter-server/wsServer/dhl.wsdl` (muuta vajadusel host ja port).

**2)** Genereeritud projekti all avada näiteks `dhlSoapBinding`->`getSendingOptions`->`Request 1`.

**3)** Üleval ripploendis kuvatakse teenuse aadress. Valida seal "Edit current" ja sisestada aadressiks `http://localhost:8080/dhx-adapter-server/wsServer` (muuta vajadusel host ja port).

**4)** Sisestada Request XML väljale SOAP päring, muutes endale sobivaks elementide `<xRoadInstance>`, `<memberClass>` ja `<memberCode>` (asutuse registrikood) väärtused (näiteks `ee-dev`, `GOV` ja `40000001`) ning käivitada päring.

Märkus:
> Vastuseks saadud manused on gzip pakitud ja seejärel BASE64 kodeeritud.
> 
> Need saab Linux/unix alla lahti kodeerida salvestades manuse faili "result.txt" ja käivitades seejärel:
> ``` 
>  cat result.txt | base64 -d | gunzip
> ```

SoapUI-ga testimise kohta loe eraldi dokumentatsioonist [SoapUI testide käivitamise juhend](adapter-server-soapui-test-juhend.md) ja [Testlood](adapter-server-testilood.md). 

## 4. Sisemine liides

Sisemist liidest kasutab asutuse DHS tarkvara dokumentide saatmiseks ja vastuvõtmiseks.

Sisemise liidese kasutamisel käitub DHS tarkvara SOAP kliendina (DHS tarkvara ei pea ise ühtegi veebiteenust pakkuma).

Märkused vana DVK X-tee liidese kasutajale: 
> Sisemist liidese operatsioonid on projekteeritud väga sarnaselt vanale [DVK liidesele](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md). 
> Sisemise liidese SOAP teenuste XML nimeruumid ja implementeeritud operatsioonide struktuur on täpselt samad nagu vanas DVK liideses.
> 
> Enamasti peaks saama vanalt DVK X-tee liideselt üle minna uuele DHX protokollile, hakates kasutama uut DHX adapterserver tarkvara, muutes DHS sees ümber DVK veebiteenuse võrguaadressi (endpoint URI aadressi).
> Kui varem pakkus seda teenust X-tee turvaserver, siis selle asemel pakub seda adapterserveri sisemine liides.
> 
> Sisemises liideses on implementeeritud ainult hädavajalikud DVK liidese operatsioonide versioonid.
>
> Lisaks tuleb silmas pidada, et esineb mõningaid sisulisi loogika erinevusi võrreldes DVK liidesega. Need on välja toodud [allpool](#5-erinevused-dvk-liidesega-võrreldes). 

Järgnevalt kirjeldatakse lühidalt kuidas toimub dhx-adpater-serveri sisemise liidese kasutamine dokumentide saatmiseks ja vastuvõtmiseks. 

### 4.1. getSendingOptions (sisemine liides)

Seda operatsiooni kasutatakse [DHX aadressiraamatu](https://e-gov.github.io/DHX/#74-lokaalne-aadressiraamat) küsimiseks.

See tagastab kõik asutused kellele võib üle DHX protokolli dokumente saata. 

Lisaks kirjeldust vana DVK spetsifikatsioonis [getSendingOptions.v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv1),
[getSendingOptions.v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv2) ja [getSendingOptions.v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv3)

Märkused vana DVK X-tee liidese kasutajale:
> DHX adpaterserveris on realiseeritud kõik getSendStatus operatsiooni versioonid [getSendingOptions.v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv1), [getSendingOptions.v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv2) ja [getSendingOptions.v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv3).


Päringu näide:
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
 xmlns:xro="http://x-road.eu/xsd/xroad.xsd" 
 xmlns:iden="http://x-road.eu/xsd/identifiers" 
 xmlns:dhl="http://producers.dhl.xrd.riik.ee/producer/dhl">
   <soapenv:Header>
      <xro:protocolVersion>4.0</xro:protocolVersion>
      <xro:id>64a3ddbd-1620-42c4-b2fe-60b854c2f32f</xro:id>
      <xro:service>
         <iden:xRoadInstance>ee-dev</iden:xRoadInstance>
         <iden:memberClass>COM</iden:memberClass>
         <iden:memberCode>10560025</iden:memberCode>
         <iden:subsystemCode>DHX</iden:subsystemCode>
         <iden:serviceCode>getSendingOptions</iden:serviceCode>
         <iden:serviceVersion>v2</iden:serviceVersion>
      </xro:service>
      <xro:client>
         <iden:xRoadInstance>ee-dev</iden:xRoadInstance>
         <iden:memberClass>COM</iden:memberClass>
         <iden:memberCode>10560025</iden:memberCode>
         <iden:subsystemCode>DHX</iden:subsystemCode>
      </xro:client>
   </soapenv:Header>
   <soapenv:Body>
      <dhl:getSendingOptions>
         <keha></keha>
      </dhl:getSendingOptions>
   </soapenv:Body>
</soapenv:Envelope>
```

Vastuse näide:
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header>
      <xro:protocolVersion xmlns:xro="http://x-road.eu/xsd/xroad.xsd">4.0</xro:protocolVersion>
      <xro:id xmlns:xro="http://x-road.eu/xsd/xroad.xsd">64a3ddbd-1620-42c4-b2fe-60b854c2f32f</xro:id>
      <xro:service xmlns:xro="http://x-road.eu/xsd/xroad.xsd">
         <iden:xRoadInstance xmlns:iden="http://x-road.eu/xsd/identifiers">ee-dev</iden:xRoadInstance>
         <iden:memberClass xmlns:iden="http://x-road.eu/xsd/identifiers">COM</iden:memberClass>
         <iden:memberCode xmlns:iden="http://x-road.eu/xsd/identifiers">10560025</iden:memberCode>
         <iden:subsystemCode xmlns:iden="http://x-road.eu/xsd/identifiers">DHX</iden:subsystemCode>
         <iden:serviceCode xmlns:iden="http://x-road.eu/xsd/identifiers">getSendingOptions</iden:serviceCode>
         <iden:serviceVersion xmlns:iden="http://x-road.eu/xsd/identifiers">v2</iden:serviceVersion>
      </xro:service>
      <xro:client xmlns:xro="http://x-road.eu/xsd/xroad.xsd">
         <iden:xRoadInstance xmlns:iden="http://x-road.eu/xsd/identifiers">ee-dev</iden:xRoadInstance>
         <iden:memberClass xmlns:iden="http://x-road.eu/xsd/identifiers">COM</iden:memberClass>
         <iden:memberCode xmlns:iden="http://x-road.eu/xsd/identifiers">10560025</iden:memberCode>
         <iden:subsystemCode xmlns:iden="http://x-road.eu/xsd/identifiers">DHX</iden:subsystemCode>
      </xro:client>
   </SOAP-ENV:Header>
   <SOAP-ENV:Body>
      <ns4:getSendingOptionsResponse xmlns:ns10="http://x-road.eu/xsd/identifiers"
       xmlns:ns11="http://x-road.eu/xsd/representation.xsd"
       xmlns:ns12="http://x-road.eu/xsd/xroad.xsd"
       xmlns:ns2="http://www.riik.ee/schemas/deccontainer/vers_2_1/"
       xmlns:ns4="http://producers.dhl.xrd.riik.ee/producer/dhl"
       xmlns:ns5="http://www.riik.ee/schemas/dhl"
       xmlns:ns6="http://www.sk.ee/DigiDoc/v1.3.0#"
       xmlns:ns7="http://www.w3.org/2000/09/xmldsig#"
       xmlns:ns8="http://www.riik.ee/schemas/dhl-meta-automatic"
       xmlns:ns9="http://dhx.x-road.eu/producer">
         <keha href="4a095fa0-b922-4746-bda2-d3498f0c5f06@dhx_4703433375683078305"/>
      </ns4:getSendingOptionsResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

Päringu vastuse `<keha>` element viitab manusele `href="4a095fa0-b922-4746-bda2-d3498f0c5f06@dhx_4703433375683078305"`, mille väärtus on näiteks:
```
H4sIAAAAAAAAAMWUv27bMBDG9z6FoM4mKct/EoNRkDZDOwQtuqSdAkZkpINN0uCRtvIufY1u3fxipRVIioCmXRxEE3n3HX/f3QHil43eJDvlEKy5SDPC0sviHTeYr9aqFknMGlwZnF2ktffbFaX7/Z44gDVRimJZKy2QynqT9tKzXirrhjQTZ4UkKtCtszKUyg3KjPXSQdagpE5tnUJlvPDRFomhoSjva7oHkUQ+aZzsfXWZsbEsewHXtJcRZT7qF9tXr6GCa1vSXUZywt4P4sVIvM+JdRWdMsYoO6dRJBGqZ/Lpv2epytLGzsFE/8fF3E3vMjpUL/+3iYlWXkxE8FbH8ZVD5fkL7YOMk4YHiKy04AKDD1hwpyrjCrlbk2XsZJFnS06fYtyAhuIbxHEkn82DxcNv9EppSK4imtM2zVEIr2MT3Ql3AFhEf5w+D3S3o5B27LGHjM0XjE3nY/6Hr7fJR2swbDyY6vTUnLVfNqZ+Ovy6V+sQNpB8Ofw8PdUJEbcWtiDFUUfexsXsr9QbMHBctIOgk++np2K4x8cI0ORt+Msn6mxMvWo1yY/X7Pf1ybT7oxd/ABLnvIDyBQAA
```

Mis dekodeeritult (base64 -d | gunzip) on näiteks:
```xml
<?xml version="1.0"?>
<ns6:keha xmlns:ns2="http://www.riik.ee/schemas/dhl" xmlns:ns8="http://dhx.x-road.eu/producer" 
   xmlns:ns10="http://x-road.eu/xsd/representation.xsd" xmlns:ns6="http://producers.dhl.xrd.riik.ee/producer/dhl" 
   xmlns:ns11="http://x-road.eu/xsd/xroad.xsd" xmlns:ns4="http://www.sk.ee/DigiDoc/v1.3.0#" 
   xmlns:ns5="http://www.w3.org/2000/09/xmldsig#" xmlns:ns3="http://www.riik.ee/schemas/deccontainer/vers_2_1/" 
   xmlns:ns7="http://www.riik.ee/schemas/dhl-meta-automatic" xmlns:ns9="http://x-road.eu/xsd/identifiers">
 <asutus>
  <regnr>30000001</regnr>
  <nimi>Hõbekuuli OÜ</nimi>
  <saatmine>
    <saatmisviis>dhl</saatmisviis>
  </saatmine>
 </asutus>
 <asutus>
  <regnr>dvk.70006317</regnr>
  <nimi>Riigi Infosüsteemi Amet</nimi>
  <saatmine>
    <saatmisviis>dhl</saatmisviis>
  </saatmine>
 </asutus>
</ns6:keha> 
```

See sisaldab asutuse kohta kolme välja:
* `<regnr>` - asutuse registrikood või alamsüsteemi kood. Üldjuhul tagastatakse siin asutuse registrikood. Aga kui asutus pakub teenust DHX alamsüsteemi kaudu (näiteks subsystemCode=`DHX.subsystem1`), siis  DHX adapterserveri getSendingOptions väljundis tagastatakse see kujul `<regnr>subsystem1.40000001</regnr>`, kus 40000001 on asutuse registrikood. Teatud spetsiifilised asutused on häälestatud tagastama ainult süsteemi koodi (näiteks `<regnr>adit</regnr>`). See on määratud `dhx.server.special-orgnisations=adit,kovtp,rt,eelnoud` parameetriga. Vaata [DHX adapeterserveri paigaldusjuhendist](adapter-server-paigaldusjuhend.md#6-häälestus-fail-dhx-applicationproperties). 
* `<nimi>` - Asutuse või alamsüsteemi nimi. Asutuse nimi leitakse X-tee globaalse konfiguratsiooni ja vahendajate [representationList]((https://github.com/e-gov/DHX/blob/master/files/representationList.md)) teenuse väljundite põhjal.
* `<saatmisviis>` - alati konstant `dhl`.


Ülejäänud DVK poolt tagastatavaid välju (`<ks_asutuse_regnr/>`, `<allyksused>`, `<ametikohad>`) DHX adapterserver kunagi ei tagasta.
Kui asutuse DHS süsteem neid vana DVK korral kasutas, siis DHX protokollile üle kolimisel peaks ta need kusagilt mujalt küsima.
Samuti ei ole neid mõtet `getSendingOptions.v3` päringu sisendis ette anda, sest neid ignoreeritakse. 
Samuti ignoreeritakse sisendis välju `<vahetatud_dokumente_vahemalt>`, `<vahetatud_dokumente_kuni>` ja `<vastuvotmata_dokumente_ootel>`.

### 4.2. sendDocuments (sisemine liides)

SOAP operatsiooni `sendDocuments.v4` kasutatakse dokumentide saatmiseks teisel asutusele.
Dokumendid peavad olema Kapsli [2.1](https://riha.eesti.ee/riha/main/xml/elektroonilise_andmevahetuse_metaandmete_loend/1) versioonis (vanemad Kapsli versioonid ei ole toetatud).

DHX adapterserver võtab dokumendi vastu, salvestab enda andmebaasi ja vastab SOAP päringule koheselt. 
Dokumendi edasine DHX addresaadile saatmine teostatakse asünkroonselt (taustatöö poolt).
Dokumendi saatmise staatuse küsimiseks tuleb kasutada operatsiooni [getSendStatus](#43-getsendstatus-sisemine-liides).

Vaata `sendDocuments.v4` saatmise näidet dokumendist Testilood - [2.1. Õige kapsli saatmine](adapter-server-testilood.md#2.1).
 
 Lisaks vaata kirjeldust vana DVK spetsifikatsioonis [sendDocuments.v4](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv4). 
> **NB!** DVK spetsifikatsiooni näidetes kasutatakse vanu X-tee versioon 4.0 päiseid (`<xtee:asutus>`, `<xtee:andmekogu>` jt). 
> DHX adapterserveri sisemise liidesega suhtlemisel tuleb kasutada  X-tee versioon 6.0 päiseid. Nagu need on [Testlugude näidetes](adapter-server-testilood.md#2.1).

Märkused vana DVK X-tee liidese kasutajale:
> Võrreldes DVK sendDocuments liidestega on dhx-adpater-serveris realiseeritud on ainult sendDocuments operatsioonide [v4](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv4) versioon, mis eeldab et dokumendi Kapsel on 2.1 formaadis.
>
> Vanemaid DVK sendDocuments versioone [v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv1), [v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv2), [v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv3) dhx-adpater-server ei paku.


### 4.3. getSendStatus (sisemine liides)

SOAP operatsiooni `getSendStatus` kasutatakse saadetud dokumendi staatuse ja saatmisel ilmnenud vea info (fault) küsimiseks.

Võimalikud staatused on:
* `saatmisel` – dokumenti üritatakse veel antud saajale edastada
* `saadetud` – dokument sai edukalt antud saajale saadetud
* `katkestatud` – dokumenti ei õnnestunud antud saajale saata.

Staatuste kohta vaata täpselt [DVK dokumentatsioonist](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#edastatud-dokumentide-staatuse-kontroll).

Vaata `getSendStatus.v2` saatmise näidet dokumendist Testilood - [2.11. DHX-i saadetud dokumendi staatuse pärimine](adapter-server-testilood.md#2.11).

Lisaks vaata kirjeldust vana DVK spetsifikatsioonis [getSendStatus.v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendstatusv1). ja [getSendStatus.v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendstatusv2).
 
> **NB!** DVK spetsifikatsiooni näidetes kasutatakse vanu X-tee versioon 4.0 päiseid (`<xtee:asutus>`, `<xtee:andmekogu>` jt). 
> DHX adapterserveri sisemise liidesega suhtlemisel tuleb kasutada  X-tee versioon 6.0 päiseid. Nagu need on [Testlugude näidetes](adapter-server-testilood.md#2.11).

Märkused vana DVK X-tee liidese kasutajale:
> DHX adpaterserveris on realiseeritud mõlemad getSendStatus operatsiooni versioonid [v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendstatusv1) ja [v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendstatusv2).


## 5. Kokkuvõtte erinevustest (DVK liidesega võrreldes)

* DHX Adapterserveri `getSendingOptions` päring ei väljasta allüksuseid ega ametikohti, sest DHX protokollis neid ei eksisteeri.

* DHX Adapterserveris on realiseeritud ainult `sendDocuments.v4` päring, mis kasutab Kaplsi 2.1 versiooni. Vanemad versioonid ei ole toetatud.  

* DHX Adapterserveri  `getSendStatus.v2` päringu sisendis ei tööta `<dokument_guid>` välja kasutamine. Tohib kasutada ainult välja `<dhl_id>>`. `<dokument_guid>` välja võõrtus on alati tühi ka väljundites.

* SWAREF manuse cid väärtus peab olema URL kodeeritud (DVK korral see ei tohtinud olla URL kodeeritud)

## 6. Vahendajana saatmine/vastuvõtmine

Asutus võib DHX adpaterserverit kasutada [DHX vahendamiseks](https://e-gov.github.io/DHX/#6-vahendamine). 



 
