package com.example.lexis.utilities;

import android.graphics.Rect;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.Calendar;

public class Utils {

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
    Return the current target language of the logged-in user.
    */
    public static String getCurrentTargetLanguage() {
        return ParseUser.getCurrentUser().getString("targetLanguage");
    }

    /*
    Return the flag emoji associated with the given ISO language code.
    */
    public static String getFlagEmoji(String language) {
        switch (language) {
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
}
