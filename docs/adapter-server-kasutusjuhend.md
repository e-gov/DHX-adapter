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
  
## 3. Sisemine liides

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
> Lisaks tuleb silmas pidada, et esineb mõningaid sisulisi loogika erinevusi võrreldes DVK liidesega. Need on välja toodud [allpool](#erinevused-vana-dvk-liidese-ja-adapterserveri-sisemise-liidese-toimimise-loogikas). 

Järgnevalt kirjeldatakse lühidalt kuidas toimub dhx-adpater-serveri sisemise liidese kasutamine dokumentide saatmiseks ja vastuvõtmiseks. 

### sendDocuments (sisemine liides)

SOAP operatsiooni `sendDocuments.v4` kasutatakse dokumentide saatmiseks teisel asutusele.
Dokumendid peavad olema Kapsli [2.1](https://github.com/e-gov/DHX-adapter/blob/master/dhx-adapter-core/src/main/resources/Dvk_kapsel_vers_2_1_eng_est.xsd) versioonis (vanemad Kapsli versioonid ei ole toetatud).

DHX adapterserver võtab dokumendi vastu, salvestab enda andmebaasi ja vastab SOAP päringule koheselt. 
Dokumendi edasine DHX addresaadile saatmine teostatakse asünkroonselt (taustatöö poolt).
Dokumendi saatmise staatuse küsimiseks tuleb kasutada operatsiooni [getSendStatus]()

Märkused vana DVK X-tee liidese kasutajale:
> Võrreldes DVK sendDocuments liidestega on dhx-adpater-serveris realiseeritud on ainult sendDocuments operatsioonide [v4](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv4) versioon, mis eeldab et dokumendi Kapsel on 2.1 formaadis.
>
> Vanemaid DVK sendDocuments versioone [v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv1), [v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv2), [v3](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#senddocumentsv3) dhx-adpater-server ei paku.



### getSendStatus (sisemine liides)

SOAP operatsiooni `getSendStatus.v2` kasutatakse saadetud dokumendi staatuse ja saatmisel ilmnenud vea info (fault) küsimiseks.
Võimalikud staatused on:
* `saatmisel` – dokumenti üritatakse veel antud saajale edastada
* `saadetud` – dokument sai edukalt antud saajale saadetud
* `katkestatud` – dokumenti ei õnnestunud antud saajale saata.

Staatuste kohta vaata täpselt [DVK dokumentatsioonist](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#edastatud-dokumentide-staatuse-kontroll).

Märkused vana DVK X-tee liidese kasutajale:
> Võrreldes DVK getSendStatus liidestega on dhx-adpater-serveris realiseeritud on ainult getSendStatus operatsioonide [v2](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendstatusv2) versioon.
>
> Vanemat DVK getSendStatus versiooni [v1](https://github.com/e-gov/DVK/blob/master/doc/DVKspek.md#getsendstatusv1) dhx-adpater-server ei paku.


## 4. Erinevused DVK liidesega võrreldes

* 

* SWAREF manuse cid väärtus peab olema URL kodeeritud (DVK korral see ei tohtinud olla URL kodeeritud)

## Vahendajana saatmine/vastuvõtmine

Asutus võib DHX adpaterserverit kasutada [DHX vahendamiseks](https://e-gov.github.io/DHX/#6-vahendamine). 



 
