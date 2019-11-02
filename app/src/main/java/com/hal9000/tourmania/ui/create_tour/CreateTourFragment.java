package com.hal9000.tourmania.ui.create_tour;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hal9000.tourmania.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;

import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_TOP;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_JUSTIFY_AUTO;

public class CreateTourFragment extends Fragment implements PermissionsListener, OnCameraTrackingChangedListener {

    private CreateTourViewModel createTourViewModel;

    public static CreateTourFragment newInstance() {
        return new CreateTourFragment();
    }

    private MapView mapView;
    private SymbolManager symbolManager;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private boolean isInTrackingMode;

    private View annotationInfoView;
    private long viewStubVisibleId = -1;
    private Symbol selectedSymbol = null;

    private static final String ID_ICON_MARKER = "place-marker-red-24";
    private static final String ID_ICON_MARKER_SELECTED = "place-marker-yellow-24";
    private static final double ICON_MARKER_SCALE = 2d;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                if (viewStubVisibleId != -1) {
                    hideAnnotationInfoView();
                }
                else
                    Navigation.findNavController(requireView()).popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        createTourViewModel =
                ViewModelProviders.of(this).get(CreateTourViewModel.class);
        View root = inflater.inflate(R.layout.fragment_create_tour, container, false);
        return root;
    }

    @Override
    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token));

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                CreateTourFragment.this.mapboxMap = mapboxMap;

                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        // Add custom markers to style
                        Drawable markerIconDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_place_red_24dp);
                        Bitmap bitmap = Bitmap.createBitmap((int)(markerIconDrawable.getIntrinsicWidth() * ICON_MARKER_SCALE),
                                (int)(markerIconDrawable.getIntrinsicHeight() * ICON_MARKER_SCALE), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        markerIconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        markerIconDrawable.draw(canvas);
                        style.addImage(ID_ICON_MARKER, bitmap);

                        // Add custom markers to style
                        markerIconDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_place_yellow_24dp);
                        bitmap = Bitmap.createBitmap((int)(markerIconDrawable.getIntrinsicWidth() * ICON_MARKER_SCALE),
                                (int)(markerIconDrawable.getIntrinsicHeight() * ICON_MARKER_SCALE), Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(bitmap);
                        markerIconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        markerIconDrawable.draw(canvas);
                        style.addImage(ID_ICON_MARKER_SELECTED, bitmap);

                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        enableLocationComponent(style);

                        // create symbol manager
                        GeoJsonOptions geoJsonOptions = new GeoJsonOptions().withTolerance(0.4f);
                        symbolManager = new SymbolManager(mapView, CreateTourFragment.this.mapboxMap, style, null, geoJsonOptions);

                        symbolManager.addClickListener(new OnSymbolClickListener() {
                            @Override
                            public void onAnnotationClick(Symbol symbol) {
                                toggleAnnotationInfoView(symbol);
                                //Toast.makeText(requireContext(),String.format("Symbol clicked %s", symbol.getId()),Toast.LENGTH_SHORT).show();
                                //Log.d("crashTest", symbolManager.getAnnotations().toString());
                            }
                        });

                        // set non data driven properties
                        symbolManager.setIconAllowOverlap(true);
                        //symbolManager.setTextAllowOverlap(true);
                        symbolManager.setTextOptional(true);
                        symbolManager.setTextVariableAnchor(new String[]{TEXT_ANCHOR_BOTTOM, TEXT_ANCHOR_TOP});

                        // DEBUG ANNOTATION
                        SymbolOptions symbolOptions = new SymbolOptions()
                                .withLatLng(new LatLng(10, 10))
                                .withIconImage(ID_ICON_MARKER)
                                .withTextOffset(new Float[]{0f, -1.5f})
                                .withTextField("Annotation text");
                        symbolManager.create(symbolOptions);
                    }
                });

                mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public boolean onMapLongClick(@NonNull LatLng point) {
                        //Log.d("crashTest", "onMapLongClick()");
                        SymbolOptions symbolOptions = new SymbolOptions()
                                .withLatLng(point)
                                .withIconImage(ID_ICON_MARKER)
                                .withTextOffset(new Float[]{0f, -1.5f})
                                .withIconOffset(new Float[]{0f, -20f})
                                .withTextField("Annotation text")
                                .withTextJustify(TEXT_JUSTIFY_AUTO);
                        symbolManager.create(symbolOptions);

                        //PointF pointF = CreateTourFragment.this.mapboxMap.getProjection().toScreenLocation(point);
                        //Toast.makeText(requireContext(), String.format("User clicked at: %s\n%s", point.toString(), pointF.toString()), Toast.LENGTH_LONG).show();

                        return false;   // returning false allows annotation's OnSymbolLongClickListener to be called (not very useful, since annotations' listeners are after onMapLongClick
                    }
                });
            }
        });
    }

    private void toggleAnnotationInfoView(Symbol symbol) {
        if (annotationInfoView == null) {
            annotationInfoView = requireView().findViewById(R.id.layoutMarkerInfoView);
            ImageButton buttonDeleteAnnotation = annotationInfoView.findViewById(R.id.buttonDeleteAnnotation);
            buttonDeleteAnnotation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    symbolManager.delete(selectedSymbol);
                    hideAnnotationInfoView();
                }
            });
        }
        if (viewStubVisibleId == symbol.getId()) {
            hideAnnotationInfoView();
        }
        else {
            TextView textViewInViewStub = annotationInfoView.findViewById(R.id.textViewAnnotationText);
            selectedSymbol = symbol;
            viewStubVisibleId = symbol.getId();
            textViewInViewStub.setText(symbol.getTextField());   // String.format("%d", viewStubVisibleId)
            annotationInfoView.setVisibility(View.VISIBLE);
        }
    }

    private void hideAnnotationInfoView() {
        if (annotationInfoView != null) {
            annotationInfoView.setVisibility(View.GONE);
            viewStubVisibleId = -1;
            selectedSymbol = null;
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        final FragmentActivity fragmentActivity = requireActivity();
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(fragmentActivity)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(fragmentActivity, loadedMapStyle)
                            //.locationComponentOptions(locationComponentOptions)
                            .build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.NONE);//, 0L, 12d, null, null, null);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            // Add the camera tracking listener. Fires if the map camera is manually moved.
            locationComponent.addOnCameraTrackingChangedListener(this);

            fragmentActivity.findViewById(R.id.back_to_camera_tracking_mode).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isInTrackingMode) {
                        isInTrackingMode = true;
                        LocationComponent locationComponent = mapboxMap.getLocationComponent();
                        locationComponent.setCameraMode(CameraMode.TRACKING, 750L, 16d, null, null, null);
                        //locationComponent.zoomWhileTracking(16f);
                        Toast.makeText(requireContext(), getString(R.string.tracking_enabled),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.tracking_already_enabled),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(fragmentActivity);
        }
    }

    @Override
    public void onCameraTrackingDismissed() {
        isInTrackingMode = false;
    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {
        // Empty on purpose
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(requireActivity(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(requireActivity(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            //finish();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createTourViewModel = ViewModelProviders.of(this).get(CreateTourViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

}
