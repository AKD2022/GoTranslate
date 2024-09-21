package com.example.translate.AlwaysAvailablePages;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.translate.ProgressDialog;
import com.example.translate.R;
import com.example.translate.SharedViewModels.SharedViewModelForConversationActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.tashila.pleasewait.PleaseWaitDialog;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;



public class ConversationActivity extends AppCompatActivity {

    private Locale selectedLocaleFrom, selectedLocaleTo;
    private MaterialButton imageNav, textNav, voiceNav, downloadNav, conversationNav;
    private MaterialButton selectTranslateTo, selectTranslateFrom;
    private String translateToButton, translateFromButton; // translateTo, translateFrom define what language is chosen
    private String languageTranslateTo, languageTranslateFrom; // defines the language code (use for translation)
    private TextView person1TextView, person2TextView;
    private MaterialButton micPerson1, micPerson2;
    private String recordedText1, recordedText2, translatedText1, translatedText2, recordedText;
    private SharedViewModelForConversationActivity sharedViewModel;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    int track = 0;
    TextToSpeech textToSpeech;
    private MaterialTextView speakingDialog;

    ProgressDialog progressDialogInstallation = new ProgressDialog();
    ProgressDialog progressDialogTranslation = new ProgressDialog();
    ProgressDialog progressDialogRecognition = new ProgressDialog();

    ChipNavigationBar navigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        selectTranslateFrom = findViewById(R.id.selectTranslateFrom);
        selectTranslateTo = findViewById(R.id.selectTranslateTo);
        person1TextView = findViewById(R.id.person1Text);
        person2TextView = findViewById(R.id.person2Text);
        micPerson1 = findViewById(R.id.micPerson1);
        micPerson2 = findViewById(R.id.micPerson2);
        speakingDialog = findViewById(R.id.speakingDialog);

        /* Navigation Bar */
        navigationBar = findViewById(R.id.menu);
        navigationBar.setItemSelected(R.id.conversation, true);

        navigationBar.setOnItemSelectedListener(i -> {
            if (i == R.id.image) {
                toImage();
            } else if (i == R.id.text) {
                toText();
            } else if (i == R.id.audio){
                toVoice();
            } else if (i == R.id.conversation) {
                toConversation();
            } else if (i == R.id.downloadLanguages) {
                toDownload();
            }
        });



