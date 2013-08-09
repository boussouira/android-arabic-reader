package org.geometerplus.zlibrary.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.arabicReader.R;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ZLAdUtil {

	public AdManager m_adManager;
	
	public class AdMessageButton {
		public String text;
		public String action;
		public String link;
	}
	
	public class AdMessage {
		public String id;
		public String text;
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

	class runThread extends AsyncTask<Integer, Integer, Integer> {
		public AdView adView;

		runThread(AdView ad) {
			adView = ad;
		}
		
		protected Integer doInBackground(Integer... topDocs) {
			publishProgress(0);

			return 0;
		}

		protected void onProgressUpdate(Integer... result) {
			// Initiate a generic request to load it with an ad
			AdRequest request = new AdRequest();

			 //request.addTestDevice(AdRequest.TEST_EMULATOR);
			 //request.addTestDevice("E83D20734F72FB3108F104ABC0FFC738"); 

			adView.loadAd(request);

		}
	}
	
	class AdUpdateTask extends TimerTask {
		private AdView adView;

		AdUpdateTask(AdView ad) {
			adView = ad;
		}

		public void run() {
			new runThread(adView).execute(0);
		}
	};
	   
	public class AdManager extends AsyncTask<Integer, Integer, Integer> implements OnClickListener, AdListener {
		private static final String MY_AD_UNIT_ID = "a151ea9a7f3440c";
		private AdView m_adView;
		private ImageButton m_adHideButton;
		private Timer m_timer = null;
		private AdConfig m_config;
		private RelativeLayout m_rootView;
		private Activity m_parentActivity;

		public AdManager(Activity parentActivity, RelativeLayout rootView) {
			m_parentActivity = parentActivity;
			m_rootView = rootView;
		}

		protected Integer doInBackground(Integer... topDocs) {
			m_config = getAdConfig();

			publishProgress(0);
			return 0;
		}

		protected void onProgressUpdate(Integer... result) {
			// Create the adView
		    m_adView = new AdView(m_parentActivity, AdSize.BANNER, MY_AD_UNIT_ID);
	    	//adView.setAlpha(0.5f);

			if (m_config.showAd == true) {
				m_adHideButton = new ImageButton(m_parentActivity);
				m_adHideButton.setImageResource(R.drawable.ic_ads_error);
				m_adHideButton.setPadding(0, 0, 0, 0);
				m_adHideButton.setVisibility(View.GONE);

				m_adView.addView(m_adHideButton, 20, 20);

				LayoutParams parms = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
						10);

				m_rootView.addView(m_adView, parms);

				m_adHideButton.setOnClickListener(this);
				m_adView.setAdListener(this);

				adShowShedule();
			}
		    
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
			if (btn.action.contains("link") && btn.link != null)
				m_parentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(btn.link)));

			if (btn.action.contains("remind"))
				opt.setValue(true);
			
			if (btn.action.contains("hide"))
				dialog.cancel();

			if (btn.action.contains("close"))
				m_parentActivity.finish();
		}
		// Ad Listener
		@Override
		public void onDismissScreen(Ad arg0) {
		}

		@Override
		public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
			
		}

		@Override
		public void onLeaveApplication(Ad arg0) {
		}

		@Override
		public void onPresentScreen(Ad arg0) {
			hideAd();
		}

		@Override
		public void onReceiveAd(Ad arg0) {
			m_adView.setVisibility(View.VISIBLE);
			m_adHideButton.setVisibility(View.VISIBLE);
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) m_adView.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_BASELINE, R.id.root_view);

			m_adView.setLayoutParams(params);
		}

		// Hide button
		@Override
		public void onClick(View v) {
			hideAd();
		}
		
		public void hideAd() {
			m_adView.setVisibility(View.GONE);
			m_adHideButton.setVisibility(View.GONE);
			
			m_adView.loadAd(null);
		}
		
		public void adShowShedule() {
			if(m_timer != null) {
				m_timer.cancel();
			}
			
			m_timer = new Timer();
			
			m_timer.schedule(new AdUpdateTask(m_adView),
					m_config.displayAdDelay * 1000, m_config.updateInterval * 1000);

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

			config.showAd = json.getBoolean("showAd");
			config.updateInterval = json.getInt("updateInterval");
			config.displayAdDelay = json.getInt("displayAdDelay");

			if (json.has("message")) {
				JSONObject message = json.getJSONObject("message");
				config.message = new AdMessage();

				if (message.has("id"))
					config.message.id = message.getString("id");

				if (message.has("text"))
					config.message.text = message.getString("text");

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
