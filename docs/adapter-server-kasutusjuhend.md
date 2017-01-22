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

**4)** Sisestada Request XML väljale SOAP päring, muutes endale sobivaks elementide `<xRoadInstance>`, `<memberClass>` ja `<memberCode>` (asutuse registrikood) väärtused (näiteks `ee-dev`, `GOV` ja `40000001`) 

**5)** Lisada vajadusel manus (Attachment) failist. NB! SoapUI arvestab manuse faili lugemisel laiendit (kui see on näiteks .txt, siis SoapUI püüand seda ise kodeerida ja lisab content-type: text/plan). Selleks et SoapUI seda ise ei teeks peaks mansue faili laiend olema näiteks `.base64`. 

**6)** Käivitada SoapUI päring.

Märkus:
> Osade päringute ja vastuste manused on gzip pakitud ja seejärel BASE64 kodeeritud.
> 
> Need saab Linux/unix all kokku pakkida salvestades manuse XML sisu faili "manus.xml" (fail peab olema salvestatud UTF-8 kodeeringus) ja käivitades seejärel:
> ``` 
>  cat manus.xml | gzip | base64 --wrap=0 > manus.base64 
> ```
> Manused peavad olema nö "basic" base64 kodeeritud ehk terviklikult ühe reana. Base64 MIME kodeeritud manustest (mitu rida, iga rida 76 märki) DHX adapterserver aru ei saa.
>
> Manused saab Linux/unix alla lahti kodeerida salvestades manuse faili "manus.base64" ja käivitades seejärel:
> ``` 
>  cat manus.base64 | base64 -d | gunzip
> ```

SoapUI-ga testimise kohta loe eraldi dokumentatsioonist [SoapUI testide käivitamise juhend](adapter-server-soapui-test-juhend.md) ja [Testlood](adapter-server-testilood.md). 

## 4. Sisemine liides

Sisemist liidest kasutab asutuse DHS tarkvara dokumentide saatmiseks ja vastuvõtmiseks.

Sisemise liidese kasutamisel käitub DHS tarkvara SOAP kliendina (DHS tarkvara ei pea ise ühtegi veebiteenust pakkuma).

Sisemise liidse operatsioonid on järgmised: 
* **getSendingOptions** - väljastab nimekirja asutustest, kellele üle DHX protokolli saab dokumente saata.
* **sendDocuments** - teenus dokumendi välja saatmiseks
* **getSendStatus** - teenus välja saadetud dokumendi staatuse kontrollimiseks
* **receiveDocuments** - asutusele saabunud (sissetulevate) dokumentide küsimiseks
* **markDocumentsReceived** - teenusega saab märkida asutusele saabunud dokumendi vastu võetuks 


