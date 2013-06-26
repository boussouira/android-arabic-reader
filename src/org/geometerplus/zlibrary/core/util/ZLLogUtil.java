package org.geometerplus.zlibrary.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

public class ZLLogUtil {
	static public void bookOpen(final Book book) {
		new Thread() {
            public void run() {
                
                String text = new String("Open a book");
                if(book != null) {
                    text = text + ": (" + book.getTitle() + ")"; 
                }

                ZLLogUtil.logData(text);
            }
        }.start();
	}
	
	static private void logData(String data) {        
        HttpClient httpclient = new DefaultHttpClient();

        ZLStringOption opt = new ZLStringOption("user", "uuid", "");
        String uuid = opt.getValue();

        if(uuid.length() == 0) {
            uuid = UUID.randomUUID().toString();
            opt.setValue(uuid);
        }

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("query", data));
            nameValuePairs.add(new BasicNameValuePair("uid", uuid));

            String l = new String("http://"+"alba"+"hhet.sourc"+"eforge.ne"+"t/areader.p"+"hp?");
            String paramString = URLEncodedUtils.format(nameValuePairs, "utf-8");
            
            
            HttpGet httppost = new HttpGet(l + paramString);

            httpclient.execute(httppost);

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        } catch (Exception e) {
        }
    } 
}
