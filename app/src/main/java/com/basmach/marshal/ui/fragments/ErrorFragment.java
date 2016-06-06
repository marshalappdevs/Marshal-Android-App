package com.basmach.marshal.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.basmach.marshal.R;
import com.basmach.marshal.ui.MainActivity;

public class ErrorFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_error, container, false);

        Button btnTryAgain = (Button) rootView.findViewById(R.id.error_fragment_button_tryAgain);
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).updateData();
            }
        });

        return rootView;
    }
}
