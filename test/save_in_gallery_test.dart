import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:save_in_gallery/save_in_gallery.dart';

void main() {
  const MethodChannel channel = MethodChannel('save_in_gallery');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {

  });
}
