package ee.ria.dhx.model;

import ee.ria.dhx.types.DhxRecipient;

import org.junit.Test;

import static org.junit.Assert.*;

public class RecipientTest {
  
  
  @Test
  public void testEquals () {
    DhxRecipient first = new DhxRecipient ("code", "system");
    DhxRecipient second = new DhxRecipient ("code", "system");
    assertTrue(first.equals(second, "DHX"));
    assertTrue(second.equals(first, "DHX"));
    
    second.setSystem("DHX.system");   
    assertTrue(first.equals(second, "DHX"));
    assertTrue(second.equals(first, "DHX"));
    
    first.setSystem("DHX.system");
    assertTrue(first.equals(second, "DHX"));
    assertTrue(second.equals(first, "DHX"));
    
    first.setSystem("TEST");
    assertFalse(first.equals(second, "DHX"));
    assertFalse(second.equals(first, "DHX"));
    
    first.setSystem(null);
    assertFalse(first.equals(second, "DHX"));
    assertFalse(second.equals(first, "DHX"));
    
    second.setSystem(null);
    assertTrue(first.equals(second, "DHX"));
    assertTrue(second.equals(first, "DHX"));
  }
  
  @Test
  public void testEqualsToCapsuleRecipient () {
    DhxRecipient first = new DhxRecipient ("code", "system");
    String capsuleRecipient = "code";
    
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
   
    capsuleRecipient = "system";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    capsuleRecipient = "DHX.system";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    capsuleRecipient = "system.code";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    capsuleRecipient = "DHX.system.code";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    
    capsuleRecipient = "code1";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    capsuleRecipient = "system1";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    capsuleRecipient = "DHX.system1";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    capsuleRecipient = "system.code1";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    capsuleRecipient = "DHX.system1.code";
    assertFalse(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    first.setSystem(null);
    capsuleRecipient = "code";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
    
    capsuleRecipient = "DHX.code";
    assertTrue(first.equalsToCapsuleRecipient(capsuleRecipient, "DHX"));
  }

}
