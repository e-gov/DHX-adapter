package ee.ria.dhx.ws.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.ws.service.AddressService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AddressServiceImplSpyProvider {

	public static AddressService getAddressServiceSpy(AddressService addressService) throws IOException, DhxException {
		AddressService adressServiceSpy = Mockito.spy(addressService);
		AddressServiceImpl addressServiceImpl = (AddressServiceImpl) adressServiceSpy;
		Mockito.doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				try {
					InputStream stream = new FileInputStream(new ClassPathResource("shared-params.xml").getFile());
					return stream;
				} catch (IOException ex) {
					log.error(ex.getMessage(), ex);
				}
				return null;
			}
		}).when(addressServiceImpl).getGlobalConfStream();
		// Mockito.doReturn(stream)
		return adressServiceSpy;
	}

}