Märkused: 
> Sisemist liidese operatsioonid on projekteeritud väga sarnaselt vanale [DVK liidesele](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md). 
> Sisemise liidese SOAP teenuste XML nimeruumid ja implementeeritud operatsioonide struktuur on täpselt samad nagu vanas DVK liideses.
> 
> Enamasti peaks saama vanalt DVK X-tee liideselt üle minna uuele DHX protokollile, hakates kasutama uut DHX adapterserver tarkvara, muutes DHS sees ümber DVK veebiteenuse võrguaadressi (endpoint URI aadressi).
> Kui varem pakkus seda teenust X-tee turvaserver, siis selle asemel pakub seda adapterserveri sisemine liides.
> 
> Sisemises liideses on implementeeritud ainult hädavajalikud DVK liidese operatsioonide versioonid.
>
> Lisaks tuleb silmas pidada, et esineb mõningaid sisulisi loogika erinevusi võrreldes DVK liidesega. Need on välja toodud [allpool](#5-erinevused-dvk-liidesega-võrreldes). 

Sisemisele liidese päringutes tuleb kaasa anda X-tee v6 standardsed päised (`<service>` ja `<client>`), kuigi Sisemine liides X-tee turvaserverit kasuta.

Päringus `<service>` päises ette antud alamelemente `<xRoadInstance>`, `<memberClass>`, `memberCode` ja `<subsystemCode>` ignoreeritakse. 
Seega need võivad olla samad vana DVK X-tee teenuse omad nagu saadeti (`GOV`, `70006317` - Riigi Infosüsteemi Amet ja `dhl`).
```xml 
      <xro:service>
         <iden:xRoadInstance>ee</iden:xRoadInstance>
         <iden:memberClass>GOV</iden:memberClass>
         <iden:memberCode>70006317</iden:memberCode>
         <iden:subsystemCode>dhl</iden:subsystemCode>
         <iden:serviceCode>getSendingOptions</iden:serviceCode>
         <iden:serviceVersion>v1</iden:serviceVersion>
      </xro:service>
```

Päringu `<client>` päises ette antud alamelementidest kasutatakse ainult `<memberCode>` elementi, mis peab olema saatja asutuse registrikood. 
Ülejäänuid `<client>` päise elemente ignoreeritakse. Seega need võivad olla samad nagu vana DVK X-tee teenuse korral saadeti.
```xml  
      <xro:client>
         <iden:xRoadInstance>ee</iden:xRoadInstance>
         <iden:memberClass>COM</iden:memberClass>
         <iden:memberCode>30000001</iden:memberCode>
         <iden:subsystemCode>DHX</iden:subsystemCode>
      </xro:client>
```

Järgnevalt kirjeldatakse lühidalt kuidas toimub dhx-adpater-serveri sisemise liidese kasutamine dokumentide saatmiseks ja vastuvõtmiseks. 

### 4.1. getSendingOptions (sisemine liides)

Seda operatsiooni kasutatakse [DHX aadressiraamatu](https://e-gov.github.io/DHX/#74-lokaalne-aadressiraamat) küsimiseks.

See tagastab kõik asutused kellele võib üle DHX protokolli dokumente saata. 

Lisaks kirjeldust vana DVK spetsifikatsioonis [getSendingOptions.v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv1),
[getSendingOptions.v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv2) ja [getSendingOptions.v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv3)

> **NB!** DVK spetsifikatsiooni näidetes kasutatakse vanu X-tee versioon 4.0 päiseid (`<xtee:asutus>`, `<xtee:andmekogu>` jt). 
> DHX adapterserveri sisemise liidesega suhtlemisel tuleb kasutada  X-tee versioon 6.0 päiseid. Nagi allpool toodud näites.

Märkused vana DVK X-tee liidese kasutajale:
> DHX adpaterserveris on realiseeritud kõik getSendStatus operatsiooni versioonid [getSendingOptions.v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv1), [getSendingOptions.v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv2) ja [getSendingOptions.v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendingoptionsv3).

Päringu `getSendingOptions.v1` näide:
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
 xmlns:xro="http://x-road.eu/xsd/xroad.xsd" 
 xmlns:iden="http://x-road.eu/xsd/identifiers" 
 xmlns:dhl="http://producers.dhl.xrd.riik.ee/producer/dhl">
   <soapenv:Header>
      <xro:protocolVersion>4.0</xro:protocolVersion>
      <xro:id>64a3ddbd-1620-42c4-b2fe-60b854c2f32f</xro:id>
      <xro:service>
         <iden:xRoadInstance>ee</iden:xRoadInstance>
         <iden:memberClass>GOV</iden:memberClass>
         <iden:memberCode>70006317</iden:memberCode>
         <iden:subsystemCode>dhl</iden:subsystemCode>
         <iden:serviceCode>getSendingOptions</iden:serviceCode>
         <iden:serviceVersion>v1</iden:serviceVersion>
      </xro:service>
      <xro:client>
         <iden:xRoadInstance>ee</iden:xRoadInstance>
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
         <iden:xRoadInstance xmlns:iden="http://x-road.eu/xsd/identifiers">ee</iden:xRoadInstance>
         <iden:memberClass xmlns:iden="http://x-road.eu/xsd/identifiers">COV</iden:memberClass>
         <iden:memberCode xmlns:iden="http://x-road.eu/xsd/identifiers">70006317</iden:memberCode>
         <iden:subsystemCode xmlns:iden="http://x-road.eu/xsd/identifiers">dhl</iden:subsystemCode>
         <iden:serviceCode xmlns:iden="http://x-road.eu/xsd/identifiers">getSendingOptions</iden:serviceCode>
         <iden:serviceVersion xmlns:iden="http://x-road.eu/xsd/identifiers">v1</iden:serviceVersion>
      </xro:service>
      <xro:client xmlns:xro="http://x-road.eu/xsd/xroad.xsd">
         <iden:xRoadInstance xmlns:iden="http://x-road.eu/xsd/identifiers">ee</iden:xRoadInstance>
         <iden:memberClass xmlns:iden="http://x-road.eu/xsd/identifiers">COM</iden:memberClass>
         <iden:memberCode xmlns:iden="http://x-road.eu/xsd/identifiers">10560025</iden:memberCode>
         <iden:subsystemCode xmlns:iden="http://x-road.eu/xsd/identifiers">DHX</iden:subsystemCode>
      </xro:client>
   </SOAP-ENV:Header>
   <SOAP-ENV:Body>
      <ns4:getSendingOptionsResponse xmlns:ns10="http://x-road.eu/xsd/identifiers" xmlns:ns11="http://x-road.eu/xsd/representation.xsd" xmlns:ns12="http://x-road.eu/xsd/xroad.xsd" xmlns:ns2="http://www.riik.ee/schemas/deccontainer/vers_2_1/" xmlns:ns4="http://producers.dhl.xrd.riik.ee/producer/dhl" xmlns:ns5="http://www.riik.ee/schemas/dhl" xmlns:ns6="http://www.sk.ee/DigiDoc/v1.3.0#" xmlns:ns7="http://www.w3.org/2000/09/xmldsig#" xmlns:ns8="http://www.riik.ee/schemas/dhl-meta-automatic" xmlns:ns9="http://dhx.x-road.eu/producer">
         <keha xmlns:ns10="http://x-road.eu/xsd/representation.xsd" xmlns:ns11="http://x-road.eu/xsd/xroad.xsd" xmlns:ns3="http://producers.dhl.xrd.riik.ee/producer/dhl" xmlns:ns4="http://www.riik.ee/schemas/dhl" xmlns:ns5="http://www.sk.ee/DigiDoc/v1.3.0#" xmlns:ns6="http://www.w3.org/2000/09/xmldsig#" xmlns:ns7="http://www.riik.ee/schemas/dhl-meta-automatic" xmlns:ns8="http://dhx.x-road.eu/producer" xmlns:ns9="http://x-road.eu/xsd/identifiers">
            <asutus>
               <regnr>dvk.70006317</regnr>
               <nimi>Riigi Infosüsteemi Amet(DHX.dvk)</nimi>
               <saatmine>
                  <saatmisviis>dhl</saatmisviis>
               </saatmine>
            </asutus>
            <asutus>
               <regnr>10560025</regnr>
               <nimi>BPW Consulting</nimi>
               <saatmine>
                  <saatmisviis>dhl</saatmisviis>
               </saatmine>
            </asutus>
            <asutus>
               <regnr>30000001</regnr>
               <nimi>Hõbekuuli OÜ</nimi>
               <saatmine>
                  <saatmisviis>dhl</saatmisviis>
               </saatmine>
            </asutus>
         </keha>
      </ns4:getSendingOptionsResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
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
Dokumendid peavad olema [Kapsli 2.1 versioonis](https://riha.eesti.ee/riha/main/xml/elektroonilise_andmevahetuse_metaandmete_loend/1). Vanemad Kapsli versioonid ei ole toetatud.

DHX adapterserver võtab dokumendi vastu, salvestab enda andmebaasi ja vastab SOAP päringule koheselt. 
Dokumendi edasine DHX addresaadile saatmine teostatakse asünkroonselt (taustatöö poolt).
Dokumendi saatmise staatuse küsimiseks tuleb kasutada operatsiooni [getSendStatus](#43-getsendstatus-sisemine-liides).

Vaata täpsemat kirjeldust vana DVK spetsifikatsioonis [sendDocuments.v4](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv4).
> **NB!** DVK spetsifikatsiooni näidetes kasutatakse vanu X-tee versioon 4.0 päiseid (`<xtee:asutus>`, `<xtee:andmekogu>` jt). 
> DHX adapterserveri sisemise liidesega suhtlemisel tuleb kasutada  X-tee versioon 6.0 päiseid. Nagu need on [Testlugude näidetes](adapter-server-testilood.md#2.1).

Päringu sisu näide:
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xtee="http://x-tee.riik.ee/xsd/xtee.xsd" xmlns:dhl="http://producers.dhl.xrd.riik.ee/producer/dhl">
    <soapenv:Header>
       <ns4:protocolVersion xmlns:ns2="http://dhx.x-road.eu/producer" xmlns:ns3="http://x-road.eu/xsd/identifiers" xmlns:ns4="http://x-road.eu/xsd/xroad.xsd" xmlns:ns5="http://www.riik.ee/schemas/deccontainer/vers_2_1/">4.0</ns4:protocolVersion>
    <ns4:id xmlns:ns2="http://dhx.x-road.eu/producer" xmlns:ns3="http://x-road.eu/xsd/identifiers" xmlns:ns4="http://x-road.eu/xsd/xroad.xsd" xmlns:ns5="http://www.riik.ee/schemas/deccontainer/vers_2_1/">64a3ddbd-1620-42c4-b2fe-60b854c2f32f
    </ns4:id>
    <ns4:client xmlns:ns2="http://dhx.x-road.eu/producer" xmlns:ns3="http://x-road.eu/xsd/identifiers" xmlns:ns4="http://x-road.eu/xsd/xroad.xsd" xmlns:ns5="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
      <ns3:xRoadInstance>ee-</ns3:xRoadInstance>
      <ns3:memberClass>GOV</ns3:memberClass>
      <ns3:memberCode>30000001</ns3:memberCode>
      <ns3:subsystemCode>DHX</ns3:subsystemCode>
    </ns4:client>
    <ns4:service ns3:objectType="SERVICE" xmlns:ns2="http://dhx.x-road.eu/producer" xmlns:ns3="http://x-road.eu/xsd/identifiers" xmlns:ns4="http://x-road.eu/xsd/xroad.xsd" xmlns:ns5="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
      <ns3:xRoadInstance>ee</ns3:xRoadInstance>
      <ns3:memberClass>GOV</ns3:memberClass>
      <ns3:memberCode>70006317</ns3:memberCode>
      <ns3:subsystemCode>dhl</ns3:subsystemCode>
      <ns3:serviceCode>sendDocuments</ns3:serviceCode>
      <ns3:serviceVersion>v4</ns3:serviceVersion>
    </ns4:service>
   </soapenv:Header>
   <soapenv:Body>
      <dhl:sendDocuments>
         <keha>
            <dokumendid href="cid:sendDoc.base64">
            </dokumendid>
            <kaust>/</kaust>
         </keha>
      </dhl:sendDocuments>
   </soapenv:Body>
</soapenv:Envelope>
```

Manus XML failina:
```xml
<DecContainer xmlns="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
  <Transport>
    <DecSender>
        <OrganisationCode>30000001</OrganisationCode>
        <PersonalIdCode>EE38806190294</PersonalIdCode>
    </DecSender>
    <DecRecipient>
        <OrganisationCode>40000001</OrganisationCode>
    </DecRecipient>
  </Transport>
  <RecordCreator>
    <Person>
      <Name>Lauri Tammemäe</Name>
      <GivenName>Lauri</GivenName>
      <Surname>Tammemäe</Surname>
      <PersonalIdCode>EE38806190294</PersonalIdCode>
      <Residency>EE</Residency>
    </Person>
    <ContactData>
      <Adit>true</Adit>
      <Phone>3726630276</Phone>
      <Email>lauri.tammemae@ria.ee</Email>
      <WebPage>www.hot.ee/lauri</WebPage>
      <MessagingAddress>skype: lauri.tammemae</MessagingAddress>
      <PostalAddress>
        <Country>Eesti</Country>
        <County>Harju maakond</County>
        <LocalGovernment>Tallinna linn</LocalGovernment>
        <AdministrativeUnit>Mustamäe linnaosa</AdministrativeUnit>
        <SmallPlace>Pääsukese KÜ</SmallPlace>
        <LandUnit></LandUnit>
        <Street>Mustamäe tee</Street>
        <HouseNumber>248</HouseNumber>
        <BuildingPartNumber>62</BuildingPartNumber>
        <PostalCode>11212</PostalCode>
      </PostalAddress>
    </ContactData>
  </RecordCreator>
  <RecordSenderToDec>
    <Person>
      <Name>Lauri Tammemäe</Name>
      <GivenName>Lauri</GivenName>
      <Surname>Tammemäe</Surname>
      <PersonalIdCode>EE38806190294</PersonalIdCode>
      <Residency>EE</Residency>
    </Person>
    <ContactData>
      <Adit>false</Adit>
      <Phone>3726630276</Phone>
      <Email>lauri.tammemae@ria.ee</Email>
      <WebPage>www.hot.ee/lauri</WebPage>
      <MessagingAddress>skype: lauri.tammemae</MessagingAddress>
      <PostalAddress>
        <Country>Eesti</Country>
        <County>Harju maakond</County>
        <LocalGovernment>Tallinna linn</LocalGovernment>
        <AdministrativeUnit>Mustamäe linnaosa</AdministrativeUnit>
        <SmallPlace>Pääsukese KÜ</SmallPlace>
        <LandUnit></LandUnit>
        <Street>Mustamäe tee</Street>
        <HouseNumber>248</HouseNumber>
        <BuildingPartNumber>62</BuildingPartNumber>
        <PostalCode>11212</PostalCode>
      </PostalAddress>
    </ContactData>
  </RecordSenderToDec>
  <Recipient>
    <Organisation>
      <Name>Riigi Infosüsteemi Amet</Name>
      <OrganisationCode>70006317</OrganisationCode>
      <Residency>EE</Residency>
    </Organisation>
  </Recipient>
  <RecordMetadata>
    <RecordGuid>25892e17-80f6-415f-9c65-7395632f0234</RecordGuid>
    <RecordType>Kiri</RecordType>
    <RecordOriginalIdentifier>213465</RecordOriginalIdentifier>
    <RecordDateRegistered>2012-11-11T19:18:03</RecordDateRegistered>
    <RecordTitle>Ettepanek</RecordTitle>
    <RecordLanguage>EE</RecordLanguage>
  </RecordMetadata>
  <Access>
    <AccessConditionsCode>Avalik</AccessConditionsCode>
  </Access>
  <File>
    <FileGuid>25892e17-80f6-415f-9c65-7395632f0001</FileGuid>
    <RecordMainComponent>0</RecordMainComponent>
    <FileName>Ettepanek.doc</FileName>
    <MimeType>application/msword</MimeType>
    <FileSize>211543</FileSize>
    <ZipBase64Content>H4sIACvlpU0AAwspqszMS1coyVcoTs1LUUjJT+YCALD0cp8TAAAA</ZipBase64Content>
  </File>
  <RecordTypeSpecificMetadata  />
</DecContainer>
```

Sama manus gzip ja base64 kodeeritult:
```
H4sIACJmhFgAA+1X3W7bNhS+z1MIvR0SSrIt2wEnTHWyJGvcGrGzYbspWOnYYSORGknZS59ll3mM3uXFekRZP1ayddjdsBqGQZ7z8fx+h6DpGcQzKQzjApTzR5YK/f2rO2PyU0J2u92J4vz+BIDo+A4ypkkCcVzDyRaUfu+/98ir8Mhx6EoxoXOpTLnDPZpegkhAVXsre6c2THDNDJdiJhMIB679eJQ8U7WnFuhICpZeJVZxfj6YTNzAm7r+dEhJT1s5Jz3vZTQ3EPOcgzB/F9DwKwFZyweWKDnInKJSqmSmgBlZe69irP3StyyD8JoVijsrlmWQPT0CJVZaQy74FkSLo6QV1JBloUS575ioRUf/unI2Ac0TEPED4ilpd/v8u7lQS57YnDHDmvNRwk1oVIHx2GUTzJ0U2PGxHwQD1x8HaMpKav15xngapmW6J8bmxOAHxRkSkJJKWUN/gQ8LtoGw5OidNCVF06pMtaZGzkFrtuFiEyWJwnWo7x9yOHUO3VDyDNdELbVhaU9qMy+EUVgj0Ab91tse4CG8ZOpj4WSM3UuR7HFd2LWMWXohcZhEVjJqxdKUC8Gc8peSvro9GCUZR34ahQzdwq3ASs8LjLVkgj3MpGZlC57BWhvLDL0tUhZDuHh6fHrUxT1ocN48/YlcanWdaJlIrA2MrF52zBkF0A3DlK3bS1vYpSw0vC2yDzid/nBCSVfQwl4XPE2wIwumzF4Z+JS8IO5cFbZbls6e53sI70jqlpIXelp25oDKJfN7g7wf7epeWUm8CP63471mqf4239/m+78/371ppr1XwsEL4XDCbzjfcOdKrKV++qyxFBl3ogxMb9SfvSPG+L4IBt74r988X5vRfkw2l86LpMpsDoYlzeTuhRcFT0J/NJn64I2PJ+46OB56o/XxNA5Gx+PBdBQM/LXrD4Z1feyBroUVzlf4hpfD2BF0Ee8UFsZePBgQX/OSBt5gGIzqEy8AuuexRXADGyQ1KMBoXc8/9jz8rrzpqTc5dQe1oR7yIExuUrwSjYGcCbhvgrXiLhB5vinKK6Uq9YGow5NuNWkUxy2zqg3SCy9DbIi2nYy2LOXo9UWlNdvaoD/yJqZy+c96ZB+pDbyb0Rzf5zOZ5Xj5IiHcJoMDcevOsrUp1Eki48puy2I65xnYNrM8T3lsiUcyvUOreLXWytbkkn8C7Lk3Gg4qW1ZQ6X/j+WumIRiWA1lGcjnUV9Fsm+a3bhTtdP67/jRferF8+DmWK+1d395+/Gn13a+z6PrMjfPJKsIPJc/M2KLWlexwdZnjbKx5XDfQcUh4ZJ/xzT+f8As/TQdcCA0AAA==
```

Nõuded päringu sisendile:
* Päringu sisendi X-tee päises ette antud saatja `<client><memberCode>30000001</ns3:memberCode>` peab ühtima Kapslis toodud saatjaga `<Transport><DecSender><OrganisationCode>30000001</OrganisationCode>`.
* Kapsel peab olema [SWAREF](http://www.ws-i.org/profiles/attachmentsprofile-1.0-2004-08-24.html) manusena. Manuse algne xml fail peab olema UTF-8 kodeeringus, lisatud manusesse gzip pakitud ja seejärel base64 kodeeritult. 
* Päringu sisendis ei pea ette andma Kapsli XML schema järgi kohustuslikku `<DecMetadata>` elementi (ega selle alamelemente `<DecId>`, `<DecFolder>`, `<DecReceiptDate>`). Need genereerib DHX adapterserver ise. Sama loogika kehtis vanas DVK liideses.
* 

Vastuse sisu näide:
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header>
      <ns4:protocolVersion xmlns:ns2="http://dhx.x-road.eu/producer" xmlns:ns3="http://x-road.eu/xsd/identifiers" xmlns:ns4="http://x-road.eu/xsd/xroad.xsd" xmlns:ns5="http://www.riik.ee/schemas/deccontainer/vers_2_1/">4.0</ns4:protocolVersion>
      <ns4:id xmlns:ns2="http://dhx.x-road.eu/producer" xmlns:ns3="http://x-road.eu/xsd/identifiers" xmlns:ns4="http://x-road.eu/xsd/xroad.xsd" xmlns:ns5="http://www.riik.ee/schemas/deccontainer/vers_2_1/">64a3ddbd-1620-42c4-b2fe-60b854c2f32f</ns4:id>
      <ns4:client xmlns:ns2="http://dhx.x-road.eu/producer" xmlns:ns3="http://x-road.eu/xsd/identifiers" xmlns:ns4="http://x-road.eu/xsd/xroad.xsd" xmlns:ns5="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
         <ns3:xRoadInstance>ee-</ns3:xRoadInstance>
         <ns3:memberClass>GOV</ns3:memberClass>
         <ns3:memberCode>30000001</ns3:memberCode>
         <ns3:subsystemCode>DHX</ns3:subsystemCode>
      </ns4:client>
      <ns4:service ns3:objectType="SERVICE" xmlns:ns2="http://dhx.x-road.eu/producer" xmlns:ns3="http://x-road.eu/xsd/identifiers" xmlns:ns4="http://x-road.eu/xsd/xroad.xsd" xmlns:ns5="http://www.riik.ee/schemas/deccontainer/vers_2_1/">
         <ns3:xRoadInstance>ee</ns3:xRoadInstance>
         <ns3:memberClass>GOV</ns3:memberClass>
         <ns3:memberCode>70006317</ns3:memberCode>
         <ns3:subsystemCode>dhl</ns3:subsystemCode>
         <ns3:serviceCode>sendDocuments</ns3:serviceCode>
         <ns3:serviceVersion>v4</ns3:serviceVersion>
      </ns4:service>
   </SOAP-ENV:Header>
   <SOAP-ENV:Body>
      <ns4:sendDocumentsResponse xmlns:ns10="http://x-road.eu/xsd/identifiers" xmlns:ns11="http://x-road.eu/xsd/representation.xsd" xmlns:ns12="http://x-road.eu/xsd/xroad.xsd" xmlns:ns2="http://www.riik.ee/schemas/deccontainer/vers_2_1/" xmlns:ns4="http://producers.dhl.xrd.riik.ee/producer/dhl" xmlns:ns5="http://www.riik.ee/schemas/dhl" xmlns:ns6="http://www.sk.ee/DigiDoc/v1.3.0#" xmlns:ns7="http://www.w3.org/2000/09/xmldsig#" xmlns:ns8="http://www.riik.ee/schemas/dhl-meta-automatic" xmlns:ns9="http://dhx.x-road.eu/producer">
         <keha href=" cid:8eadbe72-47ee-4eba-a68e-4dac1aca6e85"/>
      </ns4:sendDocumentsResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
Vastuse manus (gzip ja base64 kodeeritult):
```xml
H4sIAAAAAAAAAIWSwXKDIBCG732KjD3LgjZtdIy55D0cBraRSQQHMPL4pZkq9ZD2Buz37/67S3MKw213R+uU0ceMEZqd2pdGu7K+Ys93Mapdrd3bMeu9H2uAeZ6JVepKEMGJHgfuQPa3bEUPKyr7QEJuDZcEJxitkZNAm0hGVzRhwUmwOFp0qD330RaJT0lUrpoloSOxPglWrr6WyNYYY0/KhcdlU2W/6dc9sp7VRZ2NgDsjJaGvCX7fwHNJjL1AQSkFWkGEpFOXX3jx9yxRCBM7Vzr6/15MV3QMkvrjv03kA3qe88mbIY5PJGX1pH0l46TVp4q1sraJGTol233VwM+xgeU7tF8IZ/XFLwIAAA==
```

Vastuse manus XML-ina lahti kodeeritud:
```xml
<ns3:keha xmlns:ns4="http://www.riik.ee/schemas/dhl" xmlns:ns8="http://dhx.x-road.eu/producer" 
  xmlns:ns10="http://x-road.eu/xsd/representation.xsd" 
  xmlns:ns3="http://producers.dhl.xrd.riik.ee/producer/dhl" 
  xmlns:ns11="http://x-road.eu/xsd/xroad.xsd" 
  xmlns:ns5="http://www.sk.ee/DigiDoc/v1.3.0#" xmlns:ns6="http://www.w3.org/2000/09/xmldsig#" 
  xmlns:ns2="http://www.riik.ee/schemas/deccontainer/vers_2_1/" 
  xmlns:ns7="http://www.riik.ee/schemas/dhl-meta-automatic" 
  xmlns:ns9="http://x-road.eu/xsd/identifiers">
 <dhl_id>59</dhl_id>
</ns3:keha>
```
Märkused vastuse sisu kohta:
* DHX adapterserver võttis dokumendi vastu ja vastas SOAP päringule koheselt. Dokumendi DHX adresaadile edastamine toimub asünkroonselt.
* Vastus sisaldab DHX adapterserveri poolt genereeritud unikaalset `<dhl_id>` väärtust. See väärtus algab 1-st (DHX adapterserveri kasutusel võtmisel). Kui DHS süsteem kolib DVK X-tee liideselt üle DHX adapterserverile, siis peab ta arvestama et see võib kattuda vanade DVKsse saadetud dokumentide `<dhl_id>` väärtusega (kui näiteks see salvestatakse DHS andmebaasis unikaaalsele väljale, siis võib olla vajalik teatud andmesiire).    
* `<dhl_id>` väärtust tuleb hiljem kasutada `getSendStatus` päringu sisendis, saatmise staatuse küsimiseks.
 
Märkused vana DVK X-tee liidese kasutajale:
> Võrreldes DVK sendDocuments liidestega on dhx-adpater-serveris realiseeritud on ainult sendDocuments operatsioonide [v4](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv4) versioon, mis eeldab et dokumendi Kapsel on 2.1 formaadis.
>
> Vanemaid DVK sendDocuments versioone [v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv1), [v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv2), [v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv3) dhx-adpater-server ei paku.

Vaata ka `sendDocuments.v4` saatmise näidet dokumendist Testilood - [2.1. Õige kapsli saatmine](adapter-server-testilood.md#2.1).

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

* Väljastatavad veateated (SOAP:Fault) on teistsugused kui DVK korral.

## 6. Vahendajana saatmine/vastuvõtmine

Asutus võib DHX adpaterserverit kasutada [DHX vahendamiseks](https://e-gov.github.io/DHX/#6-vahendamine). 



 
