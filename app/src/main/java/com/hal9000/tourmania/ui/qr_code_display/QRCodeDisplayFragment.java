package com.hal9000.tourmania.ui.qr_code_display;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModel;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModelFactory;

public class QRCodeDisplayFragment extends Fragment {

    private QRCodeDisplayViewModel mViewModel;
    private CreateTourSharedViewModel createTourSharedViewModel;

    public static QRCodeDisplayFragment newInstance() {
        return new QRCodeDisplayFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code_display, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(QRCodeDisplayViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Scope ViewModel to nested nav graph.
        ViewModelStoreOwner owner = Navigation.findNavController(view).getViewModelStoreOwner(R.id.nav_nested_create_tour);
        //CreateTourSharedViewModelFactory factory = new CreateTourSharedViewModelFactory();
        ViewModelProvider.AndroidViewModelFactory factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication());
        createTourSharedViewModel = new ViewModelProvider(owner, factory).get(CreateTourSharedViewModel.class);

        String inputText = QRCodeDisplayFragmentArgs.fromBundle(getArguments()).getInputText();
        Log.d("crashTest", "QRCodeDisplayFragment inputText: " + inputText);

        generateQRCode(inputText, view);
    }

    private void generateQRCode(String inputText, View view) {
        final Handler myHandler = new Handler(Looper.getMainLooper());
        (new Thread(new Runnable() {
            @Override
            public void run() {
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                BitMatrix bitMatrix = null;
                try {
                    bitMatrix = multiFormatWriter.encode(inputText, BarcodeFormat.QR_CODE,300,300);
                    Bitmap qrCodeBitmap = bitMatrixToBitmap(bitMatrix);
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((ImageView) view.findViewById(R.id.imageView_qr_code)).setImageBitmap(qrCodeBitmap);
                            view.findViewById(R.id.progressBar_qr_code_generation).setVisibility(View.GONE);
                            view.findViewById(R.id.imageView_qr_code).setVisibility(View.VISIBLE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "An error occured while generating QR code", Toast.LENGTH_SHORT).show();
                }
            }
        })).start();
    }

    private Bitmap bitMatrixToBitmap(BitMatrix bitMatrix) {
        int height = bitMatrix.getHeight();
        int width = bitMatrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
}
