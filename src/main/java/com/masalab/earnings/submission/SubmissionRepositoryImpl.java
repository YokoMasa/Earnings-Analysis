package com.masalab.earnings.submission;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.masalab.earnings.exception.AppException;
import com.masalab.earnings.util.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component("SubmissionRepositoryImpl")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SubmissionRepositoryImpl implements SubmissionRepository {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Logger logger = LoggerFactory.getLogger(SubmissionRepositoryImpl.class);

    @Value("${earnings.submissions.url}")
    private String submissionsUrl;

    @Value("${earnings.http.userAgent}")
    private String userAgent;

    private OkHttpClient httpClient;
    
    @Override
    public List<Submission> getSubmissions(String cik, String form) throws AppException {
        JsonObject root = fetchSubmissionsJson(cik);
        return parseJson(root, form, cik);
    }

    private List<Submission> parseJson(JsonObject root, String form, String cik) throws AppException {
        List<Submission> submissions = new ArrayList<>();
        try {
            JsonObject filings = root.getAsJsonObject("filings");
            JsonObject recent = filings.getAsJsonObject("recent");

            JsonArray accessionNumbers = recent.getAsJsonArray("accessionNumber");
            JsonArray filingDates = recent.getAsJsonArray("filingDate");
            JsonArray reportDates = recent.getAsJsonArray("reportDate");
            JsonArray forms = recent.getAsJsonArray("form");
            JsonArray isXbrls = recent.getAsJsonArray("isXBRL");
            JsonArray isInlineXbrls = recent.getAsJsonArray("isInlineXBRL");
            JsonArray primaryDocuments = recent.getAsJsonArray("primaryDocument");

            for (int i = 0; i < accessionNumbers.size(); i++) {
                if (!form.equals(forms.get(i).getAsString())) {
                    continue;
                }
                
                Submission sub = new Submission();
                sub.accessionNumber = accessionNumbers.get(i).getAsString();

                String filingDateStr = filingDates.get(i).getAsString();
                if (StringUtils.hasText(filingDateStr)) {
                    sub.filingDate = dateFormat.parse(filingDateStr);
                }

                String reportDateStr = reportDates.get(i).getAsString();
                if (StringUtils.hasText(reportDateStr)) {
                    sub.reportDate = dateFormat.parse(reportDateStr);
                }

                sub.form = forms.get(i).getAsString();
                sub.isXbrl = isXbrls.get(i).getAsInt();
                sub.isInlineXbrl = isInlineXbrls.get(i).getAsInt();
                sub.primaryDocument = primaryDocuments.get(i).getAsString();
                sub.cik = cik;
                submissions.add(sub);
            }
        } catch (Exception e) {
            logger.error("Failed to parse json", e);
            e.printStackTrace();
        }

        return submissions;
    }

    private JsonObject fetchSubmissionsJson(String cik) throws AppException {
        String url = submissionsUrl + String.format("CIK%1$010d.json", Integer.parseInt(cik));
        Request request = new Request.Builder()
            .get()
            .url(url)
            .addHeader("User-Agent", userAgent)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonElement jsonElement = JsonParser.parseReader(response.body().charStream());
                return jsonElement.getAsJsonObject();
            } else {
                logger.error("Http request was not successful");
                logger.error("Error message: " + response.message());

                logger.error(response.body().string());

                throw new AppException("Http request was not successful. status: " + response.networkResponse().code());
            }
        } catch (IOException e) {
            logger.error("Http request failed", e);
            e.printStackTrace();
            throw new AppException("Http request failed");
        }
        
    }

    @PostConstruct
    private void init() throws Exception {
        httpClient = HttpUtil.getInsecureClient();
    }
}
