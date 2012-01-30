/*
 * Created on 22-Oct-2003 by rtholmes
 * 
 *  
 */
package crystal.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author rtholmes
 * 
 */
public class XMLTools {

	private static Logger _log = Logger.getLogger(XMLTools.class);

	public static String STANDARD_DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss";

	public static boolean writeXMLDocument(Document doc, String fName) throws FileNotFoundException {
		// Assert.assertNotNull(doc);
		if (doc == null)
		    _log.error("null document");
		else {
		    final long start = System.currentTimeMillis();
		    FileOutputStream fos = new FileOutputStream(fName);
		    try {
		        XMLOutputter outputter = new XMLOutputter();
		        outputter.setFormat(Format.getPrettyFormat());
		        outputter.output(doc, fos);
		        return true;
		    } catch (IOException ioe) {
		        _log.error(ioe);
		    }
		    _log.trace("Document written to " + fName + " in: " + TimeUtility.msToHumanReadableDelta(start));
		}
		return false;
	}

	public static Document readXMLDocument(String fName) {

		SAXBuilder builder = new SAXBuilder(false);
		Document doc = null;

		try {

			doc = builder.build(new File(fName));

		} catch (JDOMException jdome) {
			_log.error(jdome);
		} catch (IOException ioe) {
			_log.error(ioe);
		}

		return doc;
	}

	public static Document readXMLDocument(URI fName) {

		SAXBuilder builder = new SAXBuilder(false);
		Document doc = null;

		try {

			doc = builder.build(new File(fName));

		} catch (JDOMException jdome) {
			_log.error(jdome);
		} catch (IOException ioe) {
			_log.error(ioe);
		}
		return doc;
	}

	// public static String writeXMLDocument(Document d) {
	// String outString = "";
	// try {
	//
	// if (d != null) {
	// Writer writer = new StringWriter();
	// OutputFormat format = new OutputFormat(d);
	// format.setIndenting(true);
	// // format.setLineSeparator("&line");
	// // format.setLineSeparator("\r\n");
	// // format.setPreserveSpace(true);
	// XMLSerializer serializer = new XMLSerializer(writer, format);
	// // serializer.s
	// serializer.asDOMSerializer();
	// serializer.serialize(d.getDocumentElement());
	// writer.flush();
	// outString = writer.toString();
	// writer.close();
	// }
	// } catch (IOException e) {
	// _log.error(e.getMessage());
	// }
	//
	// return outString;
	// }

	public static Document newXMLDocument() {

		// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// DocumentBuilder builder = null;
		//	
		// try {
		// // factory.setIgnoringElementContentWhitespace(true);
		// builder = factory.newDocumentBuilder();
		//	
		// } catch (ParserConfigurationException e) {
		// _log.error(e.getMessage());
		// }
		//	
		// return builder.newDocument();
		//	
		return new Document();
	}

	/**
	 * @param string
	 * @return
	 */
	public static Document newXMLErrorDocument(String errorString) {
		// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// DocumentBuilder builder = null;
		//
		// try {
		// // factory.setIgnoringElementContentWhitespace(true);
		// builder = factory.newDocumentBuilder();
		//
		// } catch (ParserConfigurationException e) {
		// _log.error(e.getMessage());
		// }
		//
		// Document doc = builder.newDocument();
		Document d = new Document();

		Element e = new Element("error");
		e.setAttribute("target", errorString);
		d.addContent(e);

		return d;
	}
}
