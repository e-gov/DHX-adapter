package ee.ria.dhx.ws.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;

import java.io.File;

/**
 *
 * @author Kaarel Raspel
 *
 */
@Configuration
public class DhxWebServiceConfig {

    @Value("${soap.dhx.attachment.cache.threshold:#{null}}")
    @Getter
    Integer attachmentCacheThreshold;

    @Value("${soap.dhx.attachment.cache.dir:#{null}}")
    @Getter
    String attachmentCacheDir;

    /**
     * Creates messageFactory for sending and returns.
     * @return messagefactory
     */
    @Bean
    @Primary
    public SoapMessageFactory axiomSoapMessageFactorySend() {
        return new AxiomSoapMessageFactory() {{
            Integer attachmentCacheThreshold = DhxWebServiceConfig.this.getAttachmentCacheThreshold();
            if (attachmentCacheThreshold != null) {
                setAttachmentCaching(true);
                setAttachmentCacheThreshold(attachmentCacheThreshold /* MB */ * 1024 /* KB */ * 1024 /* Byte */);

                String attachmentCacheDir = DhxWebServiceConfig.this.getAttachmentCacheDir();
                if (attachmentCacheDir != null) {
                    setAttachmentCacheDir(new File(attachmentCacheDir));
                }
            }
        }};
    }

}
