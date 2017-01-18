![](EL_Regionaalarengu_Fond_horisontaalne.jpg)


# DHX adapterserveri haldusjuhend

![](DHX.PNG)  ![](X-ROAD.PNG)

## Sissejuhatus

DHX adapterserver on tarkvara, mis hõlbustab [DHX](https://e-gov.github.io/DHX/) dokumendivahetuse protokolli kasutusele võtmist.

DHX adapterserveri toimimise loogika on kirjeldatud [DHX adapterserveri kasutusjuhendis](adapter-server-kasutusjuhend.md).

DHX adapterserveri paigaldamine on kirjeldatud [DHX adapterserveri paigaldusjuhendis](adapter-server-paigaldusjuhend.md).

## Monitooring

### Monitooringu liidesed

https://tomcat.apache.org/tomcat-8.0-doc/monitoring.html


### Logimine

## Edastamise vigade uurimimine

Kõige lihtsam on alustada uurimist andmebaasist. Andmebaasi mudel on järgmine:

![](dhx-adapter-database.png)

Tabelite kirjeldused:
* DOKUMENT - sisaldab dokumendi andmeid. Väljal SISU salvestatakse faili nimi (c:\dhs_docs\ kataloogis).
* 