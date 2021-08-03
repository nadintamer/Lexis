package com.example.lexis;

import com.bumptech.glide.Glide;
import com.example.lexis.models.Word;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Word.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.parse_application_id))
                .clientKey(getString(R.string.parse_client_key))
                .server(getString(R.string.parse_server))
                .build()
        );

        DrawerImageLoader.Companion.init(new DrawerImageLoader.IDrawerImageLoader() {
            @Override
            public void set(@NotNull ImageView imageView, @NotNull Uri uri, @NotNull Drawable drawable, @Nullable String s) {
                Glide.with(imageView.getContext())
                        .load(uri)
                        .circleCrop()
                        .placeholder(placeholder(imageView.getContext()))
                        .into(imageView);
            }

            @Override
            public void cancel(@NotNull ImageView imageView) {
                Glide.with(imageView.getContext()).clear(imageView);
            }

            @NotNull
            @Override
            public Drawable placeholder(@NotNull Context ctx) {
                return AppCompatResources.getDrawable(ctx, R.drawable.placeholder);
            }

            @NotNull
            @Override
            public Drawable placeholder(@NotNull Context context, @Nullable String s) {
                return AppCompatResources.getDrawable(context, R.drawable.placeholder);
            }
        });
    }
}
