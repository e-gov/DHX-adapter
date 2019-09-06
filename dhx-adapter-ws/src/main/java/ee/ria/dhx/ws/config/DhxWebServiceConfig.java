package ee.ria.dhx.ws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;

/**
 *
 * @author Kaarel Raspel
 *
 */
@Configuration
public class DhxWebServiceConfig {

    /**
     * Creates messageFactory for sending and returns.
     * @return messagefactory
     */
    @Bean
    @Primary
    public SoapMessageFactory axiomSoapMessageFactorySend() {
        return new AxiomSoapMessageFactory() {{
            setAttachmentCaching(true);
            setAttachmentCacheThreshold(10 /* MB */ * 1024 /* KB */ * 1024 /* Byte */); // TODO: make it configurable
        }};
    }

}
