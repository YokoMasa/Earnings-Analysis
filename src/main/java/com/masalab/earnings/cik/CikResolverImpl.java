package com.masalab.earnings.cik;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.masalab.earnings.util.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component("CikResolverImpl")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CikResolverImpl implements CikResolver {

    private Logger logger = LoggerFactory.getLogger(CikResolverImpl.class);

    private OkHttpClient httpClient;

    @Value("${earnings.cik.jsonPath}")
    private String mappingJsonPath;

    @Value("${earnings.cik.jsonUrl}")
    private String mappingJsonUrl;

    @Value("${earnings.http.userAgent}")
    private String userAgent;

    private Map<String, String> mapping;

    @Override
    public String getCik(String ticker) {
        return mapping.get(ticker.toUpperCase());
    }

    @Override
    public boolean hasCik(String ticker) {
        return mapping.containsKey(ticker);
    }

    @Override
    public boolean reload() {
        return renewJson() && renewMapping();
    }

    private boolean renewMapping() {
        Map<String, String> newMapping = new HashMap<>();
        try {
            JsonElement jsonElement = JsonParser.parseReader(new FileReader(mappingJsonPath));
            JsonObject rootObject = jsonElement.getAsJsonObject();
            for (Entry<String, JsonElement> entry: rootObject.entrySet()) {
                JsonObject jsonMapping = entry.getValue().getAsJsonObject();
                String ticker = jsonMapping.get("ticker").getAsString();
                String cik = jsonMapping.get("cik_str").getAsString();
                newMapping.put(ticker, cik);
            }
        } catch (Exception e) {
            logger.error("Unable to renew mapping.", e);
        }
        this.mapping = newMapping;
        logger.debug("renewed mapping. length: " + this.mapping.size());
        return true;
    }

    private boolean renewJson() {
        File file = new File(mappingJsonPath);
        Request request = new Request.Builder()
            .get()
            .addHeader("User-Agent", userAgent)
            .url(mappingJsonUrl)
            .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                logger.error("Http request failed for some reasons. Response body follows.");
                BufferedReader br = new BufferedReader(response.body().charStream());
                String line = br.readLine();
                while (line != null) {
                    logger.error(line);
                }
                return false;
            }
            Files.copy(response.body().byteStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            logger.error("Failed to renew mapping json file.", e);
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @PostConstruct
    private void init() throws Exception {
        httpClient = HttpUtil.getInsecureClient();

        boolean isInitStatusOK = false;
        if (new File(mappingJsonPath).exists()) {
            logger.debug("mapping json path exists: " + mappingJsonPath);
            isInitStatusOK = renewMapping();
        } else {
            isInitStatusOK = reload();
        }

        if (!isInitStatusOK) {
            throw new RuntimeException("Could not init mapping!");
        }
    }

    @Override
    public Map<String, String> getMapping() {
        return Collections.unmodifiableMap(mapping);
    }
    
}
