import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class ImageSaver {
  static const MethodChannel _platform =
      const MethodChannel("com.fdt/save_in_gallery_channel");

  static const String _saveImageMethodKey = "saveImageKey";
  static const String _saveImagesMethodKey = "saveImagesKey";
  static const String _saveNamedImagesMethodKey = "saveNamedImagesKey";

  /// Saves image in gallery in a selected directory
  /// [name] is optional image file name (Android only, you can't name image files on iOS)
  /// [directoryName] is optional directory name. If [null] then saves in default directory
  /// Returns [true] if successfully saved, [false] if failed or user cancelled action
  Future<bool> saveImage({
    @required Uint8List imageBytes,
    String imageName,
    String directoryName,
  }) async {
    try {
      final bool result = await _platform.invokeMethod(
        _saveImageMethodKey,
        {
          "imageBytes": imageBytes,
          "imageName": imageName,
          "directoryName": directoryName,
        },
      );
      return result;
    } on PlatformException catch (e) {
      return false;
    }
  }

  /// Saves multiple images in gallery
  /// All images in [imageBytes] list will be saved with auto generated names
  /// Returns [true] if all images were successfully saved, [false] if any failed or user cancelled action
  /// [directoryName] is optional directory name. If [null] then saves in default directory
  Future<bool> saveImages({
    @required List<Uint8List> imageBytes,
    String directoryName,
  }) async {
    assert(
      imageBytes != null && imageBytes.isNotEmpty,
      "imageBytes must not be null and must not be empty",
    );
    try {
      final bool result = await _platform.invokeMethod(
        _saveImagesMethodKey,
        {
          "directoryName": directoryName,
          "images": imageBytes,
        },
      );
      return result;
    } on PlatformException catch (e) {
      return false;
    }
  }

  /// Saves multiple images in gallery
  /// All images in [namedImageBytes] map will be saved with provided names
  /// (Android only, you can't name image files on iOS)
  /// [directoryName] is optional directory name. If [null] then saves in default directory
  /// Returns [true] if all images were successfully saved, [false] if any failed or user cancelled action
  Future<bool> saveNamedImages({
    @required Map<String, Uint8List> namedImageBytes,
    String directoryName,
  }) async {
    assert(
      namedImageBytes != null && namedImageBytes.isNotEmpty,
      "namedImageBytes must not be null and must not be empty",
    );
    try {
      final bool result = await _platform.invokeMethod(
        _saveNamedImagesMethodKey,
        {
          "directoryName": directoryName,
          "images": namedImageBytes,
        },
      );
      return result;
    } on PlatformException catch (e) {
      return false;
    }
  }
}
