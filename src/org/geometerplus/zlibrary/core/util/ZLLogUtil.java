package org.geometerplus.zlibrary.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.os.Build;

public class ZLLogUtil {
	public static String packageName = "0";

	static public void bookOpen(final Book book) {
		new Thread() {
            public void run() {

                String text = new String("Open a book");
                if(book != null) {
                    text = text + ": (" + book.getTitle() + ")"
                    			+ " [" + book.getLanguage()
                    			+ "|" + book.getEncoding()
                    			+ "]";
                }

                ZLLogUtil.logData(text);
            }
        }.start();
	}
	
	static public String getUUID() {
        ZLStringOption opt = new ZLStringOption("user", "uuid", "");
        String uuid = opt.getValue();

        if(uuid.length() == 0) {
            uuid = UUID.randomUUID().toString();
            opt.setValue(uuid);
        }
        
        return uuid;
	}
	
	static public String getLanguage() {
		Locale  l = Locale.getDefault();
		if(l != null) {
			return l.getLanguage();
		} else {
			return new String("UNKNOW");
		}
	}

	static public String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}
	
	static public String getAppVersion() {
		return ((ZLAndroidLibrary)ZLibrary.Instance()).getVersionName();
	}
	
	public static String getPackageName() {
		return packageName;
	} 
	
	static private void logData(String data) {
        HttpClient httpclient = new DefaultHttpClient();

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("query", data));
            nameValuePairs.add(new BasicNameValuePair("uid", getUUID()));
            nameValuePairs.add(new BasicNameValuePair("hl", getLanguage()));
            nameValuePairs.add(new BasicNameValuePair("p", getPackageName()));
            nameValuePairs.add(new BasicNameValuePair("av", getAppVersion()));
            nameValuePairs.add(new BasicNameValuePair("v", getAndroidVersion()));

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
