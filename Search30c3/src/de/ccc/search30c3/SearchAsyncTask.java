package de.ccc.search30c3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SearchAsyncTask extends
		AsyncTask<String, Integer, SearchResultList> {

	private static String SERVER_URL = "http://30c3-conference.yacy.net/";
	private static int START_INDEX = 0;

	private Context caller;

	public SearchAsyncTask(Context context) {
		super();
		this.caller = context;
	}

	@Override
	protected SearchResultList doInBackground(String... search) {
		int beginItem = START_INDEX;
		try {
			beginItem = Integer.parseInt(search[1]);
		} catch (IndexOutOfBoundsException e) {
			// nothing to do
		}
		String resultString = executeSearch(search[0], beginItem);
		return parseResultList(resultString);
	}

	protected void onProgressUpdate(Integer... progress) {

	}

	protected void onPostExecute(SearchResultList result) {
		Log.d("Async Task", "onPostExecute");
		if (MainActivity.class == caller.getClass()) {
			((MainActivity) caller).displayResults(result);
		}

	}

	private SearchResultList parseResultList(String resultString) {
		SearchResultList results = new SearchResultList();

		if (null == resultString)
			return results;
		try {
			JSONObject fullJson = new JSONObject(resultString);
			JSONArray channels = fullJson.getJSONArray("channels");
			JSONObject channel = channels.getJSONObject(0);
			
			//ERROR IN REPLY!
			results.maxItems = channel.getInt("totalResults");
			results.pageSize = channel.getInt("itemsPerPage");
			results.firstItemNumber = channel.getInt("startIndex");

			
			//parse results
			JSONArray resultList = channel.getJSONArray("items");
			for (int counter = 0; counter < resultList.length(); counter++) {
				results.add(new SearchResultItem((JSONObject) resultList
						.get(counter)));
				((SearchResultItem) results.get(counter)).index = results.firstItemNumber + counter;
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}

	private String parseHttpResponse(HttpResponse response) {
		if (null == response)
			return null;

		String resultString = null;
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		String line = null;
		try {
			br = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			resultString = sb.toString();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return resultString;
	}

	private String executeSearch(String search, int beginResult) {
		// AndroidHttpClient httpClient = AndroidHttpClient
		// .newInstance("AndroidHttpClient");

		HttpClient httpClient = new DefaultHttpClient();
		String request = null;
		try {
			request = SERVER_URL + "yacysearch.json?query="
					+ URLEncoder.encode(search, "UTF-8") + "&startRecord=" + beginResult;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpGet getRequest = new HttpGet(request);

		HttpResponse response = null;
		try {
			response = httpClient.execute(getRequest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if (null != httpClient) {
		// httpClient.;
		// }

		String result = parseHttpResponse(response);

		return result;
	}

}
