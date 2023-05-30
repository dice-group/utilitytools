package org.dice.utilitytools.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.springframework.web.client.RestTemplate;


public class Translator {

    RestTemplate restTemplate;
    String url;
    public Translator(String url){
        this.restTemplate = new RestTemplate();
        this.url = url;
        Unirest.setTimeouts(600000,300000);
    }

    public String sendTranslationRequest(String textToTranslate) {
try {
    HttpResponse<String> res = Unirest.post("http://neamt.cs.upb.de:6100/custom-pipeline")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .field("components", "mbart_mt")
            .field("lang", "de")
            .field("query", textToTranslate)
            .asString();
    return res.getBody();

}catch (Exception ex){
    System.out.println(ex.getMessage());
    System.out.println(ex.getStackTrace());
    return "";
}

    }
}
