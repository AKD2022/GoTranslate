package com.example.translate.AlwaysAvailablePages;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.translate.ProgressDialog;
import com.example.translate.R;
import com.example.translate.SharedViewModels.SharedViewModelForTextTranslateActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.tashila.pleasewait.PleaseWaitDialog;


public class TextTranslateActivity extends AppCompatActivity {
    private FloatingActionButton backButton;
    private Button translate;
    private MaterialButton selectTranslateFrom, selectTranslateTo, copyBtn;
    private String translateToButton, translateFromButton; // translateTo, translateFrom define what language is chosen
    private String languageTranslateTo, languageTranslateFrom; // defines the language code (use for translation)
    private EditText typeTranslateFrom;
    private TextView translateToText;
    String translatedText, typedText;
    private MaterialButton imageNav, textNav, voiceNav, downloadNav, conversationNav;

    private SharedViewModelForTextTranslateActivity sharedViewModel;

    ProgressDialog progressDialogInstallation = new ProgressDialog();
    ProgressDialog progressDialogTranslation = new ProgressDialog();
    ProgressDialog progressDialogRecognition = new ProgressDialog();

    ChipNavigationBar navigationBar;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_translate);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        selectTranslateFrom = findViewById(R.id.selectTranslateFrom);
        selectTranslateTo = findViewById(R.id.selectTranslateTo);
        typeTranslateFrom = findViewById(R.id.typeTranslateFrom);
        translateToText = findViewById(R.id.translateToText);
        translateToText.setMovementMethod(new ScrollingMovementMethod());
        //translate = findViewById(R.id.translate);
        copyBtn = findViewById(R.id.copyBtn);


        /* Navbar */
        /* Navigation Bar */
        navigationBar = findViewById(R.id.menu);
        navigationBar.setItemSelected(R.id.text, true);

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

        /* Bottom Navigation View */

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModelForTextTranslateActivity.class);

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

        copyBtn.setOnClickListener(view -> {
            copyBtn();
        });

        typeTranslateFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Trigger translation while typing
                if (!charSequence.toString().trim().isEmpty()) {
                    translateWhileTyping(charSequence.toString());
                } else {
                    translateToText.setText(""); // Clear the output when input is empty
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not used
            }
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
            case "العربية (Arabic)":
                languageTranslateTo = "ar";
                break;
            case "Български (Bulgarian)":
                languageTranslateTo = "bg";
                break;
            case "বাংলা (Bengali)":
                languageTranslateTo = "bn";
                break;
            case "Català (Catalan)":
                languageTranslateTo = "ca";
                break;
            case "Čeština (Czech)":
                languageTranslateTo = "cs";
                break;
            case "Cymraeg (Welsh)":
                languageTranslateTo = "cy";
                break;
            case "Dansk (Danish)":
                languageTranslateTo = "da";
                break;
            case "Deutsch (German)":
                languageTranslateTo = "de";
                break;
            case "Ελληνικά (Greek)":
                languageTranslateTo = "el";
                break;
            case "English (English)":
                languageTranslateTo = "en";
                break;
            case "Español (Spanish)":
                languageTranslateTo = "es";
                break;
            case "Eesti (Estonian)":
                languageTranslateTo = "et";
                break;
            case "Suomi (Finnish)":
                languageTranslateTo = "fi";
                break;
            case "Français (French)":
                languageTranslateTo = "fr";
                break;
            case "ગુજરાતી (Gujarati)":
                languageTranslateTo = "gu";
                break;
            case "עברית (Hebrew)":
                languageTranslateTo = "he";
                break;
            case "हिन्दी (Hindi)":
                languageTranslateTo = "hi";
                break;
            case "Hrvatski (Croatian)":
                languageTranslateTo = "hr";
                break;
            case "Magyar (Hungarian)":
                languageTranslateTo = "hu";
                break;
            case "Bahasa Indonesia (Indonesian)":
                languageTranslateTo = "id";
                break;
            case "Íslenska (Icelandic)":
                languageTranslateTo = "is";
                break;
            case "Italiano (Italian)":
                languageTranslateTo = "it";
                break;
            case "日本語 (Japanese)":
                languageTranslateTo = "ja";
                break;
            case "ಕನ್ನಡ (Kannada)":
                languageTranslateTo = "kn";
                break;
            case "한국어 (Korean)":
                languageTranslateTo = "ko";
                break;
            case "Lietuvių (Lithuanian)":
                languageTranslateTo = "lt";
                break;
            case "मराठी (Marathi)":
                languageTranslateTo = "mr";
                break;
            case "Bahasa Melayu (Malay)":
                languageTranslateTo = "ms";
                break;
            case "Nederlands (Dutch)":
                languageTranslateTo = "nl";
                break;
            case "Norsk (Norwegian)":
                languageTranslateTo = "no";
                break;
            case "Polski (Polish)":
                languageTranslateTo = "pl";
                break;
            case "Português (Portuguese)":
                languageTranslateTo = "pt";
                break;
            case "Română (Romanian)":
                languageTranslateTo = "ro";
                break;
            case "Русский (Russian)":
                languageTranslateTo = "ru";
                break;
            case "Slovenčina (Slovak)":
                languageTranslateTo = "sk";
                break;
            case "Shqip (Albanian)":
                languageTranslateTo = "sq";
                break;
            case "Svenska (Swedish)":
                languageTranslateTo = "sv";
                break;
            case "Kiswahili (Swahili)":
                languageTranslateTo = "sw";
                break;
            case "தமிழ் (Tamil)":
                languageTranslateTo = "ta";
                break;
            case "తెలుగు (Telugu)":
                languageTranslateTo = "te";
                break;
            case "ไทย (Thai)":
                languageTranslateTo = "th";
                break;
            case "Tagalog (Tagalog)":
                languageTranslateTo = "tl";
                break;
            case "Türkçe (Turkish)":
                languageTranslateTo = "tr";
                break;
            case "Українська (Ukrainian)":
                languageTranslateTo = "uk";
                break;
            case "اردو (Urdu)":
                languageTranslateTo = "ur";
                break;
            case "Tiếng Việt (Vietnamese)":
                languageTranslateTo = "vi";
                break;
            case "中文 (Chinese)":
                languageTranslateTo = "zh";
                break;
        }
    }

    public void getLanguageFrom() {
        switch (translateFromButton) {
            case "العربية (Arabic)":
                languageTranslateFrom = "ar";
                break;
            case "Български (Bulgarian)":
                languageTranslateFrom = "bg";
                break;
            case "বাংলা (Bengali)":
                languageTranslateFrom = "bn";
                break;
            case "Català (Catalan)":
                languageTranslateFrom = "ca";
                break;
            case "Čeština (Czech)":
                languageTranslateFrom = "cs";
                break;
            case "Cymraeg (Welsh)":
                languageTranslateFrom = "cy";
                break;
            case "Dansk (Danish)":
                languageTranslateFrom = "da";
                break;
            case "Deutsch (German)":
                languageTranslateFrom = "de";
                break;
            case "Ελληνικά (Greek)":
                languageTranslateFrom = "el";
                break;
            case "English (English)":
                languageTranslateFrom = "en";
                break;
            case "Español (Spanish)":
                languageTranslateFrom = "es";
                break;
            case "Eesti (Estonian)":
                languageTranslateFrom = "et";
                break;
            case "Suomi (Finnish)":
                languageTranslateFrom = "fi";
                break;
            case "Français (French)":
                languageTranslateFrom = "fr";
                break;
            case "ગુજરાતી (Gujarati)":
                languageTranslateFrom = "gu";
                break;
            case "עברית (Hebrew)":
                languageTranslateFrom = "he";
                break;
            case "हिन्दी (Hindi)":
                languageTranslateFrom = "hi";
                break;
            case "Hrvatski (Croatian)":
                languageTranslateFrom = "hr";
                break;
            case "Magyar (Hungarian)":
                languageTranslateFrom = "hu";
                break;
            case "Bahasa Indonesia (Indonesian)":
                languageTranslateFrom = "id";
                break;
            case "Íslenska (Icelandic)":
                languageTranslateFrom = "is";
                break;
            case "Italiano (Italian)":
                languageTranslateFrom = "it";
                break;
            case "日本語 (Japanese)":
                languageTranslateFrom = "ja";
                break;
            case "ಕನ್ನಡ (Kannada)":
                languageTranslateFrom = "kn";
                break;
            case "한국어 (Korean)":
                languageTranslateFrom = "ko";
                break;
            case "Lietuvių (Lithuanian)":
                languageTranslateFrom = "lt";
                break;
            case "मराठी (Marathi)":
                languageTranslateFrom = "mr";
                break;
            case "Bahasa Melayu (Malay)":
                languageTranslateFrom = "ms";
                break;
            case "Nederlands (Dutch)":
                languageTranslateFrom = "nl";
                break;
            case "Norsk (Norwegian)":
                languageTranslateFrom = "no";
                break;
            case "Polski (Polish)":
                languageTranslateFrom = "pl";
                break;
            case "Português (Portuguese)":
                languageTranslateFrom = "pt";
                break;
            case "Română (Romanian)":
                languageTranslateFrom = "ro";
                break;
            case "Русский (Russian)":
                languageTranslateFrom = "ru";
                break;
            case "Slovenčina (Slovak)":
                languageTranslateFrom = "sk";
                break;
            case "Shqip (Albanian)":
                languageTranslateFrom = "sq";
                break;
            case "Svenska (Swedish)":
                languageTranslateFrom = "sv";
                break;
            case "Kiswahili (Swahili)":
                languageTranslateFrom = "sw";
                break;
            case "தமிழ் (Tamil)":
                languageTranslateFrom = "ta";
                break;
            case "తెలుగు (Telugu)":
                languageTranslateFrom = "te";
                break;
            case "ไทย (Thai)":
                languageTranslateFrom = "th";
                break;
            case "Tagalog (Tagalog)":
                languageTranslateFrom = "tl";
                break;
            case "Türkçe (Turkish)":
                languageTranslateFrom = "tr";
                break;
            case "Українська (Ukrainian)":
                languageTranslateFrom = "uk";
                break;
            case "اردو (Urdu)":
                languageTranslateFrom = "ur";
                break;
            case "Tiếng Việt (Vietnamese)":
                languageTranslateFrom = "vi";
                break;
            case "中文 (Chinese)":
                languageTranslateFrom = "zh";
                break;
        }
    }


    private void translate(String textToTranslate) {
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
                        translateWithModelAvailable(translator, textToTranslate);
                    } else {
                        PleaseWaitDialog progressDialog = progressDialogInstallation.getInstallationDialog(this);
                        downloadAndTranslate(translator, textToTranslate, progressDialog);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get downloaded models: " + e.getMessage());
                    Toast.makeText(TextTranslateActivity.this, "Failed to check installed models.", Toast.LENGTH_SHORT).show();
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
                    new MaterialAlertDialogBuilder(TextTranslateActivity.this)
                            .setMessage("Translation Failed. Please check your internet connection or try again later.")
                            .setPositiveButton("OK", null)
                            .show();
                });
    }

    private void translateWithModelAvailable(Translator translator, String sourceText) {
        translator.translate(sourceText)
                .addOnSuccessListener(s -> {
                    translatedText = s;
                    replaceTextViewWithTranslatedText();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Translation failed: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Translation failed. If the model is already installed, text cannot be translated.", Toast.LENGTH_SHORT).show();
                });
    }


    private void translateWhileTyping(String textToTranslate) {
        if (translateToButton == null) {
            Toast.makeText(this, "Please select language to translate to", Toast.LENGTH_SHORT).show();
        } else if (translateFromButton == null) {
            Toast.makeText(this, "Please select language to translate from", Toast.LENGTH_SHORT).show();
        } else if (translateToButton.equals(translateFromButton)) {
            Toast.makeText(this, "You cannot select the same language twice", Toast.LENGTH_SHORT).show();
        } else {
            getLanguageTo();
            getLanguageFrom();
            translate(textToTranslate);
        }
    }

    private void replaceTextViewWithTranslatedText() {
        translateToText.setText(translatedText);
    }

    private void copyBtn() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", translatedText);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    /* Navbar Buttons */
    void toImage() {
        startActivity(new Intent(TextTranslateActivity.this, ImageTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toText() {
        startActivity(new Intent(TextTranslateActivity.this, TextTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toVoice() {
        startActivity(new Intent(TextTranslateActivity.this, VoiceTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toDownload() {
        startActivity(new Intent(TextTranslateActivity.this,  DownloadLanguageTranslatePackages.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toConversation() {
        startActivity(new Intent(TextTranslateActivity.this,  ConversationActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}