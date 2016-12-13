package ee.ria.dhx.ws.service.impl;

import static org.junit.Assert.assertEquals;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecSender;
import ee.ria.dhx.ws.config.CapsuleConfig;

import org.junit.Before;
import org.junit.Test;

import java.util.List;



public class CapsuleConfigTest {

  CapsuleConfig capsuleConfig;

  @Before
  public void init() {
    capsuleConfig = new CapsuleConfig();
  }

  @Test
  public void getAdresseesFromContainer() throws DhxException {
    DecContainer container = new DecContainer();
    container.setTransport(new DecContainer.Transport());
    DecRecipient recipient = new DecRecipient();
    recipient.setOrganisationCode("test1");
    container.getTransport().getDecRecipient().add(recipient);
    recipient = new DecRecipient();
    recipient.setOrganisationCode("test2");
    container.getTransport().getDecRecipient().add(recipient);
    List<CapsuleAdressee> adressees = capsuleConfig.getAdresseesFromContainer(container);
    assertEquals(2, adressees.size());
    assertEquals("test1", adressees.get(0).getAdresseeCode());
    assertEquals("test2", adressees.get(1).getAdresseeCode());
  }

  @Test
  public void getSenderFromContainer() throws DhxException {
    DecContainer container = new DecContainer();
    container.setTransport(new DecContainer.Transport());
    DecSender sender = new DecSender();
    sender.setOrganisationCode("test1");
    container.getTransport().setDecSender(sender);
    CapsuleAdressee adressee = capsuleConfig.getSenderFromContainer(container);
    assertEquals("test1", adressee.getAdresseeCode());

  }



}
