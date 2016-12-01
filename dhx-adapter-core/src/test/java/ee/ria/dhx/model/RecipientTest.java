package ee.ria.dhx.model;

import ee.ria.dhx.types.DhxOrganisation;

import org.junit.Test;

import static org.junit.Assert.*;

public class RecipientTest {
  
  
  @Test
  public void testEquals () {
    DhxOrganisation first = new DhxOrganisation ("code", "system", "DHX");
    DhxOrganisation second = new DhxOrganisation ("code", "system", "DHX");
    assertTrue(first.equals(second));
    assertTrue(second.equals(first));
    
    second.setSystem("DHX.system");   
    assertTrue(first.equals(second));
    assertTrue(second.equals(first));
    
    first.setSystem("DHX.system");
    assertTrue(first.equals(second));
    assertTrue(second.equals(first));
    
    first.setSystem("TEST");
    assertFalse(first.equals(second));
    assertFalse(second.equals(first));
    
    first.setSystem(null);
    assertFalse(first.equals(second));
    assertFalse(second.equals(first));
    
    second.setSystem(null);
    assertTrue(first.equals(second));
    assertTrue(second.equals(first));
  }
  
  @Test
  public void testEqualsToCapsuleRecipient () {
    DhxOrganisation first = new DhxOrganisation ("code", "system", "DHX");
    String capsuleRecipient = "code";
    
    assertTrue(first.equalsToCapsuleOrganisation(capsuleRecipient));
   
    capsuleRecipient = "system";
    assertTrue(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    capsuleRecipient = "DHX.system";
    assertTrue(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    capsuleRecipient = "system.code";
    assertTrue(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    capsuleRecipient = "DHX.system.code";
    assertTrue(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    
    capsuleRecipient = "code1";
    assertFalse(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    capsuleRecipient = "system1";
    assertFalse(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    capsuleRecipient = "DHX.system1";
    assertFalse(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    capsuleRecipient = "system.code1";
    assertFalse(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    capsuleRecipient = "DHX.system1.code";
    assertFalse(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    first.setSystem(null);
    capsuleRecipient = "code";
    assertTrue(first.equalsToCapsuleOrganisation(capsuleRecipient));
    
    capsuleRecipient = "DHX.code";
    assertTrue(first.equalsToCapsuleOrganisation(capsuleRecipient));
  }

}
