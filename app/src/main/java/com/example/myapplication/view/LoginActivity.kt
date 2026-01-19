package com.example.myapplication.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.controller.NetworkManager
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.model.CAPRequest
import com.example.myapplication.model.CAPResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var liage: ActivityLoginBinding
    private lateinit var gestionnaireReseau: NetworkManager
    private lateinit var preferencesPartagees: SharedPreferences
    
    override fun onCreate(etatSauvegarde: Bundle?) {
        super.onCreate(etatSauvegarde)
        liage = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(liage.root)
        
        initialiserComposants()
        chargerIdentifiantsSauvegardes()
        configurerEcouteurs()
    }
    
    private fun initialiserComposants() {
        gestionnaireReseau = NetworkManager()
        preferencesPartagees = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    
    private fun chargerIdentifiantsSauvegardes() {
        val identifiantSauvegarde = preferencesPartagees.getString("doctor_login", null)
        val motDePasseSauvegarde = preferencesPartagees.getString("doctor_password", null)
        if (identifiantSauvegarde != null && motDePasseSauvegarde != null) {
            liage.loginEditText.setText(identifiantSauvegarde)
            liage.passwordEditText.setText(motDePasseSauvegarde)
        }
    }
    
    private fun configurerEcouteurs() {
        liage.loginButton.setOnClickListener {
            tenterConnexion()
        }
    }
    
    private fun tenterConnexion() {
        val identifiant = liage.loginEditText.text.toString().trim()
        val motDePasse = liage.passwordEditText.text.toString()
        
        if (champsVides(identifiant, motDePasse)) {
            afficherMessage(getString(R.string.error_empty_fields))
            return
        }
        
        afficherChargement(true)
        
        lifecycleScope.launch {
            try {
                executerRequeteConnexion(identifiant, motDePasse)
            } catch (e: Exception) {
                gererErreur(getString(R.string.error_request_failed, e.message ?: getString(R.string.unknown_error)))
            }
        }
    }
    
    private suspend fun executerRequeteConnexion(identifiant: String, motDePasse: String) {
        val resultatConnexion = gestionnaireReseau.connect()
        
        if (resultatConnexion.isFailure) {
            gererErreur(getString(R.string.error_connection_failed))
            return
        }
        
        val requete = CAPRequest.LoginRequest(identifiant, motDePasse)
        val resultatReponse = gestionnaireReseau.sendRequest(requete)
        
        resultatReponse.onSuccess { reponse ->
            traiterReponseConnexion(reponse, identifiant, motDePasse)
        }.onFailure { erreur ->
            gererErreur(erreur.message ?: getString(R.string.unknown_error))
        }
    }

    // Nous utilisons 'Any' car le type de réponse peut varier, mais ici nous attendons la structure de réponse du réseau
    private suspend fun traiterReponseConnexion(reponse: CAPResponse, identifiant: String, motDePasse: String) {
        withContext(Dispatchers.Main) {
            if (reponse.success) {
                sauvegarderSession(reponse.data, identifiant, motDePasse)
                
                var messageSucces = getString(R.string.login_success)
                if (reponse.message.isNotEmpty()) {
                    messageSucces += "\n${reponse.message}"
                }
                afficherMessage(messageSucces)
                
                naviguerVersPrincipal()
            } else {
                val messageErreur = reponse.message.ifEmpty { getString(R.string.error_connection_failed) }
                gererErreur(messageErreur)
            }
        }
    }
    
    private fun sauvegarderSession(donneesDocteur: Any?, identifiant: String, motDePasse: String) {
        if (donneesDocteur != null) {
            try {
                val classeDocteur = donneesDocteur.javaClass
                val methodeGetId = classeDocteur.getMethod("getId")
                val id = methodeGetId.invoke(donneesDocteur) as? Int
                if (id != null) {
                    gestionnaireReseau.setDoctorId(id)
                    preferencesPartagees.edit().putInt("doctor_id", id).apply()
                }
            } catch (e: Exception) {
                // Ignorer les erreurs de réflexion si l'ID ne peut pas être récupéré
            }
        }
        
        preferencesPartagees.edit()
            .putString("doctor_login", identifiant)
            .putString("doctor_password", motDePasse)
            .putString("doctor_data", Gson().toJson(donneesDocteur))
            .apply()
    }
    
    private fun naviguerVersPrincipal() {
        val intention = Intent(this, MainActivity::class.java)
        startActivity(intention)
        finish()
    }
    
    private suspend fun gererErreur(message: String) {
        withContext(Dispatchers.Main) {
            afficherChargement(false)
            afficherMessage(message)
        }
    }
    
    private fun afficherChargement(actif: Boolean) {
        liage.loginButton.isEnabled = !actif
        liage.progressBar.visibility = if (actif) View.VISIBLE else View.GONE
    }
    
    private fun afficherMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun champsVides(identifiant: String, motDePasse: String): Boolean {
        return identifiant.isEmpty() || motDePasse.isEmpty()
    }
}
