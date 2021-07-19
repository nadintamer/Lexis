package com.example.lexis.utilities;

import android.graphics.Rect;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utils {

    private static final String TAG = "Utils";

    /*
    Return a Rect object representing the bounds of the clickableSpan that was clicked. Used to
    determine position of word meaning dialog box relative to clicked word.
    Adapted from: https://stackoverflow.com/questions/11905486/how-get-coordinate-of-a-clickablespan-inside-a-textview
    */
    public static Rect getClickedWordPosition(TextView parentTextView, ClickableSpan clickedText) {
        Rect parentTextViewRect = new Rect();
        SpannableString completeText = (SpannableString) (parentTextView).getText();
        Layout textViewLayout = parentTextView.getLayout();

        // get indices where clicked text starts/ends within textView and use them to identify the
        // x positions where the text starts/ends
        double startOffsetOfClickedText = completeText.getSpanStart(clickedText);
        double endOffsetOfClickedText = completeText.getSpanEnd(clickedText);
        double startXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int) startOffsetOfClickedText);
        double endXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int) endOffsetOfClickedText);

        // get the line number where the clicked text appears and use it to store the bounds of the
        // line into the Rect
        int currentLineStartOffset = textViewLayout.getLineForOffset((int) startOffsetOfClickedText);
        textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect);

        // get the coordinates of current line on the screen
        int[] parentTextViewLocation = {0, 0};
        parentTextView.getLocationOnScreen(parentTextViewLocation);

        // calculate offset for top and bottom taking into account scroll position and padding
        double parentTextViewTopAndBottomOffset = (
                parentTextViewLocation[1] -
                        parentTextView.getScrollY() +
                        parentTextView.getCompoundPaddingTop()
        );

        parentTextViewRect.top += parentTextViewTopAndBottomOffset;
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;

        // calculate left edge of rect by adding offset of clicked text to x position of current
        // line, take into account padding and scroll
        parentTextViewRect.left += (
                parentTextViewLocation[0] +
                        startXCoordinatesOfClickedText +
                        parentTextView.getCompoundPaddingLeft() -
                        parentTextView.getScrollX()
        );

        // calculate right edge of rect by adding the width of the clicked text
        parentTextViewRect.right = (int) (
                parentTextViewRect.left +
                        endXCoordinatesOfClickedText -
                        startXCoordinatesOfClickedText
        );

        return parentTextViewRect;
    }

    /*
    Return a Calendar object representing yesterday's date. Used to get most recent top articles
    from Wikipedia (since today's top articles are usually not posted yet).
    */
    public static Calendar getYesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal;
    }

    /*
    Return a String that represents the given Date object in the format "MMMM d, yyyy" (for example,
    "July 14, 2021").
    */
    public static String formatDate(Date date) {
        SimpleDateFormat written = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
        return written.format(date);
    }

    /*
    Return the current target language of the logged-in user.
    */
    public static String getCurrentTargetLanguage() {
        String objectId = ParseUser.getCurrentUser().getObjectId();
        try {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", objectId);
            return query.getFirst().getString("targetLanguage");
        } catch (ParseException e) {
            Log.e(TAG, "Error fetching target language", e);
        }
        return "";
    }

    /*
    Return the full name of the language with the given ISO language code.
    */
    public static String getFullLanguage(String code) {
        Map<String, String> languageCodes = new HashMap<String, String>() {{
            put("fr", "French");
            put("es", "Spanish");
            put("de", "German");
            put("tr", "Turkish");
        }};

        return languageCodes.get(code);
    }

    /*
    Return the flag emoji associated with the given ISO language code.
    */
    public static String getFlagEmoji(String code) {
        switch (code) {
            case "de":
                return "🇩🇪";
            case "fr":
                return "🇫🇷";
            case "tr":
                return "🇹🇷";
            case "es":
                return "🇪🇸";
            case "en":
                return "🇺🇸";
            default:
                return "";
        }
    }

    public static String getUserErrorMessage(ParseException e, String defaultMessage) {
        String errorMessage;
        switch (e.getCode()) {
            case 101:
                errorMessage = "Invalid username/password!";
                break;
            case 200:
                errorMessage = "Username cannot be empty!";
                break;
            case 201:
                errorMessage = "Password cannot be empty!";
                break;
            case 202:
                errorMessage = "Username is already in use!";
                break;
            case 203:
                errorMessage = "E-mail is already in use!";
                break;
            default:
                errorMessage = defaultMessage;
        }
        return errorMessage;
    }

    /*
    Set the app logo for the current target language.
    */
    public static void setLanguageLogo(ImageView imageView) {
        int currentLanguageLogo = Const.languageLogos.get(Utils.getCurrentTargetLanguage());
        imageView.setImageResource(currentLanguageLogo);
    }
}
