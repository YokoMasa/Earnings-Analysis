package com.masalab.earnings.xbrl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.masalab.earnings.exception.AppException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NSInfo {

    public static final String[] WELL_KNOWN_GAAP_NS = new String[] { 
        "http://fasb.org/us-gaap/2021-01-31",
        "http://fasb.org/us-gaap/2020-01-31",
        "http://fasb.org/us-gaap/2019-01-31",
        "http://fasb.org/us-gaap/2018-01-31",
        "http://fasb.org/us-gaap/2017-01-31"
    };

    public static final String[] WELL_KNOWN_GAAP_ENTRYPOINT = new String[] {
        "https://xbrl.fasb.org/us-gaap/2021/entire/us-gaap-entryPoint-std-2021-01-31.xsd",
        "http://xbrl.fasb.org/us-gaap/2020/entire/us-gaap-entryPoint-std-2020-01-31.xsd",
        "http://xbrl.fasb.org/us-gaap/2019/entire/us-gaap-entryPoint-std-2019-01-31.xsd",
        "https://xbrl.fasb.org/us-gaap/2018/entire/us-gaap-entryPoint-std-2018-01-31.xsd",
        "http://xbrl.fasb.org/us-gaap/2017/entire/us-gaap-entryPoint-std-2017-01-31.xsd"
    };

    public static final String[] WELL_KNOWN_DOMAIN_IN_NS = new String[] {
        "xbrl.org",
        "xbrl.sec.gov",
        "fasb.org",
        "w3.org"
    };

    private static final Pattern GAAP_NS_PATTERN = Pattern.compile("http://fasb.org/us-gaap/((\\d{4})-\\d{2}-\\d{2})");

    private Logger logger = LoggerFactory.getLogger(NSInfo.class);
    private Map<String, String> nsMap = new HashMap<>();
    private Map<String, String> customNsMap = new HashMap<>();
    private String gaapNs;
    private String gaapEntrypoint;

    public String getGaapNs() {
        return this.gaapNs;
    }

    public String getGaapEntrypoint() {
        return this.gaapEntrypoint;
    }

    public Map<String, String> getNsMap() {
        return this.nsMap;
    }

    public Map<String, String> getCustomNsMap() {
        return this.customNsMap;
    }

    private void determinGaapNs() throws AppException {
        for (Entry<String, String> e: nsMap.entrySet()) {
            for (int i = 0; i < WELL_KNOWN_GAAP_NS.length ; i++) {
                String wellKnownNs = WELL_KNOWN_GAAP_NS[i];
                if (wellKnownNs.equals(e.getValue())) {
                    gaapNs = wellKnownNs;
                    gaapEntrypoint = WELL_KNOWN_GAAP_ENTRYPOINT[i];
                    return;
                }
            }
        }
        guessGaapNs();
    }

    private void guessGaapNs() throws AppException {
        logger.warn("No ns matched well known gaap ns. You might need to add well know gaap ns and entrypoint. Start guessing...");
        for (Entry<String, String> e: nsMap.entrySet()) {
            Matcher m = GAAP_NS_PATTERN.matcher(e.getValue());
            if (m.matches()) {
                String yyyyMMdd = m.group(1);
                String year = m.group(2);
                gaapNs = e.getValue();
                gaapEntrypoint = "http://xbrl.fasb.org/us-gaap/" + year + "/entire/us-gaap-entryPoint-std-" + yyyyMMdd + ".xsd";
                logger.warn("Guessed. ns: " + gaapNs + ", entrypoint: " + gaapEntrypoint);
            }
        }
        logger.warn("Gaap ns guessing failed. Ns dump follows.");
        for (Entry<String, String> e: nsMap.entrySet()) {
            logger.warn("prefix: " + e.getKey() + ", uri: " + e.getValue());
        }
        // throw new ResourceException("No gaap ns found.");
    }

    private void guessCustomNs() {
        for (Entry<String, String> e: nsMap.entrySet()) {
            boolean isNotCustom = false;

            for (int i = 0; i < WELL_KNOWN_DOMAIN_IN_NS.length ; i++) {
                String wellKnownNs = WELL_KNOWN_DOMAIN_IN_NS[i];
                if (e.getValue().contains(wellKnownNs)) {
                    isNotCustom = true;
                }
            }

            if (!isNotCustom) {
                customNsMap.put(e.getKey(), e.getValue());
            }
        }
    }

    public NSInfo(Element xbrlElement) throws AppException {
        if (!"xbrl".equals(xbrlElement.getLocalName())) {
            throw new AppException("Given element is not a xbrl element but actually: " + xbrlElement.getTagName());
        }

        NamedNodeMap attrs = xbrlElement.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String attrName = attr.getNodeName();
            String attrVal = attr.getNodeValue();
            if (attrName.startsWith("xmlns:") && 6 < attrName.length()) {
                nsMap.put(attrName.substring(6), attrVal);
            }
        }

        determinGaapNs();
        logger.debug("Gaap ns: " + gaapNs);
        guessCustomNs();
    }
    
}
