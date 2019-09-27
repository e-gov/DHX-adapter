//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package ee.ria.dhx.mock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

public class MockStrategiesHelper extends org.springframework.ws.test.support.MockStrategiesHelper {
    private static final Log logger = LogFactory.getLog(MockStrategiesHelper.class);
    private final ConfigurableListableBeanFactory beanFactory;
    private ApplicationContext applicationContext;

    public MockStrategiesHelper(ApplicationContext applicationContext) {
        super(applicationContext);
        this.applicationContext = applicationContext;
        this.beanFactory = ((ConfigurableApplicationContext) this.applicationContext).getBeanFactory();
    }

    @Override
    public <T> T getStrategy(Class<T> type) {
        Assert.notNull(type, "'type' must not be null");
        Map<String, T> map = this.applicationContext.getBeansOfType(type);
        if (map.isEmpty()) {
            return null;
        } else if (map.size() == 1) {
            Entry<String, T> entry = (Entry)map.entrySet().iterator().next();
            if (logger.isDebugEnabled()) {
                logger.debug("Using " + ClassUtils.getShortName(type) + " [" + (String)entry.getKey() + "]");
            }

            return entry.getValue();
        }

        if (this.applicationContext instanceof ConfigurableApplicationContext) {
            String[] beanNamesOfType = this.applicationContext.getBeanNamesForType(type);
            for (String beanName : beanNamesOfType) {
                if (isPrimaryBean(beanName)) {
                    return (T) this.applicationContext.getBean(beanName);
                }
            }
        }

        throw new BeanInitializationException("Could not find exactly 1 " + ClassUtils.getShortName(type) + " in application context");
    }

    private boolean isPrimaryBean(String beanName) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
            StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
            Method method = metadata.getIntrospectedMethod();
            return method.isAnnotationPresent(Primary.class);
        }
        return false;
    }
}
