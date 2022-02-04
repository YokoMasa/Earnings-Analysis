package com.masalab.earnings.cik;

import java.util.Map;

public interface CikResolver {
    
    public boolean reload();

    public String getCik(String ticker);

    public boolean hasCik(String ticker);

    public Map<String, String> getMapping();

}
