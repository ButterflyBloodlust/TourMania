package com.hal9000.tourmania.ui.tour_guides;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.User;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.tour_guides.TourGuidesService;
import com.hal9000.tourmania.rest_api.tours.ToursService;
import com.hal9000.tourmania.ui.InfiniteTourGuideAdapter;
import com.hal9000.tourmania.ui.search.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TourGuidesFragment extends Fragment {

    private TourGuidesViewModel tourGuidesViewModel;
    private RecyclerView recyclerView;
    private InfiniteTourGuideAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> tourGuides = new ArrayList<>(100);
    private int pageNumber = 1;
    private int currentFragmentId;
    private boolean reachedEnd = false;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private int retries = 0;

    private static final int RETRIES_LIMIT = 0;
    private static final int PAGE_SIZE = 10;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        tourGuidesViewModel = ViewModelProviders.of(this).get(TourGuidesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tour_guides, container, false);

        //generateTestDataset(40);

        Context context = requireContext();
        if (AppUtils.isWifiEnabled(context) && AppUtils.isLocationEnabled(context)) {
            createRecyclerView(root);
            getTourGuidesOnLastLocation();
        }
        else {
            root.findViewById(R.id.text_enable_wifi_loc_msg).setVisibility(View.VISIBLE);
        }
        return root;
    }

    private void getTourGuidesOnLastLocation(){
        Context context = requireContext();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                            initRecommendedTourGuides();
                        }
                        else {
                            Context context = getContext();
                            if (context != null)
                                Toast.makeText(context,"Could not access user location",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.d("crashTest", "Error trying to get last location");
                        e.printStackTrace();
                        Context context = getContext();
                        if (context != null)
                            Toast.makeText(context,"Could not access user location",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initRecommendedTourGuides() {
        mAdapter.setLoading();
        loadTourGuidesFromServerDb();
    }

    private void loadTourGuidesFromServerDb() {
        TourGuidesService client = RestClient.createService(TourGuidesService.class);
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<List<User>> call = client.getNearbyTourGuides(longitude, latitude, pageNumber++);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                //Log.d("crashTest", "loadTourGuidesFromServerDb onResponse");
                List<User> tourGuidesList = response.body();
                if (tourGuidesList != null) {
                    //Log.d("crashTest", "tourGuidesList size = " + tourGuidesList.size());
                    if (tourGuidesList.isEmpty()) {
                        if (pageNumber == 1)
                            Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show();
                        reachedEnd = true;
                        mAdapter.setLoaded();
                    } else {
                        if (tourGuidesList.size() < PAGE_SIZE)
                            reachedEnd = true;
                        int oldSize = mAdapter.mDataset.size();
                        mAdapter.mDataset.addAll(tourGuidesList);
                        mAdapter.notifyItemRangeInserted(oldSize, tourGuidesList.size());
                        mAdapter.setLoaded();
                        //loadToursImagesFromServerDb(tourGuidesList);
                    }
                }
                else
                    mAdapter.setLoaded();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                mAdapter.setLoaded();
                t.printStackTrace();
                Context context = getContext();
                if (context != null)
                    Toast.makeText(context,"A connection error has occurred\nRetries left: " + (RETRIES_LIMIT - retries),Toast.LENGTH_SHORT).show();
                if (retries++ >= RETRIES_LIMIT)
                    reachedEnd = true;
                //Log.d("crashTest", "loadTourGuidesFromServerDb onFailure");
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.action_search_tour_guides, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        final SearchView searchView = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        item.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

        });
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
    }

    public void generateTestDataset(int usersAmount) {
        Random r = new Random();
        for (int i = 0; i < usersAmount; i++) {
            char gender = r.nextInt(2) == 0 ? 'M' : 'F';
            tourGuides.add(new User(
                    (gender == 'M' ? maleNamePool[r.nextInt(maleNamePool.length)] : femaleNamePool[r.nextInt(femaleNamePool.length)]) + " " +
                            (surnamePool[r.nextInt(surnamePool.length)])));
        }
    }

    private void createRecyclerView(final View root) {
        // Create recycler view
        recyclerView = (RecyclerView) root.findViewById(R.id.tour_guides_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new InfiniteTourGuideAdapter(tourGuides,
                new InfiniteTourGuideAdapter.ToursAdapterCallback() {
                    @Override
                    public Context getContext() {
                        return requireContext();
                    }

                    @Override
                    public void navigateToViewTour(int position) {
                        /*
                        Navigation.findNavController(requireView()).navigate(R.id.nav_nested_create_tour,
                                new CreateTourFragmentArgs.Builder().setTourServerId(
                                        tourGuides.get(position).getServerTourGdId()).build().toBundle());
                         */
                        NavDirections navDirections =  TourGuidesFragmentDirections
                                .actionNavTourGuidesToNavTourGuideDetails(tourGuides.get(position).getUsername());
                        Navigation.findNavController(requireView()).navigate(navDirections);
                    }
                },
                R.layout.tour_guide_rec_view_row,
                recyclerView);

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!reachedEnd) {
                    mAdapter.setLoading();
                    loadTourGuidesFromServerDb();
                    //reachedEnd = true;  // temporary, until backend support is implemented
                    //mAdapter.setLoaded();  // temporary, until backend support is implemented
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    // for debug purposes
    static String[] maleNamePool = new String[] {
            "JAMES",
            "JOHN",
            "ROBERT",
            "MICHAEL",
            "WILLIAM",
            "DAVID",
            "RICHARD",
            "CHARLES",
            "JOSEPH",
            "THOMAS",
            "CHRISTOPHER",
            "DANIEL",
            "PAUL",
            "MARK",
            "DONALD",
            "GEORGE",
            "KENNETH",
            "STEVEN",
            "EDWARD",
            "BRIAN",
            "RONALD",
            "ANTHONY",
            "KEVIN",
            "JASON",
            "MATTHEW",
            "GARY",
            "TIMOTHY",
            "JOSE",
            "LARRY",
            "JEFFREY",
            "FRANK",
            "SCOTT",
            "ERIC",
            "STEPHEN",
            "ANDREW",
            "RAYMOND",
            "GREGORY",
            "JOSHUA",
            "JERRY",
            "DENNIS",
            "WALTER",
            "PATRICK",
            "PETER",
            "HAROLD",
            "DOUGLAS",
            "HENRY",
            "CARL",
            "ARTHUR",
            "RYAN",
            "ROGER",
            "JOE",
            "JUAN",
            "JACK",
            "ALBERT",
            "JONATHAN",
            "JUSTIN",
            "TERRY",
            "GERALD",
            "KEITH",
            "SAMUEL",
            "WILLIE",
            "RALPH",
            "LAWRENCE",
            "NICHOLAS",
            "ROY",
            "BENJAMIN",
            "BRUCE",
            "BRANDON",
            "ADAM",
            "HARRY",
            "FRED",
            "WAYNE",
            "BILLY",
            "STEVE",
            "LOUIS",
            "JEREMY",
            "AARON",
            "RANDY",
            "HOWARD",
            "EUGENE",
            "CARLOS",
            "RUSSELL",
            "BOBBY",
            "VICTOR",
            "MARTIN",
            "ERNEST",
            "PHILLIP",
            "TODD",
            "JESSE",
            "CRAIG",
            "ALAN",
            "SHAWN",
            "CLARENCE",
            "SEAN",
            "PHILIP",
            "CHRIS",
            "JOHNNY",
            "EARL",
            "JIMMY",
            "ANTONIO",
            "DANNY",
            "BRYAN",
            "TONY",
            "LUIS",
            "MIKE",
            "STANLEY",
            "LEONARD",
            "NATHAN",
            "DALE",
            "MANUEL",
            "RODNEY",
            "CURTIS",
            "NORMAN",
            "ALLEN",
            "MARVIN",
            "VINCENT",
            "GLENN",
            "JEFFERY",
            "TRAVIS",
            "JEFF",
            "CHAD",
            "JACOB",
            "LEE",
            "MELVIN",
            "ALFRED",
            "KYLE",
            "FRANCIS",
            "BRADLEY",
            "JESUS",
            "HERBERT",
            "FREDERICK",
            "RAY",
            "JOEL",
            "EDWIN",
            "DON",
            "EDDIE",
            "RICKY",
            "TROY",
            "RANDALL",
            "BARRY",
            "ALEXANDER",
            "BERNARD",
            "MARIO",
            "LEROY",
            "FRANCISCO",
            "MARCUS",
            "MICHEAL",
            "THEODORE",
            "CLIFFORD",
            "MIGUEL",
            "OSCAR",
            "JAY",
            "JIM",
            "TOM",
            "CALVIN",
            "ALEX",
            "JON",
            "RONNIE",
            "BILL",
            "LLOYD",
            "TOMMY",
            "LEON",
            "DEREK",
            "WARREN",
            "DARRELL",
            "JEROME",
            "FLOYD",
            "LEO",
            "ALVIN",
            "TIM",
            "WESLEY",
            "GORDON",
            "DEAN",
            "GREG",
            "JORGE",
            "DUSTIN",
            "PEDRO",
            "DERRICK",
            "DAN",
            "LEWIS",
            "ZACHARY",
            "COREY",
            "HERMAN",
            "MAURICE",
            "VERNON",
            "ROBERTO",
            "CLYDE",
            "GLEN",
            "HECTOR",
            "SHANE",
            "RICARDO",
            "SAM",
            "RICK",
            "LESTER",
            "BRENT",
            "RAMON",
            "CHARLIE",
            "TYLER",
            "GILBERT",
            "GENE",
            "MARC",
            "REGINALD",
            "RUBEN",
            "BRETT",
            "ANGEL",
            "NATHANIEL",
            "RAFAEL",
            "LESLIE",
            "EDGAR",
            "MILTON",
            "RAUL",
            "BEN",
            "CHESTER",
            "CECIL",
            "DUANE",
            "FRANKLIN",
            "ANDRE",
            "ELMER",
            "BRAD",
            "GABRIEL",
            "RON",
            "MITCHELL",
            "ROLAND",
            "ARNOLD",
            "HARVEY",
            "JARED",
            "ADRIAN",
            "KARL",
            "CORY",
            "CLAUDE",
            "ERIK",
            "DARRYL",
            "JAMIE",
            "NEIL",
            "JESSIE",
            "CHRISTIAN",
            "JAVIER",
            "FERNANDO",
            "CLINTON",
            "TED",
            "MATHEW",
            "TYRONE",
            "DARREN",
            "LONNIE",
            "LANCE",
            "CODY",
            "JULIO",
            "KELLY",
            "KURT",
            "ALLAN",
            "NELSON",
            "GUY",
            "CLAYTON",
            "HUGH",
            "MAX",
            "DWAYNE",
            "DWIGHT",
            "ARMANDO",
            "FELIX",
            "JIMMIE",
            "EVERETT",
            "JORDAN",
            "IAN",
            "WALLACE",
            "KEN",
            "BOB",
            "JAIME",
            "CASEY",
            "ALFREDO",
            "ALBERTO",
            "DAVE",
            "IVAN",
            "JOHNNIE",
            "SIDNEY",
            "BYRON",
            "JULIAN",
            "ISAAC",
            "MORRIS",
            "CLIFTON",
            "WILLARD",
            "DARYL",
            "ROSS",
            "VIRGIL",
            "ANDY",
            "MARSHALL",
            "SALVADOR",
            "PERRY",
            "KIRK",
            "SERGIO",
            "MARION",
            "TRACY",
            "SETH",
            "KENT",
            "TERRANCE",
            "RENE",
            "EDUARDO",
            "TERRENCE",
            "ENRIQUE",
            "FREDDIE",
            "WADE",
            "AUSTIN",
            "STUART",
            "FREDRICK",
            "ARTURO",
            "ALEJANDRO",
            "JACKIE",
            "JOEY",
            "NICK",
            "LUTHER",
            "WENDELL",
            "JEREMIAH",
            "EVAN",
            "JULIUS",
            "DANA",
            "DONNIE",
            "OTIS",
            "SHANNON",
            "TREVOR",
            "OLIVER",
            "LUKE",
            "HOMER",
            "GERARD",
            "DOUG",
            "KENNY",
            "HUBERT",
            "ANGELO",
            "SHAUN",
            "LYLE",
            "MATT",
            "LYNN",
            "ALFONSO",
            "ORLANDO",
            "REX",
            "CARLTON",
            "ERNESTO",
            "CAMERON",
            "NEAL",
            "PABLO",
            "LORENZO",
            "OMAR",
            "WILBUR",
            "BLAKE",
            "GRANT",
            "HORACE",
            "RODERICK",
            "KERRY",
            "ABRAHAM",
            "WILLIS",
            "RICKEY",
            "JEAN",
            "IRA",
            "ANDRES",
            "CESAR",
            "JOHNATHAN",
            "MALCOLM",
            "RUDOLPH",
            "DAMON",
            "KELVIN",
            "RUDY",
            "PRESTON",
            "ALTON",
            "ARCHIE",
            "MARCO",
            "WM",
            "PETE",
            "RANDOLPH",
            "GARRY",
            "GEOFFREY",
    };

    static String[] femaleNamePool = new String[] {
            "ALICEA",
            "ALICIA",
            "ALIDA",
            "ALIDIA",
            "ALIE",
            "ALIKA",
            "ALIKEE",
            "ALINA",
            "ALINE",
            "ALIS",
            "ALISA",
            "ALISHA",
            "ALISON",
            "ALISSA",
            "ALISUN",
            "ALIX",
            "ALIZA",
            "ALLA",
            "ALLEEN",
            "ALLEGRA",
            "ALLENE",
            "ALLI",
            "ALLIANORA",
            "ALLIE",
            "ALLINA",
            "ALLIS",
            "ALLISON",
            "ALLISSA",
            "ALLIX",
            "ALLSUN",
            "ALLX",
            "ALLY",
            "ALLYCE",
            "ALLYN",
            "ALLYS",
            "ALLYSON",
            "ALMA",
            "ALMEDA",
            "ALMERIA",
            "ALMETA",
            "ALMIRA",
            "ALMIRE",
            "ALOISE",
            "ALOISIA",
            "ALOYSIA",
            "ALTA",
            "ALTHEA",
            "ALVERA",
            "ALVERTA",
            "ALVINA",
            "ALVINIA",
            "ALVIRA",
            "ALYCE",
            "ALYDA",
            "ALYS",
            "ALYSA",
            "ALYSE",
            "ALYSIA",
            "ALYSON",
            "ALYSS",
            "ALYSSA",
            "AMABEL",
            "AMABELLE",
            "AMALEA",
            "AMALEE",
            "AMALETA",
            "AMALIA",
            "AMALIE",
            "AMALITA",
            "AMALLE",
            "AMANDA",
            "AMANDI",
            "AMANDIE",
            "AMANDY",
            "AMARA",
            "AMARGO",
            "AMATA",
            "AMBER",
            "AMBERLY",
            "AMBUR",
            "AME",
            "AMELIA",
            "AMELIE",
            "AMELINA",
            "AMELINE",
            "AMELITA",
            "AMI",
            "AMIE",
            "AMII",
            "AMIL",
            "AMITIE",
            "AMITY",
            "AMMAMARIA",
            "AMY",
            "AMYE",
            "ANA",
            "ANABAL",
            "ANABEL",
            "ANABELLA",
            "ANABELLE",
            "ANALIESE",
            "ANALISE",
            "ANALLESE",
            "ANALLISE",
            "ANASTASIA",
            "ANASTASIE",
            "ANASTASSIA",
            "ANATOLA",
            "ANDEE",
            "ANDEEE",
            "ANDEREA",
            "ANDI",
            "ANDIE",
            "ANDRA",
            "ANDREA",
            "ANDREANA",
            "ANDREE",
            "ANDREI",
            "ANDRIA",
            "ANDRIANA",
            "ANDRIETTE",
            "ANDROMACHE",
            "ANDY",
            "ANESTASSIA",
            "ANET",
            "ANETT",
            "ANETTA",
            "ANETTE",
            "ANGE",
            "ANGEL",
            "ANGELA",
            "ANGELE",
            "ANGELIA",
            "ANGELICA",
            "ANGELIKA",
            "ANGELINA",
            "ANGELINE",
            "ANGELIQUE",
            "ANGELITA",
            "ANGELLE",
            "ANGIE",
            "ANGIL",
            "ANGY",
            "ANIA",
            "ANICA",
            "ANISSA",
            "ANITA",
            "ANITRA",
            "ANJANETTE",
            "ANJELA",
            "ANN",
            "ANN-MARIE",
            "ANNA",
            "ANNA-DIANA",
            "ANNA-DIANE",
            "ANNA-MARIA",
            "ANNABAL",
            "ANNABEL",
            "ANNABELA",
            "ANNABELL",
            "ANNABELLA",
            "ANNABELLE",
            "ANNADIANA",
            "ANNADIANE",
            "ANNALEE",
            "ANNALIESE",
            "ANNALISE",
            "ANNAMARIA",
            "ANNAMARIE",
            "ANNE",
            "ANNE-CORINNE",
            "ANNE-MARIE",
            "ANNECORINNE",
            "ANNELIESE",
            "ANNELISE",
            "ANNEMARIE",
            "ANNETTA",
            "ANNETTE",
            "ANNI",
            "ANNICE",
            "ANNIE",
            "ANNIS",
            "ANNISSA",
            "ANNMARIA",
            "ANNMARIE",
            "ANNNORA",
            "ANNORA",
            "ANNY",
            "ANSELMA",
            "ANSLEY",
            "ANSTICE",
            "ANTHE",
            "ANTHEA",
            "ANTHIA",
            "ANTHIATHIA",
            "ANTOINETTE",
            "ANTONELLA",
            "ANTONETTA",
            "ANTONIA",
            "ANTONIE",
            "ANTONIETTA",
            "ANTONINA",
            "ANYA",
            "APPOLONIA",
            "APRIL",
            "APRILETTE",
            "ARA",
            "ARABEL",
            "ARABELA",
            "ARABELE",
            "ARABELLA",
            "ARABELLE",
            "ARDA",
            "ARDATH",
            "ARDEEN",
            "ARDELIA",
            "ARDELIS",
            "ARDELLA",
            "ARDELLE",
            "ARDEN",
            "ARDENE",
            "ARDENIA",
            "ARDINE",
            "ARDIS",
            "ARDISJ",
            "ARDITH",
            "ARDRA",
            "ARDYCE",
            "ARDYS",
            "ARDYTH",
            "ARETHA",
            "ARIADNE",
            "ARIANA",
            "ARIDATHA",
            "ARIEL",
            "ARIELA",
            "ARIELLA",
            "ARIELLE",
            "ARLANA",
            "ARLEE",
            "ARLEEN",
            "ARLEN",
            "ARLENA",
            "ARLENE",
            "ARLETA",
            "ARLETTE",
            "ARLEYNE",
            "ARLIE",
            "ARLIENE",
            "ARLINA",
            "ARLINDA",
            "ARLINE",
            "ARLUENE",
            "ARLY",
            "ARLYN",
            "ARLYNE",
            "ARYN",
            "ASHELY",
            "ASHIA",
            "ASHIEN",
            "ASHIL",
            "ASHLA",
            "ASHLAN",
            "ASHLEE",
            "ASHLEIGH",
            "ASHLEN",
            "ASHLEY",
            "ASHLI",
            "ASHLIE",
            "ASHLY"
    };

    static String[] surnamePool = new String[]{
            "SMITH",
            "JOHNSON",
            "WILLIAMS",
            "JONES",
            "BROWN",
            "DAVIS",
            "MILLER",
            "WILSON",
            "MOORE",
            "TAYLOR",
            "ANDERSON",
            "THOMAS",
            "JACKSON",
            "WHITE",
            "HARRIS",
            "MARTIN",
            "THOMPSON",
            "GARCIA",
            "MARTINEZ",
            "ROBINSON",
            "CLARK",
            "RODRIGUEZ",
            "LEWIS",
            "LEE",
            "WALKER",
            "HALL",
            "ALLEN",
            "YOUNG",
            "HERNANDEZ",
            "KING",
            "WRIGHT",
            "LOPEZ",
            "HILL",
            "SCOTT",
            "GREEN",
            "ADAMS",
            "BAKER",
            "GONZALEZ",
            "NELSON",
            "CARTER",
            "MITCHELL",
            "PEREZ",
            "ROBERTS",
            "TURNER",
            "PHILLIPS",
            "CAMPBELL",
            "PARKER",
            "EVANS",
            "EDWARDS",
            "COLLINS",
            "STEWART",
            "SANCHEZ",
            "MORRIS",
            "ROGERS",
            "REED",
            "COOK",
            "MORGAN",
            "BELL",
            "MURPHY",
            "BAILEY",
            "RIVERA",
            "COOPER",
            "RICHARDSON",
            "COX",
            "HOWARD",
            "WARD",
            "TORRES",
            "PETERSON",
            "GRAY",
            "RAMIREZ",
            "JAMES"
    };
}