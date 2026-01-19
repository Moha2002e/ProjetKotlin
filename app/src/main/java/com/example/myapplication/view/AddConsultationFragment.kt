package com.example.myapplication.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.controller.NetworkManager
import com.example.myapplication.databinding.FragmentAddConsultationBinding
import com.example.myapplication.model.CAPRequest
import com.example.myapplication.model.CAPResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddConsultationFragment : Fragment() {
    private var _liage: FragmentAddConsultationBinding? = null
    private val liage get() = _liage!!
    private lateinit var gestionnaireReseau: NetworkManager
    
    override fun onCreateView(
        gonfleur: LayoutInflater,
        conteneur: ViewGroup?,
        etatSauvegarde: Bundle?
    ): View {
        _liage = FragmentAddConsultationBinding.inflate(gonfleur, conteneur, false)
        return liage.root
    }
    
    override fun onViewCreated(vue: View, etatSauvegarde: Bundle?) {
        super.onViewCreated(vue, etatSauvegarde)
        
        if (!initialiserDependances()) return
        
        configurerSelecteurDate()
        configurerSelecteurHeure()
        configurerBoutonAjout()
    }
    
    private fun initialiserDependances(): Boolean {
        val activitePrincipale = activity as? MainActivity
        if (activitePrincipale == null) {
            return false
        }
        
        gestionnaireReseau = activitePrincipale.obtenirGestionnaireReseau()
        return true
    }
    
    private fun configurerSelecteurDate() {
        val calendrier = Calendar.getInstance()
        
        liage.dateEditText.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, annee, mois, jour ->
                    calendrier.set(annee, mois, jour)
                    val formatDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    liage.dateEditText.setText(formatDate.format(calendrier.time))
                },
                calendrier.get(Calendar.YEAR),
                calendrier.get(Calendar.MONTH),
                calendrier.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        configurerEditTextLectureSeule(liage.dateEditText)
    }
    
    private fun configurerSelecteurHeure() {
        val calendrier = Calendar.getInstance()
        
        liage.timeEditText.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, heure, minute ->
                    calendrier.set(Calendar.HOUR_OF_DAY, heure)
                    calendrier.set(Calendar.MINUTE, minute)
                    val formatHeure = SimpleDateFormat("HH:mm", Locale.getDefault())
                    liage.timeEditText.setText(formatHeure.format(calendrier.time))
                },
                calendrier.get(Calendar.HOUR_OF_DAY),
                calendrier.get(Calendar.MINUTE),
                true
            ).show()
        }
        
        configurerEditTextLectureSeule(liage.timeEditText)
    }
    
    private fun configurerEditTextLectureSeule(champ: EditText) {
        champ.isFocusable = false
        champ.isClickable = true
    }

    private fun configurerBoutonAjout() {
        liage.addButton.setOnClickListener {
            ajouterConsultation()
        }
    }
    
    private fun ajouterConsultation() {
        val date = liage.dateEditText.text.toString().trim()
        val heure = liage.timeEditText.text.toString().trim()
        val dureeTexte = liage.durationEditText.text.toString().trim()
        val nombreTexte = liage.countEditText.text.toString().trim()
        
        if (champsVides(date, heure, dureeTexte, nombreTexte)) {
            afficherMessage(getString(R.string.error_empty_fields))
            return
        }
        
        val nombre = nombreTexte.toIntOrNull()
        if (nombre == null || nombre <= 0) {
            afficherMessage(getString(R.string.error_count_invalid))
            return
        }
        
        val duree = dureeTexte.toIntOrNull()
        if (duree == null || duree <= 0) {
            afficherMessage(getString(R.string.error_duration_invalid))
            return
        }
        
        envoyerRequeteAjout(date, heure, nombre, duree)
    }
    
    private fun champsVides(vararg champs: String): Boolean {
        return champs.any { it.isEmpty() }
    }
    
    private fun envoyerRequeteAjout(date: String, heure: String, nombre: Int, duree: Int) {
        liage.addButton.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val requete = CAPRequest.AddConsultationRequest(
                    date = date,
                    hour = heure,
                    consecutiveCount = nombre,
                    duree = duree
                )
                
                val resultatReponse = gestionnaireReseau.sendRequest(requete)
                
                resultatReponse.onSuccess { reponse ->
                    traiterReponseAjout(reponse)
                }.onFailure { erreur ->
                    gererErreur(erreur.message ?: "Erreur")
                }
            } catch (e: Exception) {
                gererErreur(e.message ?: "Erreur")
            }
        }
    }
    
    private suspend fun traiterReponseAjout(reponse: CAPResponse) {
        withContext(Dispatchers.Main) {
            liage.addButton.isEnabled = true
            
            if (reponse.success) {
                afficherMessage(getString(R.string.success_consultation_added))
                viderChamps()
            } else {
                afficherMessage(reponse.message, longDuration = true)
            }
        }
    }
    
    private suspend fun gererErreur(message: String) {
        withContext(Dispatchers.Main) {
            liage.addButton.isEnabled = true
            afficherMessage(message)
        }
    }
    
    private fun viderChamps() {
        liage.dateEditText.text?.clear()
        liage.timeEditText.text?.clear()
        liage.durationEditText.text?.clear()
        liage.countEditText.text?.clear()
    }
    
    private fun afficherMessage(message: String, longDuration: Boolean = false) {
        Toast.makeText(context, message, if (longDuration) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _liage = null
    }
}
