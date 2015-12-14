package org.jasig.cas.web.wavity;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThemeUtils {
    
    private static final String API_HOST = "http://www.wavity.com:8080/scim/v2/";
    private static final String API_TYPE_TENANTS = "Tenants";
    private static final String API_TYPE_SERVICE = "CloudServices";
    private static final String JSON_ATTR_RESOURCE = "Resources";
    private static final String JSON_ATTR_TENANT_NAME = "tenantName";
    private static final String JSON_ATTR_TENANT_THUMBNAILS = "tenantThumbnails";
    private static final String JSON_ATTR_VALUE = "value";
    private static final String JSON_ATTR_SERVICE_NAME = "serviceName";
    private static final String JSON_ATTR_SERVICE_THUMBNAILS = "serviceThumbnails";
    private static final Logger logger = LoggerFactory.getLogger(ThemeUtils.class);
    
    public ThemeUtils() {
    }
    
    public static final String fetchTenantLogo(String tenantName) {
        if ("".equals(tenantName)) {
            logger.error("Tenant name can't be empty");
            return null;
        }
        String response = request(API_TYPE_TENANTS);
        if (response == null) {
            logger.error("Response is null");
            return null;
        }
        return parseJson(response, tenantName, JSON_ATTR_TENANT_NAME, JSON_ATTR_TENANT_THUMBNAILS);
    }
    
    public static final String fetchAppLogo(String appName) {
        if ("".equals(appName)) {
            logger.error("App name can't be empty");
            return null;
        }
        String response = request(API_TYPE_SERVICE);
        if (response == null) {
            logger.error("Response is null");
            return null;
        }
        return parseJson(response, appName, JSON_ATTR_SERVICE_NAME, JSON_ATTR_SERVICE_THUMBNAILS);
    }
    
    private static final String parseJson(String response, String name, String nameAttr, 
            String thumbnailsAttr) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray(JSON_ATTR_RESOURCE);
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString(nameAttr).equals(name)) {
                    String imageUrl = jsonObject.getJSONArray(thumbnailsAttr)
                        .getJSONObject(0)
                        .getString(JSON_ATTR_VALUE);
                    return imageUrl;
                }
            }
        } catch (JSONException je) {
            logger.warn("An error happened while fetching the {} logo image URL", name);
        }
        return null;
    }
    
    private static final String request(String apiType) {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .build();
        String url = null;
        if (apiType.equals(API_TYPE_TENANTS)) {
            url = new StringBuilder(API_HOST).append(API_TYPE_TENANTS).toString();
        } else if (apiType.equals(API_TYPE_SERVICE)) {
            url = new StringBuilder(API_HOST).append(API_TYPE_SERVICE).toString();
        }
        if (url == null) {
            logger.error("There happened an error while building the request URL");
            return null;
        }
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.addHeader("content-type", "application/json");
        HttpResponse response;
        try {
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() < 300) {
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity);
                return responseString;
            }
        } catch (ClientProtocolException e) {
            logger.warn("An exception happened!", e);
        } catch (IOException e) {
            logger.warn("An exception happened!", e);
        } catch (Exception e) {
            logger.warn("An exception happened!", e);
        }
        return null;
    }
    
    public static void main(String...args) {
        String tenantName = "Acme";
        String appName = "OneTeam";
        
        String tenantLogo = ThemeUtils.fetchTenantLogo(tenantName);
        String appLogo = ThemeUtils.fetchAppLogo(appName);
        
        System.out.println(tenantLogo);
        System.out.println(appLogo);
    }
        
}
