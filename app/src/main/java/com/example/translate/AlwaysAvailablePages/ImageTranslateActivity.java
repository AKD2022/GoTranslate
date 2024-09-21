package com.example.translate.AlwaysAvailablePages;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.translate.ProgressDialog;
import com.example.translate.R;
import com.example.translate.SharedViewModels.SharedViewModelForImageTranslateActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
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
    private MaterialButton imageNav, textNav, voiceNav, downloadNav;
    ProgressDialog progressDialogInstallation = new ProgressDialog();
    ProgressDialog progressDialogTranslation = new ProgressDialog();
    ProgressDialog progressDialogRecognition = new ProgressDialog();

    ChipNavigationBar navigationBar;


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

        imageView = findViewById(R.id.imageView);
        takePicture = findViewById(R.id.takePicture);
        selectImage = findViewById(R.id.openPhotos);
        selectTranslateFrom = findViewById(R.id.selectTranslateFrom);
        selectTranslateTo = findViewById(R.id.selectTranslateTo);



        /* Navigation Bar */
        navigationBar = findViewById(R.id.menu);
        navigationBar.setItemSelected(R.id.image, true);

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

        /* Camera */
        imageUri = createUri();
        registerPictureLauncher();

        takePicture.setOnClickListener(view -> {
            checkCameraPermissionAndOpenCamera();
        });

        selectImage.setOnClickListener(view -> {
            openFolderWithImages();
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

                        getLanguageFrom();
                        getLanguageTo();

                        translateToButton = selectTranslateTo.getText().toString();
                        translateFromButton = selectTranslateFrom.getText().toString();

                        if (translateToButton == null || translateToButton.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Please select language to translate to", Toast.LENGTH_SHORT).show();
                        } else if (translateFromButton == null || translateFromButton.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Please select language to translate from", Toast.LENGTH_SHORT).show();
                        } else if (translateToButton.equals(translateFromButton)) {
                            Toast.makeText(getApplicationContext(), "You cannot select the same language twice", Toast.LENGTH_SHORT).show();
                        } else {
                            startRecognitionAndTranslation();
                        }
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
            sharedViewModelForImageTranslateActivity.setSelectedTranslateFromLanguage(selectedLanguageTranslateFrom);
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


    private void translate() {
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
                        translateWithModelAvailable(translator, recognizedText);
                    } else {
                        PleaseWaitDialog progressDialog = progressDialogInstallation.getInstallationDialog(this);
                        downloadAndTranslate(translator, recognizedText, progressDialog);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get downloaded models: " + e.getMessage());
                    Toast.makeText(ImageTranslateActivity.this, "Failed to check installed models.", Toast.LENGTH_SHORT).show();
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
                    new MaterialAlertDialogBuilder(ImageTranslateActivity.this)
                            .setMessage("Translation Failed. Please check your internet connection or try again later.")
                            .setPositiveButton("OK", null)
                            .show();
                });
    }

    private void translateWithModelAvailable(Translator translator, String sourceText) {
        progressDialogTranslation.getTranslationDialog(this);
        Task<String> result = translator.translate(sourceText)
                .addOnSuccessListener(s -> {
                    progressDialogInstallation.dismissInstallationDialog();
                    progressDialogTranslation.dismissTranslateDialog();
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
                    progressDialogInstallation.dismissTranslateDialog();
                    Log.e(TAG, "Translation failed: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Translation failed. If the model is already installed, text cannot be translated.", Toast.LENGTH_SHORT).show();
                });
    }

    private void startRecognitionAndTranslation() {
        progressDialogRecognition.getRecognizingDialog(this);
        try {
            InputImage inputImage = InputImage.fromFilePath(getApplicationContext(), imageUri);

            defaultRecognizer.process(inputImage)
                    .addOnSuccessListener(text -> {
                        recognizedText = text.getText();

                        if (recognizedText.isEmpty()) {
                            recognizeWithOtherRecognizers(progressDialogRecognition.getRecognizingDialog(this));
                        } else {
                            progressDialogRecognition.dismissRecognizingDialog();
                            translate();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialogRecognition.dismissRecognizingDialog();
                        Log.e(TAG, "Text recognition failed", e);
                        Toast.makeText(getApplicationContext(), "Could not get text from image", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            progressDialogRecognition.dismissRecognizingDialog();
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
    void toImage() {
        startActivity(new Intent(ImageTranslateActivity.this, ImageTranslateActivity.class));
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

    void toConversation() {
        startActivity(new Intent(ImageTranslateActivity.this,  ConversationActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}