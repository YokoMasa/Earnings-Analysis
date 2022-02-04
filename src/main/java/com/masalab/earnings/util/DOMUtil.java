package com.masalab.earnings.util;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DOMUtil {
    
    public static String getAttrValue(Node node, String attrName) {
        return getAttrValue(node, attrName, null);
    }

    public static String getAttrValue(Node node, String attrName, String ns) {
        NamedNodeMap attrList = node.getAttributes();
        if (attrList != null) {
            Node attr = null;
            if (ns != null) {
                attr = attrList.getNamedItemNS(ns, attrName);
            } else {
                attr = attrList.getNamedItem(attrName);
            }

            if (attr != null) {
                return attr.getNodeValue();
            }
        }
        return null;
    }

}
