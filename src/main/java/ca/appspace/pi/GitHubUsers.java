/*******************************************************************************
 * Copyright (c) ecobee, Inc. 2015. All rights reserved.
 *******************************************************************************/
package ca.appspace.pi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * Following this article: 
 * 		https://gist.github.com/staltz/868e7e9bc2a7b8c1f754
 * @author Eugene
 *
 */
public class GitHubUsers {

	public static void main(String... args) throws InterruptedException {

		Observable<String> requestStream = Observable
			.just("https://api.github.com/users")
			.mergeWith(Observable.timer(10, 10, TimeUnit.SECONDS).map(
					t-> "https://api.github.com/users?since="+Math.floor(Math.random()*500) 
			));

		Observable<Response> responseStream = requestStream.flatMap(url-> {
			return Observable.create(observer -> {
				Request request = new Request.Builder()
				.url(url)
				.get().build();
				OkHttpClient client = new OkHttpClient();
				client.newCall(request).enqueue(new Callback() {
					@Override
					public void onResponse(final Response response) {
						if (response.isSuccessful()) {
							observer.onNext(response);
						} else {
							observer.onCompleted();
						}
					}
					
					@Override
					public void onFailure(Request request, IOException e) {
						e.printStackTrace();
						observer.onError(e);
					}
				});
			});
		});

		Observable<JsonArray> streamOfUserArrays = responseStream.map(response -> {
			if (response.isSuccessful()) {
				try {
					JsonElement json = new JsonParser().parse(response.body().charStream()); 
					if (json.isJsonArray()) {
						return json.getAsJsonArray();
					}
				} catch (Exception e) {}
			}
			return null;
		});
		
		Observable<JsonElement> streamOfUsers = streamOfUserArrays.flatMap(arrayOfUsers -> Observable.from(arrayOfUsers));
		
		streamOfUsers.subscribe(userJson -> System.out.println("Succesfull response: "+userJson.toString())); 

		Thread.sleep(30000);
		System.out.println("Done");
	}
}
