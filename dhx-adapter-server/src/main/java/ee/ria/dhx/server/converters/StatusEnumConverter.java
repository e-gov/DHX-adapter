package ee.ria.dhx.server.converters;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StatusEnumConverter implements Converter<String, StatusEnum> {

    @Override
    public StatusEnum convert(String value) {
        try {
            return StatusEnum.forClassificatorId(Integer.parseInt(value));
        } catch (DhxException ex) {
            log.warn("Could not parse Status from invalid integer string: " + value);
            return null;
        }
    }

}