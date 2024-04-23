package com.example.retrofit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.retrofit.databinding.ActivityMainBinding
import com.example.retrofit.API.retrofitService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    var allLanguages = emptyList<Language>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
        getLanguages()
    }

    private fun initListeners() {
        binding.btnDetectLanguage.setOnClickListener() {
            val text:String = binding.etDescription.text.toString()
            if (text.isNotEmpty()){
                showLoading()
                getTextLanguage(text)
            }
        }
    }

    private fun showLoading() {
        binding.progressbar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        runOnUiThread {
            binding.progressbar.visibility = View.GONE
        }
    }

    private fun getTextLanguage(text:String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = retrofitService.getTextLanguage(text)
            if (result.isSuccessful) {
                checkResult(result.body())
            } else {
                showError()
            }
            clearText()
            hideLoading()
        }
    }

    private fun clearText() {
        binding.etDescription.setText("")
    }

    private fun checkResult(detectionResponse: DetectionResponse?) {
        if (detectionResponse != null && !detectionResponse.data.detections.isNullOrEmpty()) {
            val correctLanguage:List<Detection> = detectionResponse.data.detections.filter { it.isReliable }
            if (correctLanguage.isNotEmpty()) {
                val languageName:Language? = allLanguages.find { it.code == correctLanguage.first().language}
                if(languageName != null){
                    runOnUiThread {
                        Toast.makeText(applicationContext, "El idioma es ${languageName.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getLanguages() {
        CoroutineScope(Dispatchers.IO).launch{
            val languages : Response<List<Language>> = retrofitService.getLanguages()
            if(languages.isSuccessful){
                allLanguages = languages.body() ?: emptyList()
                showSuccess()
            } else {
                showError()
            }
        }
    }

    private fun showSuccess() {
        runOnUiThread {
            Toast.makeText(applicationContext, "Petición correcta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError() {
        runOnUiThread {
            Toast.makeText(applicationContext, "Error al hacer la llamada", Toast.LENGTH_SHORT).show()
        }
    }
}