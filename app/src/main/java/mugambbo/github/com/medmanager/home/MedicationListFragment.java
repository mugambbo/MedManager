package mugambbo.github.com.medmanager.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mugambbo.github.com.medmanager.R;
import mugambbo.github.com.medmanager.constant.Constants;
import mugambbo.github.com.medmanager.database.AppDatabase;
import mugambbo.github.com.medmanager.interfaces.RecyclerViewListener;
import mugambbo.github.com.medmanager.model.DateItem;
import mugambbo.github.com.medmanager.model.ListItem;
import mugambbo.github.com.medmanager.model.MedicationItem;
import mugambbo.github.com.medmanager.util.DateTimeUtil;

public class MedicationListFragment extends Fragment implements View.OnClickListener, RecyclerViewListener {

//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<ListItem> listItems = new ArrayList<>();
    private TextView mNoMedication;
    public static final String SELECTED_MEDICATION = "SelectedMedication";
    String query = "";


    public MedicationListFragment() {
        // Required empty public constructor
    }


    public static MedicationListFragment newInstance(String param1, String param2) {
        MedicationListFragment fragment = new MedicationListFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medication_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_medication_list);
        mNoMedication = (TextView) rootView.findViewById(R.id.tv_no_medication);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MedicationAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        attachSwipeToDeleteToRecyclerView();
        FloatingActionButton mAddMeducationFab = (FloatingActionButton) rootView.findViewById(R.id.fab_add_medication);
        mAddMeducationFab.setOnClickListener(this);
        return rootView;
    }

    private void attachSwipeToDeleteToRecyclerView() {

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                // Row is swiped from recycler view
                // remove it from adapter
                if (listItems.get(viewHolder.getAdapterPosition()).getType() != ListItem.TYPE_DATE) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            AppDatabase.getInstance(getContext()).medicationItemDao().deleteMedication((MedicationItem) listItems.get(viewHolder.getAdapterPosition()));
                            assert getActivity() != null;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateMedicationList();
                                }
                            });
                        }
                    });

                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // view the background view
            }
        };

// attaching the touch helper to recycler view
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

    }

    private void updateMedicationList(){
        fetchMedicationItems();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMedicationList();
    }

    private void fetchMedicationItems (){
        //Query DB
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<MedicationItem> medicationItemsDB;
                medicationItemsDB = AppDatabase.getInstance(getContext()).medicationItemDao().getMedicationsByName("%"+query+"%");
                HashMap<String, List<MedicationItem>> groupedHashMap = groupDataIntoHashMap(medicationItemsDB);
                List<ListItem> consolidatedMedicationList = new ArrayList<>();
                for (String date : groupedHashMap.keySet()) {
                    DateItem dateItem = new DateItem();
                    dateItem.setMedStartDate(date);
                    consolidatedMedicationList.add(dateItem);

                    consolidatedMedicationList.addAll(groupedHashMap.get(date));
                }

                listItems = consolidatedMedicationList;

                assert getActivity() != null;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listItems.size() < 1){
                            //show no medication
                            mNoMedication.setVisibility(View.VISIBLE);
                        } else {
                            //hide no medication
                            mNoMedication.setVisibility(View.GONE);
                        }
                        ((MedicationAdapter)mAdapter).updateMedicationList(listItems);
                    }
                });

            }
        });
    }

    private HashMap<String, List<MedicationItem>> groupDataIntoHashMap(List<MedicationItem> medicationItems) {
        HashMap<String, List<MedicationItem>> groupedHashMap = new HashMap<>();
        for(MedicationItem medicationItem : medicationItems) {
            String hashMapKey;
            try {
                hashMapKey = DateTimeUtil.changeDateFormat(Constants.TIMESTAMP_PATTERN, Constants.MONTH_PATTERN, medicationItem.getMedStartDate());
            } catch (Exception err){
                err.printStackTrace();
                hashMapKey = medicationItem.getMedStartDate();
            }

            if(groupedHashMap.containsKey(hashMapKey)) {
                // The key is already in the HashMap; add the pojo object
                // against the existing key.
                groupedHashMap.get(hashMapKey).add(medicationItem);
            } else {
                // The key is not there in the HashMap; create a new key-value pair
                List<MedicationItem> list = new ArrayList<>();
                list.add(medicationItem);
                groupedHashMap.put(hashMapKey, list);
            }
        }

        return groupedHashMap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_medication:
                launchAddMedicationActivity();
        }
    }

    private void launchAddMedicationActivity() {
        Intent addMedicationIntent = new Intent(getContext(), AddMedicationActivity.class);
        startActivity(addMedicationIntent);
    }

    @Override
    public void onMedicationItemClick(View v, int adapterPosition) {
        Intent addMedicationIntent = new Intent(getContext(), AddMedicationActivity.class);
        addMedicationIntent.putExtra(SELECTED_MEDICATION, ((MedicationItem) listItems.get(adapterPosition)).getMedId());
        startActivity(addMedicationIntent);
    }

    public void queryMedicationDatabase(String query){
        this.query = query;
        updateMedicationList();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
