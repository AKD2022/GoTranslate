package com.example.translate.AlwaysAvailablePages;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.translate.ProgressDialog;
import com.example.translate.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.tashila.pleasewait.PleaseWaitDialog;

public class DownloadLanguageTranslatePackages extends AppCompatActivity {


    private FloatingActionButton goBack;
    private String translateToButton, translateFromButton; // translateTo, translateFrom define what language is chosen
    private String languageTranslateTo, languageTranslateFrom; // define what language the code is given (use this for translation)
    private MaterialButton selectTranslateFrom, selectTranslateTo, downloadSelectedLanguages;
    private MaterialButton imageNav, textNav, voiceNav, downloadNav, conversationNav;
    ProgressDialog progressDialogInstallation = new ProgressDialog();
    ProgressDialog progressDialogTranslation = new ProgressDialog();
    ProgressDialog progressDialogRecognition = new ProgressDialog();

    ChipNavigationBar navigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_language_translate_packages);

        selectTranslateFrom = findViewById(R.id.selectTranslateFrom);
        selectTranslateTo = findViewById(R.id.selectTranslateTo);
        downloadSelectedLanguages = findViewById(R.id.downloadTranslationPackageBtn);


        /* Navigation Bar */
        navigationBar = findViewById(R.id.menu);
        navigationBar.setItemSelected(R.id.downloadLanguages, true);

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



        /* End of Navigation Bar */

        selectTranslateTo.setOnClickListener(view -> {
            selectTranslateTo();
        });

        selectTranslateFrom.setOnClickListener(view -> {
            selectTranslateFrom();
        });

        downloadSelectedLanguages.setOnClickListener(view -> {
            if (translateToButton == null) {
                Toast.makeText(this, "Please select language to translate from", Toast.LENGTH_SHORT).show();
            } else if (translateFromButton == null ) {
                Toast.makeText(this, "Please select language to translate to", Toast.LENGTH_SHORT).show();
            } else if (translateToButton.equals(translateFromButton)) {
                Toast.makeText(this, "You cannot select the same language twice", Toast.LENGTH_SHORT).show();
            } else {
                getLanguageTo();
                getLanguageFrom();
                installLanguagePackage();
            }
        });


    }

    private void selectTranslateTo() {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), selectTranslateTo);

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.select_language, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // Toast message on menu item clicked
                selectTranslateTo.setText(menuItem.getTitle());
                translateToButton = menuItem.getTitle().toString();
                Toast.makeText(getApplicationContext(), "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        // Showing the popup menu
        popupMenu.show();
    }

    private void selectTranslateFrom() {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), selectTranslateFrom);

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.select_language, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // Toast message on menu item clicked
                selectTranslateFrom.setText(menuItem.getTitle());
                translateFromButton = menuItem.getTitle().toString();
                Toast.makeText(getApplicationContext(), "You Clicked " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
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

    private void installLanguagePackage() {
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
                        new MaterialAlertDialogBuilder(DownloadLanguageTranslatePackages.this)
                                .setTitle("Package is already installed")
                                .setPositiveButton("Ok", null)
                                .show();

                    } else {
                        progressDialogInstallation.getInstallationDialog(this);
                        downloadModel(translator, progressDialogInstallation.dismissInstallationDialog());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get downloaded models: " + e.getMessage());
                    Toast.makeText(DownloadLanguageTranslatePackages.this, "Failed to check installed models.", Toast.LENGTH_SHORT).show();
                });
    }

    private void downloadModel(Translator translator, PleaseWaitDialog progressDialog) {
        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Translation model download failed: " + e.getMessage());
                    new MaterialAlertDialogBuilder(DownloadLanguageTranslatePackages.this)
                            .setMessage("Translation Failed. Please check your internet connection or try again later.")
                            .setPositiveButton("OK", null)
                            .show();
                });
    }

    void toImage() {
        startActivity(new Intent(DownloadLanguageTranslatePackages.this, ImageTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toText() {
        startActivity(new Intent(DownloadLanguageTranslatePackages.this, TextTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toVoice() {
        startActivity(new Intent(DownloadLanguageTranslatePackages.this, VoiceTranslateActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toDownload() {
        startActivity(new Intent(DownloadLanguageTranslatePackages.this,  DownloadLanguageTranslatePackages.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    void toConversation() {
        startActivity(new Intent(DownloadLanguageTranslatePackages.this,  ConversationActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

}