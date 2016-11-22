![](EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg)

ET | [EN](GUIDE.md)

# DHX-adapteri kasutusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

## Sissejuhatus

DHX adapter on Java teek, milles on realiseeritud dokumendi saatmise, vastuvõtmise ja aadressiraamatu koostamise funktsionaalsus vastavalt DHX protokolli nõuetele.

Antud juhend on mõeldud kasutamiseks tarkvara arendajatele, kes soovivad hakata oma Dokumendihaldussüsteemis (DHS) kasutama DHX protokolli.

DHX adapteri lähtekood asub aadressil https://github.com/e-gov/DHX-adapter

Selles asuvad kolm alamteeki
- **dhx-adapter-core** – selles asuvad klassid XML (Kaplsi) ja SOAP objektide koostamiseks ja töötlemiseks,  vigade klassid ning mõned üldkasutatavad utiliit klassid
- **dhx-adapter-ws** – selles asuvad klassid dokumendi saatmiseks (SOAP client), aadressiraamatu koostamiseks (SOAP client) ja dokumendi vastuvõtmiseks (SOAP Service Endpoint)
- **dhx-adapter-server** – eraldiseisev adapter server (Variant C), mis puhverdab saabunud dokumendid vahe andmebaasis ja pakub vana [DVK liidese]((https://github.com/e-gov/DVK)) sarnaseid SOAP teenuseid


