package com.paypal.butterfly.utilities.operations.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.paypal.butterfly.extensions.api.ExecutionResult;
import com.paypal.butterfly.extensions.api.TOExecutionResult;
import com.paypal.butterfly.extensions.api.TransformationContext;
import com.paypal.butterfly.extensions.api.TransformationOperation;
import com.paypal.butterfly.extensions.api.exception.TransformationDefinitionException;
import com.paypal.butterfly.extensions.api.exception.TransformationOperationException;
import com.paypal.butterfly.extensions.api.exception.TransformationUtilityException;

public class UpdateDsimport extends TransformationOperation<UpdateDsimport> {

    private static final String DESCRIPTION = "Analyze %s and %s and modify dsimport.xml accordingly";

    private File dsimportFile;
    private File siconfigFile;
    private String env;
    private Document dsimportDom;
    
    public UpdateDsimport() {
		// TODO Auto-generated constructor stub
	}
    
    public UpdateDsimport(File dsimport, File siconfig, String environment) {
        setSiconfigFileUrl(siconfig);
        setDsimportFileUrl(dsimport);
        setEnv(environment);
        dsimportDom = null;
    }

    public UpdateDsimport setDsimportFileUrl(File file) {
        checkForNull("dsimport File", file);
        this.dsimportFile = file;
        return this;
    }

    public UpdateDsimport setSiconfigFileUrl(File file) {
        checkForNull("siconfig File", file);
        this.siconfigFile = file;
        return this;
    }

    public void setEnv(String environment) {
        if(environment == null || environment.isEmpty()) {environment = "Production";}
        this.env = environment;
    }

	@Override
	public String getDescription() {
        return String.format(DESCRIPTION, dsimportFile.getName(), siconfigFile.getName());
    }
    
	@Override
	protected TOExecutionResult execution(File transformedAppFolder, TransformationContext arg1) {
		TOExecutionResult result = null;
		try {
			// get hash map for socket intercepter config file
			HashMap<String, String> siConfigMap = loadAndParseSIConfigXml();
			System.out.println("siConfig map:"+ siConfigMap.toString());

			// get hash map for dsimport
			HashMap<String, String> dsimportMap = loadAndParseDsimportXml();
			System.out.println("dsimport map:"+ dsimportMap.toString());

			// modify dsimport based on information in dsimport hash and siconfig hash
			compareAndUpdateDsimportXml(siConfigMap, dsimportMap);

			String details = String.format("File '%s' has been modified", dsimportFile);
	        result = TOExecutionResult.success(this, details);
		} catch (ParserConfigurationException | SAXException | IOException e) {
            result = TOExecutionResult.error(this,
                    new TransformationOperationException("File content could not be parsed properly. " + e.getMessage(), e));
		}  catch (TransformationUtilityException | TransformerException e) {
            result = TOExecutionResult.error(this, e);
		} catch (Exception e) {
            result = TOExecutionResult.error(this, e);
        }
		return result;
	}

	/* get a hash map of 'attribute' element name-value from si config */
	private HashMap<String, String> loadAndParseSIConfigXml() {
        HashMap<String, String> map = new HashMap<String, String>();

        try {
	        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(this.siconfigFile);

            NodeList nList = doc.getElementsByTagName("attribute");
            for (int i = 0; i < nList.getLength(); i++) {
                Node attributeNode = nList.item(i);

                if (attributeNode.getNodeType() == Node.ELEMENT_NODE) {
                   String attributeName = ((Element)attributeNode).getAttribute("name");
                   if(attributeName.equals("connectTimeout") || attributeName.equals("readTimeout") 
                     || attributeName.endsWith(".readTimeout")){
                       map.put(attributeName, ((Element)attributeNode).getAttribute("value"));
                   }
                }
            }
	    } catch(ParserConfigurationException | IllegalArgumentException | IOException | SAXException e) {
	        // problems with SI file? return default values based on env
	        if(env.equalsIgnoreCase("Dev")) {
	            map.put("connectTimeout", "7000");
	        } else if(env.equalsIgnoreCase("QA")) {
	            map.put("connectTimeout", "3000");
	        } else {
	            map.put("connectTimeout", "100");
	        }
	        map.put("readTimeout", "60000");
	    }

	    return map;
	}

