package at.rocworks.oa4j.logger.dbs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
	
    //-----------------------------------------------------------------------------------------
    public static int httpGet(String url, StringBuffer response) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        //Debug.out.info("Response Code: "+responseCode);

        if (response != null && responseCode == 200 /*OK*/) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
        }

        con.disconnect();

		//Logger.DebugTN("Response Data: "+response.toString());
        return responseCode;
    }

    //-----------------------------------------------------------------------------------------
    public static int httpPost(String url, StringBuffer data) throws IOException {
        return httpPost(url, data, null);
    }

    public static int httpPost(String url, StringBuffer data, StringBuffer response) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");

        // Send post request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(data.toString());
            wr.flush();
        }

        int responseCode = con.getResponseCode();
        if (response!=null) {
            InputStream stream;
            if (responseCode >= 200 && responseCode <= 299 /*OK*/ ) {
                stream=con.getInputStream();
            } else {
                stream=con.getErrorStream();
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }            
        }        

        con.disconnect();

        return responseCode;
    }	
}
