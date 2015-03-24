package ca.appspace.pi;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action;
import rx.functions.Action1;
import rx.functions.Func1;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiGpioProvider;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.impl.PinImpl;

public class PirSensor {
    
    public static void main(String[] args) throws InterruptedException {
    	final Pin PIR_SENSOR_PIN = new PinImpl(RaspiGpioProvider.NAME, 
    			21, "PIR Sensor connection", 
                EnumSet.of(PinMode.DIGITAL_INPUT),
                EnumSet.of(PinPullResistance.OFF));

    	final Pin GPIO_25 = new PinImpl(RaspiGpioProvider.NAME, 25, "GPIO 25", 
                EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                PinPullResistance.all());
    	final Pin GPIO_26 = new PinImpl(RaspiGpioProvider.NAME, 26, "GPIO 26", 
                EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                PinPullResistance.all());
    	
        System.out.println("<--Pi4J--> GPIO Blink Example ... started.");
        
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
        
        // provision gpio pin #01 & #03 as an output pins and blink
        final GpioPinDigitalOutput led1 = gpio.provisionDigitalOutputPin(GPIO_25);
        final GpioPinDigitalOutput led2 = gpio.provisionDigitalOutputPin(GPIO_26);

        // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput pirSensorInput = gpio.provisionDigitalInputPin(PIR_SENSOR_PIN, PinPullResistance.OFF);

        Observable<Boolean> pirSensorStateChanges = ObservablePinInput.observablePin(pirSensorInput);
        
        Observable<Integer> stateChangeCounts = pirSensorStateChanges.buffer(1, TimeUnit.MINUTES).map(new Func1<List<Boolean>, Integer>() {
			@Override
			public Integer call(List<Boolean> t1) {
				System.out.println("List of state changes: "+t1);
				return t1.size();
			}
		});
        
        stateChangeCounts.subscribe(new Action1<Integer>() {
			@Override
			public void call(Integer t1) {
				System.out.println("Received state change count: "+t1);
			}
        });
        // create and register gpio pin listener
        pirSensorInput.addListener(new GpioPinListenerDigital() {
                
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    // when button is pressed, speed up the blink rate on LED #2
                    if(event.getState().isHigh()){
                      led2.blink(200);
                    }                        
                    else{
                      led2.blink(1000);
                    }
                }
            });

        // continuously blink the led every 1/2 second for 15 seconds
        //led1.blink(500, 15000);

        // continuously blink the led every 1 second 
        //led2.blink(1000);
        
        System.out.println(" ... the LED will continue blinking until the program is terminated.");
        System.out.println(" ... PRESS <CTRL-C> TO STOP THE PROGRAM.");
        
        // keep program running until user aborts (CTRL-C)
        for (;;) {
            Thread.sleep(500);
        }
        
        // stop all GPIO activity/threads
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller
    }
}

//END SNIPPET: blink-gpio-snippet
