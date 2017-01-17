![](docs/EL_Regionaalarengu_Fond_horisontaalne.jpg)

# DHX-adapter

![](docs/DHX.PNG)  ![](docs/X-ROAD.PNG)

#### ET

Tarkvarakomponendid DHX-i rakendajatele.

DHX adaptertarkvara koosneb kahest osast:

- __DHX Java teegid__ (adapteriteegid) sisaldavad funktsionaalsust dokumendi saatmiseks, vastuvõtmiseks ja aadressiraamatu koostamiseks. DHX-i võimekuse saab luua, lõimides teegid dokumendihaldussüsteemi vm dokumente vahetava süsteemi tarkvarasse;
* [DHX Java teegi kasutusjuhend](docs/java-teegid-kasutusjuhend.md)

- __DHX adapterserver__ on eraldipaigaldatav rakendus, mis X-tee poole räägib DHX protokolli, dokumendihaldussüsteemi poole aga pakub vana DVK (Dokumendivahetuskeskuse) liidesele sarnast SOAP-liidest.
* [DHX adapterserveri kasutusjuhend](docs/adapter-server-kasutusjuhend.md)
* [DHX adapterserveri paigaldusjuhend](docs/adapter-server-paigaldusjuhend.md)



#### EN

Software components for DHX implementors.

DHX Adapter software consists of two parts:

- __DHX Java library__ implements document sending, receiving and local address book functionality. DHX capability can be created by integrating DHX Java library into document management system;
* [DHX Java teegi kasutusjuhend](docs/java-library-usage-guide.md)

- __DHX adapter server__ - separately installed application that towards X-Road talks DHX protocol, and towards document management system provides a SOAP interface similar to old DEC (Document Exchange Centre) interface.
* [DHX adapterserver usage guide (estonian)](docs/adapter-server-kasutusjuhend.md)
* [DHX adapterserveri installation guide (estonian)](docs/adapter-server-paigaldusjuhend.md)


