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

/**
 * This class is used for supporting the customized version of wavity CAS UI
 * especially in the scriptlet in JSP views.
 * 
 * @author davidlee
 *
 */
public final class ThemeUtils {
    
    /**
     * API host
     */
    private static final String API_HOST = "http://www.wavity.com:8080/scim/v2/";
    
    /**
     * Tenants API suffix
     */
    private static final String API_TYPE_TENANTS = "Tenants";
    
    /**
     * Service API suffix
     */
    private static final String API_TYPE_SERVICE = "CloudServices";
    
    /**
     * Resources JSON attribute
     */
    private static final String JSON_ATTR_RESOURCE = "Resources";
    
    /**
     * Tenant name JSON attribute
     */
    private static final String JSON_ATTR_TENANT_NAME = "tenantName";
    
    /**
     * Tenant thumbnail JSON attribute
     */
    private static final String JSON_ATTR_TENANT_THUMBNAILS = "tenantThumbnails";
    
    /**
     * Value JSON attribute
     */
    private static final String JSON_ATTR_VALUE = "value";
    
    /**
     * Service name JSON attribute
     */
    private static final String JSON_ATTR_SERVICE_NAME = "serviceName";
    
    /**
     * Service thumbnail attribute
     */
    private static final String JSON_ATTR_SERVICE_THUMBNAILS = "serviceThumbnails";
    
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ThemeUtils.class);
    
    /**
     * ThemeUtils constructor
     * 
     * Nothing to do in the constructor for now
     */
    public ThemeUtils() {
    }
    
    /**
     * Fetches the tenant logo image URL
     * 
     * @param tenantName
     * @return JSON string | null
     */
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
        return parseJson(response, tenantName, API_TYPE_TENANTS);
    }
    
    /**
     * Fetches the app logo image URL
     * 
     * @param appName
     * @return JSON string | null
     */
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
        return parseJson(response, appName, API_TYPE_SERVICE);
    }
    
    /**
     * Parses JSON string to extract the image URLs
     * 
     * @param response
     * @param name
     * @param apiType
     * @return extracted image URL | null
     */
    private static final String parseJson(String response, String name, String apiType) {
        if (("".equals(response) || response == null) || ("".equals(name) || name == null)
                || ("".equals(apiType) || apiType == null)) {
            logger.error("Some required parameters are missing");
            return null;
        }
        String nameAttr = null;
        String thumbnailsAttr = null;
        if (apiType.equals(API_TYPE_TENANTS)) {
            nameAttr = JSON_ATTR_TENANT_NAME;
            thumbnailsAttr = JSON_ATTR_TENANT_THUMBNAILS;
        } else if (apiType.equals(API_TYPE_SERVICE)) {
            nameAttr = JSON_ATTR_SERVICE_NAME;
            thumbnailsAttr = JSON_ATTR_SERVICE_THUMBNAILS;
        }
        if (nameAttr == null || thumbnailsAttr == null) {
            logger.error("There's no matched API type");
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            final JSONArray jsonArray = jsonObject.getJSONArray(JSON_ATTR_RESOURCE);
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString(nameAttr).toLowerCase().equals(name.toLowerCase())) {
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
    
    /**
     * Sends requests considering API type
     * 
     * @param apiType
     * @return JSON string | null
     */
    private static final String request(String apiType) {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final RequestConfig requestConfig = RequestConfig.custom()
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
        final HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.addHeader("content-type", "application/json");
        final HttpResponse response;
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
    
    /**
     * Main method for testing
     * 
     * @param args
     */
    public static void main(String...args) {
        String tenantName = "Acme";
        String appName = "Oneteam";
        
        String tenantLogo = ThemeUtils.fetchTenantLogo(tenantName);
        String appLogo = ThemeUtils.fetchAppLogo(appName);
        
        System.out.println(tenantLogo);
        System.out.println(appLogo);
    }
        
}
