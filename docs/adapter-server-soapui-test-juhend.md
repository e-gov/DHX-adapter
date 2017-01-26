![](EL_Regionaalarengu_Fond_horisontaalne.jpg)

# SoapUI testide käivitamise juhend

## Sissejuhatus

SoapUI on tarkvara, mis võimaldab lihtsal viisil teha SOAP päringuid. SoapUI saab alla laadida ja  rohkem infot leida on võimalik leheküljel: [https://www.soapui.org/](https://www.soapui.org/).

Käesolev juhend kirjeldab DVK-s DHX protokolliga seotud muudatuste testimiseks väljatöötatud SoapUI testide konfigureerimist ja käivitamist. 

SoapUi testid on koostatud ja nimetatud vastavalt [testilugudele](adapter-server-testilood.md).

## SoapUI testide konfigureerimine.

* Importida SoapUI project.(SoapUI projekti importimise kohta võib rohkem infot  leida [siin](https://www.soapui.org/articles/import-project.html))
  *SoapUi projekt asub [siin](../dhx-adapter-server/tests), fail DHX_server-soapui.xml*

* Muuta DVK_DHX SoapUi projekti parameetrid (SoapUI parameetrite kohta võib rohkem infot leida  [siin](https://www.soapui.org/functional-testing/properties/working-with-properties.html)):


| Parameetri nimi | Näidisväärtus | Kommentaar |
|-------|----------|----------------|
| dvkEndpoint | http://10.0.13.198/cgi-bin/consumer_proxy  | endpoint kuhu tuleb saata DVK päringud. Tavaliselt turvaserveri aadress. |
| dhxEndpoint | http://10.0.13.198/cgi-bin/consumer_proxy  | endpoint kuhu tuleb saata DHX päringud. Tavaliselt turvaserveri aadress. |
| xroadInstance | ee-dev | SOAP headeri Xtee parameetri xroadInstance väärtus |
| dhs2MemberClass | COM | testilugudes kirjeldatud DHS2 Xtee liikme memberClass |
| dhs2MemberCode | 30000001 | testilugudes kirjeldatud DHS2 Xtee liikme memberCode |
| dhs2Subsystem | DHX | testilugudes kirjeldatud DHS2 Xtee liikme subsystemCode |
| dhs2Subsystem2 | subsystem |  testilugudes kirjeldatud DHS2 Xtee liikme alamsüsteemi nimi |
| dhs2RepresenteeCode | 70000001 | testilugudes kirjeldatud DHS2 Xtee liikme poolt vahendatava ettevõtte registrikood |
| dhs3MemberClass | GOV | testilugudes kirjeldatud DHS3 Xtee liikme memberClass |
| dhs3MemberCode | 40000001 | testilugudes kirjeldatud DHS3 Xtee liikme memberCode |
| dhs3Subsystem | DHX | testilugudes kirjeldatud DHS3 Xtee liikme subsystemCode |
| dhs3Subsystem2 | DHX.subsystem | testilugudes kirjeldatud DHS3 Xtee liikme alamsüsteemi nimi |
| goodCapsule | C:\Users\alex\Desktop\xmls/kapsel_21.xml | viide failile, mis sisaldab Elektroonilise andmevahetuse metaandmete loendile 2.1 vastavalt korrektselt kapseldatud faili.|
| badCapsule | C:\Users\alex\Desktop\xmls/kapsel_21_wrong.xml | viide failile, mis sisaldab XML-i, mis ei vasta Elektroonilise andmevahetuse metaandmete loendile 2.1 |
| notCapsule | C:\Users\alex\Desktop\xmls/kapsel_21_not_kapsel.xml | viide failile, mis ei ole XML-vormingus või on XML vales vormingus.  |
| bigCapsule | C:\Users\alex\Desktop\xmls/kapsel_big_21.xml | viide failile (suur fail), mis sisaldab Elektroonilise andmevahetuse metaandmete loendile 2.1 vastavalt korrektselt kapseldatud faili.|
| bigCapsuleDVK | C:\Users\alex\Desktop\xmls/kapsel_big_21_5 dvk.xml | viide failile (suur fail), mis sisaldab Elektroonilise andmevahetuse metaandmete loendile 2.1 vastavalt korrektselt kapseldatud fail. |
| dhs3RepresenteeCode | vahendatav1 | testilugudes kirjeldatud DHS3 Xtee liikme poolt vahendatava ettevõtte registrikood |
| dhs3RepresenteeSubsystem |  | testilugudes kirjeldatud DHS3 Xtee liikme vahendatava alamsüsteemi nimi |
| dhs3RepresenteeCode2 | vahendatav1 | testilugudes kirjeldatud DHS3 Xtee liikme vahendatava registrikood |
| dhs3RepresenteeSubsystem2 | subsytem | testilugudes kirjeldatud DHS3 Xtee liikme vahendatava alamsüsteemi nimi |
| dhs1MemberCode | 70000004 | testilugudes kirjeldatud DHS1 Xtee liikme memberCode |

**Failid asuvad [xmls](xmls) kaustas. Faili viidetega parameetrid(goodCapsule, badCapsule, notCapsule, bigCapsuleDVK) tuleb muuta igas keskkonnas kus teste käivitatakse. Faili viide peab olema absolute path failini.**  

## SoapUI testide käivitamine
Testide struktuuri ja käivitamise kirjeldus on esitatud [siin](https://www.soapui.org/functional-testing/structuring-and-running-tests.html). 

Pärast testide läbimist tuleb veenduda, et kõik testid on õnnestunud.

