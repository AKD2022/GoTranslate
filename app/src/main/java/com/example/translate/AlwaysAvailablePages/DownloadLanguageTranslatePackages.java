package com.example.translate.AlwaysAvailablePages;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.translate.MainActivity;
import com.example.translate.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    private final int ID_IMAGE = 1;
    private final int ID_TEXT = 2;
    private final int ID_VOICE = 3;
    private final int ID_DOWNLOAD = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_language_translate_packages);

        selectTranslateFrom = findViewById(R.id.selectTranslateFrom);
        selectTranslateTo = findViewById(R.id.selectTranslateTo);
        downloadSelectedLanguages = findViewById(R.id.downloadTranslationPackageBtn);
        goBack = findViewById(R.id.goBackButton);

        goBack.setOnClickListener(view -> {
            toHome();
        });

        /* Bottom Navigation View */
        ChipNavigationBar navigationBar = findViewById(R.id.nav);
        navigationBar.setItemSelected(R.id.downloadLanguages, true);

        navigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.image) {
                    toImage();
                } else if (i == R.id.audio) {
                    toVoice();
                } else if (i == R.id.text) {
                    toText();
                }
            }
        });
        /* Bottom Navigation View */

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

    private void getLanguageTo() {
        switch (translateToButton) {
            case "Arabic":
                languageTranslateTo = "ar";
                break;
            case "Bulgarian":
                languageTranslateTo = "bg";
                break;
            case "Bengali":
                languageTranslateTo = "bn";
                break;
            case "Catalan":
                languageTranslateTo = "ca";
                break;
            case "Czech":
                languageTranslateTo = "cs";
                break;
            case "Welsh":
                languageTranslateTo = "cy";
                break;
            case "Danish":
                languageTranslateTo = "da";
                break;
            case "German":
                languageTranslateTo = "de";
                break;
            case "Greek":
                languageTranslateTo = "el";
                break;
            case "English":
                languageTranslateTo = "en";
                break;
            case "Spanish":
                languageTranslateTo = "es";
                break;
            case "Estonian":
                languageTranslateTo = "et";
                break;
            case "Finnish":
                languageTranslateTo = "fi";
                break;
            case "French":
                languageTranslateTo = "fr";
                break;
            case "Gujarati":
                languageTranslateTo = "gu";
                break;
            case "Hebrew":
                languageTranslateTo = "he";
                break;
            case "Hindi":
                languageTranslateTo = "hi";
                break;
            case "Croatian":
                languageTranslateTo = "hr";
                break;
            case "Hungarian":
                languageTranslateTo = "hu";
                break;
            case "Indonesian":
                languageTranslateTo = "id";
                break;
            case "Icelandic":
                languageTranslateTo = "is";
                break;
            case "Italian":
                languageTranslateTo = "it";
                break;
            case "Japanese":
                languageTranslateTo = "ja";
                break;
            case "Kannada":
                languageTranslateTo = "kn";
                break;
            case "Korean":
                languageTranslateTo = "ko";
                break;
            case "Lithuanian":
                languageTranslateTo = "lt";
                break;
            case "Marathi":
                languageTranslateTo = "mr";
                break;
            case "Malay":
                languageTranslateTo = "ms";
                break;
            case "Dutch":
                languageTranslateTo = "nl";
                break;
            case "Norwegian":
                languageTranslateTo = "no";
                break;
            case "Poland":
                languageTranslateTo = "pl";
                break;
            case "Portuguese":
                languageTranslateTo = "pt";
                break;
            case "Romanian":
                languageTranslateTo = "ro";
                break;
            case "Russian":
                languageTranslateTo = "ru";
                break;
            case "Slovak":
                languageTranslateTo = "sk";
                break;
            case "Albanian":
                languageTranslateTo = "sq";
                break;
            case "Swedish":
                languageTranslateTo = "sv";
                break;
            case "Swahili":
                languageTranslateTo = "sw";
                break;
            case "Tamil":
                languageTranslateTo = "ta";
                break;
            case "Telugu":
                languageTranslateTo = "te";
                break;
            case "Thai":
                languageTranslateTo = "th";
                break;
            case "Tagalog":
                languageTranslateTo = "tl";
                break;
            case "Turkish":
                languageTranslateTo = "tr";
                break;
            case "Ukrainian":
                languageTranslateTo = "uk";
                break;
            case "Urdu":
                languageTranslateTo = "ur";
                break;
            case "Vietnamese":
                languageTranslateTo = "vi";
                break;
            case "Chinese":
                languageTranslateTo = "zh";
                break;
        }
    }

    private void getLanguageFrom() {
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

    public void installLanguagePackage () {
        PleaseWaitDialog progressDialog = new PleaseWaitDialog(this);
        progressDialog.setTitle("Installation \n");
        progressDialog.setEnterTransition(R.anim.fade_in);
        progressDialog.setExitTransition(R.anim.fade_out);
        progressDialog.setHasOptionsMenu(true);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(PleaseWaitDialog.ProgressStyle.LINEAR);
        progressDialog.setMessage("Please wait while the " + translateFromButton.toString() +
                " to " + translateToButton.toString() + " language package is being installed");
        progressDialog.show();

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setTargetLanguage(languageTranslateTo)
                .setSourceLanguage(languageTranslateFrom)
                .build();
        Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Successful Installation")
                            .setMessage(translateFromButton.toString() + " to " + translateToButton.toString() +
                                    " translation model has been successfully installed. Translation will now" +
                                    " occur faster")
                            .setPositiveButton("Great, thanks!", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Translation model download failed: " + e.getMessage());
                    new MaterialAlertDialogBuilder(this)
                            .setMessage("Translation Failed. There may be a problem with installing the translation model.")
                            .setPositiveButton("OK", null)
                            .show();
                });
    }

    void toHome() {
        startActivity(new Intent(DownloadLanguageTranslatePackages.this, MainActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    void toText() {
        startActivity(new Intent(DownloadLanguageTranslatePackages.this, TextTranslateActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    void toVoice() {
        startActivity(new Intent(DownloadLanguageTranslatePackages.this, VoiceTranslateActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    void toImage() {
        startActivity(new Intent(DownloadLanguageTranslatePackages.this,  ImageTranslateActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

}