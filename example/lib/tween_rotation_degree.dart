import 'package:flutter/animation.dart';

class TweenRotationDegree extends Tween<double> {
  TweenRotationDegree({required super.begin, required super.end});

  @override
  double lerp(double t) {
    return _circularLerp(begin!, end!, t, 360);
  }

  double _circularLerp(double begin, double end, double t, double oneCircle) {
    final halfCircle = oneCircle / 2;
    begin = begin % oneCircle;
    end = end % oneCircle;

    final compareResult = (end - begin).abs().compareTo(halfCircle);
    final crossZero = compareResult == 1 ||
        (compareResult == 0 && begin != end && begin >= halfCircle);
    if (crossZero) {
      double opposite(double value) {
        return (value + halfCircle) % oneCircle;
      }

      return opposite(doubleLerp(opposite(begin), opposite(end), t));
    } else {
      return doubleLerp(begin, end, t);
    }
  }

  double doubleLerp(double begin, double end, double t) =>
      begin + (end - begin) * t;
}
