package com.example.translate.AlwaysAvailablePages;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.translate.MainActivity;
import com.example.translate.R;
import com.example.translate.SharedViewModels.SharedViewModelForVoiceTranslateActivity;
import com.google.android.gms.tasks.Task;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.tashila.pleasewait.PleaseWaitDialog;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;


public class VoiceTranslateActivity extends AppCompatActivity {

    FloatingActionButton backButton;
    Locale selectedLocale;
    private MaterialButton translate;
    private MaterialButton selectTranslateFrom, selectTranslateTo, switchLanguage;
    MaterialButton micButton, playAudio;
    private String translateToButton, translateFromButton; // translateTo, translateFrom define what language is chosen
    private String languageTranslateTo, languageTranslateFrom; // defines the language code (use for translation)
    private String recordedText, translatedText;
    TextView recordedTextView, translatedTextView;
    TextToSpeech textToSpeech;
    private int speechProgress = 0;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private boolean isSpeaking = false;

    private SharedViewModelForVoiceTranslateActivity sharedViewModel;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_translate);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        backButton = findViewById(R.id.goBackButton);
        selectTranslateFrom = findViewById(R.id.selectTranslateFrom);
        selectTranslateTo = findViewById(R.id.selectTranslateTo);
        micButton = findViewById(R.id.micButton);
        recordedTextView = findViewById(R.id.recordedTextView);
        translate = findViewById(R.id.translate);
        playAudio = findViewById(R.id.playAudio);

        backButton.setOnClickListener(view -> {
            toHome();
        });

        /* Bottom Navigation View */
        ChipNavigationBar navigationBar = findViewById(R.id.nav);
        navigationBar.setItemSelected(R.id.audio, true);

        navigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.image) {
                    toImage();
                } else if (i == R.id.text) {
                    toText();
                } else if (i == R.id.downloadLanguages) {
                    toDownload();
                }
            }
        });
        /* Bottom Navigation View */

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModelForVoiceTranslateActivity.class);

        sharedViewModel.getSelectedTranslateToLanguage().observe(this, selectedLanguageTranslateTo -> {
            if (selectedLanguageTranslateTo != null) {
                selectTranslateTo.setText(selectedLanguageTranslateTo);
                translateToButton = selectedLanguageTranslateTo; // update translateToButton
            }

            assert selectedLanguageTranslateTo != null;
            if (selectedLanguageTranslateTo.isBlank()) {
                selectTranslateTo.setText("Select Language");
            }
        });

        sharedViewModel.getSelectedTranslateFromLanguage().observe(this, selectedLanguageTranslateFrom -> {
            if (selectedLanguageTranslateFrom != null) {
                selectTranslateFrom.setText(selectedLanguageTranslateFrom);
                translateFromButton = selectedLanguageTranslateFrom; // update translateFromButton
            }

            assert selectedLanguageTranslateFrom != null;
            if (selectedLanguageTranslateFrom.isBlank()) {
                selectTranslateTo.setText("Select Language");
            }
        });

        selectTranslateTo.setOnClickListener(view -> {
            selectTranslateTo();
        });

        selectTranslateFrom.setOnClickListener(view -> {
            selectTranslateFrom();
        });

        micButton.setOnClickListener(view -> {
            checkRecordingPermissionAndRecord();
        });



        playAudio.setOnClickListener(view -> {
            if (translateToButton == null) {
                Toast.makeText(this, "Please select language to translate from", Toast.LENGTH_SHORT).show();
            } else if (translateFromButton == null ) {
                Toast.makeText(this, "Please select language to translate to", Toast.LENGTH_SHORT).show();
            } else if (translateToButton.equals(translateFromButton)) {
                Toast.makeText(this, "You cannot select the same language twice", Toast.LENGTH_SHORT).show();
            } else if (recordedText == null || recordedText.isEmpty()) {
                Toast.makeText(this, "Please record voice first", Toast.LENGTH_SHORT).show();
            } else if (translatedText == null || translatedText.isBlank()) {
                Toast.makeText(this, "Please Translate Text first", Toast.LENGTH_SHORT).show();
            }

            if (translatedText != null && !translatedText.isEmpty() || translateToButton != null && !translateToButton.isEmpty() || translateFromButton != null && !translateFromButton.isEmpty()) {

                if (isSpeaking) {
                    textToSpeech.stop();
                    playAudio.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.outline_play_circle_24));
                    isSpeaking = false;
                } else {
                    textToSpeech.speak(translatedText.substring(speechProgress), TextToSpeech.QUEUE_FLUSH, null, "utteranceId");
                    playAudio.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.outline_pause_circle_outline_24));
                    isSpeaking = true;
                }
            } else {
                Toast.makeText(this, "Error, please select languages, translate, and then play audio", Toast.LENGTH_SHORT).show();
            }
        });

        translate.setOnClickListener(view -> {
            if (translateToButton == null) {
                Toast.makeText(this, "Please select language to translate from", Toast.LENGTH_SHORT).show();
            } else if (translateFromButton == null ) {
                Toast.makeText(this, "Please select language to translate to", Toast.LENGTH_SHORT).show();
            } else if (translateToButton.equals(translateFromButton)) {
                Toast.makeText(this, "You cannot select the same language twice", Toast.LENGTH_SHORT).show();
            } else if (recordedText == null || recordedText.isEmpty()) {
                Toast.makeText(this, "Please record voice first", Toast.LENGTH_SHORT).show();
            } else {
                getLanguageTo();
                getLanguageFrom();
                translate();
                initializeTextToSpeech();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                recordedText = Objects.requireNonNull(result).get(0);
                recordedTextView.setText(recordedText);
            }
        }
    }

    private void checkRecordingPermissionAndRecord() {
        if (ActivityCompat.checkSelfPermission(VoiceTranslateActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(VoiceTranslateActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_SPEECH_INPUT);
        } else {
           record();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                record();
            } else {
                Toast.makeText(this, "Audio Recording Denied. Please Allow to record audio for full functionality.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void record() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTranslateFrom);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }
        catch (Exception e) {
            Toast
                    .makeText(VoiceTranslateActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }


    public void selectTranslateTo() {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), selectTranslateTo);

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.select_language, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String selectedLanguageTranslateTo = menuItem.getTitle().toString();
            selectTranslateTo.setText(selectedLanguageTranslateTo);
            sharedViewModel.setSelectedTranslateToLanguage(selectedLanguageTranslateTo);
            Toast.makeText(getApplicationContext(), "You Clicked " + selectedLanguageTranslateTo, Toast.LENGTH_SHORT).show();
            return true;
        });
        // Showing the popup menu
        popupMenu.show();
    }

    public void selectTranslateFrom() {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), selectTranslateFrom);

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.select_language, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String selectedLanguageTranslateFrom = menuItem.getTitle().toString();
            selectTranslateFrom.setText(selectedLanguageTranslateFrom);
            sharedViewModel.setSelectedTranslateFromLanguage(selectedLanguageTranslateFrom);
            Toast.makeText(getApplicationContext(), "You Clicked " + selectedLanguageTranslateFrom, Toast.LENGTH_SHORT).show();
            return true;
        });
        // Showing the popup menu
        popupMenu.show();
    }

    public void getLanguageTo() {
        switch (translateToButton) {
            case "Arabic":
                languageTranslateTo = "ar";
                selectedLocale = new Locale("ar");
                break;
            case "Bulgarian":
                languageTranslateTo = "bg";
                selectedLocale = new Locale("bg");
                break;
            case "Bengali":
                languageTranslateTo = "bn";
                selectedLocale = new Locale("bn");
                break;
            case "Catalan":
                languageTranslateTo = "ca";
                selectedLocale = new Locale("ca");
                break;
            case "Czech":
                languageTranslateTo = "cs";
                selectedLocale = new Locale("cs");
                break;
            case "Welsh":
                languageTranslateTo = "cy";
                selectedLocale = new Locale("cy");
                break;
            case "Danish":
                languageTranslateTo = "da";
                selectedLocale = new Locale("da");
                break;
            case "German":
                languageTranslateTo = "de";
                selectedLocale = new Locale("de");
                break;
            case "Greek":
                languageTranslateTo = "el";
                selectedLocale = new Locale("el");
                break;
            case "English":
                languageTranslateTo = "en";
                selectedLocale = new Locale("en");
                break;
            case "Spanish":
                languageTranslateTo = "es";
                selectedLocale = new Locale("es");
                break;
            case "Estonian":
                languageTranslateTo = "et";
                selectedLocale = new Locale("et");
                break;
            case "Finnish":
                languageTranslateTo = "fi";
                selectedLocale = new Locale("fi");
                break;
            case "French":
                languageTranslateTo = "fr";
                selectedLocale = new Locale("fr");
                break;
            case "Gujarati":
                languageTranslateTo = "gu";
                selectedLocale = new Locale("gu");
                break;
            case "Hebrew":
                languageTranslateTo = "he";
                selectedLocale = new Locale("he");
                break;
            case "Hindi":
                languageTranslateTo = "hi";
                selectedLocale = new Locale("hi");
                break;
            case "Croatian":
                languageTranslateTo = "hr";
                selectedLocale = new Locale("hr");
                break;
            case "Hungarian":
                languageTranslateTo = "hu";
                selectedLocale = new Locale("hu");
                break;
            case "Indonesian":
                languageTranslateTo = "id";
                selectedLocale = new Locale("id");
                break;
            case "Icelandic":
                languageTranslateTo = "is";
                selectedLocale = new Locale("is");
                break;
            case "Italian":
                languageTranslateTo = "it";
                selectedLocale = new Locale("it");
                break;
            case "Japanese":
                languageTranslateTo = "ja";
                selectedLocale = new Locale("ja");
                break;
            case "Kannada":
                languageTranslateTo = "kn";
                selectedLocale = new Locale("kn");
                break;
            case "Korean":
                languageTranslateTo = "ko";
                selectedLocale = new Locale("ko");
                break;
            case "Lithuanian":
                languageTranslateTo = "lt";
                selectedLocale = new Locale("lt");
                break;
            case "Marathi":
                languageTranslateTo = "mr";
                selectedLocale = new Locale("mr");
                break;
            case "Malay":
                languageTranslateTo = "ms";
                selectedLocale = new Locale("ms");
                break;
            case "Dutch":
                languageTranslateTo = "nl";
                selectedLocale = new Locale("nl");
                break;
            case "Norwegian":
                languageTranslateTo = "no";
                selectedLocale = new Locale("no");
                break;
            case "Poland":
                languageTranslateTo = "pl";
                selectedLocale = new Locale("pl");
                break;
            case "Portuguese":
                languageTranslateTo = "pt";
                selectedLocale = new Locale("pt");
                break;
            case "Romanian":
                languageTranslateTo = "ro";
                selectedLocale = new Locale("ro");
                break;
            case "Russian":
                languageTranslateTo = "ru";
                selectedLocale = new Locale("ru");
                break;
            case "Slovak":
                languageTranslateTo = "sk";
                selectedLocale = new Locale("sk");
                break;
            case "Albanian":
                languageTranslateTo = "sq";
                selectedLocale = new Locale("sq");
                break;
            case "Swedish":
                languageTranslateTo = "sv";
                selectedLocale = new Locale("sv");
                break;
            case "Swahili":
                languageTranslateTo = "sw";
                selectedLocale = new Locale("sw");
                break;
            case "Tamil":
                languageTranslateTo = "ta";
                selectedLocale = new Locale("ta");
                break;
            case "Telugu":
                languageTranslateTo = "te";
                selectedLocale = new Locale("te");
                break;
            case "Thai":
                languageTranslateTo = "th";
                selectedLocale = new Locale("th");
                break;
            case "Tagalog":
                languageTranslateTo = "tl";
                selectedLocale = new Locale("tl");
                break;
            case "Turkish":
                languageTranslateTo = "tr";
                selectedLocale = new Locale("tr");
                break;
            case "Ukrainian":
                languageTranslateTo = "uk";
                selectedLocale = new Locale("uk");
                break;
            case "Urdu":
                languageTranslateTo = "ur";
                selectedLocale = new Locale("ur");
                break;
            case "Vietnamese":
                languageTranslateTo = "vi";
                selectedLocale = new Locale("vi");
                break;
            case "Chinese":
                languageTranslateTo = "zh";
                selectedLocale = new Locale("zh");
                break;
        }
    }

    public void getLanguageFrom() {
        switch (translateFromButton) {
            case "Arabic":
                languageTranslateFrom = "ar";
                break;
            case "Bulgarian":
                languageTranslateFrom = "bg";
                break;
            case "Bengali":
                languageTranslateFrom = "bn";
                break;
            case "Catalan":
                languageTranslateFrom = "ca";
                break;
            case "Czech":
                languageTranslateFrom = "cs";
                break;
            case "Welsh":
                languageTranslateFrom = "cy";
                break;
            case "Danish":
                languageTranslateFrom = "da";
                break;
            case "German":
                languageTranslateFrom = "de";
                break;
            case "Greek":
                languageTranslateFrom = "el";
                break;
            case "English":
                languageTranslateFrom = "en";
                break;
            case "Spanish":
                languageTranslateFrom = "es";
                break;
            case "Estonian":
                languageTranslateFrom = "et";
                break;
            case "Finnish":
                languageTranslateFrom = "fi";
                break;
            case "French":
                languageTranslateFrom = "fr";
                break;
            case "Gujarati":
                languageTranslateFrom = "gu";
                break;
            case "Hebrew":
                languageTranslateFrom = "he";
                break;
            case "Hindi":
                languageTranslateFrom = "hi";
                break;
            case "Croatian":
                languageTranslateFrom = "hr";
                break;
            case "Hungarian":
                languageTranslateFrom = "hu";
                break;
            case "Indonesian":
                languageTranslateFrom = "id";
                break;
            case "Icelandic":
                languageTranslateFrom = "is";
                break;
            case "Italian":
                languageTranslateFrom = "it";
                break;
            case "Japanese":
                languageTranslateFrom = "ja";
                break;
            case "Kannada":
                languageTranslateFrom = "kn";
                break;
            case "Korean":
                languageTranslateFrom = "ko";
                break;
            case "Lithuanian":
                languageTranslateFrom = "lt";
                break;
            case "Marathi":
                languageTranslateFrom = "mr";
                break;
            case "Malay":
                languageTranslateFrom = "ms";
                break;
            case "Dutch":
                languageTranslateFrom = "nl";
                break;
            case "Norwegian":
                languageTranslateFrom = "no";
                break;
            case "Poland":
                languageTranslateFrom = "pl";
                break;
            case "Portuguese":
                languageTranslateFrom = "pt";
                break;
            case "Romanian":
                languageTranslateFrom = "ro";
                break;
            case "Russian":
                languageTranslateFrom = "ru";
                break;
            case "Slovak":
                languageTranslateFrom = "sk";
                break;
            case "Albanian":
                languageTranslateFrom = "sq";
                break;
            case "Swedish":
                languageTranslateFrom = "sv";
                break;
            case "Swahili":
                languageTranslateFrom = "sw";
                break;
            case "Tamil":
                languageTranslateFrom = "ta";
                break;
            case "Telugu":
                languageTranslateFrom = "te";
                break;
            case "Thai":
                languageTranslateFrom = "th";
                break;
            case "Tagalog":
                languageTranslateFrom = "tl";
                break;
            case "Turkish":
                languageTranslateFrom = "tr";
                break;
            case "Ukrainian":
                languageTranslateFrom = "uk";
                break;
            case "Urdu":
                languageTranslateFrom = "ur";
                break;
            case "Vietnamese":
                languageTranslateFrom = "vi";
                break;
            case "Chinese":
                languageTranslateFrom = "zh";
                break;
        }
    }

    private void translate() {
        PleaseWaitDialog progressDialog = new PleaseWaitDialog(this);
        progressDialog.setEnterTransition(R.anim.fade_in);
        progressDialog.setExitTransition(R.anim.fade_out);
        progressDialog.setHasOptionsMenu(true);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(PleaseWaitDialog.ProgressStyle.LINEAR);
        progressDialog.setTitle("Installing Translation Model");
        progressDialog.setMessage("This is done to speed up the translation, next time you use this. Next time you translate, " +
                "translation will happen in a few seconds, skipping this process");
        progressDialog.show();

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setTargetLanguage(languageTranslateTo)
                .setSourceLanguage(languageTranslateFrom)
                .build();
        Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    translateWithModelAvailable(translator, recordedText);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Translation model download failed: " + e.getMessage());
                    new MaterialAlertDialogBuilder(VoiceTranslateActivity.this)
                            .setMessage("Translation Failed. There may be a problem with installing the translation model.")
                            .setPositiveButton("OK", null)
                            .show();
                });
    }

    private void translateWithModelAvailable(Translator translator, String sourceText) {
        PleaseWaitDialog progressDialog = new PleaseWaitDialog(this);
        progressDialog.setEnterTransition(R.anim.fade_in);
        progressDialog.setExitTransition(R.anim.fade_out);
        progressDialog.setHasOptionsMenu(true);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(PleaseWaitDialog.ProgressStyle.LINEAR);
        progressDialog.setMessage("Translating Text...");
        progressDialog.show();


        Task<String> result = translator.translate(sourceText)
                .addOnSuccessListener(s -> {
                    progressDialog.dismiss();
                    translatedText = s;
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Translation failed: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Translation failed. If the model is already installed, text cannot be translated.", Toast.LENGTH_SHORT).show();
                });
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(selectedLocale);
                textToSpeech.getVoice();
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        isSpeaking = true;
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        isSpeaking = false;
                        speechProgress = 0;
                        runOnUiThread(() -> playAudio.setIcon(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.outline_play_circle_24)));
                    }

                    @Override
                    public void onError(String utteranceId) {}

                    @Override
                    public void onRangeStart(String utteranceId, int start, int end, int frame) {
                        speechProgress = start;
                    }
                });
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    /* Navbar Buttons */
    void toHome() {
        startActivity(new Intent(VoiceTranslateActivity.this, MainActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    void toText() {
        startActivity(new Intent(VoiceTranslateActivity.this, TextTranslateActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    void toImage() {
        startActivity(new Intent(VoiceTranslateActivity.this, ImageTranslateActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    void toDownload() {
        startActivity(new Intent(VoiceTranslateActivity.this,  DownloadLanguageTranslatePackages.class));
        overridePendingTransition(0, 0);
        finish();
    }
}