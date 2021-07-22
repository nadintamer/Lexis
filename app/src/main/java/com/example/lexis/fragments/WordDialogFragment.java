package com.example.lexis.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lexis.databinding.FragmentWordBinding;
import com.example.lexis.models.Word;

import org.parceler.Parcels;

public class WordDialogFragment extends DialogFragment {

    FragmentWordBinding binding;
    private Integer left;
    private Integer top;
    private Integer width;

    public WordDialogFragment() {}

    public WordDialogFragment(int left, int top, int width) {
        this.left = left;
        this.top = top;
        this.width = width;
    }

    public static WordDialogFragment newInstance(String target, String english, int left, int top, int width) {
        WordDialogFragment frag = new WordDialogFragment(left, top, width);
        Bundle args = new Bundle();
        args.putString("targetLanguage", target);
        args.putString("english", english);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWordBinding.inflate(inflater);
        setDialogPosition();
        setTransparentBackground();
        return binding.getRoot();
    }

    private void setTransparentBackground() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void setDialogPosition() {
        if (left == null || top == null || width == null) {
            return;
        }

        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP | Gravity.START); // set origin to top-left

        // set X and Y to be slightly above clicked word
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = left - width / 2;
        params.y = top - 260;
        window.setAttributes(params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            String target = getArguments().getString("targetLanguage", "");
            String english = getArguments().getString("english", "");
            binding.tvTargetLanguage.setText(target);
            binding.tvEnglish.setText(english);
        }
    }

    @Override public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0; // don't dim background view
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
    }
}
