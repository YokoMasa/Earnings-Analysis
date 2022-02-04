package com.masalab.earnings.submission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.masalab.earnings.exception.AppException;
import com.masalab.earnings.util.HttpUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component("SubmittedFileRepositoryImpl")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SubmittedFileRepositoryImpl implements SubmittedFileRepository {
    
    private String baseUrl = "https://www.sec.gov/Archives/edgar/data/";
    private Pattern xbrlInstanceFilePattern = Pattern.compile("^(?!.*(pre|lab|def|cal)).+\\.xml");

    @Value("${earnings.http.userAgent}")
    private String userAgent;

    private OkHttpClient httpClient;

    @Override
    public List<SubmittedFile> getSubmittedFiles(Submission submission) throws AppException {
        JsonElement jsonElement = fetchJson(submission);
        return parseJson(submission, jsonElement);
    }

    @Override
    public SubmittedFile getXbrlInstance(Submission submission) throws AppException {
        long largestFileSize = 0;
        SubmittedFile xbrlInstance = null;
        for (SubmittedFile file: getSubmittedFiles(submission)) {
            Matcher m = xbrlInstanceFilePattern.matcher(file.name);
            if (m.matches() && largestFileSize < file.size) {
                largestFileSize = file.size;
                xbrlInstance = file;
            }
        }

        if (xbrlInstance == null) {
            throw new AppException("Could not find xbrlInstance file. submission: " + submission.accessionNumber + ", cik: " + submission.cik);
        }

        return xbrlInstance;
    }

    private List<SubmittedFile> parseJson(Submission submission, JsonElement jsonElement) throws AppException {
        JsonObject root = jsonElement.getAsJsonObject();
        JsonObject directory = root.getAsJsonObject("directory");
        JsonArray item = directory.getAsJsonArray("item");

        List<SubmittedFile> files = new ArrayList<>();
        for (int i = 0; i < item.size(); i++) {
            SubmittedFile file = new SubmittedFile();
            JsonObject submittedFileJson = item.get(i).getAsJsonObject();
            file.name = submittedFileJson.get("name").getAsString();
            file.type = submittedFileJson.get("type").getAsString();
            file.url = baseUrl + submission.cik + "/" + submission.accessionNumber.replace("-", "") + "/" + file.name;
            String sizeStr = submittedFileJson.get("size").getAsString();
            if (StringUtils.hasText(sizeStr)) {
                file.size = Long.parseLong(sizeStr);
            } else {
                file.size = 0;
            }
            files.add(file);
        }
        return files;
    }

    private JsonElement fetchJson(Submission submission) throws AppException {
        if (!StringUtils.hasText(submission.cik) || !StringUtils.hasText(submission.accessionNumber)) {
            throw new AppException("cik or accession number is blank");
        }

        String url = baseUrl + submission.cik + "/" + submission.accessionNumber.replace("-", "") + "/index.json";
        Request request = new Request.Builder()
            .get()
            .url(url)
            .addHeader("User-Agent", userAgent)
            .build();
        try {
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new AppException("http request failed. status: " + response.code());
            } else {
                return JsonParser.parseReader(response.body().charStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new AppException("http request failed");
        }
    }

    @PostConstruct
    private void init() throws Exception {
        httpClient = HttpUtil.getInsecureClient();
    }

}
