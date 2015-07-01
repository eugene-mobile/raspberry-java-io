/*******************************************************************************
 * Copyright (c) ecobee, Inc. 2015. All rights reserved.
 *******************************************************************************/
package ca.appspace.pi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

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
public class GitHubUsers
{
	public static void main(String... args) throws InterruptedException {

		Observable<String> requestStream = Observable
				.just("https://api.github.com/users")
				.mergeWith(Observable.timer(10, 10, TimeUnit.SECONDS).map(new Func1<Long, String>() {
					@Override
					public String call(Long t) {
						return "https://api.github.com/users?since="+Math.floor(Math.random()*500);
					}
				}));
				
		
		Observable<Response> responseStream = requestStream.flatMap(new Func1<String, Observable<Response>>() {
			@Override
			public Observable<Response> call(final String url) {
				return Observable.create(new OnSubscribe<Response>() {
					@Override
					public void call(final Subscriber<? super Response> observer) {
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
					}
				});
			}
		});
		
		Observable<JsonArray> streamOfUserArrays = responseStream.map(new Func1<Response, JsonArray>() {
			@Override
			public JsonArray call(Response response) {
				if (response.isSuccessful()) {
					try {
						JsonElement json = new JsonParser().parse(response.body().charStream()); 
						if (json.isJsonArray()) {
							return json.getAsJsonArray();
						}
					} catch (Exception e) {}
				}
				return null;
			}
		});
		
		Observable<JsonElement> streamOfUsers = streamOfUserArrays.flatMap(new Func1<JsonArray, Observable<JsonElement>>() {
			@Override
			public Observable<JsonElement> call(JsonArray arrayOfUsers) {
				return Observable.from(arrayOfUsers);
			}
			
		});
		
		streamOfUsers.subscribe(new Action1<JsonElement>() {
			@Override
			public void call(JsonElement userJson) {
				System.out.println("Succesfull response: "+userJson.toString());
			}
		});

		Thread.sleep(30000);
		System.out.println("Done");
	}
}
