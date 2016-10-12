package org.droidplanner.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import org.droidplanner.android.R;


public class SearchToolFragment extends Fragment {

    public interface SearchToolListener {
        void onSearch(String search);
    }

    SearchToolListener listener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_tool, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final EditText searchEditText = (EditText) view.findViewById(R.id.id_cliente_edit_text);
        view.findViewById(R.id.cerca_polizze_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSearch(searchEditText.getText().toString());
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof SearchToolListener)) {
            throw new IllegalStateException("Parent activity must be an instance of " + SearchToolListener.class
                    .getName());
        }

        listener = (SearchToolListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }


}
