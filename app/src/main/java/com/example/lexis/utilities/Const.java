package com.example.lexis.utilities;

import com.example.lexis.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Const {
    public static final List<String> languageCodes = Arrays.asList("fr", "es", "de", "tr");
    public static final Map<String, Integer> languageLogos = new HashMap<String, Integer>() {{
        put("fr", R.drawable.logo_france);
        put("es", R.drawable.logo_spain);
        put("de", R.drawable.logo_germany);
        put("tr", R.drawable.logo_turkey);
    }};

    public static final String personModelFile = "personModel.txt";
    public static final String locationModelFile = "locationModel.txt";
    public static final String organizationModelFile = "organizationModel.txt";
}