	/* get a hash map of 'connectionProperties' and its value string from dsimport */
	private HashMap<String, String> loadAndParseDsimportXml() throws ParserConfigurationException, SAXException, IOException {
		HashMap<String, String> map = new HashMap<String, String>();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        dsimportDom = docBuilder.parse(this.dsimportFile);

        NodeList nList = dsimportDom.getElementsByTagName("data-source");
        if(nList.getLength() <= 0){
            return map;
        }

        for (int i = 0; i < nList.getLength(); i++) {
            Node dsNode = nList.item(i);

            if (dsNode.getNodeType() == Node.ELEMENT_NODE) {
               String dsName = ((Element)dsNode).getAttribute("name");
               // push an empty value in hash for this dsname. value might get updated later in loop
               map.put(dsName, "");
               Node configPropNode = ((Element)dsNode).getElementsByTagName("config-properties").item(0);
               NodeList propNodeList = configPropNode.getChildNodes();

               for(int j = 0; j < propNodeList.getLength(); j++) {
                   Node propNode = propNodeList.item(j);
                   if (propNode.getNodeType() == Node.ELEMENT_NODE) {
                       if( ((Element)propNode).getAttribute("name").equals("connectionProperties") ) {
                       String connPropString = ((Element)propNode).getAttribute("value");
                           map.put(dsName, connPropString);
                           // while we are here, cleanup response, connection timeout from connectionProperties
                           String cleanedupConnPropString = cleanUpConnectionPropertiesString(connPropString);
                           ((Element)propNode).setAttribute("value", cleanedupConnPropString);
                       }
                   }
               }
            }
        }
        return map;
	}

	/* massage data from hash maps and construct new elements in dom */
	private void compareAndUpdateDsimportXml(HashMap<String, String> SIConfig, HashMap<String, String> dsimportConfig) 
			throws TransformerException, DOMException, NumberFormatException {
        NodeList nList = dsimportDom.getElementsByTagName("jdbc-driver");
        if(nList.getLength() <= 0){
            return;
        }

        Element queryPropNode = dsimportDom.createElement("query-properties");
        nList.item(0).appendChild(queryPropNode);

        copyGlobalAndQueryConfigFromSI(SIConfig, queryPropNode);

        calculateReadTimeoutAndAppendToQueryPropertyNode(SIConfig, dsimportConfig, queryPropNode);

        updateXmlFile();
        return;
	}


