package com.printmate.PrintMate.Fragmenti;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.printmate.PrintMate.Activity.HomeActivity;
import com.printmate.PrintMate.Activity.SpojNaPrinterActivity;
import com.printmate.PrintMate.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetaljiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetaljiFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DetaljiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetaljiFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetaljiFragment newInstance(String param1, String param2) {
        DetaljiFragment fragment = new DetaljiFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1) Inflairaj layout fragmenta
        View view = inflater.inflate(R.layout.fragment_detalji, container, false);

        // 2) Dohvati gumb pomoću view.findViewById
        Button btnNastavi = view.findViewById(R.id.buttonfoword);

        // 3) Postavi OnClickListener
        btnNastavi.setOnClickListener(v -> {
            // Intent iz fragmenta: koristimo getActivity() kao Context
            Intent intent = new Intent(getActivity(), SpojNaPrinterActivity.class);
            startActivity(intent);
        });

        // 4) Vrati view
        return view;
    }

}