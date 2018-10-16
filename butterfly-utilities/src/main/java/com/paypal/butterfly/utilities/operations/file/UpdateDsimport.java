package com.paypal.butterfly.utilities.operations.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.paypal.butterfly.extensions.api.ExecutionResult;
import com.paypal.butterfly.extensions.api.TOExecutionResult;
import com.paypal.butterfly.extensions.api.TransformationContext;
import com.paypal.butterfly.extensions.api.TransformationOperation;
import com.paypal.butterfly.extensions.api.exception.TransformationDefinitionException;

public class UpdateDsimport extends TransformationOperation<UpdateDsimport> {

    private static final String DESCRIPTION = "Analyze %s and %s and modify dsimport.xml accordingly";

    private URL dsimportFileUrl;
    private URL siconfigFileUrl;
    
    public UpdateDsimport() {
		// TODO Auto-generated constructor stub
	}
    
    public UpdateDsimport(String dsimport, String siconfig) {
        try {
            setDsimportFileUrl(new URL(dsimport));
            setSiconfigFileUrl(new URL(siconfig));
        } catch (MalformedURLException e) {
            throw new TransformationDefinitionException("Malformed file URL", e);
        }
    }

    public UpdateDsimport setDsimportFileUrl(URL fileUrl) {
        this.dsimportFileUrl = fileUrl;
        return this;
    }

    public UpdateDsimport setSiconfigFileUrl(URL fileUrl) {
        this.siconfigFileUrl = fileUrl;
        return this;
    }

    
	@Override
	protected TOExecutionResult execution(File transformedAppFolder, TransformationContext arg1) {
		// get hashmap for siconfig
		// get hashmap for dsimport
		// modify dsimport based on information in dsimport hash and siconfig hash
		return null;
	}

	@Override
	public String getDescription() {
        return String.format(DESCRIPTION, dsimportFileUrl.getFile(), siconfigFileUrl.getFile());
    }
	
}
