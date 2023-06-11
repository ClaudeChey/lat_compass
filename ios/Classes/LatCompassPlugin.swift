import Flutter
import UIKit
import CoreLocation

public class LatCompassPlugin: NSObject, FlutterPlugin, FlutterStreamHandler, CLLocationManagerDelegate {
    
    
    private var eventSink: FlutterEventSink?;
    private var location: CLLocationManager = CLLocationManager();
    
    init(channel: FlutterEventChannel) {
        super.init()
        location.delegate = self
        location.headingFilter = 0.1;
        
        channel.setStreamHandler(self);
    }
    
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let eventChannel = FlutterEventChannel(name: "lat_compass/event", binaryMessenger: registrar.messenger())
        let instance = LatCompassPlugin(channel: eventChannel)
        let channel = FlutterMethodChannel(name: "lat_compass", binaryMessenger: registrar.messenger())
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.eventSink = events;
        location.startUpdatingHeading();
        return nil;
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        location.stopUpdatingHeading();
        return nil
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        let accuracy = newHeading.headingAccuracy
        guard accuracy > 0 else { return }
        eventSink?([newHeading.magneticHeading, newHeading.trueHeading, accuracy])
    }
    
    
}
