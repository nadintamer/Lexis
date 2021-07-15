package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lexis.databinding.FragmentProfileInfoBinding;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.Utils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class ProfileInfoFragment extends Fragment {

    private static final String TAG = "ProfileInfoFragment";

    FragmentProfileInfoBinding binding;
    ParseUser user;

    private static final String ARG_USER = "user";

    public ProfileInfoFragment() {}

    public static ProfileInfoFragment newInstance(ParseUser user) {
        ProfileInfoFragment fragment = new ProfileInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = Parcels.unwrap(getArguments().getParcelable(ARG_USER));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileInfoBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String targetLanguage = Utils.getFullLanguage(Utils.getCurrentTargetLanguage());
        String flag = Utils.getFlagEmoji(Utils.getCurrentTargetLanguage());
        String userJoined = Utils.formatDate(user.getCreatedAt());

        binding.tvUsername.setText(user.getUsername());
        binding.tvMemberSince.setText(String.format("Joined %s", userJoined));
        binding.tvTargetLanguage.setText(String.format("%s %s", flag, targetLanguage));
        binding.tvVocabularyTarget.setText(String.format("%s words studied", targetLanguage));

        setNumWordsSeen(true); // target language
        setNumWordsSeen(false); // total
    }

    /*
    Set the contents of the text views with information about the number of words the user studied.
    If targetOnly is true, the number of words studied in the target language will be counted. If
    it is false, all the words the user has studied (in any language) will be counted.
    */
    private void setNumWordsSeen(Boolean targetOnly) {
        ParseQuery<Word> query = ParseQuery.getQuery(Word.class);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        if (targetOnly) {
            query.whereEqualTo(Word.KEY_TARGET_LANGUAGE, Utils.getCurrentTargetLanguage());
        }
        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary size", e);
                return;
            }

            String numWords = String.valueOf(words.size());
            if (targetOnly) {
                binding.tvNumTarget.setText(numWords);
            } else {
                binding.tvNumTotal.setText(numWords);
            }
        });
    }
}