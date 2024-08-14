package com.example.translate.AlwaysAvailablePages;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.translate.MainActivity;
import com.example.translate.R;
import com.example.translate.SharedViewModels.SharedViewModelForImageTranslateActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.tashila.pleasewait.PleaseWaitDialog;

import java.io.File;


public class ImageTranslateActivity extends AppCompatActivity {

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri imageUri;
    private ImageView imageView;
    private FloatingActionButton backButton;
    private static final int CAMERA_PERMISSION_CODE = 1;
    private MaterialButton takePicture, selectImage, translate;
    private String recognizedText, translateToButton, translateFromButton; // translateTo, translateFrom define what language is chosen
    private String languageTranslateTo, languageTranslateFrom; // define what language the code is given (use this for translation)
    private MaterialButton selectTranslateFrom, selectTranslateTo;
    String translatedText;


    /* Shared View Model */
    private SharedViewModelForImageTranslateActivity sharedViewModelForImageTranslateActivity;

    // When using Latin script library
    TextRecognizer defaultRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    // When using Chinese script library
    TextRecognizer chineseRecognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
    // When using Devanagari script library
    TextRecognizer devanagariRecognizer = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
    // When using Japanese script library
    TextRecognizer japaneseRecognizer = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
    // When using Korean script library
    TextRecognizer koreanRecognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_translate);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        backButton = findViewById(R.id.goBackButton);
        imageView = findViewById(R.id.imageView);
        takePicture = findViewById(R.id.takePicture);
        selectImage = findViewById(R.id.openPhotos);
        translate = findViewById(R.id.translate);
        selectTranslateFrom = findViewById(R.id.selectTranslateFrom);
        selectTranslateTo = findViewById(R.id.selectTranslateTo);

        backButton.setOnClickListener(view -> {
            toHome();
        });

        /* Navigation Bar */

        ChipNavigationBar navigationBar = findViewById(R.id.nav);
        navigationBar.setItemSelected(R.id.image, true);

        navigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == R.id.text) {
                    toText();
                } else if (i == R.id.audio) {
                    toVoice();
                } else if (i == R.id.downloadLanguages) {
                    toDownload();
                }
            }
        });


        /* End of Navigation Bar */

        /* Camera */
        imageUri = createUri();
        registerPictureLauncher();

        takePicture.setOnClickListener(view -> {
            checkCameraPermissionAndOpenCamera();
        });

        selectImage.setOnClickListener(view -> {
            openFolderWithImages();
        });

        translate.setOnClickListener(view ->{
            translateToButton = selectTranslateTo.getText().toString();
            translateFromButton = selectTranslateFrom.getText().toString();

            if (translateToButton == null || translateToButton.isEmpty()) {
                Toast.makeText(this, "Please select language to translate to", Toast.LENGTH_SHORT).show();
            } else if (translateFromButton == null || translateFromButton.isEmpty()) {
                Toast.makeText(this, "Please select language to translate from", Toast.LENGTH_SHORT).show();
            } else if (translateToButton.equals(translateFromButton)) {
                Toast.makeText(this, "You cannot select the same language twice", Toast.LENGTH_SHORT).show();
            } else {
                getLanguageTo();
                getLanguageFrom();
                startRecognitionAndTranslation();
            }
        });

        sharedViewModelForImageTranslateActivity = new ViewModelProvider(this).get(SharedViewModelForImageTranslateActivity.class);

        sharedViewModelForImageTranslateActivity.getSelectedTranslateToLanguage().observe(this, selectedLanguageTranslateTo -> {
            if (selectedLanguageTranslateTo != null) {
                selectTranslateTo.setText(selectedLanguageTranslateTo);
                translateToButton = selectedLanguageTranslateTo; // update translateToButton
            }

            assert selectedLanguageTranslateTo != null;
            if (selectedLanguageTranslateTo.isBlank()) {
                selectTranslateTo.setText("Select Language");
            }
        });

        sharedViewModelForImageTranslateActivity.getSelectedTranslateFromLanguage().observe(this, selectedLanguageTranslateFrom -> {
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
    }



    private Uri createUri() {
        File imageFile = new File(getApplicationContext().getFilesDir(), "translate_photo.jpg");
        return FileProvider.getUriForFile(
            getApplicationContext(),
            "com.example.translate.fileProvider",
            imageFile
        );
    }

    private void registerPictureLauncher() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean o) {
                        try {
                            if (o) {
                                imageView.setImageURI(null);
                                imageView.setImageURI(imageUri);
                            }
                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                }
        );
    }

    private void openFolderWithImages() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ActivityCompat.checkSelfPermission(ImageTranslateActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ImageTranslateActivity.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            takePictureLauncher.launch(imageUri);
        }
    }


    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri " + imageUri);
                        imageView.setImageURI(imageUri);
                        imageView.setBackground(null);

                    } else {
                        Toast.makeText(getApplicationContext(), "Cancelled....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(imageUri);
            } else {
                Toast.makeText(this, "Camera Permission Denied, please allow Permission to take camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* Getting Text From Image */
    public void selectTranslateTo() {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), selectTranslateTo);

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.select_language, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String selectedLanguageTranslateTo = menuItem.getTitle().toString();
            selectTranslateTo.setText(selectedLanguageTranslateTo);
            sharedViewModelForImageTranslateActivity.setSelectedTranslateToLanguage(selectedLanguageTranslateTo);
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
            sharedViewModelForImageTranslateActivity.setSelectedTranslateFromLanguage(selectedLanguageTranslateFrom);
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
                    translateWithModelAvailable(translator, recognizedText);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Translation model download failed: " + e.getMessage());
                    new MaterialAlertDialogBuilder(ImageTranslateActivity.this)
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
                    new MaterialAlertDialogBuilder(ImageTranslateActivity.this)
                            .setMessage(s)
                            .setPositiveButton("Copy To Clipboard", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    copyBtn();
                                }
                            })
                            .setNegativeButton("Close", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Translation failed: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Translation failed. If the model is already installed, text cannot be translated.", Toast.LENGTH_SHORT).show();
                });
    }

    private void startRecognitionAndTranslation() {
        PleaseWaitDialog progressDialog = new PleaseWaitDialog(this);
        progressDialog.setEnterTransition(R.anim.fade_in);
        progressDialog.setExitTransition(R.anim.fade_out);
        progressDialog.setHasOptionsMenu(true);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(PleaseWaitDialog.ProgressStyle.LINEAR);
        progressDialog.setMessage("Recognizing Text...");
        progressDialog.show();

        try {
            InputImage inputImage = InputImage.fromFilePath(getApplicationContext(), imageUri);

            defaultRecognizer.process(inputImage)
                    .addOnSuccessListener(text -> {
                        recognizedText = text.getText();

                        if (recognizedText.isEmpty()) {
                            recognizeWithOtherRecognizers(progressDialog);
                        } else {
                            progressDialog.dismiss();
                            translate();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Log.e(TAG, "Text recognition failed", e);
                        Toast.makeText(getApplicationContext(), "Could not get text from image", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Failed to create InputImage", e);
            Toast.makeText(getApplicationContext(), "Could not get text from image", Toast.LENGTH_SHORT).show();
        }
    }

    private void recognizeWithOtherRecognizers(PleaseWaitDialog progressDialog) {
        try {
            InputImage inputImage = InputImage.fromFilePath(getApplicationContext(), imageUri);

            chineseRecognizer.process(inputImage)
                    .addOnSuccessListener(text -> {
                        recognizedText = text.getText();

                        if (recognizedText.isEmpty()) {
                            japaneseRecognizer.process(inputImage)
                                    .addOnSuccessListener(textJapanese -> {
                                        recognizedText = textJapanese.getText();

                                        if (recognizedText.isEmpty()) {
                                            koreanRecognizer.process(inputImage)
                                                    .addOnSuccessListener(textKorean -> {
                                                        recognizedText = textKorean.getText();

                                                        if (recognizedText.isEmpty()) {
                                                            devanagariRecognizer.process(inputImage)
                                                                    .addOnSuccessListener(textDevanagari -> {
                                                                        recognizedText = textDevanagari.getText();

                                                                        progressDialog.dismiss();
                                                                        if (recognizedText.isEmpty()) {
                                                                            Toast.makeText(getApplicationContext(), "Could not recognize text in image", Toast.LENGTH_SHORT).show();
                                                                        } else {
                                                                            translate();
                                                                        }
                                                                    });
                                                        } else {
                                                            progressDialog.dismiss();
                                                            translate();
                                                        }
                                                    });
                                        } else {
                                            progressDialog.dismiss();
                                            translate();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            translate();
                        }
                    });
        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Failed during text recognition with other recognizers", e);
            Toast.makeText(getApplicationContext(), "Could not get text from image", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyBtn() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", translatedText);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    /* Navbar Buttons */
    void toHome() {
        Intent i = new Intent(ImageTranslateActivity.this, MainActivity.class);
        overridePendingTransition(0, 0);
        finish();
    }

    void toText() {
        startActivity(new Intent(ImageTranslateActivity.this, TextTranslateActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    void toVoice() {
        startActivity(new Intent(ImageTranslateActivity.this, VoiceTranslateActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    void toDownload() {
        startActivity(new Intent(ImageTranslateActivity.this,  DownloadLanguageTranslatePackages.class));
        overridePendingTransition(0, 0);
        finish();
    }
}