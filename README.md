<img src="https://raw.githubusercontent.com/ClaudeChey/lat_compass/main/images/screenshot.gif" width=200>


# lat_compass

[![pub package](https://img.shields.io/pub/v/lat_compass.svg)](https://pub.dartlang.org/packages/lat_compass)

A compass that provides true north and magnetic north

It is based on data provided by the Android and iOS platforms

See the [example app](https://github.com/ClaudeChey/lat_compass/blob/main/example/lib/main.dart) for more details


## Getting started

```yaml
dependencies:
  lat_compass: ^1.0.4
```


## Android
For Android, you need to have location information to calculate true north

Use an external plugin like [Permission_handler](https://pub.dev/packages/permission_handler)  to request location permission

The plugin will get the location permission on its own and stream the true north when it receives the location information

(Note that until location information is received, true north and magnetic north are the same)


## Usage
```dart
LatCompass().stream?.listen((event) {
  event?.magneticHeading;
  event?.trueHeading;
  event?.accuracy;
})
```
