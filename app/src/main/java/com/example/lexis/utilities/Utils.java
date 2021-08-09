package com.example.lexis.utilities;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.adapters.VocabularyAdapter;
import com.example.lexis.models.Word;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String TAG = "Utils";
    private static final int MINUTE_STRING_LENGTH = 5;
    private static final int MILLIS_PER_HOUR = 3600000;
    private static final String CHRONOMETER_FORMAT_HOUR = "H:mm:ss";
    private static final String CHRONOMETER_FORMAT_MINUTE = "mm:ss";

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
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return "";

        String objectId = user.getObjectId();
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
    Return the studied languages of the logged-in user.
    */
    public static List<String> getCurrentStudiedLanguages() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return new ArrayList<>();

        String objectId = user.getObjectId();
        try {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", objectId);
            return query.getFirst().getList("studyingLanguages");
        } catch (ParseException e) {
            Log.e(TAG, "Error fetching studied languages", e);
        }
        return new ArrayList<>();
    }

    /*
    Update the studied languages of the logged-in user.
    */
    public static void setCurrentStudiedLanguages(List<String> languages) {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Error setting studied languages");
            return;
        }

        user.put("studyingLanguages", languages);
        user.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "Error setting studied languages", e);
            }
            Log.i(TAG, "Successfully updated studied languages");
        });
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
    Return the ISO language code of the language with the spinner text.
    */
    public static String getLanguageCode(String spinnerText) {
        Map<String, String> languageCodes = new HashMap<String, String>() {{
            put("French", "fr");
            put("Spanish", "es");
            put("German", "de");
            put("Turkish", "tr");
        }};

        int index = spinnerText.indexOf(' ');
        String language = spinnerText.substring(index + 1);
        return languageCodes.get(language);
    }

    /*
    Return the appropriate spinner text with flag and full language name the given ISO language code.
    */
    public static String getSpinnerText(String code) {
        return getFlagEmoji(code) + " " + getFullLanguage(code);
    }

    /*
    Convert a list of ISO language codes into a list appropriate for displaying in a spinner.
    */
    public static List<String> getSpinnerList(List<String> codes) {
        List<String> output = new ArrayList<>();
        for (String code : codes) {
            output.add(getSpinnerText(code));
        }
        return output;
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

    /*
    Return the appropriate error message for the code in the given exception; return the default
    message if none of the listed errors apply.
    */
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
    Strip leading and trailing punctuation from the given string. If an int array is provided,
    store the indices where the stripped word starts and ends within the original string.
    */
    public static String stripPunctuation(String s, int[] ind) {
        String noPunctuationWord = s;
        int wordBegin = 0;
        int wordEnd = s.length() - 1;

        Pattern p = Pattern.compile("\\w+");
        Matcher m = p.matcher(s);
        if (m.find()) {
            wordBegin = m.start();
            wordEnd = m.end();
            noPunctuationWord = s.substring(wordBegin, wordEnd);
        }
        if (ind != null) { // client has access to start and end indices
            ind[0] = wordBegin;
            ind[1] = wordEnd;
        }
        return noPunctuationWord;
    }

    /*
    Set the app logo for the current target language.
    */
    public static void setLanguageLogo(ImageView imageView) {
        Integer currentLanguageLogo = Const.languageLogos.get(Utils.getCurrentTargetLanguage());
        if (currentLanguageLogo != null) {
            imageView.setImageResource(currentLanguageLogo);
        }
    }

    /*
    Add a word with the provided target language and English meanings to the Parse database,
    only if it doesn't already exist in the user's vocabulary.
    */
    public static void addWordToDatabase(String targetLanguage, String targetWord, String englishWord, RecyclerView recyclerView) {
        ParseQuery<Word> query = ParseQuery.getQuery(Word.class);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Word.KEY_TARGET_WORD, targetWord);
        query.whereEqualTo(Word.KEY_TARGET_LANGUAGE, targetLanguage);
        query.getFirstInBackground((word, e) -> {
            if (word == null) {
                saveWord(targetLanguage, targetWord, englishWord, recyclerView);

                // add target language to studied languages if not there already
                List<String> studiedLanguages = Utils.getCurrentStudiedLanguages();
                if (!studiedLanguages.contains(targetLanguage)) {
                    studiedLanguages.add(targetLanguage);
                    Utils.setCurrentStudiedLanguages(studiedLanguages);
                }
            } else {
                Log.i(TAG, "Word already exists in database: " + targetWord);
                if (recyclerView != null) {
                    String message = String.format("Word already exists in vocabulary: %s", targetWord);
                    Toast.makeText(recyclerView.getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
    Save the word with the provided target language and English meanings to the Parse database.
    */
    public static void saveWord(String targetLanguage, String targetWord, String englishWord, RecyclerView recyclerView) {
        Word word = new Word();
        word.setTargetWord(targetWord);
        word.setEnglishWord(englishWord);
        word.setTargetWordLength(targetWord.length());
        word.setTargetWordLower(targetWord.toLowerCase());
        word.setEnglishWordLower(englishWord.toLowerCase());
        word.setTargetLanguage(targetLanguage);
        word.setIsStarred(false);
        word.setUser(ParseUser.getCurrentUser());
        word.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "Error while saving word", e);
                return;
            }

            Log.i(TAG, "Successfully saved word!");
            if (recyclerView != null) {
                VocabularyAdapter adapter = (VocabularyAdapter) recyclerView.getAdapter();
                adapter.insertAt(0, word);
                recyclerView.scrollToPosition(0);
            }
        });
    }

    /*
    Return the URL for the profile picture of the provided ParseUser.
    */
    public static String getProfilePictureUrl(ParseUser user) {
        ParseFile profilePic = user.getParseFile("profilePicture");
        if (profilePic != null) {
            return profilePic.getUrl();
        }
        return null;
    }

    /*
    Create a Bitmap from the image at the specified URI.
    */
    public static Bitmap loadFromUri(Fragment fragment, Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27){
                ImageDecoder.Source source = ImageDecoder.createSource(fragment.getActivity().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(fragment.getActivity().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /*
    Convert the provided string representing a time into milliseconds.
    */
    public static long timerStringToMillis(String time) {
        boolean hasHour = time.length() > MINUTE_STRING_LENGTH;

        // construct date object from given time string
        String myDate = hasHour ? "1970/01/01 " : "1970/01/01 0:";
        myDate += time;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd H:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
        try {
            date = sdf.parse(myDate);
            return date.getTime();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /*
    Convert the provided milliseconds into a displayable timer string.
    */
    public static String millisToTimerString(long millis) {
        boolean hasHour = millis >= MILLIS_PER_HOUR;
        String format = hasHour ? CHRONOMETER_FORMAT_HOUR : CHRONOMETER_FORMAT_MINUTE;
        DateFormat formatter = new SimpleDateFormat(format, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date(millis));
    }

    /*
    Return the best word search time (in milliseconds) for the provided ParseUser.
    */
    public static long getBestTime(ParseUser user) {
        return user.getLong("bestTime");
    }

    /*
    Check whether the provided milliseconds value is a personal best for the user.
    */
    public static boolean isPersonalBest(long millis, ParseUser user) {
        long currentBest = getBestTime(user);
        return currentBest == 0 || millis < currentBest;
    }

    /*
    Update the best word search time for the provided ParseUser with the provided value.
    */
    public static void updateBestTime(long millis, ParseUser user) {
        user.put("bestTime", millis);
        user.saveInBackground();
    }

    /*
    Return the translation frequency interval for the provided ParseUser.
    */
    public static int getTranslationInterval(ParseUser user) {
        return user.getInt("frequencyInterval");
    }

    /*
    Return true if two views overlap on the screen, false otherwise.
    */
    public static boolean viewsOverlap(View firstView, View secondView) {
        int[] firstPosition = new int[2];
        int[] secondPosition = new int[2];

        firstView.getLocationOnScreen(firstPosition);
        secondView.getLocationOnScreen(secondPosition);

        // Rect constructor parameters: left, top, right, bottom
        Rect rectFirstView = new Rect(firstPosition[0], firstPosition[1],
                firstPosition[0] + firstView.getMeasuredWidth(), firstPosition[1] + firstView.getMeasuredHeight());
        Rect rectSecondView = new Rect(secondPosition[0], secondPosition[1],
                secondPosition[0] + secondView.getMeasuredWidth(), secondPosition[1] + secondView.getMeasuredHeight());
        return rectFirstView.intersect(rectSecondView);
    }
}
