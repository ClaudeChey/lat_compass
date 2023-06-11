import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:geolocator/geolocator.dart';
import 'package:lat_compass/lat_compass.dart';
import 'dart:math' as math;

import 'package:lat_compass_example/tween_rotation_degree.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);

  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with SingleTickerProviderStateMixin {
  late AnimationController controller;
  late Animation<double> animation;
  late TweenRotationDegree tween;

  double _radian = 0;
  StreamSubscription? _subscription;
  CompassEvent? _compassEvent;

  double degToRad(double value) => value * math.pi / 180;

  @override
  void dispose() {
    _subscription?.cancel();
    controller.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();

    controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 300),
    );
    animation = CurvedAnimation(
      parent: controller,
      curve: Curves.fastOutSlowIn,
    );
    tween = TweenRotationDegree(begin: 0, end: 0);
    controller.addListener(() {
      final value = tween.evaluate(animation);
      _radian = degToRad(value) * -1;
      setState(() {});
    });

    _subscription = LatCompass().stream?.listen((event) {
      if (Platform.isIOS) {
        _radian = degToRad(event.magneticHeading % 360) * -1;
        setState(() {});
      } else {
        // animation for android
        tween = TweenRotationDegree(
          begin: (_compassEvent?.magneticHeading ?? 0) % 360,
          end: event.magneticHeading % 360,
        );
        controller
          ..reset()
          ..forward();
      }
      _compassEvent = event;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Lat Compass example app'),
          actions: [
            TextButton(
              onPressed: () async {
                await Geolocator.requestPermission();
              },
              child: const Icon(
                Icons.gps_fixed,
                color: Colors.white,
              ),
            ),
          ],
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Transform.rotate(
                angle: _radian,
                child: Image.asset('assets/compass.jpg'),
              ),
              Text(
                'Magnetic: ${_compassEvent?.magneticHeading}',
                textAlign: TextAlign.center,
              ),
              Text(
                'True: ${_compassEvent?.trueHeading}',
                textAlign: TextAlign.center,
              ),
              Text(
                'Accuracy: ${_compassEvent?.accuracy}',
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
