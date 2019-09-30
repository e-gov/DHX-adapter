package ee.ria.dhx

import groovy.sql.GroovyRowResult
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Unroll

@ContextConfiguration(
        classes = [TestConfiguration.class],
        initializers = ConfigFileApplicationContextInitializer.class)
class Performance extends Spec {

    @Unroll
    @Ignore
    def "Generate #kapselFile attachment file for DVK request in performance tests "() {
        //Precondition: generate files with required size: head -c 10MB < /dev/urandom | gzip | base64 --wrap=0 > 10mb.base64
        given:
        String basePath = 'performance/'
        File inFile = new File(basePath + testFile)
        File outFile = new File(basePath + kapselFile)
        outFile.text = DVK.generateAttachment(Steps.getKapsel('10391131', recipient, inFile.text))
        where:
        testFile       | kapselFile            | sender     | recipient
        '100kb.base64' | 'kapsel_100KB.base64' | '10391131' | '70000001'
        '10mb.base64'  | 'kapsel_10MB.base64'  | '10391131' | '70000002'
        '20mb.base64'  | 'kapsel_20MB.base64'  | '10391131' | '70000002'
        '50mb.base64'  | 'kapsel_50MB.base64'  | '10391131' | '70000002'
        '100mb.base64' | 'kapsel_100MB.base64' | '10391131' | '70000003'
        '200mb.base64' | 'kapsel_200MB.base64' | '10391131' | '70000003'
    }

    @Ignore
    @Unroll
    def "Add DVK samplers to JMeter summary where vastuvotja #vastuvotja"() {

        given:
        File results = new File("performance/summary_pr_dvk_1g.jtl")
        postgresDb.db.rows("select saatmise_algus, saatmise_lopp,staatus_id from vastuvotja where asutus_id = (select asutus_id from asutus where registrikood = ?);", vastuvotja).each { row ->
            println row
            convertRowToJMeterResult(results, row, name)
        }
        where:
        vastuvotja | name
        '70000001' | 'DHX sendDocument - 100KB file'
        '70000002' | 'DHX sendDocument - 10MB file'
        '70000003' | 'DHX sendDocument - 100MB file'
    }

    def convertRowToJMeterResult(File file, GroovyRowResult row, String name) {
        //timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
        //1569328032879,2028,DHX sendDocument - 100KB file,200,,Small Files Thread Group 1-1,text,true,,2608,139998,7,10,http://localhost:8081/dhx-adapter-server/ws,2027,0,54
        def elapsed = 0
        if (row['saatmise_lopp']) {
            elapsed = row['saatmise_lopp'].getTime() - row['saatmise_algus'].getTime()
        }
        def returnCode = (row['staatus_id'] == 102) ? "200" : "500"
        def success = (row['staatus_id'] == 102) ? "true" : "false"
        def resultName = (success == 'true') ? name : name + ' - failed'
        file.append("${row['saatmise_algus'].time + 3 * 60 * 60 * 1000},${elapsed},${resultName},${returnCode},,DHX Thread Group 1-1,text,${success},,0,0,1,1,http://turvaserver/cgi-bin/proxy,${elapsed},0,0\n")
    }

}
