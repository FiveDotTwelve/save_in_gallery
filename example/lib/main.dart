import 'dart:async';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:save_in_gallery/save_in_gallery.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final imageSaver = ImageSaver();

  @override
  void initState() {
    super.initState();
  }

  Future<void> saveNetworkImage() async {
    final url =
        "https://solarsystem.nasa.gov/system/downloadable_items/519_solsticeflare.jpg";
    final image = NetworkImage(url);
    final key = await image.obtainKey(ImageConfiguration());
    final load = image.load(key);
    load.addListener((listener, err) async {
      final byteData =
          await listener.image.toByteData(format: ImageByteFormat.png);
      final bytes = byteData.buffer.asUint8List();
//      final res = await imageSaver.saveImage(imageBytes: bytes);
//      final res = await imageSaver.saveImages(imageBytes: [bytes]);
      final res = await imageSaver
          .saveNamedImages(namedImageBytes: {"testImageName": bytes});
      print(res);
    });
  }

  Future<void> saveAssetImage() async {
    final url = "assets/sun.jpg";
    final bytes = await rootBundle.load(url);
    final res =
        await imageSaver.saveImage(imageBytes: bytes.buffer.asUint8List());
    print(res);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: RaisedButton(
            onPressed: () => saveNetworkImage(),
            child: Text("press"),
          ),
        ),
      ),
    );
  }
}
