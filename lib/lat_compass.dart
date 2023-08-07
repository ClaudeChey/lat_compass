import 'package:flutter/services.dart';

class CompassEvent {
  const CompassEvent({
    required this.magneticHeading,
    required this.trueHeading,
    required this.accuracy,
  });

  final double magneticHeading;
  final double trueHeading;
  final double accuracy;

  @override
  String toString() =>
      'CompassEvent(magneticHeading: $magneticHeading, trueHeading: $trueHeading, accuracy: $accuracy)';
}

class LatCompass {
  static final LatCompass _instance = LatCompass._();
  factory LatCompass() => _instance;
  LatCompass._();

  static const EventChannel _eventChannel = EventChannel('lat_compass/event');
  Stream<CompassEvent>? _stream;

  Stream<CompassEvent> get onUpdate {
    _stream ??= _eventChannel.receiveBroadcastStream().map((dynamic raw) {
      final data = raw as List;
      final magneticHeading = data[0];
      final trueHeading = data[1];
      final accuracy = data[2];
      return CompassEvent(
          magneticHeading: magneticHeading,
          trueHeading: trueHeading,
          accuracy: accuracy);
    });
    return _stream!;
  }
}
