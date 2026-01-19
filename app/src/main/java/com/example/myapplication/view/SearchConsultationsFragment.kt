package com.example.myapplication.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.controller.NetworkManager
import com.example.myapplication.databinding.FragmentSearchConsultationsBinding
import com.example.myapplication.model.CAPRequest
import com.example.myapplication.model.CAPResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hepl.fead.model.entity.Consultation
import hepl.fead.model.entity.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SearchConsultationsFragment : Fragment() {
    private var _liage: FragmentSearchConsultationsBinding? = null
    private val liage get() = _liage!!
    private lateinit var adaptateur: ConsultationAdapter
    private lateinit var gestionnaireReseau: NetworkManager
    
    override fun onCreateView(
        gonfleur: LayoutInflater,
        conteneur: ViewGroup?,
        etatSauvegarde: Bundle?
    ): View {
        _liage = FragmentSearchConsultationsBinding.inflate(gonfleur, conteneur, false)
        return liage.root
    }
    
    private data class ElementPatient(val id: Int?, val nom: String) {
        override fun toString(): String = nom
    }

    override fun onViewCreated(vue: View, etatSauvegarde: Bundle?) {
        super.onViewCreated(vue, etatSauvegarde)
        
        if (!initialiserDependances()) return
        
        configurerRecycleur()
        configurerSelecteurPatients()
        configurerSelecteurDate()
        configurerBoutonRecherche()
    }
    
    private fun initialiserDependances(): Boolean {
        val activitePrincipale = activity as? MainActivity
        if (activitePrincipale == null) {
            return false
        }
        
        gestionnaireReseau = activitePrincipale.obtenirGestionnaireReseau()
        return true
    }
    
    private fun configurerRecycleur() {
        adaptateur = ConsultationAdapter(
            surClicSupprimer = { },
            afficherBoutonSupprimer = false
        )
        
        liage.resultsRecyclerView.layoutManager = LinearLayoutManager(context)
        liage.resultsRecyclerView.adapter = adaptateur
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
    
    private fun configurerEditTextLectureSeule(champ: EditText) {
        champ.isFocusable = false
        champ.isClickable = true
    }
    
    private fun configurerSelecteurPatients() {
        val listePatients = mutableListOf(ElementPatient(null, getString(R.string.all_patients)))
        
        val adaptateurSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listePatients
        )
        adaptateurSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        liage.patientSpinner.adapter = adaptateurSpinner
        
        chargerPatients(adaptateurSpinner, listePatients)
    }
    
    private fun chargerPatients(adaptateurSpinner: ArrayAdapter<ElementPatient>, listePatients: MutableList<ElementPatient>) {
        lifecycleScope.launch {
            try {
                val requete = CAPRequest.ListPatientsRequest
                val resultatReponse = gestionnaireReseau.sendRequest(requete)
                
                resultatReponse.onSuccess { reponse ->
                    traiterReponsePatients(reponse, adaptateurSpinner, listePatients)
                }.onFailure { erreur ->
                    gererErreur(erreur.message ?: getString(R.string.error_loading_patients))
                }
            } catch (e: Exception) {
                gererErreur(e.message ?: getString(R.string.error_loading_patients))
            }
        }
    }
    
    private suspend fun traiterReponsePatients(reponse: CAPResponse, adaptateurSpinner: ArrayAdapter<ElementPatient>, listePatients: MutableList<ElementPatient>) {
        withContext(Dispatchers.Main) {
            if (reponse.success) {
                val patientsCharges = convertirPatients(reponse.data)

                listePatients.clear()
                listePatients.add(ElementPatient(null, getString(R.string.all_patients)))
                listePatients.addAll(patientsCharges)
                
                adaptateurSpinner.notifyDataSetChanged()
                
                if (patientsCharges.isNotEmpty()) {
                    afficherMessage(getString(R.string.patients_loaded, patientsCharges.size))
                } else {
                    afficherMessage(getString(R.string.no_patient_found))
                }
            } else {
                afficherMessage(reponse.message)
            }
        }
    }
    
    private fun convertirPatients(donnees: Any?): List<ElementPatient> {
        if (donnees == null) return emptyList()
        
        return try {
            when (donnees) {
                is List<*> -> donnees.mapNotNull { item -> extrairePatient(item) }
                else -> {
                    // Essayer via Gson si ce n'est pas une liste directe (cas rare mais possible selon l'implémentation serveur)
                    try {
                        val chaineJson = Gson().toJson(donnees)
                        val typeListe = object : TypeToken<List<Patient>>() {}.type
                        val resultat = Gson().fromJson<List<Patient>>(chaineJson, typeListe)
                        resultat?.mapNotNull { item -> extrairePatient(item) } ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun extrairePatient(item: Any?): ElementPatient? {
        if (item == null) return null
        
        if (item is Patient) {
            val id = item.getId()
            val nom = item.getName()
            return if (id != null && nom.isNotEmpty()) ElementPatient(id, nom) else null
        }
        
        return extraireViaReflexion(item)
    }

    private fun extraireViaReflexion(item: Any): ElementPatient? {
        try {
            val classePatient = item.javaClass
            val methodeGetId = classePatient.getMethod("getId")
            val methodeGetLastName = classePatient.getMethod("getLast_name")
            val methodeGetFirstName = classePatient.getMethod("getFirst_name")
            
            val idObj = methodeGetId.invoke(item)
            val nomFamille = methodeGetLastName.invoke(item) as? String ?: ""
            val prenom = methodeGetFirstName.invoke(item) as? String ?: ""
            
            val id = when (idObj) {
                is Number -> idObj.toInt()
                else -> null
            }
            
            val nomComplet = "$nomFamille $prenom".trim()
            
            return if (id != null && nomComplet.isNotEmpty()) {
                ElementPatient(id, nomComplet)
            } else {
                null
            }
        } catch (e: Exception) {
            // Tenter le parsing via Gson sur l'objet individuel si la réflexion échoue
            return try {
                val chaineJson = Gson().toJson(item)
                val patient = Gson().fromJson(chaineJson, Patient::class.java)
                val id = patient.getId()
                val nom = patient.getName()
                if (id != null && nom.isNotEmpty()) ElementPatient(id, nom) else null
            } catch (e2: Exception) {
                null
            }
        }
    }
    
    private fun configurerBoutonRecherche() {
        liage.searchButton.setOnClickListener {
            lancerRecherche()
        }
    }
    
    private fun lancerRecherche() {
        val date = liage.dateEditText.text.toString().trim()
        val patientSelectionne = liage.patientSpinner.selectedItem as? ElementPatient
        val idPatient = patientSelectionne?.id

        if (date.isEmpty() && idPatient == null) {
            afficherMessage(getString(R.string.error_search_criteria))
            return
        }
        
        liage.searchButton.isEnabled = false
        
        lifecycleScope.launch {
            try {
                executerRequeteRecherche(idPatient, date)
            } catch (e: Exception) {
                gererErreur(e.message ?: "Erreur")
                liage.searchButton.isEnabled = true
            }
        }
    }
    
    private suspend fun executerRequeteRecherche(idPatient: Int?, date: String) {
        val requete = CAPRequest.SearchConsultationsRequest(
            patientId = idPatient,
            date = if (date.isNotEmpty()) date else null
        )
        
        val resultatReponse = gestionnaireReseau.sendRequest(requete)
        
        resultatReponse.onSuccess { reponse ->
            traiterReponseRecherche(reponse)
        }.onFailure { erreur ->
            gererErreur(erreur.message ?: "Erreur")
            withContext(Dispatchers.Main) { liage.searchButton.isEnabled = true }
        }
    }
    
    private suspend fun traiterReponseRecherche(reponse: CAPResponse) {
        withContext(Dispatchers.Main) {
            liage.searchButton.isEnabled = true
            
            if (reponse.success) {
                val consultations = convertirConsultations(reponse.data)
                adaptateur.submitList(consultations)
                
                if (consultations.isEmpty()) {
                    afficherMessage(getString(R.string.no_consultations))
                }
            } else {
                afficherMessage(reponse.message)
            }
        }
    }
    
    private fun convertirConsultations(donnees: Any?): List<Consultation> {
        if (donnees == null) return emptyList()
        
        return try {
            val chaineJson = Gson().toJson(donnees)
            val typeListe = object : TypeToken<List<Consultation>>() {}.type
            Gson().fromJson<List<Consultation>>(chaineJson, typeListe) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun gererErreur(message: String) {
        withContext(Dispatchers.Main) {
            afficherMessage(message)
        }
    }
    
    private fun afficherMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _liage = null
    }
}