        /* Shared View Model */
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModelForConversationActivity.class);
        sharedViewModel.getSelectedTranslateToLanguage().observe(this, selectedLanguageTranslateTo -> {
            if (selectedLanguageTranslateTo != null) {
                selectTranslateTo.setText(selectedLanguageTranslateTo);
                translateToButton = selectedLanguageTranslateTo; // update translateToButton
                getLanguageTo();
            }

            assert selectedLanguageTranslateTo != null;
            if (selectedLanguageTranslateTo.isBlank()) {
                selectTranslateTo.setText("Select Language");
            }
        });
        sharedViewModel.getSelectedTranslateFromLanguage().observe(this, selectedLanguageTranslateFrom -> {
            if (selectedLanguageTranslateFrom != null) {
                selectTranslateFrom.setText(selectedLanguageTranslateFrom);
                translateFromButton = selectedLanguageTranslateFrom;
                getLanguageFrom();
            }

            assert selectedLanguageTranslateFrom != null;
            if (selectedLanguageTranslateFrom.isBlank()) {
                selectTranslateTo.setText("Select Language");
            }
        });

        micPerson1.setOnClickListener(v -> {
            track = 1;
            checkRecordingPermissionAndRecord();
            textToSpeechPerson2();
        });

        micPerson2.setOnClickListener(v -> {
            track = 2;
            checkRecordingPermissionAndRecord();
            textToSpeechPerson1();
        });

        selectTranslateTo.setOnClickListener(view -> {
            selectTranslateTo();
        });

        selectTranslateFrom.setOnClickListener(view -> {
            selectTranslateFrom();
        });
    }

    public void selectTranslateTo() {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), selectTranslateTo);

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.select_language, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String selectedLanguageTranslateTo = menuItem.getTitle().toString();
            selectTranslateTo.setText(selectedLanguageTranslateTo);
            sharedViewModel.setSelectedTranslateToLanguage(selectedLanguageTranslateTo);
            getLanguageTo();
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
            getLanguageFrom();
            Toast.makeText(getApplicationContext(), "You Clicked " + selectedLanguageTranslateFrom, Toast.LENGTH_SHORT).show();
            return true;
        });
        // Showing the popup menu
        popupMenu.show();
    }

    public void getLanguageTo() {
        switch (translateToButton) {
            case "العربية (Arabic)":
                languageTranslateTo = "ar";
                selectedLocaleTo = new Locale("ar");
                break;
            case "Български (Bulgarian)":
                languageTranslateTo = "bg";
                selectedLocaleTo = new Locale("bg");
                break;
            case "বাংলা (Bengali)":
                languageTranslateTo = "bn";
                selectedLocaleTo = new Locale("bn");
                break;
            case "Català (Catalan)":
                languageTranslateTo = "ca";
                selectedLocaleTo = new Locale("ca");
                break;
            case "Čeština (Czech)":
                languageTranslateTo = "cs";
                selectedLocaleTo = new Locale("cs");
                break;
            case "Cymraeg (Welsh)":
                languageTranslateTo = "cy";
                selectedLocaleTo = new Locale("cy");
                break;
            case "Dansk (Danish)":
                languageTranslateTo = "da";
                selectedLocaleTo = new Locale("da");
                break;
            case "Deutsch (German)":
                languageTranslateTo = "de";
                selectedLocaleTo = new Locale("de");
                break;
            case "Ελληνικά (Greek)":
                languageTranslateTo = "el";
                selectedLocaleTo = new Locale("el");
                break;
            case "English (English)":
                languageTranslateTo = "en";
                selectedLocaleTo = new Locale("en");
                break;
            case "Español (Spanish)":
                languageTranslateTo = "es";
                selectedLocaleTo = new Locale("es");
                break;
            case "Eesti (Estonian)":
                languageTranslateTo = "et";
                selectedLocaleTo = new Locale("et");
                break;
            case "Suomi (Finnish)":
                languageTranslateTo = "fi";
                selectedLocaleTo = new Locale("fi");
                break;
            case "Français (French)":
                languageTranslateTo = "fr";
                selectedLocaleTo = new Locale("fr");
                break;
            case "ગુજરાતી (Gujarati)":
                languageTranslateTo = "gu";
                selectedLocaleTo = new Locale("gu");
                break;
            case "עברית (Hebrew)":
                languageTranslateTo = "he";
                selectedLocaleTo = new Locale("he");
                break;
            case "हिन्दी (Hindi)":
                languageTranslateTo = "hi";
                selectedLocaleTo = new Locale("hi");
                break;
            case "Hrvatski (Croatian)":
                languageTranslateTo = "hr";
                selectedLocaleTo = new Locale("hr");
                break;
            case "Magyar (Hungarian)":
                languageTranslateTo = "hu";
                selectedLocaleTo = new Locale("hu");
                break;
            case "Bahasa Indonesia (Indonesian)":
                languageTranslateTo = "id";
                selectedLocaleTo = new Locale("id");
                break;
            case "Íslenska (Icelandic)":
                languageTranslateTo = "is";
                selectedLocaleTo = new Locale("is");
                break;
            case "Italiano (Italian)":
                languageTranslateTo = "it";
                selectedLocaleTo = new Locale("it");
                break;
            case "日本語 (Japanese)":
                languageTranslateTo = "ja";
                selectedLocaleTo = new Locale("ja");
                break;
            case "ಕನ್ನಡ (Kannada)":
                languageTranslateTo = "kn";
                selectedLocaleTo = new Locale("kn");
                break;
            case "한국어 (Korean)":
                languageTranslateTo = "ko";
                selectedLocaleTo = new Locale("ko");
                break;
            case "Lietuvių (Lithuanian)":
                languageTranslateTo = "lt";
                selectedLocaleTo = new Locale("lt");
                break;
            case "मराठी (Marathi)":
                languageTranslateTo = "mr";
                selectedLocaleTo = new Locale("mr");
                break;
            case "Bahasa Melayu (Malay)":
                languageTranslateTo = "ms";
                selectedLocaleTo = new Locale("ms");
                break;
            case "Nederlands (Dutch)":
                languageTranslateTo = "nl";
                selectedLocaleTo = new Locale("nl");
                break;
            case "Norsk (Norwegian)":
                languageTranslateTo = "no";
                selectedLocaleTo = new Locale("no");
                break;
            case "Polski (Polish)":
                languageTranslateTo = "pl";
                selectedLocaleTo = new Locale("pl");
                break;
            case "Português (Portuguese)":
                languageTranslateTo = "pt";
                selectedLocaleTo = new Locale("pt");
                break;
            case "Română (Romanian)":
                languageTranslateTo = "ro";
                selectedLocaleTo = new Locale("ro");
                break;
            case "Русский (Russian)":
                languageTranslateTo = "ru";
                selectedLocaleTo = new Locale("ru");
                break;
            case "Slovenčina (Slovak)":
                languageTranslateTo = "sk";
                selectedLocaleTo = new Locale("sk");
                break;
            case "Shqip (Albanian)":
                languageTranslateTo = "sq";
                selectedLocaleTo = new Locale("sq");
                break;
            case "Svenska (Swedish)":
                languageTranslateTo = "sv";
                selectedLocaleTo = new Locale("sv");
                break;
            case "Kiswahili (Swahili)":
                languageTranslateTo = "sw";
                selectedLocaleTo = new Locale("sw");
                break;
            case "தமிழ் (Tamil)":
                languageTranslateTo = "ta";
                selectedLocaleTo = new Locale("ta");
                break;
            case "తెలుగు (Telugu)":
                languageTranslateTo = "te";
                selectedLocaleTo = new Locale("te");
                break;
            case "ไทย (Thai)":
                languageTranslateTo = "th";
                selectedLocaleTo = new Locale("th");
                break;
            case "Tagalog (Tagalog)":
                languageTranslateTo = "tl";
                selectedLocaleTo = new Locale("tl");
                break;
            case "Türkçe (Turkish)":
                languageTranslateTo = "tr";
                selectedLocaleTo = new Locale("tr");
                break;
            case "Українська (Ukrainian)":
                languageTranslateTo = "uk";
                selectedLocaleTo = new Locale("uk");
                break;
            case "اردو (Urdu)":
                languageTranslateTo = "ur";
                selectedLocaleTo = new Locale("ur");
                break;
            case "Tiếng Việt (Vietnamese)":
                languageTranslateTo = "vi";
                selectedLocaleTo = new Locale("vi");
                break;
            case "中文 (Chinese)":
                languageTranslateTo = "zh";
                selectedLocaleTo = new Locale("zh");
                break;
        }
    }


    public void getLanguageFrom() {
        switch (translateFromButton) {
            case "العربية (Arabic)":
                languageTranslateFrom = "ar";
                selectedLocaleFrom = new Locale("ar");
                break;
            case "Български (Bulgarian)":
                languageTranslateFrom = "bg";
                selectedLocaleFrom = new Locale("bg");
                break;
            case "বাংলা (Bengali)":
                languageTranslateFrom = "bn";
                selectedLocaleFrom = new Locale("bn");
                break;
            case "Català (Catalan)":
                languageTranslateFrom = "ca";
                selectedLocaleFrom = new Locale("ca");
                break;
            case "Čeština (Czech)":
                languageTranslateFrom = "cs";
                selectedLocaleFrom = new Locale("cs");
                break;
            case "Cymraeg (Welsh)":
                languageTranslateFrom = "cy";
                selectedLocaleFrom = new Locale("cy");
                break;
            case "Dansk (Danish)":
                languageTranslateFrom = "da";
                selectedLocaleFrom = new Locale("da");
                break;
            case "Deutsch (German)":
                languageTranslateFrom = "de";
                selectedLocaleFrom = new Locale("de");
                break;
            case "Ελληνικά (Greek)":
                languageTranslateFrom = "el";
                selectedLocaleFrom = new Locale("el");
                break;
            case "English (English)":
                languageTranslateFrom = "en";
                selectedLocaleFrom = new Locale("en");
                break;
            case "Español (Spanish)":
                languageTranslateFrom = "es";
                selectedLocaleFrom = new Locale("es");
                break;
            case "Eesti (Estonian)":
                languageTranslateFrom = "et";
                selectedLocaleFrom = new Locale("et");
                break;
            case "Suomi (Finnish)":
                languageTranslateFrom = "fi";
                selectedLocaleFrom = new Locale("fi");
                break;
            case "Français (French)":
                languageTranslateFrom = "fr";
                selectedLocaleFrom = new Locale("fr");
                break;
            case "ગુજરાતી (Gujarati)":
                languageTranslateFrom = "gu";
                selectedLocaleFrom = new Locale("gu");
                break;
            case "עברית (Hebrew)":
                languageTranslateFrom = "he";
                selectedLocaleFrom = new Locale("he");
                break;
            case "हिन्दी (Hindi)":
                languageTranslateFrom = "hi";
                selectedLocaleFrom = new Locale("hi");
                break;
            case "Hrvatski (Croatian)":
                languageTranslateFrom = "hr";
                selectedLocaleFrom = new Locale("hr");
                break;
            case "Magyar (Hungarian)":
                languageTranslateFrom = "hu";
                selectedLocaleFrom = new Locale("hu");
                break;
            case "Bahasa Indonesia (Indonesian)":
                languageTranslateFrom = "id";
                selectedLocaleFrom = new Locale("id");
                break;
            case "Íslenska (Icelandic)":
                languageTranslateFrom = "is";
                selectedLocaleFrom = new Locale("is");
                break;
            case "Italiano (Italian)":
                languageTranslateFrom = "it";
                selectedLocaleFrom = new Locale("it");
                break;
            case "日本語 (Japanese)":
                languageTranslateFrom = "ja";
                selectedLocaleFrom = new Locale("ja");
                break;
            case "ಕನ್ನಡ (Kannada)":
                languageTranslateFrom = "kn";
                selectedLocaleFrom = new Locale("kn");
                break;
            case "한국어 (Korean)":
                languageTranslateFrom = "ko";
                selectedLocaleFrom = new Locale("ko");
                break;
            case "Lietuvių (Lithuanian)":
                languageTranslateFrom = "lt";
                selectedLocaleFrom = new Locale("lt");
                break;
            case "मराठी (Marathi)":
                languageTranslateFrom = "mr";
                selectedLocaleFrom = new Locale("mr");
                break;
            case "Bahasa Melayu (Malay)":
                languageTranslateFrom = "ms";
                selectedLocaleFrom = new Locale("ms");
                break;
            case "Nederlands (Dutch)":
                languageTranslateFrom = "nl";
                selectedLocaleFrom = new Locale("nl");
                break;
            case "Norsk (Norwegian)":
                languageTranslateFrom = "no";
                selectedLocaleFrom = new Locale("no");
                break;
            case "Polski (Polish)":
                languageTranslateFrom = "pl";
                selectedLocaleFrom = new Locale("pl");
                break;
            case "Português (Portuguese)":
                languageTranslateFrom = "pt";
                selectedLocaleFrom = new Locale("pt");
                break;
            case "Română (Romanian)":
                languageTranslateFrom = "ro";
                selectedLocaleFrom = new Locale("ro");
                break;
            case "Русский (Russian)":
                languageTranslateFrom = "ru";
                selectedLocaleFrom = new Locale("ru");
                break;
            case "Slovenčina (Slovak)":
                languageTranslateFrom = "sk";
                selectedLocaleFrom = new Locale("sk");
                break;
            case "Shqip (Albanian)":
                languageTranslateFrom = "sq";
                selectedLocaleFrom = new Locale("sq");
                break;
            case "Svenska (Swedish)":
                languageTranslateFrom = "sv";
                selectedLocaleFrom = new Locale("sv");
                break;
            case "Kiswahili (Swahili)":
                languageTranslateFrom = "sw";
                selectedLocaleFrom = new Locale("sw");
                break;
            case "தமிழ் (Tamil)":
                languageTranslateFrom = "ta";
                selectedLocaleFrom = new Locale("ta");
                break;
            case "తెలుగు (Telugu)":
                languageTranslateFrom = "te";
                selectedLocaleFrom = new Locale("te");
                break;
            case "ไทย (Thai)":
                languageTranslateFrom = "th";
                selectedLocaleFrom = new Locale("th");
                break;
            case "Tagalog (Tagalog)":
                languageTranslateFrom = "tl";
                selectedLocaleFrom = new Locale("tl");
                break;
            case "Türkçe (Turkish)":
                languageTranslateFrom = "tr";
                selectedLocaleFrom = new Locale("tr");
                break;
            case "Українська (Ukrainian)":
                languageTranslateFrom = "uk";
                selectedLocaleFrom = new Locale("uk");
                break;
            case "اردو (Urdu)":
                languageTranslateFrom = "ur";
                selectedLocaleFrom = new Locale("ur");
                break;
            case "Tiếng Việt (Vietnamese)":
                languageTranslateFrom = "vi";
                selectedLocaleFrom = new Locale("vi");
                break;
            case "中文 (Chinese)":
                languageTranslateFrom = "zh";
                selectedLocaleFrom = new Locale("zh");
                break;
        }
    }


    private void checkRecordingPermissionAndRecord() {
        if (ActivityCompat.checkSelfPermission(ConversationActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ConversationActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_SPEECH_INPUT);
        } else {
            if (track == 1) {
                record(languageTranslateFrom);
            } if (track == 2) {
                record(languageTranslateTo);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (track == 1) {
                    record(languageTranslateFrom);
                } if (track == 2) {
                    record(languageTranslateTo);
                }
            } else {
                Toast.makeText(this, "Audio Recording Denied. Please Allow to record audio for full functionality.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSpeakingDialog() {
        speakingDialog.setVisibility(View.INVISIBLE);
    }


    private void record(String languageTranslation) {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    if (track == 1) {
                        speakingDialog.setVisibility(View.VISIBLE);
                    } if (track == 2) {
                        speakingDialog.setRotationX(180);
                        speakingDialog.setRotationY(180);
                        speakingDialog.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onBeginningOfSpeech() {
                    if (track == 1) {
                        speakingDialog.setVisibility(View.INVISIBLE);
                    } if (track == 2) {
                        speakingDialog.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Sound level changed
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    // Audio buffer received
                }

                @Override
                public void onEndOfSpeech() {
                    // User stopped speaking
                }

                @Override
                public void onError(int error) {
                    Toast.makeText(ConversationActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResults(Bundle results) {
                    // Access recognized text here
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);

                        if (track == 1) {
                            recordedText1 = recognizedText;
                            person1TextView.setText(recordedText1);
                            translate(languageTranslateFrom, languageTranslateTo);
                        } if (track == 2) {
                            recordedText2 = recognizedText;
                            person2TextView.setText(recordedText2);
                            translate(languageTranslateTo, languageTranslateFrom);
                        }
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Handle partial recognition results
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    // Reserved for future events
                }
            });

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTranslation);
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, languageTranslation);

            speechRecognizer.startListening(intent);
        } else {
            Toast.makeText(ConversationActivity.this, "Speech recognition is not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void translate(String languageTranslateFrom, String languageTranslateTo) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setTargetLanguage(languageTranslateTo)
                .setSourceLanguage(languageTranslateFrom)
                .build();
        Translator translator = Translation.getClient(options);

        RemoteModelManager modelManager = RemoteModelManager.getInstance();
        TranslateRemoteModel model = new TranslateRemoteModel.Builder(languageTranslateTo).build();

        modelManager.getDownloadedModels(TranslateRemoteModel.class)
                .addOnSuccessListener(models -> {
                    boolean isModelInstalled = false;
                    for (TranslateRemoteModel downloadedModel : models) {
                        if (downloadedModel.getLanguage().equals(languageTranslateTo)) {
                            isModelInstalled = true;
                            break;
                        }
                    }

                    if (isModelInstalled) {
                        if (track == 1) {
                            recordedText = recordedText1; // sets recorded text based on person
                        } if (track == 2) {
                            recordedText = recordedText2;
                        }
                        translateWithModelAvailable(translator, recordedText);
                    } else {
                        PleaseWaitDialog progressDialog = progressDialogInstallation.getInstallationDialog(this);
                        downloadAndTranslate(translator, recordedText, progressDialog);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get downloaded models: " + e.getMessage());
                    Toast.makeText(ConversationActivity.this, "Failed to check installed models.", Toast.LENGTH_SHORT).show();
                });
    }

    private void downloadAndTranslate(Translator translator, String textToTranslate, PleaseWaitDialog progressDialog) {
        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    translateWithModelAvailable(translator, textToTranslate);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Translation model download failed: " + e.getMessage());
                    new MaterialAlertDialogBuilder(ConversationActivity.this)
                            .setMessage("Translation Failed. Please check your internet connection or try again later.")
                            .setPositiveButton("OK", null)
                            .show();
                });
    }

    private void translateWithModelAvailable(Translator translator, String sourceText) {
        progressDialogTranslation.getTranslationDialog(this);
        Task<String> result = translator.translate(sourceText)
                .addOnSuccessListener(s -> {
                    progressDialogTranslation.dismissTranslateDialog();
                    if (track == 1) {
                        translatedText2 = s;
                        person2TextView.setText(translatedText2);
                        textToSpeech.speak(translatedText2, TextToSpeech.QUEUE_FLUSH, null, null);
                    }

                    if (track == 2) {
                        translatedText1 = s;
                        person1TextView.setText(translatedText1);
                        textToSpeech.speak(translatedText1, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialogTranslation.dismissTranslateDialog();
                    Log.e(TAG, "Translation failed: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Translation failed. If the model is already installed, text cannot be translated.", Toast.LENGTH_SHORT).show();
                });
    }


    private void textToSpeechPerson2() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(selectedLocaleTo);
                textToSpeech.getVoice();
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });
    }

    private void textToSpeechPerson1() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(selectedLocaleFrom);
                textToSpeech.getVoice();
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
    void toImage() {
        startActivity(new Intent(ConversationActivity.this, ImageTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toText() {
        startActivity(new Intent(ConversationActivity.this, TextTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toVoice() {
        startActivity(new Intent(ConversationActivity.this, VoiceTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toDownload() {
        startActivity(new Intent(ConversationActivity.this,  DownloadLanguageTranslatePackages.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toConversation() {
        startActivity(new Intent(ConversationActivity.this, ConversationActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}