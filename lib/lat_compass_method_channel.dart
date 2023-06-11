import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'lat_compass_platform_interface.dart';

/// An implementation of [LatCompassPlatform] that uses method channels.
class MethodChannelLatCompass extends LatCompassPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('lat_compass');
}
