package org.geometerplus.zlibrary.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.arabicReader.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
	
	public class AdConfig {
		public boolean showAd = true;
		public int updateInterval = 10 * 60; // Update interval in seconds
		public int displayAdDelay = 1;
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

			m_adHideButton = new ImageButton(m_parentActivity);
			m_adHideButton.setImageResource(R.drawable.ic_ads_error);
			m_adHideButton.setPadding(0, 0, 0, 0);
			m_adHideButton.setVisibility(View.GONE);
			
			m_adView.addView(m_adHideButton, 20, 20);

			LayoutParams parms = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 10);
			
			m_rootView.addView(m_adView, parms);

			m_adHideButton.setOnClickListener(this);
			m_adView.setAdListener(this);
			
			adShowShedule();
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String getHttpResponse(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
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
