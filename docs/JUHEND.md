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

DHS-iga otse liidestamiseks tuleb kasutada 2 esimest teeki **dhx-adapter-core** ja **dhx-adapter-ws**.

**dhx-adapter-server** on vajalik ainult neile, kes ei soovi kasutada otse liidestust, vaid plaanivad paigaldada vahepealse puhverserveri, selleks et kasutada edasi vana DVK SOAP liidesele sarnast liidest.

##Välised sõltuvused ja baasplatvorm

DHX adapteri teekide kasutamisel tuleb arvestada et DHX adapter sõltub allpool toodud komponentidest.

Kompileerimiseks ja käivitamiseks on vajalik [Java SE](https://en.wikipedia.org/wiki/Java_Platform,_Standard_Edition) 1.7 (või uuem) versioon.

Kuna DHX adapteri teek pakub väljapoole veebiteenust (ei ole ainult teenuse klient), siis sõltutakse J2EE [Java Servlet API](https://en.wikipedia.org/wiki/Java_servlet) teegist, läbi [Spring Web Services](https://en.wikipedia.org/wiki/Java_servlet) mooduli).

XML töötluseks kasutatakse [Java Architecture for XML Binding - JAXB](https://docs.oracle.com/javase/7/docs/api/javax/xml/bind/package-summary.html) teeki, mis on Java SE 7 osa.

DHX adapteri Java teek baseerub Spring Framework arhitektuuril, kasutades selle mooduleid:
- Häälestamiseks ja laadimiseks (Spring AOP, Spring Context, jne)
- HTTP SOAP kliendina päringute tegemiseks (Spring WS Client, Apache HttpClient)
- HTTP SOAP veebiteenuse pakkumiseks (Spring WS Server Endpoint, Java Servlet API)

DHX adapteri otsesed ja kaudsed välised sõltuvused on järgmised:

Grupp | Moodul | Versioon | Märkused
------------ | ------------- | ------------- | -------------
org.springframework | spring-core | 4.3.3.RELEASE | Spring Core
org.springframework | spring-aop | 4.2.7.RELEASE | Spring AOP
org.springframework | spring-beans | 4.2.7.RELEASE | Spring Beans







 

