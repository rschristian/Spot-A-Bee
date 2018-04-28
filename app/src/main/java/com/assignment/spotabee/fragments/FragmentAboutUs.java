package com.assignment.spotabee.fragments;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.assignment.spotabee.R;


public class FragmentAboutUs extends Fragment {
    private AppCompatButton goToYouTube;
    private View rootView;
    private static final String videoURL = "https://www.youtube.com/watch?v=pbeSHt4B3hg";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments

        rootView = inflater.inflate(R.layout.fragment_menu_aboutus, container, false);
        this.goToYouTube = rootView.findViewById(R.id.goToYoutube);
        goToYouTube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri videoUri =  Uri.parse(videoURL);
                Intent youtubeVideo = new Intent(Intent.ACTION_VIEW);
                youtubeVideo.setData(videoUri);
                startActivity(youtubeVideo);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("About Us");
    }
}