	/* copy global and query level key-value pairs into new query-properties element */
	private void copyGlobalAndQueryConfigFromSI(HashMap<String, String> SIConfig, Element queryPropNode) {
        Iterator<Entry<String, String>> it = SIConfig.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            // only add those entries which are global or query level...not datasource level
            if(pair.getKey().toString().startsWith("layer.DAL.datasource")) {
                continue;
            }
            // remove unwanted prefix from key name
            String updatedKeyName = pair.getKey().toString().replaceFirst("layer.DAL", "");
            Element propNode = createPropertyNode(updatedKeyName, pair.getValue().toString());
            queryPropNode.appendChild(propNode);
        }
        return;
	}

	/* helper method to create a property element node in dom */
	private Element createPropertyNode(String key, String value) {
        Element propNode = dsimportDom.createElement("property");
        propNode.setAttribute("name", key);
        propNode.setAttribute("value", value);
        return propNode;
	}

	/* this method does the critical massaging related to readTimeout */
	private void calculateReadTimeoutAndAppendToQueryPropertyNode(HashMap<String, String> SIConfig, 
            HashMap<String, String> dsimportConfig, Element queryPropNode) throws NumberFormatException, DOMException{
        // iterate over dsimport has map. For each datasource name
        Iterator<Entry<String, String>> itDS = dsimportConfig.entrySet().iterator();
        while(itDS.hasNext()) {
            Element propNode = null;
            Map.Entry pair = (Map.Entry)itDS.next();
            // normalize ds name. current SI keys do not work with '_' in start of datasource name
            String dsName = pair.getKey().toString().replaceFirst("_", "");
            String readTimeoutKeyName = "datasource." + dsName + ".readTimeout";

            // get dsimport's effective response timeout value
            String dsImportResponseTimeout = "";
            if(pair.getValue() != null) {
                dsImportResponseTimeout = getEffectiveResponseTimeoutDsimport(pair.getValue().toString());
            }
            // get SIConfig's effective readTimeout value
            String SIReadTimeout = getEffectiveReadTimeoutSIConfig(dsName, SIConfig);

            if(Integer.parseInt(SIReadTimeout) < 30000 && 
                (dsImportResponseTimeout != null && Integer.parseInt(dsImportResponseTimeout) > 0) ) {
                // use this value to set in query-properties for this datasource
                propNode = createPropertyNode(readTimeoutKeyName, SIReadTimeout);
            } else {
                // check if dsimport response_timeout is present, if yes, use that value
                if(dsImportResponseTimeout != null) {
                    propNode = createPropertyNode(readTimeoutKeyName, dsImportResponseTimeout);
                }
            }

            if(propNode != null){
                Comment comment = dsimportDom.createComment("description of timeout calculation");
                comment.setData(":SI effective readTimeout:'" + SIReadTimeout + "'");
                comment.appendData("::connectionProperties response_timeout_ms:'" + dsImportResponseTimeout + "'");
                queryPropNode.appendChild(comment);
                queryPropNode.appendChild(propNode);
            }
        }
	}

	/* parse connectionProperties value string to extract response timeout value */
	private String getEffectiveResponseTimeoutDsimport(String input){
	    String timeoutStr = null;
	    StringTokenizer st = new StringTokenizer(input, ";");
	    for (int i = 1; st.hasMoreTokens(); i++) {
	        // check if token has response_timeout_ms, 
            String token = st.nextToken();
	        if(token.startsWith("occ.response.timeout.ms")){
	            String[] pair = token.split("=");
	            timeoutStr = pair[1];
	        }
		}
		return timeoutStr;
	}

	/* parse various readTimeout values and figure out which one is applicable (based on hierarchy) */
	private String getEffectiveReadTimeoutSIConfig(String dsName, HashMap<String, String> SIConfig) {
		String timeout = "60000"; // default readTimeout
		String globalTimeout = SIConfig.get("readTimeout");
		String dsTimeoutKey = "layer.DAL.datasource." + dsName + ".readTimeout";
		String dsTimeout = SIConfig.get(dsTimeoutKey);
		try {
			if (dsTimeout != null) {
				timeout = dsTimeout;
			} else if (globalTimeout != null) {
				timeout = globalTimeout;
			}
		} catch (NumberFormatException n) {
			// let default value go through timeout = 6000;
		}
		return timeout;
	}

	/* this function removes occ.response.timeout.ms and   
	 * occ.connection.timeout.msecs from connectionProperty string */
	private static String cleanUpConnectionPropertiesString(String input) {
		StringTokenizer st1 = new StringTokenizer(input, ";");
		// create a stringbuffer to hold str to return
		StringBuffer sb = new StringBuffer();
		for (int i = 1; st1.hasMoreTokens(); i++) {
	         // check if token has connection_timeout_msec, if yes then 
			// skip it and move to next token
			String token = st1.nextToken();
	        if(token.startsWith("occ.response.timeout.ms") || 
	            token.startsWith("occ.connection.timeout.msecs")){
	            continue;
	        }
	         // append token to string buffer
	         sb.append(token).append(";");
		}
		// return stringbuffer tostring
		return sb.toString();
	}

	
	/* this method takes care of writing modified dom to dsimport file*/
	private void updateXmlFile() throws TransformerException {
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
		transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4"); 

        DOMSource source = new DOMSource(dsimportDom);
        StreamResult result = new StreamResult(dsimportFile);
        transformer.transform(source, result);
	}
}
