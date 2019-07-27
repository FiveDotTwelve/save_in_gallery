import 'dart:async';
import 'dart:ui';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:save_in_gallery/save_in_gallery.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _imageSaver = ImageSaver();

  bool _isLoading = false;
  bool _showResult = false;
  String _resultText = "";
  Color _resultColor = Colors.red;

  @override
  void initState() {
    super.initState();
  }

  /// Fetches image from web and saves in gallery
  Future<void> saveNetworkImage() async {
    _startLoading();
    final url =
        "https://solarsystem.nasa.gov/system/downloadable_items/519_solsticeflare.jpg";
    final image = NetworkImage(url);
    final key = await image.obtainKey(ImageConfiguration());
    final load = image.load(key);
    load.addListener(
      ImageStreamListener((listener, err) async {
        final byteData =
            await listener.image.toByteData(format: ImageByteFormat.png);
        final bytes = byteData.buffer.asUint8List();
        final res = await _imageSaver.saveImage(
          imageBytes: bytes,
          directoryName: "dir_name",
        );
        _stopLoading();
        _displayResult(res);
        print(res);
      }),
    );
  }

  /// Saves one of asset images to gallery
  Future<void> saveAssetImage() async {
    _startLoading();
    final urls = ["assets/sun.jpg"];
    List<Uint8List> bytesList = [];
    for (final url in urls) {
      final bytes = await rootBundle.load(url);
      bytesList.add(bytes.buffer.asUint8List());
    }
    final res = await _imageSaver.saveImages(imageBytes: bytesList);
    _stopLoading();
    _displayResult(res);
    print(res);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Stack(
          children: <Widget>[
            Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: <Widget>[
                Container(),
                RaisedButton(
                  onPressed: () => saveNetworkImage(),
                  child: Text("Save image from web"),
                ),
                SizedBox(height: 16.0),
                RaisedButton(
                  onPressed: () => saveAssetImage(),
                  child: Text("Save image from assets"),
                ),
                AnimatedOpacity(
                  opacity: _showResult ? 1.0 : 0.0,
                  duration: Duration(seconds: 1),
                  child: Text(
                    _resultText,
                    style: TextStyle(color: _resultColor),
                  ),
                )
              ],
            ),
            _progressIndictaor,
          ],
        ),
      ),
    );
  }

  Widget get _progressIndictaor {
    return _isLoading
        ? Container(
            child: Center(child: CircularProgressIndicator()),
            color: Color.fromRGBO(0, 0, 0, 0.3),
          )
        : Container();
  }

  void _startLoading() {
    setState(() {
      _isLoading = true;
    });
  }

  void _stopLoading() {
    setState(() {
      _isLoading = false;
    });
  }

  void _displayResult(bool success) {
    _showResult = true;
    if (success) {
      _displaySuccessMessage();
    } else {
      _displayErrorMessage();
    }
    Timer(Duration(seconds: 2), () {
      _hideResult();
    });
  }

  void _displaySuccessMessage() {
    setState(() {
      _resultText = "Images saved successfullty";
      _resultColor = Colors.green;
    });
  }

  void _displayErrorMessage() {
    setState(() {
      _resultText = "An error occurred while saving images";
      _resultColor = Colors.red;
    });
  }

  void _hideResult() {
    setState(() {
      _showResult = false;
    });
  }
}
