package ca.appspace.pi;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class ObservablePinInput {

	public static Observable<Boolean> observablePin(final GpioPinDigitalInput pin) {
		return Observable.create(new OnSubscribe<Boolean>() {
			@Override
			public void call(final Subscriber<? super Boolean> subscriber) {
				pin.addListener(new GpioPinListenerDigital() {

					@Override
					public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
						subscriber.onNext(event.getState().isHigh());
					}					
				});
			}
		});
	}

}
