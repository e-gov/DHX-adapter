
import java.text.SimpleDateFormat

report {
    enabled true
    logFileDir "target/reports/tests/spock/"
    logFileName "spock-report.json"
    logFileSuffix new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date())
    issueUrlPrefix "https://jira.ria.ee/browse/"
}

runner {
    optimizeRunOrder true
}