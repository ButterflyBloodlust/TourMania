package com.hal9000.tourmania.ui.join_tour;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

public abstract class QrCodeAnalyzer implements ImageAnalysis.Analyzer {
    private AtomicBoolean isAnalyzing = new AtomicBoolean(false);

    private FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build();
    private FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
    private OnSuccessListener<List<FirebaseVisionBarcode>> onSuccessListener = new OnSuccessListener<List<FirebaseVisionBarcode>>() {
        @Override
        public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
            onQrCodesDetected(firebaseVisionBarcodes);
            isAnalyzing.set(false);
        }
    };

    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            Log.d("crashTest", "something went wrong", e);
            isAnalyzing.set(false);
        }
    };

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (isAnalyzing.get())
            return;
        isAnalyzing.set(true);

        int rotation = rotationDegreesToFirebaseRotation(rotationDegrees);
        FirebaseVisionImage visionImage = FirebaseVisionImage.fromMediaImage(image.getImage(), rotation);

        detector.detectInImage(visionImage)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    private int rotationDegreesToFirebaseRotation(int rotationDegrees) {
        switch (rotationDegrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(rotationDegrees + " rotation degrees not supported");
        }
    }

    abstract void onQrCodesDetected(List<FirebaseVisionBarcode> qrCodes);
}
