package org.dice.utilitytools.mapper;

public class TranslatedResult2ElasticMapper {
    public String map(String input,String url){
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"text\":\"");
        sb.append(input);
        sb.append("\",\"url\":\"");
        sb.append(url);
        sb.append("\"}");
        return sb.toString();
    }
}
