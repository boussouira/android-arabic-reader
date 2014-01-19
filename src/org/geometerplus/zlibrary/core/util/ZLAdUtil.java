package org.geometerplus.zlibrary.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.RelativeLayout;

public class ZLAdUtil {

	public AdManager m_adManager;
	
	public class AdMessageButton {
		public String text;
		public String action;
		public String link;
		public String shareSubject;
		public String shareText;
	}
	
	public class AdMessage {
		public String id;
		public String text;
		public String title;
		public String link;
		public boolean cancelable;

		public AdMessageButton firstButton;
		public AdMessageButton secondButton;
		public AdMessageButton thirdButton;

		public AdMessage() {
			cancelable = true;
			
			firstButton = new AdMessageButton();
			firstButton.text = "Ok";
			firstButton.action = "hide";
		}
	}
	
	public class AdConfig {
		public boolean showAd;
		public int updateInterval;
		public int displayAdDelay;
		
		public AdMessage message;
		
		public AdConfig() {
			 showAd = false;
			 updateInterval = 10 * 60; // Update interval in seconds
			 displayAdDelay = 1;
		}
	}
	   
	public class AdManager extends AsyncTask<Integer, Integer, Integer> {
		private AdConfig m_config;
		private Activity m_parentActivity;

		public AdManager(Activity parentActivity, RelativeLayout rootView) {
			m_parentActivity = parentActivity;
		}

		protected Integer doInBackground(Integer... topDocs) {
			m_config = getAdConfig();

			publishProgress(0);
			return 0;
		}

		protected void onProgressUpdate(Integer... result) {

	        boolean showMessage = false;
			final ZLBooleanOption opt = ((m_config.message != null && m_config.message.id != null) ? new ZLBooleanOption(
					"message", m_config.message.id, true) : null);

			if(opt != null)
				showMessage = (opt.getValue() || m_config.message.id.equalsIgnoreCase("always"));

	        
			if (showMessage) {
				if(opt != null)
					opt.setValue(false);
		        
				AlertDialog.Builder builder = new AlertDialog.Builder(m_parentActivity);
				builder.setMessage(m_config.message.text);
				builder.setCancelable(m_config.message.cancelable);

				if(m_config.message.title != null && m_config.message.title.length() > 0)
					builder.setTitle(m_config.message.title);

				if (m_config.message.firstButton != null) {
					builder.setPositiveButton(m_config.message.firstButton.text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							buttonAction(dialog, m_config.message.firstButton, opt);
						}
					});
				}

				if (m_config.message.secondButton != null) {
					builder.setNegativeButton(m_config.message.secondButton.text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							buttonAction(dialog, m_config.message.secondButton, opt);
						}
					});
				}
				
				if (m_config.message.thirdButton != null) {
					builder.setNeutralButton(m_config.message.thirdButton.text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							buttonAction(dialog, m_config.message.thirdButton, opt);
						}
					});
				}

				AlertDialog alert = builder.create();
				alert.show();
			}
		}
		
		protected void buttonAction(DialogInterface dialog, AdMessageButton btn, ZLBooleanOption opt) {
			if (btn.action.contains("link") && btn.link != null) {
				m_parentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(btn.link)));
			}

			if (btn.action.contains("remind")) {
				opt.setValue(true);
			}

			if(btn.action.contains("share")) {
				try {
					m_parentActivity.startActivity(
						new Intent(Intent.ACTION_SEND)
							.setType("text/plain")
							.putExtra(Intent.EXTRA_SUBJECT, btn.shareSubject)
							.putExtra(Intent.EXTRA_TEXT, btn.shareText)
					);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			if (btn.action.contains("hide")) {
				dialog.cancel();
			}

			if (btn.action.contains("close")) {
				m_parentActivity.finish();
			}
		}
	}
	
	public ZLAdUtil(Activity parentActivity, RelativeLayout rootView) {
		m_adManager = new AdManager(parentActivity, rootView);
	}
	
	public void start() {
		m_adManager.execute(0);
	}
	
	public AdConfig getAdConfig() {
		String JsonResponse = getHttpResponse("http://"+"alba"+"hhet.sourc"+"eforge.ne"+"t/adsconfig.p"+"hp");
		AdConfig config = new AdConfig();
		
		if(JsonResponse == null || JsonResponse.length() <= 0) {
			//Log.d("RES", "Can't get config from server");
			return config;
		}
		
		//Log.d("RES", "Server config: " + JsonResponse);

		try {
			JSONObject json = new JSONObject(JsonResponse);

			if (json.has("message")) {
				JSONObject message = json.getJSONObject("message");
				config.message = new AdMessage();

				if (message.has("id"))
					config.message.id = message.getString("id");

				if (message.has("text"))
					config.message.text = message.getString("text");

				if (message.has("title"))
					config.message.title = message.getString("title");

				if (message.has("link"))
					config.message.link = message.getString("link");

				if (message.has("cancelable"))
					config.message.cancelable = message
							.getBoolean("cancelable");

				if (message.has("firstButton")) {
					JSONObject btn = message.getJSONObject("firstButton");
					config.message.firstButton = new AdMessageButton();

					if (btn.has("text"))
						config.message.firstButton.text = btn.getString("text");

					if (btn.has("action"))
						config.message.firstButton.action = btn
								.getString("action");

					if (btn.has("link"))
						config.message.firstButton.link = btn.getString("link");
					
					if (btn.has("share_subject"))
						config.message.firstButton.shareSubject = btn.getString("share_subject");
					
					if (btn.has("share_text"))
						config.message.firstButton.shareText = btn.getString("share_text");
				}

				if (message.has("secondButton")) {
					JSONObject btn = message.getJSONObject("secondButton");
					config.message.secondButton = new AdMessageButton();

					if (btn.has("text"))
						config.message.secondButton.text = btn
								.getString("text");

					if (btn.has("action"))
						config.message.secondButton.action = btn
								.getString("action");

					if (btn.has("link"))
						config.message.secondButton.link = btn
								.getString("link");
					
					if (btn.has("share_subject"))
						config.message.secondButton.shareSubject = btn.getString("share_subject");
					
					if (btn.has("share_text"))
						config.message.secondButton.shareText = btn.getString("share_text");
				}

				if (message.has("thirdButton")) {
					JSONObject btn = message.getJSONObject("thirdButton");
					config.message.thirdButton = new AdMessageButton();

					if (btn.has("text"))
						config.message.thirdButton.text = btn.getString("text");

					if (btn.has("action"))
						config.message.thirdButton.action = btn
								.getString("action");

					if (btn.has("link"))
						config.message.thirdButton.link = btn.getString("link");
					
					if (btn.has("share_subject"))
						config.message.thirdButton.shareSubject = btn.getString("share_subject");
					
					if (btn.has("share_text"))
						config.message.thirdButton.shareText = btn.getString("share_text");
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return config;
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		StringBuilder sb = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String getHttpResponse(String url) {
		if(url.contains("?"))
			url = url + "&";
		else if(!url.endsWith("?"))
			url = url + "?";
		
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("uid", ZLLogUtil.getUUID()));
        nameValuePairs.add(new BasicNameValuePair("hl", ZLLogUtil.getLanguage()));
        nameValuePairs.add(new BasicNameValuePair("v", ZLLogUtil.getAppVersion()));

        String paramString = URLEncodedUtils.format(nameValuePairs, "utf-8");
        
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url + paramString);
		
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Log.i(TAG,response.getStatusLine().toString());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				instream.close();
				return result;
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
		return null;
	}
}
