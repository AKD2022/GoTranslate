package com.example.translate.SharedViewModels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// Shared View Model saves the language last selected by the user.

public class SharedViewModelForVoiceTranslateActivity extends AndroidViewModel {
    private static final String PREFERENCES_FILE = "com.example.translate_preferences";
    private static final String KEY_TRANSLATE_TO_LANGUAGE = "translate_to_language_voice";
    private static final String KEY_TRANSLATE_FROM_LANGUAGE = "translate_from_language_voice";

    private MutableLiveData<String> selectedTranslateToLanguage;
    private MutableLiveData<String> selectedTranslateFromLanguage;
    private SharedPreferences sharedPreferences;

    public SharedViewModelForVoiceTranslateActivity(Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        selectedTranslateToLanguage = new MutableLiveData<>(sharedPreferences.getString(KEY_TRANSLATE_TO_LANGUAGE, "Select Language"));
        selectedTranslateFromLanguage = new MutableLiveData<>(sharedPreferences.getString(KEY_TRANSLATE_FROM_LANGUAGE, "Select Language"));
    }

    public LiveData<String> getSelectedTranslateToLanguage() {
        return selectedTranslateToLanguage;
    }

    public void setSelectedTranslateToLanguage(String language) {
        selectedTranslateToLanguage.setValue(language);
        sharedPreferences.edit().putString(KEY_TRANSLATE_TO_LANGUAGE, language).apply();
    }

    public LiveData<String> getSelectedTranslateFromLanguage() {
        return selectedTranslateFromLanguage;
    }

    public void setSelectedTranslateFromLanguage(String language) {
        selectedTranslateFromLanguage.setValue(language);
        sharedPreferences.edit().putString(KEY_TRANSLATE_FROM_LANGUAGE, language).apply();
    }
}
