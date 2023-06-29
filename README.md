<img src="https://raw.githubusercontent.com/ClaudeChey/lat_compass/main/images/screenshot.gif" width=200>


# lat_compass

[![pub package](https://img.shields.io/pub/v/lat_compass.svg)](https://pub.dartlang.org/packages/lat_compass)

A compass plugin written in native.

Provides true north, magnetic north, and accuracy.

See the [example app](https://github.com/ClaudeChey/lat_compass/blob/main/example/lib/main.dart) for more details.

## Getting started

```yaml
dependencies:
  lat_compass: ^1.0.3
```

## Usage
```dart
LatCompass().stream?.listen((event) {
  event?.magneticHeading;
  event?.trueHeading;
  event?.accuracy;
})
```
