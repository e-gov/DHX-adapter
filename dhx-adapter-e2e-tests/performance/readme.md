#Koormustestid
##Testandmed
Enne testide käivitamist on vaja genereerida DHX sendDocument failid:
```shell script
cd dhx-adapter-e2e-tests/performance

head -c 100KB < /dev/urandom | gzip | base64 --wrap=0 > 100kb.base64
head -c 10MB < /dev/urandom | gzip | base64 --wrap=0 > 10mb.base64
head -c 20MB < /dev/urandom | gzip | base64 --wrap=0 > 20mb.base64
head -c 50MB < /dev/urandom | gzip | base64 --wrap=0 > 50mb.base64
head -c 100MB < /dev/urandom | gzip | base64 --wrap=0 > 100mb.base64
head -c 200MB < /dev/urandom | gzip | base64 --wrap=0 > 200mb.base64
```

ja DVK sendDocuments attachmendid:

`dhx-adapter-e2e-tests/src/test/groovy/ee/ria/dhx/Performance.groovy` testiga 'Generate #kapselFile attachment file for DVK request in performance tests'

##Keskkond
Käivitamiseks:
Kuna Wiremock ei suuda suuri faile edastada tuleb `dhx-adapter-e2e-tests/conf/tomcat/dhx-application-test.properties` failis muudatus 
```
#soap.security-server=http://xroad-mock:8080
soap.security-server=http://nginx
```
Seejärel:
```shell script
docker-compose -f docker-compose-performance.yml up -d nginx
``` 
Peatamiseks:
```shell script
docker-compose -f docker-compose-performance.yml down -v
``` 

##Testide käivitamine
Alla laadida JMeteri viimane versioon(5.1.1 testide loomise hetkel)
Enne testide käivitamist anda JMeterile rohkem mälu luues jmeter/bin kaustas faili setenv.sh: 
```shell script
export HEAP="-Xms16g -Xmx16g -XX:MaxMetaspaceSize=256m"
```
DHX testide käivitamisel võib kohe genereerida dashboardi:
```shell script
cd dhx-adapter-e2e-tests/performance
~/bin/jmeter -n -t dhx.jmx -Jnum_threads_small=30 -Jnum_threads_medium=10 -Jnum_threads_large=1 -Jduration=600 -Jrampup_period=10 -e -o results -l summary.jtl
```

DVK koormustestid vajavad kõigepealt summary.jtl faili genereerimist
```shell script
~/bin/jmeter -n -t dhx.jmx -Jdelay=10000 -Jnum_threads_small=30 -Jnum_threads_medium=10 -Jnum_threads_large=1 -Jduration=600 -Jrampup_period=10 -l summary_dvk.jtl
```
Seejärel lisada andmebaasist saatmise aeg JMeteri tulemustesse `dhx-adapter-e2e-tests/src/test/groovy/ee/ria/dhx/Performance.groovy` testiga 'Add DVK samplers to JMeter summary where vastuvotja #vastuvotja' 
ja genereerida JMeter dashboard
```shell script
~/bin/jmeter -g summary_dvk.jtl -o dvk_results
```