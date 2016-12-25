package com.example.rishabh.you4to;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class AddLinkFragment extends BaseFragment
        implements View.OnClickListener {

    private EditText url;
    private Button submit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_main, container, false);

        url = (EditText)v.findViewById(R.id.url);
        submit = (Button) v.findViewById(R.id.submit_url);
        submit.setOnClickListener(this);

        return v;

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.submit_url){
            MenuItem item = ((MainActivity) getActivity()).getNavigationView().getMenu().getItem(1);
            item.setChecked(true);
            Bundle args = new Bundle();
            args.putString(Intent.EXTRA_TEXT, url.getText().toString());
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = new PlayerFragment();
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }
    }
}
