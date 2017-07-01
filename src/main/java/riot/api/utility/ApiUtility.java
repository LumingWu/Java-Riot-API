package main.java.riot.api.utility;

import main.java.riot.api.request.RequestPool;

import java.time.Instant;
import java.util.LinkedList;

public class ApiUtility {

    public ApiUtility(){

    }

    public static String concatQuery(String uri, LinkedList<String> query_parameters){
        StringBuilder sb = new StringBuilder();
        sb.append(uri);
        if(!query_parameters.isEmpty()){
            sb.append(query_parameters.pop());
            while(!query_parameters.isEmpty()){
                sb.append("&");
                sb.append(query_parameters.pop());
            }
        }
        return sb.toString();
    }

    public static RequestPool.Regions parseRegions(String region){
        RequestPool.Regions reg = null;
        switch(region){
            case "RU":
                reg = RequestPool.Regions.ru;
                break;
            case "KR":
                reg = RequestPool.Regions.kr;
                break;
            case "BR1":
                reg = RequestPool.Regions.br1;
                break;
            case "OC1":
                reg = RequestPool.Regions.oc1;
                break;
            case "JP1":
                reg = RequestPool.Regions.jp1;
                break;
            case "NA1":
                reg = RequestPool.Regions.na1;
                break;
            case "EUN1":
                reg = RequestPool.Regions.eun1;
                break;
            case "EUW1":
                reg = RequestPool.Regions.euw1;
                break;
            case "TR1":
                reg = RequestPool.Regions.tr1;
                break;
            case "LA1":
                reg = RequestPool.Regions.la1;
                break;
            case "LA2":
                reg = RequestPool.Regions.la2;
                break;
            default:
                System.out.println("Error: parseRegions(" + region + ") has incorrect argument");
        }
        return reg;
    }

}
