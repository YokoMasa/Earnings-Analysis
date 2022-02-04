package com.masalab.earnings.xml;

import javax.xml.stream.XMLEventReader;

import com.masalab.earnings.exception.AppException;

import org.w3c.dom.Document;

public interface XmlDocumentRepository {
    
    public Document getDocument(String resourceIdentifier) throws AppException;

    public XMLEventReader gXmlEventReader(String resourceIdentifier) throws AppException;

}
