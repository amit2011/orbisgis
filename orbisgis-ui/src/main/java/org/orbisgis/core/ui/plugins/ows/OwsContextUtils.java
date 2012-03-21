/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.orbisgis.core.ui.plugins.ows;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gdms.data.db.DBSource;

/**
 *
 * @author C�dric Le Glaunec <cedric.leglaunec@gmail.com>
 */
public class OwsContextUtils {

    /**
     * Extracts connection string's fields such as host, port, db name and table.
     * 
     * @param connectionString
     * @return 
     */
    public static DbConnectionString extractDbConnectionString(String connectionString) {
        String regex = "pgsql://(.*):(\\d+)/(.*)/(.*)";
        Scanner scanner = new Scanner(connectionString);
        scanner.findInLine(regex);

        MatchResult result = scanner.match();
        DbConnectionString db = new DbConnectionString(result.group(1), 
                Integer.parseInt(result.group(2)), result.group(3), 
                result.group(4));
        
        return db;
    }
    
    public static InputStream callService(String serviceUrl) {
        InputStream instream = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(serviceUrl);
            HttpResponse response = httpClient.execute(httpGet);
            System.out.println(response.getStatusLine());

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                instream = new BufferedInputStream(entity.getContent());

                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                //httpClient.getConnectionManager().shutdown();
            }
        } catch (ClientProtocolException ex) {
            Logger.getLogger(OwsImportPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OwsImportPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return instream;
    }
    
    public static InputStream callServicePost(String serviceUrl, List<NameValuePair> formparams) {
        InputStream instream = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(serviceUrl);
            
            UrlEncodedFormEntity owcEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(owcEntity);
            
            HttpResponse response = httpClient.execute(httpPost);
            System.out.println(response.getStatusLine());

            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                instream = new BufferedInputStream(responseEntity.getContent());

                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                httpClient.getConnectionManager().shutdown();
            }
        } catch (ClientProtocolException ex) {
            Logger.getLogger(OwsImportPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OwsImportPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
 
        return instream;
    }
    
    private static Properties readProperties(String filename) throws FileNotFoundException, IOException {
        Properties properties = new Properties();
        InputStream in = OwsService.class.getResourceAsStream(filename);
        properties.load(in);
        return properties;
    }
    
    public static String getServiceGetAllUrl() {
        String url = "";
        try {
            Properties properties = readProperties("services.properties");
            url = properties.getProperty("orb.orbisgis.core.ui.plugins.ows.service_url_getall");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OwsContextUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OwsContextUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return url;
    }
    
    public static String getServiceGetOneOwsUrl() {
        String url = "";
        try {
            Properties properties = readProperties("services.properties");
            url = properties.getProperty("orb.orbisgis.core.ui.plugins.ows.service_url_getoneows");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OwsContextUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OwsContextUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return url;
    }
    
    public static String getServiceExportOwsAsUrl() {
        String url = "";
        try {
            Properties properties = readProperties("services.properties");
            url = properties.getProperty("orb.orbisgis.core.ui.plugins.ows.service_url_exportows");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OwsContextUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OwsContextUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return url;
    }
    
    private static String generateSourceId(String table, String db, String host) {
        return table + "_" + db + "_" + host;
    }
    
    public static String generateSourceId(DbConnectionString db) {
        return generateSourceId(db.getTable(), db.getDb(), db.getHost());
    }
    
    public static String generateSourceId(DBSource db) {
        return generateSourceId(db.getTableName(), db.getDbName(), db.getHost());
    }
}