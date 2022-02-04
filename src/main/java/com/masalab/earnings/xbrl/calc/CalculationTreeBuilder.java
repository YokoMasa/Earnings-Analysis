package com.masalab.earnings.xbrl.calc;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.masalab.earnings.exception.AppException;
import com.masalab.earnings.util.DOMUtil;
import com.masalab.earnings.util.PathUtil;
import com.masalab.earnings.xbrl.XbrlConstants;
import com.masalab.earnings.xml.XmlDocumentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class CalculationTreeBuilder {

    private Logger logger = LoggerFactory.getLogger(CalculationTreeBuilder.class);

    @Autowired
    private XmlDocumentRepository xmlDocumentRepository;

    private Set<String> parsedXsd = new HashSet<>();
    private Map<String, String> elements = new HashMap<>();
    private Set<String> elementSet = new HashSet<>();
    private CalculationTree currentTree;

    public CalculationTree buildTree(String... taxonomySchemaEntryPoint) throws AppException {
        currentTree = new CalculationTree();
        for (String entryPoint: taxonomySchemaEntryPoint) {
            parseXsd(entryPoint);
        }
        return currentTree;
    }
    
    private void parseXsd(String resourcePath) throws AppException {
        if (!StringUtils.hasText(resourcePath)) {
            return;
        }

        resourcePath = PathUtil.getAbsolutePath(resourcePath.trim());

        if (parsedXsd.contains(resourcePath)) {
            return;
        }
        parsedXsd.add(resourcePath);

        Document document;
        try {
            document = xmlDocumentRepository.getDocument(resourcePath);
        } catch (AppException e) {
            e.printStackTrace();
            return;
        }

        Element root = document.getDocumentElement();
        parseElements(root, resourcePath);
        parseImport(root, resourcePath);
        parseLinkbaseRef(root, resourcePath);
    }

    private void parseElements(Element root, String resourcePath) {
        NodeList elementNodeList = root.getElementsByTagNameNS(XbrlConstants.NS_XML_SCHEMA, "element");
        logger.trace("parseElements: " + resourcePath + ", count: " + elementNodeList.getLength());
        for (int i = 0; i < elementNodeList.getLength(); i++) {
            Node elementNode = elementNodeList.item(i);
            String id = DOMUtil.getAttrValue(elementNode, "id");
            String name = DOMUtil.getAttrValue(elementNode, "name");
            elementSet.add(name);
            elements.put(resourcePath + "#" + id, name);
            // logger.trace(resourcePath + "#" + id);
        }
    }

    private void parseLinkbaseRef(Element root, String resourcePath) throws AppException {
        NodeList elementNodeList = root.getElementsByTagNameNS(XbrlConstants.NS_LINK, "linkbaseRef");
        logger.trace("parseLinkbaseRef: " + resourcePath + ", count: " + elementNodeList.getLength());
        for (int i = 0; i < elementNodeList.getLength(); i++) {
            Node elementNode = elementNodeList.item(i);
            String role = DOMUtil.getAttrValue(elementNode, "role", XbrlConstants.NS_XLINK);
            String href = DOMUtil.getAttrValue(elementNode, "href", XbrlConstants.NS_XLINK);
            if (XbrlConstants.ROLE_CAL_LINKBASE_REF.equals(role)) {
                // System.out.println("role: " + role + ", href: " + PathUtil.getAbsolutePathRelativeToFile(href, resourcePath));
                if (!href.startsWith("http")) {
                    try {
                        href = PathUtil.getAbsolutePathRelativeToFile(href, resourcePath);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        logger.error("Failed to parseLinkbaseRef: " + href, e);
                    }
                }
                parseCalLinkbase(xmlDocumentRepository.getDocument(href).getDocumentElement(), href);
            }
        }
    }

    private void parseImport(Element root, String resourcePath) throws AppException {
        NodeList elementNodeList = root.getElementsByTagNameNS(XbrlConstants.NS_XML_SCHEMA, "import");
        for (int i = 0; i < elementNodeList.getLength(); i++) {
            Node elementNode = elementNodeList.item(i);
            String schemaLocation = DOMUtil.getAttrValue(elementNode, "schemaLocation");
            // logger.trace("parseImport: " + schemaLocation);
            if (schemaLocation.startsWith("http")) {
                parseXsd(schemaLocation);
            } else {
                try {
                    parseXsd(PathUtil.getAbsolutePathRelativeToFile(schemaLocation, resourcePath));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    logger.error("Failed to parse import: " + schemaLocation, e);
                }
            }
        }
    }

    private void parseCalLinkbase(Element root, String resourcePath) {
        // System.out.println("★★ " + resourcePath + " ★★");
        Map<String, String> locMap = parseLoc(root, resourcePath);
        NodeList elementNodeList = root.getElementsByTagNameNS(XbrlConstants.NS_LINK, "calculationLink");
        logger.debug("parseCalLinkbase: " + resourcePath + ", calculationLinkCount: " + elementNodeList.getLength());
        for (int i = 0; i < elementNodeList.getLength(); i++) {
            Node calculationLinkNode = elementNodeList.item(i);
            String role = DOMUtil.getAttrValue(calculationLinkNode, "role", XbrlConstants.NS_XLINK);
            
            List<Concept> rootConcepts = new ArrayList<>();
            List<Concept> toConcepts = new ArrayList<>();

            NodeList calculationLinkChildren = calculationLinkNode.getChildNodes();
            for (int j = 0; j < calculationLinkChildren.getLength(); j++) {
                Node elementNode = calculationLinkChildren.item(j);
                if (!"calculationArc".equals(elementNode.getLocalName())) {
                    continue;
                }

                String orderStr = DOMUtil.getAttrValue(elementNode, "order");
                String weightStr = DOMUtil.getAttrValue(elementNode, "weight");
                // String use = DOMUtil.getAttrValue(elementNode, "use");
                String priority = DOMUtil.getAttrValue(elementNode, "priority");
                String fromLoc = DOMUtil.getAttrValue(elementNode, "from", XbrlConstants.NS_XLINK);
                String toLoc = DOMUtil.getAttrValue(elementNode, "to", XbrlConstants.NS_XLINK);

                if (locMap.containsKey(fromLoc) && locMap.containsKey(toLoc)) {
                    String from = locMap.get(fromLoc);
                    String to = locMap.get(toLoc);
                    double order = 0;
                    if (StringUtils.hasText(orderStr)) {
                        order = Double.parseDouble(orderStr);
                    }

                    double weight = 0;
                    if (StringUtils.hasText(weightStr)) {
                        weight = Double.parseDouble(weightStr);
                    }

                    Concept fromConcept = null;
                    if (currentTree.hasConceptInTree(from)) {
                        fromConcept = currentTree.getConcept(from);
                    } else {
                        fromConcept = new Concept(from);
                        currentTree.addConcept(fromConcept);
                    }

                    Concept toConcept = null;
                    if (currentTree.hasConceptInTree(to)) {
                        toConcept = currentTree.getConcept(to);
                    } else {
                        toConcept = new Concept(to);
                        currentTree.addConcept(toConcept);
                    }

                    toConcepts.add(toConcept);
                    if (!rootConcepts.contains(fromConcept)) {
                        rootConcepts.add(fromConcept);
                    }

                    CalculationItem<Concept> calculationItem = new CalculationItem<>(toConcept, weight, order);

                    double priorityDouble = 0;
                    if (StringUtils.hasText(priority)) {
                        priorityDouble = Double.parseDouble(priority);
                    }
                    currentTree.getConcept(from).addCalculationItem(calculationItem, role, priorityDouble);
                }
            }

            for (Concept c: rootConcepts.toArray(new Concept[0])) {
                if (toConcepts.contains(c)) {
                    rootConcepts.remove(c);
                }
            }
            currentTree.addRootConcepts(role, rootConcepts);
        }
    }

    private Map<String, String> parseLoc(Element root, String resourcePath) {
        Map<String, String> locMap = new HashMap<>();

        NodeList elementNodeList = root.getElementsByTagNameNS(XbrlConstants.NS_LINK, "loc");
        for (int i = 0; i < elementNodeList.getLength(); i++) {
            Node elementNode = elementNodeList.item(i);
            String label = DOMUtil.getAttrValue(elementNode, "label", XbrlConstants.NS_XLINK);
            String href = DOMUtil.getAttrValue(elementNode, "href", XbrlConstants.NS_XLINK);

            String elementId = "";
            if (href.startsWith("#")) {
                elementId = resourcePath + href;
            } else if (href.startsWith("http")) {
                elementId = href;
            } else {
                String[] splittedHref = href.split("#");
                if (splittedHref.length == 2) {
                    try {
                        String docPath = PathUtil.getAbsolutePathRelativeToFile(splittedHref[0], resourcePath);
                        elementId = docPath + "#" + splittedHref[1];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            String name = "";
            if (elements.containsKey(elementId)) {
                name = elements.get(elementId);
            } else {
                logger.warn("elementId not found: " + elementId);
            }
            // System.out.println("label: " + label + ", name: " + name + ", elementId: " + elementId);
            locMap.put(label, name);
        }
        return locMap;
    } 

    public CalculationTreeBuilder() {
        
    }

}
