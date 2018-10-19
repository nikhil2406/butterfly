package com.paypal.butterfly.utilities.operations.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.testng.annotations.Test;

import com.paypal.butterfly.extensions.api.TOExecutionResult;
import com.paypal.butterfly.extensions.api.exception.TransformationDefinitionException;
import com.paypal.butterfly.extensions.api.exception.TransformationOperationException;
import com.paypal.butterfly.utilities.TransformationUtilityTestHelper;

public class UpdateDsimportTest extends TransformationUtilityTestHelper {
    @Test
    public void test() {
    	File dsimportFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-1/dsimport.xml");
    	assertTrue(dsimportFile.exists());
    	File siconfigFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-1/SocketInterceptorConfig.xml");
    	assertTrue(siconfigFile.exists());

        UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");

        TOExecutionResult executionResult = updateDsimportXform.execution(transformedAppFolder, transformationContext);
        System.out.println("execution result:" + executionResult.getDetails());
        assertEquals(executionResult.getType(), TOExecutionResult.Type.SUCCESS);
    }

    @Test
    public void test_blank_file_siconfig() {
    	File dsimportFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-1/dsimport.xml");
    	assertTrue(dsimportFile.exists());
    	File siconfigFile = new File("");

        UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");

        TOExecutionResult executionResult = updateDsimportXform.execution(transformedAppFolder, transformationContext);
        System.out.println("execution result:" + executionResult.getDetails());
        assertEquals(executionResult.getType(), TOExecutionResult.Type.SUCCESS);
    }

    @Test(expectedExceptions = TransformationDefinitionException.class, expectedExceptionsMessageRegExp = "siconfig File cannot be null")
    public void test_null_file_siconfig() {
    	File dsimportFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-1/dsimport.xml");
    	assertTrue(dsimportFile.exists());
    	File siconfigFile = null;

        UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");
    }

    @Test
    public void test_blank_file_dsimport() {
    	File dsimportFile = new File("");
    	File siconfigFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-1/SocketInterceptorConfig.xml");
    	assertTrue(siconfigFile.exists());

        UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");

        TOExecutionResult executionResult = updateDsimportXform.execution(transformedAppFolder, transformationContext);
        System.out.println("execution result:" + executionResult.getDetails() + ":" + executionResult.getException().getMessage());
        assertEquals(executionResult.getType(), TOExecutionResult.Type.ERROR);
        assertEquals(executionResult.getException().getClass(), TransformationOperationException.class);
        assertEquals(executionResult.getException().getMessage(), 
        		"File content could not be parsed properly. Content is not allowed in prolog.");
    }

    @Test
    public void test_wrong_number_value_siconfig() {
    	File dsimportFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-2/dsimport.xml");
    	File siconfigFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-2/SocketInterceptorConfig.xml");
    	assertTrue(siconfigFile.exists());

        UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");

        TOExecutionResult executionResult = updateDsimportXform.execution(transformedAppFolder, transformationContext);
        System.out.println("execution result:" + executionResult.getDetails() + ":" + executionResult.getException().getMessage());
        assertEquals(executionResult.getType(), TOExecutionResult.Type.ERROR);
        assertEquals(executionResult.getException().getClass(), NumberFormatException.class);
        assertEquals(executionResult.getException().getMessage(), "For input string: \"5a\"");
    }

    @Test
    public void test_no_attribute_in_siconfig() {
    	File dsimportFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-3/dsimport.xml");
    	File siconfigFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-3/SocketInterceptorConfig.xml");
    	assertTrue(siconfigFile.exists());

        UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");

        TOExecutionResult executionResult = updateDsimportXform.execution(transformedAppFolder, transformationContext);
        System.out.println("execution result:" + executionResult.getDetails());
        assertEquals(executionResult.getType(), TOExecutionResult.Type.SUCCESS);
    }

  @Test
  public void test_no_datasource_in_dsimport() {
  	File dsimportFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-4/dsimport.xml");
  	File siconfigFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-4/SocketInterceptorConfig.xml");
  	assertTrue(siconfigFile.exists());

      UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");

      TOExecutionResult executionResult = updateDsimportXform.execution(transformedAppFolder, transformationContext);
      System.out.println("execution result:" + executionResult.getDetails());
      assertEquals(executionResult.getType(), TOExecutionResult.Type.SUCCESS);
  }

  @Test
  public void test_commented_config_prop_in_dsimport() {
  	File dsimportFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-5/dsimport.xml");
  	File siconfigFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-5/SocketInterceptorConfig.xml");
  	assertTrue(siconfigFile.exists());

      UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");

      TOExecutionResult executionResult = updateDsimportXform.execution(transformedAppFolder, transformationContext);
      System.out.println("execution result:" + executionResult.getDetails());
      assertEquals(executionResult.getType(), TOExecutionResult.Type.SUCCESS);
  }

  @Test
  public void test_wrong_xml_siconfig() {
	File dsimportFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-6/dsimport.xml");
	File siconfigFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-6/SocketInterceptorConfig.xml");
	assertTrue(siconfigFile.exists());
	
	UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");
	
	TOExecutionResult executionResult = updateDsimportXform.execution(transformedAppFolder, transformationContext);
	System.out.println("execution result:" + executionResult.getDetails());
	assertEquals(executionResult.getType(), TOExecutionResult.Type.SUCCESS);
  }

  @Test
  public void test_wrong_xml_dsimport() {
	File dsimportFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-7/dsimport.xml");
	File siconfigFile = new File(transformedAppFolder, "src/main/resources/updateDsimportData/test-7/SocketInterceptorConfig.xml");
	assertTrue(siconfigFile.exists());
	
	UpdateDsimport updateDsimportXform = new UpdateDsimport(dsimportFile, siconfigFile, "dev");
	
	TOExecutionResult executionResult = updateDsimportXform.execution(transformedAppFolder, transformationContext);
	System.out.println("execution result:" + executionResult.getDetails() + ":" + executionResult.getException().getMessage());
	assertEquals(executionResult.getType(), TOExecutionResult.Type.ERROR);
    assertEquals(executionResult.getException().getClass(), TransformationOperationException.class);
    assertEquals(executionResult.getException().getMessage(), 
    		"File content could not be parsed properly. The element type \"property\" must be terminated by the matching end-tag \"</property>\".");
  }


}
