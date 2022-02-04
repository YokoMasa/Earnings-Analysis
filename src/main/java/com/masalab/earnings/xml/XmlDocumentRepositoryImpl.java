package com.masalab.earnings.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.masalab.earnings.exception.AppException;
import com.masalab.earnings.util.HttpUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component("XmlDocumentRepositoryImpl")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class XmlDocumentRepositoryImpl implements XmlDocumentRepository {

    @Value("${earnings.http.userAgent}")
    private String userAgent;

    private DocumentBuilder documentBuilder;
    private OkHttpClient httpClient;
    private FileCache fileCache;
    private XMLInputFactory xmlInputFactory;

    @PostConstruct
    private void init() {
        fileCache = new FileCache("C:\\Users\\masato\\Downloads\\cache");
    }
    
    @Override
    public Document getDocument(String resourceIdentifier) throws AppException {
        if (!StringUtils.hasText(resourceIdentifier)) {
            throw new AppException("resourceIdentifier cannot be empty.");
        }

        InputStream is = getInputStream(resourceIdentifier);
        try {
            return documentBuilder.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("Failed to build document. message: " + e.getMessage());
        }
    }

    @Override
    public XMLEventReader gXmlEventReader(String resourceIdentifier) throws AppException {
        InputStream is = getInputStream(resourceIdentifier);
        if (xmlInputFactory == null) {
            xmlInputFactory = XMLInputFactory.newFactory();
        }
        try {
            return xmlInputFactory.createXMLEventReader(is);
        } catch (XMLStreamException e) {
            e.printStackTrace();
            throw new AppException("XMLEventReader creation failed. message: " + e.getMessage());
        }
    }

    private InputStream getInputStream(String resourceIdentifier) throws AppException {
        resourceIdentifier = resourceIdentifier.trim();
        InputStream is = null;
        if (resourceIdentifier.startsWith("http")) {
            if (fileCache.hasCache(resourceIdentifier)) {
                is = getISFromCache(resourceIdentifier);
            } else {
                is = getISFromHttpServer(resourceIdentifier);
            }
        } else {
            is = getISFromLocalDirectory(resourceIdentifier);
        }
        return is;
    }

    private InputStream getISFromCache(String url) throws AppException {
        try {
            return fileCache.get(url);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("Failed to open inputstream from cache");
        }
    }

    private InputStream getISFromHttpServer(String url) throws AppException {
        try {
            Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", userAgent)
                .build();
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                fileCache.put(url, response.body().byteStream());
                return fileCache.get(url);
            } else {
                throw new AppException("Http request failed. Status: " + response.code() + ", message: " + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new AppException("Http request failed. Message: " + e.getMessage());
        }
    }

    private InputStream getISFromLocalDirectory(String filePath) throws AppException {
        try {
            return new FileInputStream(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AppException(e.getMessage());
        }
    }

    public XmlDocumentRepositoryImpl() throws ParserConfigurationException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();

            httpClient = HttpUtil.getInsecureClient();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

}
