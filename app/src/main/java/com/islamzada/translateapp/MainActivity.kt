package com.islamzada.translateapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.islamzada.translateapp.`class`.ModelLanguage
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var sourceLanguage: EditText
    private lateinit var targetLanguage: TextView
    private lateinit var sourceLanguageBtn: MaterialButton
    private lateinit var targetLanguageBtn: MaterialButton
    private lateinit var translateBtn: MaterialButton

    companion object{

        private const val TAG = "MAIN_TAG"
    }
    //
    private var languageArrayList: ArrayList<ModelLanguage>? = null

    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private var targetLanguageCode = "tr"
    private var targetLanguageTitle = "Turkish"


    private lateinit var translatorOptions: TranslatorOptions

    private lateinit var translator: Translator

    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sourceLanguage = findViewById(R.id.sourceLanguage)
        targetLanguage = findViewById(R.id.targetLanguage)
        sourceLanguageBtn = findViewById(R.id.sourceLanguageBtn)
        targetLanguageBtn = findViewById(R.id.targetLanguageBtn)
        translateBtn = findViewById(R.id.translateBtn)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadAvailableLanguage()

        sourceLanguageBtn.setOnClickListener{
            sourceLanguageChoose()
        }

        targetLanguageBtn.setOnClickListener {
            targetLanguageChoose()
        }

        translateBtn.setOnClickListener {
            validateData()
        }

    }
    private var sourceLanguageText = ""
    private fun validateData() {

        sourceLanguageText = sourceLanguage.text.toString().trim()

        Log.d(TAG, "validateData: sourceLanguageText: $sourceLanguageText")

        if (sourceLanguageText.isEmpty()) {

            showToast("Enter Text translate...")
        }else{
            startTranslation()
        }
    }

    private fun startTranslation() {
        progressDialog.setMessage("Processing language model...")
        progressDialog.show()

        translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(targetLanguageCode)
            .build()
        translator = Translation.getClient(translatorOptions)

        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                Log.d(TAG, "startTranslation: model ready, start translation...")

                progressDialog.setMessage("Translating...")

                translator.translate(sourceLanguageText)
                    .addOnSuccessListener { translatedText ->
                        Log.d(TAG, "startTranslation: translatedText: $translatedText")

                        progressDialog.dismiss()

                        targetLanguage.text = translatedText

                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Log.e(TAG, "startTranslation", e)

                        showToast("Failed to translate due to ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()

                Log.e(TAG, "startTranslation", e)

                showToast("Failed due to ${e.message}")
            }
    }

    private fun loadAvailableLanguage(){
        languageArrayList = ArrayList()

        val languageCodeList = TranslateLanguage.getAllLanguages()

        for (languageCode in languageCodeList){

            val languageTitle = Locale(languageCode).displayLanguage

            Log.d(TAG, "loadAvailableLanguages: languageCode: $languageCode")
            Log.d(TAG, "loadAvailableLanguages: languageTitle: $languageTitle")

            val modelLanguage = ModelLanguage(languageCode, languageTitle)

            languageArrayList!!.add(modelLanguage)
        }
    }

    private fun sourceLanguageChoose(){

        val popupMenu = PopupMenu(this, sourceLanguageBtn)

        for (i in languageArrayList!!.indices){

            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->

            val position = menuItem.itemId

            sourceLanguageCode = languageArrayList!![position].languageCode
            sourceLanguageTitle = languageArrayList!![position].languageTitle

            sourceLanguageBtn.text = sourceLanguageTitle
            sourceLanguage.hint = "Enter $sourceLanguageTitle"

            Log.d(TAG, "sourceLanguageChoose: sourceLanguageCode: $sourceLanguageCode")
            Log.d(TAG, "sourceLanguageChoose: sourceLanguageTitle: $sourceLanguageTitle")

            false
        }
    }

    private fun targetLanguageChoose() {

        val popupMenu = PopupMenu(this, targetLanguageBtn)

        for (i in languageArrayList!!.indices) {

            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->

            val position = menuItem.itemId

            targetLanguageCode = languageArrayList!![position].languageCode
            targetLanguageTitle = languageArrayList!![position].languageTitle


            targetLanguageBtn.text = targetLanguageTitle


            Log.d(TAG, "targetLanguageChoose: targetLanguageCode: $targetLanguageCode")
            Log.d(TAG, "targetLanguageChoose: targetLanguageTitle: $targetLanguageTitle")

            false
        }
    }

    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}