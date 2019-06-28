//
//  ImageSaver.swift
//  Runner
//
//  Created by Patryk on 13/06/2019.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//

import Foundation

class ImageSaver: NSObject {
    private var imagesRemaining: Int = 0
    private var savingMultipleImages: Bool = false

    private var onImagesSave: ((Bool) -> Void)?

    init(onImagesSave: ((Bool) -> Void)?) {
        self.onImagesSave = onImagesSave
    }

    func saveImage(_ image: UIImage) {
        imagesRemaining = 1
        savingMultipleImages = false
        save(image)
    }

    func saveImages(_ images: [UIImage]) {
        imagesRemaining = images.count
        savingMultipleImages = true
        for image in images {
            save(image)
        }
    }

    func saveImages(_ images: [String: UIImage]) {
        savingMultipleImages = true
        imagesRemaining = images.count
        for (_, image) in images {
            save(image)
        }
    }

    private func save(_ image: UIImage) {
        UIImageWriteToSavedPhotosAlbum(
            image,
            self,
            #selector(image(_: didFinishSavingWithError: contextInfo:)),
            nil
        )
    }

    @objc func image(_ image: UIImage, didFinishSavingWithError error: Error?, contextInfo: UnsafeRawPointer) {
        if error != nil {
            onSave(success: false)
            return
        }
        imagesRemaining -= 1
        if imagesRemaining == 0 {
            onSave(success: true)
        }
    }

    func onSave(success: Bool) {
        onImagesSave?(success)
        if !success {
            onImagesSave = nil
        }
    }
}


