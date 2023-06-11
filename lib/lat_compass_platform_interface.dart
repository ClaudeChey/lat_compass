import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'lat_compass_method_channel.dart';

abstract class LatCompassPlatform extends PlatformInterface {
  /// Constructs a LatCompassPlatform.
  LatCompassPlatform() : super(token: _token);

  static final Object _token = Object();

  static LatCompassPlatform _instance = MethodChannelLatCompass();

  /// The default instance of [LatCompassPlatform] to use.
  ///
  /// Defaults to [MethodChannelLatCompass].
  static LatCompassPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [LatCompassPlatform] when
  /// they register themselves.
  static set instance(LatCompassPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }
}
