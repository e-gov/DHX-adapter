package ee.ria.dhx.ws.service.impl;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.ws.service.AddressService;

import lombok.extern.slf4j.Slf4j;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class AddressServiceImplSpyProvider {

  public static AddressService getAddressServiceSpy(AddressService addressService)
      throws IOException, DhxException {
    AddressService adressServiceSpy = Mockito.spy(addressService);
    return getAddressServiceSpy(adressServiceSpy, "shared-params.xml");
  }

  public static AddressService getAddressServiceSpy(AddressService addressService,
      final String shredParamFilename) throws IOException, DhxException {
    
    log.debug("AddressServiceImplSpyProvider.AddressService shredParamFilename: {}", shredParamFilename);
    
    AddressServiceImpl addressServiceImpl = (AddressServiceImpl) addressService;
    Mockito.doAnswer(new Answer() {
      public Object answer(InvocationOnMock invocation) {
        try {
          InputStream stream =
              new FileInputStream(new ClassPathResource(shredParamFilename).getFile());
          
          log.debug("AddressServiceImplSpyProvider.AddressService from file");
          return stream;
        } catch (IOException ex) {
          log.error(ex.getMessage(), ex);
        }
        return null;
      }
    }).when(addressServiceImpl).getGlobalConfStream(Mockito.any(String.class));
    // Mockito.doReturn(stream)
    return addressService;
  }

}
