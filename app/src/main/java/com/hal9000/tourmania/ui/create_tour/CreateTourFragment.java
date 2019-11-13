package com.hal9000.tourmania.ui.create_tour;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hal9000.tourmania.R;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.TourWpWithPicPaths;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.geojson.Point;
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
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_JUSTIFY_AUTO;

public class CreateTourFragment extends Fragment implements PermissionsListener, OnCameraTrackingChangedListener {

    private CreateTourSharedViewModel createTourSharedViewModel;

    public static CreateTourFragment newInstance() {
        return new CreateTourFragment();
    }

    private MapView mapView;
    private SymbolManager symbolManager;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private boolean isInTrackingMode;
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsAPI";
    private NavigationMapRoute navigationMapRoute;

    private View annotationInfoView;
    private long selectedSymbolId = -1;
    private Symbol selectedSymbol = null;

    private static final String ID_ICON_MARKER = "place-marker-red-24";
    private static final String ID_ICON_MARKER_SELECTED = "place-marker-yellow-24";
    private static final double ICON_MARKER_SCALE = 2d;
    private static int PICK_IMAGE_REQUEST_CODE = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                if (selectedSymbolId != -1) {
                    hideAnnotationInfoView();
                }
                else {
                    //if (createTourSharedViewModel != null)
                    //    createTourSharedViewModel.onCleared();
                    Navigation.findNavController(requireView()).popBackStack();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_tour_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_save_route:
                new AlertDialog.Builder(requireContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Save the tour and exit")
                        .setMessage("Are you sure you want to save the tour and exit tour creation screen?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast savinToast = Toast.makeText(requireContext(),"Saving ...",Toast.LENGTH_SHORT);
                                savinToast.show();
                                Future future = createTourSharedViewModel.saveTourToDb(requireContext());
                                try {
                                    future.get();
                                } catch (ExecutionException | InterruptedException e) {
                                    //e.printStackTrace();
                                }
                                savinToast.cancel();
                                Toast.makeText(requireContext(),"Tour saved",Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(requireView()).popBackStack();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            case R.id.action_add_tour_image:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST_CODE);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri tourImgUri = data.getData();
            if (tourImgUri != null) {
                createTourSharedViewModel.getTour().setTourImgPath(tourImgUri.toString());
                try {
                    setTourImage(tourImgUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else {
                ((ImageView)requireView().findViewById(R.id.tour_image)).setImageResource(R.drawable.ic_menu_gallery);
            }

        }
    }

    private void setTourImage(Uri tourImgUri) throws FileNotFoundException {
        InputStream inStream = requireContext().getContentResolver().openInputStream(tourImgUri);
        Bitmap bmp = BitmapFactory.decodeStream(inStream);
        ImageView tourImageView = (ImageView)requireView().findViewById(R.id.tour_image);
        int viewHeight = tourImageView.getHeight();
        tourImageView.setAdjustViewBounds(true);
        tourImageView.setImageBitmap(bmp);
        tourImageView.setMaxWidth(bmp.getWidth() * viewHeight / bmp.getHeight());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_tour, container, false);
        setHasOptionsMenu(true);
        //createTourSharedViewModel = ViewModelProviders.of(requireActivity()).get(CreateTourSharedViewModel.class);
        return root;
    }

    @Override
    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {

        // Scope ViewModel to nested nav graph.
        ViewModelStoreOwner owner = Navigation.findNavController(view).getViewModelStoreOwner(R.id.nav_nested_create_tour);
        CreateTourSharedViewModelFactory factory = new CreateTourSharedViewModelFactory();
        createTourSharedViewModel = new ViewModelProvider(owner, factory).get(CreateTourSharedViewModel.class);

        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token));

        // Set up tour waypoints list fragment floating action button
        view.findViewById(R.id.fab_edit_annotated_places).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.tourWaypointsListFragment, null);
            }
        });

        // Set up route generation floating action button
        view.findViewById(R.id.fab_generate_route).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LongSparseArray<Symbol> annotations = symbolManager.getAnnotations();
                int annotationsSize = annotations.size();

                if (annotationsSize < 2) {
                    Toast toast = Toast.makeText(requireContext(),"Drawing a route requires\nat least 2 waypoints", Toast.LENGTH_LONG);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    if( v != null) v.setGravity(Gravity.CENTER);
                    toast.show();
                    return;
                }

                NavigationRoute.Builder builder = NavigationRoute.builder(requireContext())
                        .accessToken(Mapbox.getAccessToken())
                        .profile(DirectionsCriteria.PROFILE_WALKING)
                        .origin(Point.fromLngLat(annotations.valueAt(0).getLatLng().getLongitude(), annotations.valueAt(0).getLatLng().getLatitude()))
                        .destination(Point.fromLngLat(annotations.valueAt(annotationsSize - 1).getLatLng().getLongitude(), annotations.valueAt(annotationsSize - 1).getLatLng().getLatitude()));


                for (int i = 1; i < annotationsSize - 1; i++) {
                    builder.addWaypoint(Point.fromLngLat(annotations.valueAt(i).getLatLng().getLongitude(), annotations.valueAt(i).getLatLng().getLatitude()));
                }


                builder.build()
                        .getRoute(new Callback<DirectionsResponse>() {
                            @Override
                            public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
                                // You can get the generic HTTP info about the response
                                //Log.d(TAG, "Response code: " + response.code());
                                if (response.body() == null) {
                                    //Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                                    return;
                                } else if (response.body().routes().size() < 1) {
                                    //Log.e(TAG, "No routes found");
                                    return;
                                }

                                currentRoute = response.body().routes().get(0);

                                // Draw the route on the map
                                if (navigationMapRoute != null) {
                                    navigationMapRoute.removeRoute();
                                } else {
                                    navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                                }
                                navigationMapRoute.addRoute(currentRoute);
                            }

                            @Override
                            public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable throwable) {
                                //Log.e(TAG, "Error: " + throwable.getMessage());
                            }
                        });
            }
        });

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                CreateTourFragment.this.mapboxMap = mapboxMap;

                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        // Add custom marker to style
                        Drawable markerIconDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_place_red_24dp);
                        Bitmap bitmap = Bitmap.createBitmap((int)(markerIconDrawable.getIntrinsicWidth() * ICON_MARKER_SCALE),
                                (int)(markerIconDrawable.getIntrinsicHeight() * ICON_MARKER_SCALE), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        markerIconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        markerIconDrawable.draw(canvas);
                        style.addImage(ID_ICON_MARKER, bitmap);

                        // Add custom marker to style
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
                                //Log.d("crashTest", "symbolManager.ClickListener");
                            }
                        });

                        // set symbolManager / annotations' properties
                        symbolManager.setIconAllowOverlap(true);
                        //symbolManager.setTextAllowOverlap(true);
                        symbolManager.setTextOptional(true);
                        symbolManager.setTextTranslate(new Float[]{0f, -44f});
                        symbolManager.setIconTranslate(new Float[]{0f, -20f});
                        symbolManager.setTextVariableAnchor(new String[]{TEXT_ANCHOR_BOTTOM});

                        /*
                        // DEBUG ANNOTATION
                        SymbolOptions symbolOptions = new SymbolOptions()
                                .withLatLng(new LatLng(10, 10))
                                .withIconImage(ID_ICON_MARKER)
                                .withTextField("Annotation text");
                        symbolManager.create(symbolOptions);
                        */

                        // Add an annotation on long click on the map
                        CreateTourFragment.this.mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                            @Override
                            public boolean onMapLongClick(@NonNull LatLng point) {
                                //Log.d("crashTest", "onMapLongClick()");
                                addWaypoint(point);

                                //PointF pointF = CreateTourFragment.this.mapboxMap.getProjection().toScreenLocation(point);
                                //Toast.makeText(requireContext(), String.format("User clicked at: %s\n%s", point.toString(), pointF.toString()), Toast.LENGTH_LONG).show();

                                return false;   // action not consumed - returning false allows annotation's OnSymbolLongClickListener to be called (not very useful, since annotations' listeners are after onMapLongClick
                            }
                        });

                        // Restore annotations from ViewModel
                        ArrayList<TourWpWithPicPaths> tourWpWithPicPathsArrayList = createTourSharedViewModel.getTourWaypointList();
                        List<SymbolOptions> symbolOptionsList = new LinkedList<>();
                        for (TourWpWithPicPaths tourWpWithPicPaths : tourWpWithPicPathsArrayList) {
                            SymbolOptions symbolOptions = new SymbolOptions()
                                    .withLatLng(new LatLng(tourWpWithPicPaths.tourWaypoint.getLatitude(), tourWpWithPicPaths.tourWaypoint.getLongtitude()))
                                    .withIconImage(ID_ICON_MARKER)
                                    .withTextField(tourWpWithPicPaths.tourWaypoint.getTitle())
                                    .withTextJustify(TEXT_JUSTIFY_AUTO);
                            symbolOptionsList.add(symbolOptions);
                        }
                        // Mark selected annotation if aplicable
                        int choosenLocateWaypointIndex = createTourSharedViewModel.getChoosenLocateWaypointIndex();
                        if (choosenLocateWaypointIndex != -1 ) {
                            symbolOptionsList.get(choosenLocateWaypointIndex).withIconImage(ID_ICON_MARKER_SELECTED);
                            createTourSharedViewModel.removeChoosenLocateWaypointIndex();
                        }
                        // Register restored annotations
                        if (symbolOptionsList.size() > 0)
                            symbolManager.create(symbolOptionsList);
                    }
                });
            }
        });

        // Handle annotation label updates.
        ((EditText)view.findViewById(R.id.textViewAnnotationText)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (selectedSymbol != null) {
                        String waypointTitle = v.getText().toString();
                        selectedSymbol.setTextField(waypointTitle);
                        symbolManager.update(selectedSymbol);
                        createTourSharedViewModel.getTourWaypointList().get((int)selectedSymbol.getId()).tourWaypoint.setTitle(waypointTitle);
                    }
                }
                return false;   // don't consume action (allow propagation)
            }
        });

        // Handle annotation label updates.
        ((EditText)view.findViewById(R.id.text_input_edit_text_create_tour)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    createTourSharedViewModel.getTour().setTitle(v.getText().toString());
                }
                return false;   // don't consume action (allow propagation)
            }
        });

        // Handle displaying tour waypoint images
        final ImageView tourImageView = (ImageView)requireView().findViewById(R.id.tour_image);
        tourImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tourImgPath = createTourSharedViewModel.getTour().getTourImgPath();
                if (tourImgPath != null)
                    requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(tourImgPath)));
            }
        });

        // Delay loading tour image (if present) until it's ImageView has non zero height / width
        final String tourImgPath = createTourSharedViewModel.getTour().getTourImgPath();
        if (tourImgPath != null) {
            tourImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    tourImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    try {
                        setTourImage(Uri.parse(tourImgPath));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void addWaypoint(@NonNull LatLng point) {
        SymbolOptions symbolOptions = new SymbolOptions()
                .withLatLng(point)
                .withIconImage(ID_ICON_MARKER)
                .withTextField("")
                .withTextJustify(TEXT_JUSTIFY_AUTO);
        Symbol symbol = symbolManager.create(symbolOptions);

        TourWpWithPicPaths tourWpWithPicPaths = new TourWpWithPicPaths();
        tourWpWithPicPaths.tourWaypoint = new TourWaypoint(point.getLatitude(), point.getLongitude(), symbol.getTextField(), null);
        createTourSharedViewModel.getTourWaypointList().add(tourWpWithPicPaths);
    }

    private void toggleAnnotationInfoView(Symbol symbol) {
        if (annotationInfoView == null) {
            annotationInfoView = requireView().findViewById(R.id.layoutMarkerInfoView);
            ImageButton buttonDeleteAnnotation = annotationInfoView.findViewById(R.id.buttonDeleteAnnotation);
            buttonDeleteAnnotation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((int)(selectedSymbol.getId()) < createTourSharedViewModel.getTourWaypointList().size())
                        createTourSharedViewModel.getTourWaypointList().remove((int)(selectedSymbol.getId()));
                    symbolManager.delete(selectedSymbol);
                    hideAnnotationInfoView();
                }
            });
        }
        if (selectedSymbolId == symbol.getId()) {
            hideAnnotationInfoView();
        }
        else {
            TextView textViewInViewStub = annotationInfoView.findViewById(R.id.textViewAnnotationText);
            selectedSymbol = symbol;
            selectedSymbolId = symbol.getId();
            textViewInViewStub.setText(symbol.getTextField());   // String.format("%d", selectedSymbolId)
            annotationInfoView.setVisibility(View.VISIBLE);
        }
    }

    private void hideAnnotationInfoView() {
        if (annotationInfoView != null) {
            annotationInfoView.setVisibility(View.GONE);
            selectedSymbolId = -1;
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

            // Set up localization tracking floating action button
            fragmentActivity.findViewById(R.id.fab_camera_tracking_mode).setOnClickListener(new View.OnClickListener() {
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
        if (navigationMapRoute != null)
            navigationMapRoute.onStop();
        navigationMapRoute = null;
        mapView.onStop();
        isInTrackingMode = false;

        // Store annotations in ViewModel
        LongSparseArray<Symbol> annotations = symbolManager.getAnnotations();
        ArrayList<TourWpWithPicPaths> tourWpWithPicPathsLinkedList = createTourSharedViewModel.getTourWaypointList();
        int annotationsSize = annotations.size();
        int tourWpWithPicPathsLinkedListInitialSize = tourWpWithPicPathsLinkedList.size();
        tourWpWithPicPathsLinkedList.ensureCapacity(annotationsSize);
        for (int i = 0; i < tourWpWithPicPathsLinkedListInitialSize; i++) {
            Symbol symbol = annotations.valueAt(i);
            LatLng latLng = symbol.getLatLng();

            TourWpWithPicPaths tourWpWithPicPaths = tourWpWithPicPathsLinkedList.get(i);
            tourWpWithPicPaths.tourWaypoint.setLatitude(latLng.getLatitude());
            tourWpWithPicPaths.tourWaypoint.setLongtitude(latLng.getLongitude());
            tourWpWithPicPaths.tourWaypoint.setTitle(symbol.getTextField());
        }
        for (int i = tourWpWithPicPathsLinkedList.size(); i < annotationsSize; i++) {
            Symbol symbol = annotations.valueAt(i);
            LatLng latLng = symbol.getLatLng();

            TourWpWithPicPaths tourWpWithPicPaths = new TourWpWithPicPaths();
            tourWpWithPicPaths.tourWaypoint = new TourWaypoint(latLng.getLatitude(), latLng.getLongitude(), symbol.getTextField(), null);
            tourWpWithPicPathsLinkedList.add(tourWpWithPicPaths);
        }
    }

    private void resetAnnotationViewStatus() {
        annotationInfoView = null;
        selectedSymbolId = -1;
        selectedSymbol = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null)
            mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        resetAnnotationViewStatus();
        super.onDestroyView();
        mapView.onDestroy();
    }

}
