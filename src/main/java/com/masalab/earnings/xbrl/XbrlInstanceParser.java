package com.masalab.earnings.xbrl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.masalab.earnings.exception.AppException;
import com.masalab.earnings.util.PathUtil;
import com.masalab.earnings.xbrl.calc.CalculationTree;
import com.masalab.earnings.xbrl.calc.CalculationTreeBuilder;
import com.masalab.earnings.xml.XmlDocumentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("XbrlInstanceParser2")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class XbrlInstanceParser {

    private Logger logger = LoggerFactory.getLogger(XbrlInstanceParser.class);
    
    @Autowired
    private XmlDocumentRepository xmlDocumentRepository;

    @Autowired
    private CalculationTreeBuilder calculationTreeBuilder;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public XbrlInstance parse(String url) throws AppException {
        XbrlInstance instance = new XbrlInstance();
        logger.debug("Building calculation tree...");
        instance.setCalculationTree(buildCalculationTree(url));
        logger.debug("Parsing contexts...");
        instance.setContexts(parseContexts(url));
        logger.debug("Parsing facts...");
        instance.setFactSet(parseFacts(url, instance));
        return instance;
    }

    private CalculationTree buildCalculationTree(String url) throws AppException {
        XMLEventReader xmlEventReader = xmlDocumentRepository.gXmlEventReader(url);
        try {
            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    if ("schemaRef".equals(startElement.getName().getLocalPart())) {
                        QName qName = new QName(XbrlConstants.NS_XLINK, "href");
                        Attribute attr = startElement.getAttributeByName(qName);
                        String schemaRefUrl = PathUtil.getAbsolutePathRelativeToFile(attr.getValue(), url);
                        return calculationTreeBuilder.buildTree(schemaRefUrl);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("Failed to build calculation tree. message: " + e.getMessage());
        } finally {
            try {
                xmlEventReader.close();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        throw new AppException("Failed to build calculation tree. Schemaref not found");
    }

    private FactSet parseFacts(String url, XbrlInstance instance) throws AppException {
        FactSet factSet = new FactSet();
        XMLEventReader xmlEventReader = xmlDocumentRepository.gXmlEventReader(url);
        try {
            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    Fact fact = parseSingleFact(xmlEventReader, startElement, instance);
                    if (fact != null) {
                        factSet.addFact(fact);
                        // System.out.println(fact.ns + ", " + fact.name + ", " + fact.value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                xmlEventReader.close();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        return factSet;
    }

    private Fact parseSingleFact(XMLEventReader xmlEventReader, StartElement startElement, XbrlInstance instance) {
        try {
            Fact fact = new Fact();
            Attribute contextAttr = startElement.getAttributeByName(new QName("contextRef"));
            if (contextAttr == null) {
                return null;
            } else {
                if (instance.getContexts().containsKey(contextAttr.getValue())) {
                    fact.context = instance.getContexts().get(contextAttr.getValue());
                } else {
                    return null;
                }
            }

            Attribute decimalsAttr = startElement.getAttributeByName(new QName("decimals"));
            if (decimalsAttr != null) {
                fact.decimals = decimalsAttr.getValue();
            }

            Attribute precisionAttr = startElement.getAttributeByName(new QName("precision"));
            if (precisionAttr != null) {
                fact.precision = decimalsAttr.getValue();
            }

            fact.name = startElement.getName().getLocalPart();
            fact.ns = startElement.getName().getPrefix();
            
            XMLEvent nextEvent = xmlEventReader.nextEvent();
            if (nextEvent.isEndElement()) {
                return fact;
            }

            fact.value = nextEvent.asCharacters().getData();
            return fact;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Context> parseContexts(String url) throws AppException {
        Map<String, Context> contextMap = new HashMap<>();
        XMLEventReader xmlEventReader = xmlDocumentRepository.gXmlEventReader(url);
        try {
            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    if ("context".equals(startElement.getName().getLocalPart())) {
                        Context context = parseSingleContext(xmlEventReader, startElement);
                        if (context != null) {
                            contextMap.put(context.id, context);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                xmlEventReader.close();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        return contextMap;
    }

    private Context parseSingleContext(XMLEventReader xmlEventReader, StartElement startElement) {
        Context context = new Context();
        Attribute attr = startElement.getAttributeByName(new QName("id"));
        if (attr != null) {
            context.id = attr.getValue();
        } else {
            logger.warn("Id attribute of the context is null.");
            return null;
        }

        try {
            boolean inContextBlock = true;
            while (inContextBlock) {
                XMLEvent nextEvent = xmlEventReader.nextEvent();
                if (nextEvent.isEndElement() && "context".equals(nextEvent.asEndElement().getName().getLocalPart())) {
                    inContextBlock = false;
                } else if (nextEvent.isStartElement() && "entity".equals(nextEvent.asStartElement().getName().getLocalPart())) {
                    parseEntityInContext(xmlEventReader, context);
                } else if (nextEvent.isStartElement() && "period".equals(nextEvent.asStartElement().getName().getLocalPart())) {
                    parsePeriodInContext(xmlEventReader, context);
                }
            }

            if (context.periodType == Context.PeriodType.RANGE) {
                Duration duration = Duration.between(context.startDate.toInstant(), context.endDate.toInstant());
                if (duration.toDays() < 350 || 380 < duration.toDays()) {
                    logger.trace("Discarding context as it is not an year range. Start: " + dateFormat.format(context.startDate) + ", End: " + dateFormat.format(context.endDate));
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return context;
    }

    private void parsePeriodInContext(XMLEventReader xmlEventReader, Context context) throws Exception {
        boolean isInPeriodBlock = true;
        while (isInPeriodBlock) {
            XMLEvent nextEvent = xmlEventReader.nextEvent();
            if (nextEvent.isEndElement() && "period".equals(nextEvent.asEndElement().getName().getLocalPart())) {
                isInPeriodBlock = false;
            } else if (nextEvent.isStartElement() && "instant".equals(nextEvent.asStartElement().getName().getLocalPart())) {
                nextEvent = xmlEventReader.nextEvent();
                context.periodType = Context.PeriodType.INSTANT;
                context.instant = dateFormat.parse(nextEvent.asCharacters().getData());
                nextEvent = xmlEventReader.nextEvent();
            } else if (nextEvent.isStartElement() && "startDate".equals(nextEvent.asStartElement().getName().getLocalPart())) {
                nextEvent = xmlEventReader.nextEvent();
                context.periodType = Context.PeriodType.RANGE;
                context.startDate = dateFormat.parse(nextEvent.asCharacters().getData());
                nextEvent = xmlEventReader.nextEvent();
            } else if (nextEvent.isStartElement() && "endDate".equals(nextEvent.asStartElement().getName().getLocalPart())) {
                nextEvent = xmlEventReader.nextEvent();
                context.periodType = Context.PeriodType.RANGE;
                context.endDate = dateFormat.parse(nextEvent.asCharacters().getData());
                nextEvent = xmlEventReader.nextEvent();
            }
        }
    }

    private void parseEntityInContext(XMLEventReader xmlEventReader, Context context) throws XMLStreamException {
        boolean isInEntityBlock = true;
        while (isInEntityBlock) {
            XMLEvent nextEvent = xmlEventReader.nextEvent();
            if (nextEvent.isEndElement() && "entity".equals(nextEvent.asEndElement().getName().getLocalPart())) {
                isInEntityBlock = false;
            } else if (nextEvent.isStartElement() && "segment".equals(nextEvent.asStartElement().getName().getLocalPart())) {
                StringBuilder segmentContent = new StringBuilder();
                nextEvent = xmlEventReader.nextEvent();
                while (!nextEvent.isEndElement()) {
                    segmentContent.append(nextEvent.toString());
                    nextEvent = xmlEventReader.nextEvent();
                }
                context.segmentContent = segmentContent.toString();
            } else if (nextEvent.isStartElement() && "scenario".equals(nextEvent.asStartElement().getName().getLocalPart())) {
                StringBuilder scenarioContent = new StringBuilder();
                nextEvent = xmlEventReader.nextEvent();
                while (!nextEvent.isEndElement()) {
                    scenarioContent.append(nextEvent.asCharacters().getData());
                    nextEvent = xmlEventReader.nextEvent();
                }
                context.scenarioContent = scenarioContent.toString();
            }
        }
    }
}
