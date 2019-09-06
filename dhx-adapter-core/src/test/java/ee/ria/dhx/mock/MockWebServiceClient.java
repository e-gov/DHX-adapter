//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package ee.ria.dhx.mock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.test.server.RequestCreator;
import org.springframework.ws.test.server.ResponseActions;
import org.springframework.ws.test.server.ResponseMatcher;
import org.springframework.ws.test.support.AssertionErrors;
import org.springframework.ws.transport.WebServiceMessageReceiver;

import java.io.IOException;

public class MockWebServiceClient {
    private static final Log logger = LogFactory.getLog(MockWebServiceClient.class);
    private final WebServiceMessageReceiver messageReceiver;
    private final WebServiceMessageFactory messageFactory;

    private MockWebServiceClient(WebServiceMessageReceiver messageReceiver, WebServiceMessageFactory messageFactory) {
        Assert.notNull(messageReceiver, "'messageReceiver' must not be null");
        Assert.notNull(messageFactory, "'messageFactory' must not be null");
        this.messageReceiver = messageReceiver;
        this.messageFactory = messageFactory;
    }

    public static MockWebServiceClient createClient(WebServiceMessageReceiver messageReceiver, WebServiceMessageFactory messageFactory) {
        return new MockWebServiceClient(messageReceiver, messageFactory);
    }

    public static MockWebServiceClient createClient(ApplicationContext applicationContext) {
        Assert.notNull(applicationContext, "'applicationContext' must not be null");
        MockStrategiesHelper strategiesHelper = new MockStrategiesHelper(applicationContext);
        WebServiceMessageReceiver messageReceiver = (WebServiceMessageReceiver)strategiesHelper.getStrategy(WebServiceMessageReceiver.class, SoapMessageDispatcher.class);
        WebServiceMessageFactory messageFactory = (WebServiceMessageFactory)strategiesHelper.getStrategy(WebServiceMessageFactory.class, AxiomSoapMessageFactory.class);
        return new MockWebServiceClient(messageReceiver, messageFactory);
    }

    public ResponseActions sendRequest(RequestCreator requestCreator) {
        Assert.notNull(requestCreator, "'requestCreator' must not be null");

        try {
            WebServiceMessage request = requestCreator.createRequest(this.messageFactory);
            MessageContext messageContext = new DefaultMessageContext(request, this.messageFactory);
            this.messageReceiver.receive(messageContext);
            return new MockWebServiceClient.MockWebServiceClientResponseActions(messageContext);
        } catch (Exception var4) {
            logger.error("Could not send request", var4);
            AssertionErrors.fail(var4.getMessage());
            return null;
        }
    }

    private static class MockWebServiceClientResponseActions implements ResponseActions {
        private final MessageContext messageContext;

        private MockWebServiceClientResponseActions(MessageContext messageContext) {
            Assert.notNull(messageContext, "'messageContext' must not be null");
            this.messageContext = messageContext;
        }

        public ResponseActions andExpect(ResponseMatcher responseMatcher) {
            WebServiceMessage request = this.messageContext.getRequest();
            WebServiceMessage response = this.messageContext.getResponse();
            if (response == null) {
                AssertionErrors.fail("No response received");
                return null;
            } else {
                try {
                    responseMatcher.match(request, response);
                    return this;
                } catch (IOException var5) {
                    MockWebServiceClient.logger.error("Could not match request", var5);
                    AssertionErrors.fail(var5.getMessage());
                    return null;
                }
            }
        }
    }
}
