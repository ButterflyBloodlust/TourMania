package com.hal9000.tourmania.ui.create_tour;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.MainActivityViewModel;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.FavouriteTour;
import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWpWithPicPaths;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.tour_guides.GetTourGuideLocationResponse;
import com.hal9000.tourmania.rest_api.tour_guides.LocationShareTokenResponse;
import com.hal9000.tourmania.rest_api.tour_guides.TourGuidesService;
import com.hal9000.tourmania.ui.qr_code_display.QRCodeDisplayFragmentArgs;
import com.hal9000.tourmania.ui.tour_guide_details.TourGuideDetailsFragmentDirections;
import com.hal9000.tourmania.ui.tour_guides.TourGuidesFragmentDirections;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.content.Context.LOCATION_SERVICE;
import static com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModel.VIEW_TYPE_FAV_TOUR;
import static com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModel.VIEW_TYPE_MY_TOUR;
import static com.hal9000.tourmania.ui.join_tour.JoinTourFragment.LOCATION_SHARING_TOKEN_KEY;
import static com.hal9000.tourmania.ui.join_tour.JoinTourFragment.LOCATION_SHARING_TOKEN_TOUR_ID_KEY;
import static com.hal9000.tourmania.ui.search_tours.SearchToursFragment.TOUR_SEARCH_CACHE_DIR_NAME;
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
    private Observer<Tour> observerFavIcon;
    private ScheduledThreadPoolExecutor executor_;
    private final List<ValueAnimator> animators = new ArrayList<>();

    private View annotationInfoView;
    private long selectedSymbolId = -1;
    private Symbol selectedSymbol = null;
    private Symbol tourGuideSymbol = null;

    private static final String ID_ICON_MARKER = "place-marker-red-24";
    private static final String ID_ICON_MARKER_SELECTED = "place-marker-yellow-24";
    private static final String ID_ICON_MARKER_TOUR_GUIDE_POS = "tour-guide-marker-blue-24";
    private static final double ICON_MARKER_SCALE = 2d;
    private static final int PICK_IMAGE_REQUEST_CODE = 100;
    private static final int EASE_SYMBOL_DURATION_MS = 500;
    private static final long TOUR_GUIDE_LOC_UPDATES_DELAY = 5L;
    public static final String NEW_TOUR_RATING_BUNDLE_KEY = "new_tour_rating";
    public static final String NEW_TOUR_RATING_VAL_BUNDLE_KEY = "new_tour_val_rating";
    public static final String NEW_TOUR_RATING_COUNT_BUNDLE_KEY = "new_tour_count_rating";
    public static final String ACTIVE_TOUR_ID_KEY = "active_tour_id";
    public static final String ACTIVE_TOUR_SERVER_ID_KEY = "active_tour_server_id";

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Log.d("crashTest", "onCreateOptionsMenu : " + createTourSharedViewModel.isLoadedFromServerDb() + ", " + createTourSharedViewModel.isLoadedFromDb());
        if(createTourSharedViewModel.isEditingEnabled()) {
            inflater.inflate(R.menu.create_tour_toolbar_menu_edit_mode, menu);
        }
        else {
            inflater.inflate(R.menu.create_tour_toolbar_menu_not_editing, menu);

            final MenuItem itemToggleActive = menu.findItem(R.id.action_toggle_active);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            if ((prefs.contains(ACTIVE_TOUR_ID_KEY) && prefs.getInt(ACTIVE_TOUR_ID_KEY, -2)
                        == createTourSharedViewModel.getTour().getTourId())
                    || (prefs.contains(ACTIVE_TOUR_SERVER_ID_KEY)
                        && prefs.getString(ACTIVE_TOUR_SERVER_ID_KEY, "_")
                            .equals(createTourSharedViewModel.getTour().getServerTourId())))
                itemToggleActive.setTitle(getString(R.string.action_unset_active));
            else
                itemToggleActive.setTitle(getString(R.string.action_set_active));

            if (createTourSharedViewModel.isEditingPossible()) {
                inflater.inflate(R.menu.create_tour_toolbar_menu_my_tour, menu);
            }
            else if (createTourSharedViewModel.isLoadedFromServerDb() || createTourSharedViewModel.isLoadedFromDb()) {
                inflater.inflate(R.menu.create_tour_toolbar_menu_not_my_tour, menu);
                final MenuItem item = menu.findItem(R.id.action_add_tour_to_favourites);
                if (observerFavIcon != null)
                    createTourSharedViewModel.getTourLiveData().removeObserver(observerFavIcon);
                observerFavIcon = new Observer<Tour>() {
                    @Override
                    public void onChanged(@Nullable final Tour tour) {
                        AppDatabase.databaseWriteExecutor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //Log.d("crashTest", "observer: tour.getTourId() : " + tour.getTourId());
                                    if (tour.isInFavs()) {
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                item.setIcon(R.drawable.ic_star_white_filled_50dp);
                                                item.setTitle(R.string.action_remove_from_favourites);
                                                item.setVisible(true);
                                            }
                                        });
                                    }
                                    else if (tour.getTourId() != 0) {
                                        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                                        final FavouriteTour favouriteTour = appDatabase.favouriteTourDAO().getFavouriteTourByTourId(tour.getTourId());
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (favouriteTour == null) {
                                                    item.setIcon(R.drawable.ic_star_white_border_50dp);
                                                    item.setTitle(R.string.action_add_to_favourites);
                                                }
                                                else {
                                                    item.setIcon(R.drawable.ic_star_white_filled_50dp);
                                                    item.setTitle(R.string.action_remove_from_favourites);
                                                }
                                                item.setVisible(true);
                                            }
                                        });
                                    }
                                    else if (createTourSharedViewModel.isCheckedForCacheLink()) {
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                item.setVisible(true);
                                            }
                                        });
                                    }
                                } catch (Exception e ) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                };
                createTourSharedViewModel.getTourLiveData().observe(this, observerFavIcon);
                if (createTourSharedViewModel.isLoadedFromServerDb())
                    observerFavIcon.onChanged(createTourSharedViewModel.getTour());
            }
        }
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
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
                                Future future = createTourSharedViewModel.saveTourToDb(requireContext(), VIEW_TYPE_MY_TOUR);
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
            case R.id.action_edit_tags:
                Navigation.findNavController(requireView()).navigate(R.id.tourTagsListFragment, null);
                return true;
            case R.id.action_add_tour_image:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST_CODE);
                return true;
            case R.id.action_edit_tour:
                createTourSharedViewModel.setEditingEnabled(true);
                requireActivity().invalidateOptionsMenu();
                enableEditing();
                return true;
            case R.id.action_add_tour_to_favourites:
                //Log.d("crashTest", "action_add_tour_to_favourites");
                AppDatabase.databaseWriteExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!createTourSharedViewModel.getTour().isInFavs() && !createTourSharedViewModel.checkIfInCachedFavs(requireContext())) {
                                // Save tour to db
                                Future future = createTourSharedViewModel.saveTourToDb(requireContext(), VIEW_TYPE_FAV_TOUR);
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        item.setIcon(R.drawable.ic_star_white_filled_50dp);
                                        item.setTitle(R.string.action_remove_from_favourites);
                                    }
                                });
                                try {
                                    future.get();
                                } catch (ExecutionException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                createTourSharedViewModel.deleteTourFromFavs(requireContext());
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        item.setIcon(R.drawable.ic_star_white_border_50dp);
                                        item.setTitle(R.string.action_add_to_favourites);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            case R.id.action_rate_tour:
                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating_bar, null);
                ((RatingBar) dialogView.findViewById(R.id.rating_bar)).setRating(createTourSharedViewModel.getTour().getMyRating());
                final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                        .setIcon(R.drawable.ic_star_white_border_50dp)
                        .setTitle("Rate this tour")
                        .setView(dialogView)
                        .setCancelable(true)
                        .setPositiveButton("Done", null)
                        .setNegativeButton("Cancel", null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
                                float rating = ratingBar.getRating();
                                if (rating > 0) {
                                    createTourSharedViewModel.updateTourRating(requireContext(), rating);

                                    Tour tour = createTourSharedViewModel.getTour();
                                    MainActivityViewModel activityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putFloat(NEW_TOUR_RATING_BUNDLE_KEY, rating);
                                    bundle.putFloat(NEW_TOUR_RATING_VAL_BUNDLE_KEY, tour.getRateVal());
                                    bundle.putInt(NEW_TOUR_RATING_COUNT_BUNDLE_KEY, tour.getRateCount());
                                    activityViewModel.putToBundle(CreateTourFragment.class, bundle);

                                    alertDialog.dismiss();
                                }
                                else
                                    Toast.makeText(requireContext(),"Rating above 0 required",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                alertDialog.show();
                return true;
            case R.id.action_share_location:
                // Check if user is logged in
                if (!AppUtils.isUserLoggedIn(requireContext())) {
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                    return true;
                }

                TourGuidesService client = RestClient.createService(TourGuidesService.class,
                        SharedPrefUtils.getDecryptedString(getContext(), MainActivity.getLoginTokenKey()));
                Call<LocationShareTokenResponse> call = client.getTourGuideLocationSharingToken(createTourSharedViewModel.getTour().getServerTourId());
                call.enqueue(new Callback<LocationShareTokenResponse>() {
                    @Override
                    public void onResponse(Call<LocationShareTokenResponse> call, Response<LocationShareTokenResponse> response) {
                        //Log.d("crashTest", "onSharedPreferenceChanged onResponse");
                        if (response.isSuccessful()) {
                            String token = response.body().token;
                            if (token != null)
                                Navigation.findNavController(requireView()).navigate(R.id.QRCodeDisplayFragment,
                                        new QRCodeDisplayFragmentArgs.Builder(token).build().toBundle());
                        }
                    }

                    @Override
                    public void onFailure(Call<LocationShareTokenResponse> call, Throwable t) {
                        Context context = getContext();
                        if (context != null)
                            Toast.makeText(context,"A connection error has occurred",Toast.LENGTH_SHORT).show();
                        //Log.d("crashTest", "onSharedPreferenceChanged onFailure");
                    }
                });
                return true;
            case R.id.action_about_tour:
                final View dialogViewAboutTour = getLayoutInflater().inflate(R.layout.dialog_about_tour, null);
                ((TextView)dialogViewAboutTour.findViewById(R.id.tour_author_textview)).setText(createTourSharedViewModel.getUser().getUsername());
                final AlertDialog.Builder builderAboutTour = new AlertDialog.Builder(requireContext())
                        .setTitle("About tour")
                        .setView(dialogViewAboutTour)
                        .setCancelable(true)
                        .setNegativeButton("Cancel", null);
                if (createTourSharedViewModel.getUser().isTourGuide())
                    builderAboutTour.setPositiveButton("Tour guide profile", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NavDirections navDirections = CreateTourFragmentDirections
                                    .actionCreateTourFragmentToTourGuideDetails(createTourSharedViewModel.getUser().getUsername());
                            Navigation.findNavController(requireView()).navigate(navDirections);
                        }
                    });
                builderAboutTour.create().show();
                return true;
            case R.id.action_toggle_active:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                if (prefs.contains(ACTIVE_TOUR_SERVER_ID_KEY) && prefs.getString(ACTIVE_TOUR_SERVER_ID_KEY, "_")
                        .equals(createTourSharedViewModel.getTour().getServerTourId())) {
                    prefs.edit().remove(ACTIVE_TOUR_SERVER_ID_KEY).apply();
                    item.setTitle(getString(R.string.action_set_active));
                } else if (prefs.contains(ACTIVE_TOUR_ID_KEY) && prefs.getInt(ACTIVE_TOUR_ID_KEY, -2)
                        == createTourSharedViewModel.getTour().getTourId()) {
                    prefs.edit().remove(ACTIVE_TOUR_ID_KEY).apply();
                    item.setTitle(getString(R.string.action_set_active));
                } else {
                    if (!TextUtils.isEmpty(createTourSharedViewModel.getTour().getServerTourId())) {
                        prefs.edit().remove(ACTIVE_TOUR_ID_KEY).apply();
                        prefs.edit().putString(ACTIVE_TOUR_SERVER_ID_KEY, createTourSharedViewModel.getTour().getServerTourId()).apply();
                        item.setTitle(getString(R.string.action_unset_active));
                    }
                    else if (createTourSharedViewModel.getTour().getTourId() != 0) {
                        prefs.edit().remove(ACTIVE_TOUR_SERVER_ID_KEY).apply();
                        prefs.edit().putInt(ACTIVE_TOUR_ID_KEY, createTourSharedViewModel.getTour().getTourId()).apply();
                        item.setTitle(getString(R.string.action_unset_active));
                    }
                }
                return true;
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

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        //editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setTextColor(0xffffffff);
    }

    private void enableEditText(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setEnabled(true);
        editText.setCursorVisible(true);
        //editText.setKeyListener(null);
        editText.setTextColor(0xffffffff);
    }

    private void enableEditing() {
        View view = requireView();
        enableEditText(((EditText)view.findViewById(R.id.text_input_edit_text_create_tour)));
        enableEditText(((EditText)view.findViewById(R.id.textViewAnnotationText)));
        view.findViewById(R.id.buttonDeleteAnnotation).setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_create_tour, container, false);
        //createTourSharedViewModel = ViewModelProviders.of(requireActivity()).get(CreateTourSharedViewModel.class);
        return root;
    }

    private boolean hasTourGuideLocationTracking(final String prefsTourServerId) {
        return !TextUtils.isEmpty(createTourSharedViewModel.getTour().getServerTourId())
                && createTourSharedViewModel.getTour().getServerTourId().equals(prefsTourServerId);
    }

    @Override
    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {

        // Scope ViewModel to nested nav graph.
        ViewModelStoreOwner owner = Navigation.findNavController(view).getViewModelStoreOwner(R.id.nav_nested_create_tour);
        CreateTourSharedViewModelFactory factory = new CreateTourSharedViewModelFactory();
        createTourSharedViewModel = new ViewModelProvider(owner, factory).get(CreateTourSharedViewModel.class);

        createTourSharedViewModel.getLoadedFromDb().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isLoadedFromDb) {
                //Log.d("crashTest", "onChanged isLoadedFromDb = " + isLoadedFromDb);
                requireActivity().invalidateOptionsMenu();
            }
        });
        createTourSharedViewModel.getLoadedFromServerDb().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isLoadedFromServerDb) {
                requireActivity().invalidateOptionsMenu();
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        final String token = prefs.getString(LOCATION_SHARING_TOKEN_KEY, "");
        final String prefsTourServerId = prefs.getString(LOCATION_SHARING_TOKEN_TOUR_ID_KEY, "");
        // Handle possible future tour guide location updates
        createTourSharedViewModel.getTourLiveData().observe(this, new Observer<Tour>() {
            @Override
            public void onChanged(@Nullable Tour tour) {
                //Log.d("crashTest", "ScheduledThreadPoolExecutor onChanged()");
                // if current tour is the one for which user has location sharing token, then enable tour guide location updates
                if (hasTourGuideLocationTracking(prefsTourServerId) && (executor_ == null || executor_.isShutdown())) {
                    executor_ = new ScheduledThreadPoolExecutor(1);
                    executor_.scheduleWithFixedDelay(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d("crashTest", "ScheduledThreadPoolExecutor run()");
                            createTourSharedViewModel.handleTourGuideLocation(requireContext(), token);
                        }
                    }, 0L, TOUR_GUIDE_LOC_UPDATES_DELAY, TimeUnit.SECONDS);
                }
                if (createTourSharedViewModel.isLoadedFromServerDb())
                    createTourSharedViewModel.getTourLiveData().removeObserver(this);
            }
        });

        int tourId = CreateTourFragmentArgs.fromBundle(getArguments()).getTourId();
        String tourServerId = CreateTourFragmentArgs.fromBundle(getArguments()).getTourServerId();
        //Log.d("crashTest", "tourId = " + Integer.toString(tourId));
        AppCompatActivity appCompatActivity = ((AppCompatActivity)getActivity());
        ActionBar actionBar = null;
        if (appCompatActivity != null)
            actionBar = appCompatActivity.getSupportActionBar();
        if (tourId != -1) {
            if (actionBar != null) {
                actionBar.setTitle("Tour");
            }
            Future future = createTourSharedViewModel.loadTourFromDb(tourId, requireContext());
            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            if (createTourSharedViewModel.getViewType() == VIEW_TYPE_MY_TOUR)
                createTourSharedViewModel.setEditingPossible(true);
            else
                createTourSharedViewModel.setEditingPossible(false);
            createTourSharedViewModel.setInitialEditingEnabled(false);
            //requireActivity().invalidateOptionsMenu();
        }
        else if (tourServerId != null) {
            if (actionBar != null) {
                actionBar.setTitle("Tour");
            }
            createTourSharedViewModel.setInitialEditingEnabled(false);
            createTourSharedViewModel.setEditingPossible(false);
            createTourSharedViewModel.loadTourFromServerDb(tourServerId, requireContext(), TOUR_SEARCH_CACHE_DIR_NAME);
        }
        else {
            if (actionBar != null) {
                actionBar.setTitle("Create Tour");
            }
            createTourSharedViewModel.setEditingPossible(true);
        }

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

        //mapView = (MapView) view.findViewById(R.id.mapView);

        final int choosenLocateWaypointIndex = createTourSharedViewModel.getChoosenLocateWaypointIndex();
        if (choosenLocateWaypointIndex != -1 ) {
            TourWaypoint tourWaypoint = createTourSharedViewModel.getTourWaypointList().get(choosenLocateWaypointIndex).tourWaypoint;
            MapboxMapOptions mapboxMapOptions = MapboxMapOptions.createFromAttributes(requireContext(), null)
                    .camera(new CameraPosition.Builder()
                            .target(new LatLng(tourWaypoint.getLatitude(), tourWaypoint.getLongitude()))
                            .zoom(10)
                            .build());
            mapView = new MapView(requireContext(), mapboxMapOptions);
            createTourSharedViewModel.removeChoosenLocateWaypointIndex();
        }
        else {
            Location location = null;
            LocationManager locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                MapboxMapOptions mapboxMapOptions = MapboxMapOptions.createFromAttributes(requireContext(), null)
                        .camera(new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(10)
                                .build());
                mapView = new MapView(requireContext(), mapboxMapOptions);
            } else if (tourId != -1 || tourServerId != null) {
                ArrayList<TourWpWithPicPaths> tourWpWithPicPaths = createTourSharedViewModel.getTourWaypointList();
                if (!createTourSharedViewModel.getTourWaypointList().isEmpty()) {
                    TourWaypoint tourWaypoint = tourWpWithPicPaths.get(0).tourWaypoint;
                    MapboxMapOptions mapboxMapOptions = MapboxMapOptions.createFromAttributes(requireContext(), null)
                            .camera(new CameraPosition.Builder()
                                    .target(new LatLng(tourWaypoint.getLatitude(), tourWaypoint.getLongitude()))
                                    .zoom(10)
                                    .build());
                    mapView = new MapView(requireContext(), mapboxMapOptions);
                } else
                    mapView = new MapView(requireContext());
            } else
                mapView = new MapView(requireContext());
        }
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

                        // Add custom marker to style
                        markerIconDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_tour_guide_pos_24dp);
                        bitmap = Bitmap.createBitmap((int)(markerIconDrawable.getIntrinsicWidth() * ICON_MARKER_SCALE),
                                (int)(markerIconDrawable.getIntrinsicHeight() * ICON_MARKER_SCALE), Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(bitmap);
                        markerIconDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        markerIconDrawable.draw(canvas);
                        style.addImage(ID_ICON_MARKER_TOUR_GUIDE_POS, bitmap);

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

                        if (createTourSharedViewModel.getTourGuideLocLiveData().getValue() != null) {
                            SymbolOptions tourGuidePositionOptions = new SymbolOptions()
                                    .withLatLng(new LatLng(createTourSharedViewModel.getTourGuideLocLiveData().getValue().latitude,
                                            createTourSharedViewModel.getTourGuideLocLiveData().getValue().longitude))
                                    .withIconImage(ID_ICON_MARKER_TOUR_GUIDE_POS)
                                    .withTextField("Tour\nGuide");
                            tourGuideSymbol = symbolManager.create(tourGuidePositionOptions);
                        }
                        createTourSharedViewModel.getTourGuideLocLiveData().observe(CreateTourFragment.this, new Observer<GetTourGuideLocationResponse>() {
                            @Override
                            public void onChanged(GetTourGuideLocationResponse location) {
                                //Log.d("crashTest", "Observer<GetTourGuideLocationResponse> onChanged");
                                try {
                                    if (location == null) {
                                        if (tourGuideSymbol != null)
                                            symbolManager.delete(tourGuideSymbol);
                                        createTourSharedViewModel.getTourGuideLocLiveData().removeObserver(this);
                                        executor_.shutdown();
                                    }
                                    else {
                                        if (tourGuideSymbol == null) {
                                            SymbolOptions tourGuidePositionOptions = new SymbolOptions()
                                                    .withLatLng(new LatLng(location.latitude, location.longitude))
                                                    .withIconImage(ID_ICON_MARKER_TOUR_GUIDE_POS)
                                                    .withTextField("Tour\nGuide");
                                            tourGuideSymbol = symbolManager.create(tourGuidePositionOptions);
                                        } else {
                                            easeSymbol(tourGuideSymbol, new LatLng(location.latitude, location.longitude));
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // Add an annotation on long click on the map
                        CreateTourFragment.this.mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                            @Override
                            public boolean onMapLongClick(@NonNull LatLng point) {
                                //Log.d("crashTest", "onMapLongClick()");
                                if (createTourSharedViewModel.isEditingEnabled())
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
                                    .withLatLng(new LatLng(tourWpWithPicPaths.tourWaypoint.getLatitude(), tourWpWithPicPaths.tourWaypoint.getLongitude()))
                                    .withIconImage(ID_ICON_MARKER)
                                    .withTextField(tourWpWithPicPaths.tourWaypoint.getTitle())
                                    .withTextJustify(TEXT_JUSTIFY_AUTO);
                            symbolOptionsList.add(symbolOptions);
                        }
                        // Mark selected annotation if aplicable
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

        FrameLayout frameLayout = view.findViewById(R.id.mapView);
        frameLayout.addView(mapView);

        // Handle annotation label updates.
        EditText annotationTextEditText = ((EditText)view.findViewById(R.id.textViewAnnotationText));
        annotationTextEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        // Handle tour title and it's updates.
        final EditText tourTitleEditText = ((EditText)view.findViewById(R.id.text_input_edit_text_create_tour));
        tourTitleEditText.setText(createTourSharedViewModel.getTour().getTitle());
        tourTitleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    createTourSharedViewModel.getTour().setTitle(v.getText().toString());
                }
                return false;   // don't consume action (allow propagation)
            }
        });

        // Disable option to edit text if not in editing mode
        if (!createTourSharedViewModel.isEditingEnabled()) {
            disableEditText(tourTitleEditText);
            disableEditText(annotationTextEditText);
            view.findViewById(R.id.buttonDeleteAnnotation).setVisibility(View.GONE);
        }

        // Handle displaying tour waypoint images
        final ImageView tourImageView = (ImageView)requireView().findViewById(R.id.tour_image);
        tourImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tourImgPath = createTourSharedViewModel.getTour().getTourImgPath();
                if (tourImgPath != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(Uri.parse(tourImgPath), AppUtils.getMimeType(tourImgPath));
                    requireContext().startActivity(intent);
                }
            }
        });

        createTourSharedViewModel.getTourLiveData().observe(this, new Observer<Tour>() {
            @Override
            public void onChanged(@Nullable Tour tour) {
                try {
                    tourTitleEditText.setText(tour.getTitle());

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

                    if (mapboxMap != null) {
                        ArrayList<TourWpWithPicPaths> tourWpWithPicPaths = createTourSharedViewModel.getTourWaypointList();
                        if (!tourWpWithPicPaths.isEmpty()) {
                            TourWaypoint tourWaypoint = tourWpWithPicPaths.get(0).tourWaypoint;
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(new LatLng(tourWaypoint.getLatitude(), tourWaypoint.getLongitude()))
                                    .zoom(10)
                                    .build();
                            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void easeSymbol(Symbol symbol, final LatLng location) {
        final LatLng originalPosition = symbol.getLatLng();
        final boolean changeLocation = originalPosition.distanceTo(location) > 0;
        if (!changeLocation) {
            return;
        }

        ValueAnimator moveSymbol = ValueAnimator.ofFloat(0, 1).setDuration(EASE_SYMBOL_DURATION_MS);
        moveSymbol.setInterpolator(new LinearInterpolator());
        moveSymbol.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (symbolManager == null || symbolManager.getAnnotations().indexOfValue(symbol) < 0) {
                    return;
                }
                float fraction = (float) animation.getAnimatedValue();

                if (changeLocation) {
                    double lat = ((location.getLatitude() - originalPosition.getLatitude()) * fraction) + originalPosition.getLatitude();
                    double lng = ((location.getLongitude() - originalPosition.getLongitude()) * fraction) + originalPosition.getLongitude();
                    symbol.setGeometry(Point.fromLngLat(lng, lat));
                }

                symbolManager.update(symbol);
            }
        });

        moveSymbol.start();
        animators.add(moveSymbol);
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
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null && !fragmentActivity.isChangingConfigurations())
            AppUtils.hideSoftKeyboard(requireActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        for (ValueAnimator animator : animators) {
            animator.cancel();
        }
        if (navigationMapRoute != null)
            navigationMapRoute.onStop();
        navigationMapRoute = null;
        mapView.onStop();
        isInTrackingMode = false;

        // Store annotations in ViewModel
        if (symbolManager != null) {
            LongSparseArray<Symbol> annotations = symbolManager.getAnnotations();
            ArrayList<TourWpWithPicPaths> tourWpWithPicPathsLinkedList = createTourSharedViewModel.getTourWaypointList();
            int annotationsSize = annotations.size();
            int tourWpWithPicPathsLinkedListInitialSize = tourWpWithPicPathsLinkedList.size();
            tourWpWithPicPathsLinkedList.ensureCapacity(annotationsSize);
            boolean hadTourGuideSymbol = false;
            for (int i = 0; i < tourWpWithPicPathsLinkedListInitialSize && i < annotationsSize; i++) {
                Symbol symbol = annotations.valueAt(i);
                if (symbol != tourGuideSymbol) {
                    LatLng latLng = symbol.getLatLng();

                    TourWpWithPicPaths tourWpWithPicPaths;
                    if (hadTourGuideSymbol)
                        tourWpWithPicPaths = tourWpWithPicPathsLinkedList.get(i - 1);
                    else
                        tourWpWithPicPaths = tourWpWithPicPathsLinkedList.get(i);
                    tourWpWithPicPaths.tourWaypoint.setLatitude(latLng.getLatitude());
                    tourWpWithPicPaths.tourWaypoint.setLongitude(latLng.getLongitude());
                    tourWpWithPicPaths.tourWaypoint.setTitle(symbol.getTextField());
                } else
                    hadTourGuideSymbol = true;
            }
            for (int i = tourWpWithPicPathsLinkedList.size(); i < annotationsSize; i++) {
                Symbol symbol = annotations.valueAt(i);
                if (symbol != tourGuideSymbol) {
                    LatLng latLng = symbol.getLatLng();

                    TourWpWithPicPaths tourWpWithPicPaths = new TourWpWithPicPaths();
                    tourWpWithPicPaths.tourWaypoint = new TourWaypoint(latLng.getLatitude(), latLng.getLongitude(), symbol.getTextField(), null);
                    tourWpWithPicPathsLinkedList.add(tourWpWithPicPaths);
                }
            }
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
        if (symbolManager != null) {
            symbolManager.onDestroy();
        }
        mapView.onDestroy();
        if (executor_ != null) {
            executor_.shutdown();
        }
    }

}
