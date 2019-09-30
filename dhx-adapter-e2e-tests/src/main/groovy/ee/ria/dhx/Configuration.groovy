package ee.ria.dhx

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated


@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "dhx")
@Validated
class Configuration {
    final Hobekuul hobekuul = new Hobekuul();
    final Test test = new Test();
    public static class Hobekuul {
        public String dhxServiceUrl

        String getDhxServiceUrl() {
            return dhxServiceUrl
        }

        void setDhxServiceUrl(String dhxServiceUrl) {
            println "Setting Hobekuul DHX Service URL: " + (System.getenv("DHX_ADAPTER_HOBEKUUL_DHX_URL") ?: dhxServiceUrl)
            this.dhxServiceUrl = System.getenv("DHX_ADAPTER_HOBEKUUL_DHX_URL") ?: dhxServiceUrl
        }

        public String healthUrl

        String getHealthUrl() {
            return healthUrl
        }

        void setHealthUrl(String healthUrl) {
            println "Setting Hobekuul health URL: " + (System.getenv("DHX_ADAPTER_HOBEKUUL_HEALTH_URL") ?: healthUrl)
            this.healthUrl = System.getenv("DHX_ADAPTER_HOBEKUUL_HEALTH_URL") ?: healthUrl
        }
    }

    public static class Test {
        public String dhxServiceUrl

        String getServiceUrl() {
            return dhxServiceUrl
        }

        void setDhxServiceUrl(String dhxServiceUrl) {
            println "Setting Test DHX Service URL: " + (System.getenv("DHX_ADAPTER_TEST_DHX_URL") ?: dhxServiceUrl)
            this.dhxServiceUrl = System.getenv("DHX_ADAPTER_TEST_DHX_URL") ?: dhxServiceUrl
        }

        public String dvkServiceUrl

        String getDvkServiceUrl() {
            return dvkServiceUrl
        }

        void setDvkServiceUrl(String dhxServiceUrl) {
            println "Setting Test DVK Service URL: " + (System.getenv("DHX_ADAPTER_TEST_DVK_URL") ?: dhxServiceUrl)
            this.dvkServiceUrl = System.getenv("DHX_ADAPTER_TEST_DVK_URL") ?: dhxServiceUrl
        }

        public String healthUrl

        String getHealthUrl() {
            return healthUrl
        }

        void setHealthUrl(String healthUrl) {
            println "Setting Test Health URL: " + (System.getenv("DHX_ADAPTER_TEST_HEALTH_URL") ?: healthUrl)
            this.healthUrl = System.getenv("DHX_ADAPTER_TEST_HEALTH_URL") ?: healthUrl
        }

        public String metricsUrl

        String getMetricsUrl() {
            return metricsUrl
        }

        void setMetricsUrl(String metricsUrl) {
            println "Setting Test Metrics URL: " + (System.getenv("DHX_ADAPTER_TEST_METRICS_URL") ?: metricsUrl)
            this.metricsUrl = System.getenv("DHX_ADAPTER_TEST_METRICS_URL") ?: metricsUrl
        }
    }


}
