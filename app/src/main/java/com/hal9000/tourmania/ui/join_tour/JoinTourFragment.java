package com.hal9000.tourmania.ui.join_tour;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.hal9000.tourmania.R;

import java.util.List;

public class JoinTourFragment extends Fragment {

    private JoinTourViewModel joinTourViewModel;
    private TextureView textureView;
    private HandlerThread handlerThread;

    private static final int REQUEST_CAMERA_PERMISSION_ID = 110;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        joinTourViewModel = ViewModelProviders.of(this).get(JoinTourViewModel.class);
        View root = inflater.inflate(R.layout.fragment_join_tour, container, false);

        textureView = root.findViewById(R.id.texture_view_qr_camera);

        // Request camera permissions
        if (isCameraPermissionGranted()) {
            textureView.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_ID);
        }

        /*
        Button joinTourButton = root.findViewById(R.id.button_join_tour);
        joinTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(requireView()).navigate(R.id.nav_nested_create_tour,
                        new CreateTourFragmentArgs.Builder().setTourServerId(
                                "5dee3c74121532341089d2dc").build().toBundle());
            }
        });
        */

        return root;
    }

    private void startCamera() {
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .build();
        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                textureView.setSurfaceTexture(output.getSurfaceTexture());
            }
        });
        handlerThread = new HandlerThread("QRCodeDetectionThread");
        handlerThread.start();

        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setCallbackHandler(new Handler(handlerThread.getLooper()))
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);
        QrCodeAnalyzer qrCodeAnalyzer = new QrCodeAnalyzer() {
            @Override
            void onQrCodesDetected(List<FirebaseVisionBarcode> qrCodes) {
                for (FirebaseVisionBarcode firebaseVisionBarcode : qrCodes) {
                    Log.d("crashTest", "QR Code detected: " + firebaseVisionBarcode.getRawValue());
                }
                if (!qrCodes.isEmpty()) {
                    CameraX.unbindAll();
                    requireView().findViewById(R.id.texture_view_qr_camera).setVisibility(View.GONE);
                    requireView().findViewById(R.id.group_waiting_for_server_response).setVisibility(View.VISIBLE);
                }
            }
        };
        imageAnalysis.setAnalyzer(qrCodeAnalyzer);
        CameraX.bindToLifecycle(this, preview, imageAnalysis);
    }

    private boolean isCameraPermissionGranted() {
        int permissionState = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                textureView.post(new Runnable() {
                    @Override
                    public void run() {
                        startCamera();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to scan QR code.", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handlerThread != null)
            handlerThread.quitSafely();
        handlerThread = null;
    }
}