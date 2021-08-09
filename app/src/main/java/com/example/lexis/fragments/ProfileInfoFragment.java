package com.example.lexis.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.lexis.R;
import com.example.lexis.databinding.FragmentProfileInfoBinding;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.Utils;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;

public class ProfileInfoFragment extends Fragment {

    private static final String TAG = "ProfileInfoFragment";

    FragmentProfileInfoBinding binding;
    ParseUser user;

    private static final String ARG_USER = "user";
    private static final int GALLERY_REQUEST_CODE = 845;
    private ImageButton hamburgerMenu;

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

        long bestTime = Utils.getBestTime(user);
        if (bestTime == 0) {
            binding.tvNumPersonalBest.setText("N/A");
        } else {
            binding.tvNumPersonalBest.setText(Utils.millisToTimerString(bestTime));
        }

        fetchProfilePicture();
        binding.ivProfilePicture.setOnClickListener(v -> launchGallery());

        setNumWordsSeen(true); // words studied in target language
        setNumWordsSeen(false); // words studied total

        if (getActivity() != null) {
            hamburgerMenu = getActivity().findViewById(R.id.toolbar).findViewById(R.id.ibHamburger);
        }
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

    /*
    Launch photo gallery so that user can upload a profile picture.
    */
    private void launchGallery() {
        // create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // bring up gallery to select a photo
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        }
    }

    /*
    Handle returning from gallery choosing intent and save profile photo.
    */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((data != null) && requestCode == GALLERY_REQUEST_CODE) {
            // user cannot open navigation drawer until picture is saved (otherwise it can crash)
            hamburgerMenu.setEnabled(false);
            showProgressBar();

            Uri photoUri = data.getData();
            Bitmap selectedImage = Utils.loadFromUri(this, photoUri);
            saveProfilePhoto(selectedImage);
        }
    }

    /*
    Save the user's new profile picture (stored in the provided Bitmap) onto Parse.
    Code adapted from:
    https://stackoverflow.com/questions/27146665/putting-image-from-gallery-in-parsefile-in-android
    */
    public void saveProfilePhoto(Bitmap map) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress image to lower quality scale 1 - 100
        map.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image = stream.toByteArray();

        ParseFile file  = new ParseFile("profilePhoto.jpeg", image);
        ParseUser user = ParseUser.getCurrentUser();
        user.put("profilePicture", file);
        user.saveInBackground(e -> {
            if (e != null) {
                Toast.makeText(getActivity(), "Error uploading profile photo!", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchProfilePicture();
            hamburgerMenu.setEnabled(true);
        });
    }

    /*
    Fetch the user's latest profile picture from Parse and load it into the image view.
    */
    private void fetchProfilePicture() {
        ParseFile imageFile = user.getParseFile("profilePicture");
        if (imageFile != null && getActivity() != null) {
            Glide.with(getActivity())
                    .load(imageFile.getUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            hideProgressBar();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            hideProgressBar();
                            return false;
                        }
                    })
                    .circleCrop()
                    .into(binding.ivProfilePicture);
        }
    }

    /*
    Helper functions to show and hide progress bar.
    */
    private void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
    }
}