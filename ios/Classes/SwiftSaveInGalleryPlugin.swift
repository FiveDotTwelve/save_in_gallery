import Flutter
import UIKit

public class SwiftSaveInGalleryPlugin: NSObject, FlutterPlugin {
    var imageSaver: ImageSaver?

    let errorMessage = "Saving in gallery error"

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "com.fdt/save_in_gallery_channel", binaryMessenger: registrar.messenger())
        let instance = SwiftSaveInGalleryPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        self.imageSaver = ImageSaver(onImagesSave: { (success) in
            if success {
                result(true)
            } else {
                result(channelError(message: self.errorMessage))
            }
        })
        guard let imageSaver = imageSaver else {
            result(channelError(message: errorMessage))
            return
        }

        if call.method == "saveImageKey" {
            guard let params = call.arguments as? [String: Any],
                let imageData = params["image"] as? FlutterStandardTypedData,
                let image = UIImage(data: imageData.data)
            else {
                    result(channelError(message: errorMessage))
                    return
            }
            imageSaver.saveImage(image)
        } else if call.method == "saveImagesKey" {
            guard let params = call.arguments as? [String: Any],
                var images = params["images"] as? [FlutterStandardTypedData],
                let namedImages = params["namedImages"] as? [String: FlutterStandardTypedData]
            else {
                    result(channelError(message: errorMessage))
                    return
            }
            images.append(contentsOf: Array(namedImages.values))
            imageSaver.saveImages(images.compactMap({ UIImage(data: $0.data) }))
        }
    }
}

private func channelError(message: String) -> FlutterError {
    return FlutterError(code: "ERROR", message: message, details: nil)
}
