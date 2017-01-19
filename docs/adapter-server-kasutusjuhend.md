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

**4)** Sisestada Request XML väljale järgmine väärtus, muutes endale sobivaks väärtused `ee-dev`, `GOV` ja `40000001` (asutuse enda registrikood) ning käivitada päring.

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xro="http://x-road.eu/xsd/xroad.xsd" xmlns:iden="http://x-road.eu/xsd/identifiers" xmlns:dhl="http://producers.dhl.xrd.riik.ee/producer/dhl">
   <soapenv:Header>
      <xro:protocolVersion>4.0</xro:protocolVersion>
      <xro:id>64a3ddbd-1620-42c4-b2fe-60b854c2f32f</xro:id>
      <xro:service>
         <iden:xRoadInstance>ee-dev</iden:xRoadInstance>
         <iden:memberClass>GOV</iden:memberClass>
         <iden:memberCode>40000001</iden:memberCode>
         <iden:subsystemCode>DHX</iden:subsystemCode>
         <iden:serviceCode>getSendingOptions</iden:serviceCode>
         <iden:serviceVersion>v2</iden:serviceVersion>
      </xro:service>
      <xro:client>
         <iden:xRoadInstance>ee-dev</iden:xRoadInstance>
         <iden:memberClass>GOV</iden:memberClass>
         <iden:memberCode>40000001</iden:memberCode>
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

Märkus:
> DHX adapterserveri `getSendingOptions` realisatsioon ei väljasta allüksuseid ega ametikohti, sest DHX protokollis neid ei eksisteeri.
> Kui asutuse DHS süsteem neid vana DVK korral kasutas, siis DHX protokollile üle kolimisel peaks ta need kusagilt mujalt küsima. 

Päringu väljund on sarnane DVK väljundile, iga asutuse kohta on seal: 
```xml
<asutus>
  <regnr>30000001</regnr>
  <nimi>Hõbekuuli OÜ</nimi>
  <saatmine>
    <saatmisviis>dhl</saatmisviis>
  </saatmine>
 </asutus>
```
See sisaldab asutuse kohta kolme välja:
* `<regnr>` - asutuse registrikood või alamsüsteemi kood. Üldjuhul tagastatakse siin asutuse registrikood. Aga kui asutus pakub teenust DHX alamsüsteemi kaudu (näiteks subsystemCode=`DHX.subsystem1`), siis  DHX adapterserveri getSendingOptions väljundis tagastatakse see kujul `<regnr>subsystem1.40000001</regnr>`, kus 40000001 on asutuse registrikood. Teatud spetsiifilised asutused on häälestatud tagastama ainult süsteemi koodi (näiteks `<regnr>adit</regnr>`). See on määratud `dhx.server.special-orgnisations=adit,kovtp,rt,eelnoud` parameetriga. Vaata [DHX adapeterserveri paigaldusjuhendist](adapter-server-paigaldusjuhend.md#6-häälestus-fail-dhx-applicationproperties). 
* `<nimi>` - Asutuse või alamsüsteemi nimi. Asutuse nimi leitakse X-tee globaalse konfiguratsiooni põhjal.
* `<saatmisviis>` - alati konstant `dhl`.

Ülejäänud DVK poolt tagastatavaid välju (`<ks_asutuse_regnr/>`, `<allyksused>`, `<ametikohad>`) DHX adapterserver kunagi ei tagasta.
Samuti pole neid mõtet `getSendingOptions.v3` päringu sisendis ette anda, sest neid ignoreeritakse.

Vaata `getSendingOptions.v2` saatmise näidet dokumendist Testilood - [2.10. Aadressaatide nimekirja pärimine](adapter-server-testilood.md#2.10).

Lisaks vaata kirjeldust vana DVK spetsifikatsioonis [getSendingOptions.v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv1),
[getSendingOptions.v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv2) ja [getSendingOptions.v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv3)

Märkused vana DVK X-tee liidese kasutajale:
> DHX adpaterserveris on realiseeritud kõik getSendStatus operatsiooni versioonid [getSendingOptions.v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv1), [getSendingOptions.v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv2) ja [getSendingOptions.v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv3).


### 4.2. sendDocuments (sisemine liides)

SOAP operatsiooni `sendDocuments.v4` kasutatakse dokumentide saatmiseks teisel asutusele.
Dokumendid peavad olema Kapsli [2.1](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-core/src/main/resources/Dvk_kapsel_vers_2_1_eng_est.xsd) versioonis (vanemad Kapsli versioonid ei ole toetatud).

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


## 5. Erinevused DVK liidesega võrreldes

* DHX Adapterserveri `getSendingOptions` realisatsioon ei väljasta allüksuseid ega ametikohti, sest DHX protokollis neid ei eksisteeri.

* DHX Adapterserveri  `getSendStatus.v2` päringu sisendis ei tööta `<dokument_guid>` välja kasutamine. Tohib kasutada ainult välja `<dhl_id>>`.

* SWAREF manuse cid väärtus peab olema URL kodeeritud (DVK korral see ei tohtinud olla URL kodeeritud)

## 6. Vahendajana saatmine/vastuvõtmine

Asutus võib DHX adpaterserverit kasutada [DHX vahendamiseks](https://e-gov.github.io/DHX/#6-vahendamine). 



 
