![alt text](logo.jpg "FDT logo")

# save_in_gallery

  
Flutter plugin that allows you to save images in native gallery in both Android and iOS. You can either save them in default album or in named album of your choice.

## Installation
For installation details see `Installing` tab on this page.
  
## Configuration
  
### iOS
To use this plugin in iOS you must add 2 new records to configuration file  `info.plist` located in `ios/Runner` directory. To do so, open iOS project in Xcode by running
`open ios/Runner.xcworkspace`
then right-click on `info.plist` file in project navigator and click `open as > source code`. Then add following 2 records at the end of `<dict>` object:

- `<key>NSPhotoLibraryAddUsageDescription</key>`
`<string>write</string>` 
Where `write` should be reason why your app requires write access to user's gallery. This is necessary to save images in gallery.
- `<key>NSPhotoLibraryUsageDescription</key>`
`<string>read</string>`
Where `read` should be reason why your app requires read access to user's gallery. This is necessary to create and use named albums in gallery.

Be aware that app will ask user's permission to read/write gallery access using standard iOS alerts when you will use methods provided by this plugin. These alerts' appearance can not be changed. 

If user declines access, these alerts can not be shown again, instead you must use custom controls to redirect user to Settings app, so they can grant access rights themselves. Methods provided by this plugin will not work unless user provides proper access rights.

### Android

This plugin should work out-of-the-box and does not require additional configuration.

Be aware that app will ask user's permission to `WRITE_EXTERNAL_STORAGE` using standard Android alerts when you will use methods provided by this plugin. These alerts' appearance can not be changed. 

If user declines access, these alerts can not be shown again, instead you must use custom controls to redirect user to Settings app, so they can grant access rights themselves. Methods provided by this plugin will not work unless user provides proper access rights.

## Examples

### Save multiple images from app's assets to default gallery
```dart
import  'package:save_in_gallery/save_in_gallery.dart';
final _imageSaver =  ImageSaver();
Future<void> saveAssetImage() async {
	// 1
	final urls =  [
		"assets/image1.jpg",
		"assets/image2.jpg",
		"assets/image3.jpg",
	];
	// 2 
	List<Uint8List> bytesList = [];
	for (final url in urls) {
		final bytes =  await rootBundle.load(url);
		bytesList.add(bytes.buffer.asUint8List());
	}
	// 3
	final res = await _imageSaver.saveImages(
		imageBytes: bytesList
	);
	// 4
}
```
1. Prepare list of asset URLs;
2. Load each image, extract byte data, and map to `Uint8List` type;
3. Invoke plugin method to save loaded images;
4. `res` variable will hold result of saving operation. If it's true, then everything went as expected and images have been successfully saved. Otherwise something went wrong along the way. Use this variable to provide feedback to user.

### Fetch image from web and save it to a named album in gallery
```dart
import  'package:save_in_gallery/save_in_gallery.dart';
final _imageSaver =  ImageSaver();
Future<void> saveNetworkImage() async {
	// 1
	final url = "https://some_url/image.png";
	// 2
	final image =  NetworkImage(url);
	final key =  await image.obtainKey(ImageConfiguration());
	final load = image.load(key);
	load.addListener((listener, err) async {
		// 3
		final byteData = await listener.image.toByteData(
			format:  ImageByteFormat.png
		);		
		final bytes = byteData.buffer.asUint8List();
		// 4
		final res =  await _imageSaver.saveImage(
			imageBytes: bytes,
			directoryName:  "dir_name",
		);
		// 5
	});
}
```
1. Prepapre URL to image you want to fetch;
2. Create `NetworkImage` object with your URL and load it's configuration;
3. Extract byte data from image;
4. Invoke plugin method to save fetched image in custom album named `dir_name`;
5. `res` variable will hold result of saving operation. If it's true, then everything went as expected and image have been successfully saved. Otherwise something went wrong along the way. Use this variable to provide feedback to user.
