package com.example.myapplication.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.R
import com.example.myapplication.controller.NetworkManager
import com.example.myapplication.databinding.FragmentConsultationListBinding
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

class ConsultationListFragment : Fragment() {
    private var _liage: FragmentConsultationListBinding? = null
    private val liage get() = _liage!!
    private lateinit var adaptateur: ConsultationAdapter
    private lateinit var gestionnaireReseau: NetworkManager
    
    override fun onCreateView(
        gonfleur: LayoutInflater,
        conteneur: ViewGroup?,
        etatSauvegarde: Bundle?
    ): View {
        _liage = FragmentConsultationListBinding.inflate(gonfleur, conteneur, false)
        return liage.root
    }
    
    override fun onViewCreated(vue: View, etatSauvegarde: Bundle?) {
        super.onViewCreated(vue, etatSauvegarde)
        
        if (!initialiserDependances()) return
        
        configurerRecycleur()
        configurerRafraichissement()
        chargerConsultations()
    }
    
    private fun initialiserDependances(): Boolean {
        val activitePrincipale = activity as? MainActivity
        if (activitePrincipale == null) {
            afficherMessage(getString(R.string.error_activity_invalid))
            return false
        }
        
        gestionnaireReseau = activitePrincipale.obtenirGestionnaireReseau()
        return true
    }
    
    private fun configurerRecycleur() {
        adaptateur = ConsultationAdapter(
            surClicSupprimer = { consultation ->
                supprimerConsultation(consultation)
            },
            surClicEditer = { consultation ->
                afficherBoiteDialogueEdition(consultation)
            },
            afficherBoutonSupprimer = true
        )
        
        liage.consultationsRecyclerView.layoutManager = LinearLayoutManager(context)
        liage.consultationsRecyclerView.adapter = adaptateur
    }
    
    private fun configurerRafraichissement() {
        liage.swipeRefreshLayout.setOnRefreshListener {
            chargerConsultations()
        }
    }
    
    private fun chargerConsultations() {
        liage.swipeRefreshLayout.isRefreshing = true
        
        lifecycleScope.launch {
            try {
                executerRequeteChargement()
            } catch (e: Exception) {
                gererErreur(e.message ?: "Erreur")
            }
        }
    }
    
    private suspend fun executerRequeteChargement() {
        val requete = CAPRequest.SearchConsultationsRequest()
        val resultatReponse = gestionnaireReseau.sendRequest(requete)
        
        resultatReponse.onSuccess { reponse ->
            traiterReponseChargement(reponse)
        }.onFailure { erreur ->
            gererErreur(erreur.message ?: "Erreur")
        }
    }
    
    private suspend fun traiterReponseChargement(reponse: CAPResponse) {
        withContext(Dispatchers.Main) {
            liage.swipeRefreshLayout.isRefreshing = false
            
            if (reponse.success) {
                val listeConsultations = convertirConsultations(reponse.data)
                adaptateur.submitList(listeConsultations)
                
                if (listeConsultations.isEmpty()) {
                    afficherMessage(getString(R.string.no_consultations))
                }
            } else {
                afficherMessage(reponse.message)
            }
        }
    }
    
    private suspend fun gererErreur(message: String) {
        withContext(Dispatchers.Main) {
            liage.swipeRefreshLayout.isRefreshing = false
            afficherMessage(message)
        }
    }
    
    // On transforme les données reçues en liste de consultations propre
    private fun convertirConsultations(donnees: Any?): List<Consultation> {
        val maListe = mutableListOf<Consultation>()
        
        if (donnees is List<*>) {
            for (objet in donnees) {
                if (objet is Consultation) {
                    maListe.add(objet)
                }
            }
        }
        return maListe
    }
    
    private fun supprimerConsultation(consultation: Consultation) {
        val idConsultation = consultation.getId() ?: return

        lifecycleScope.launch {
            try {
                val requete = CAPRequest.DeleteConsultationRequest(idConsultation)
                val resultatReponse = gestionnaireReseau.sendRequest(requete)
                
                resultatReponse.onSuccess { reponse ->
                    withContext(Dispatchers.Main) {
                        afficherMessage(reponse.message)
                        if (reponse.success) {
                            chargerConsultations()
                        }
                    }
                }.onFailure { erreur ->
                    afficherMessage(erreur.message ?: "Erreur")
                }
            } catch (e: Exception) {
                afficherMessage(e.message ?: "Erreur")
            }
        }
    }
    
    private data class ElementPatient(val id: Int?, val nom: String) {
        override fun toString(): String = nom
    }
    
    private fun configurerSelecteurDateInDialogue(champDate: EditText) {
        val calendrier = Calendar.getInstance()
        
        initialiserDateCalendrier(calendrier, champDate.text.toString())
        
        champDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, annee, mois, jour ->
                    calendrier.set(annee, mois, jour)
                    val formatDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    champDate.setText(formatDate.format(calendrier.time))
                },
                calendrier.get(Calendar.YEAR),
                calendrier.get(Calendar.MONTH),
                calendrier.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        champDate.isFocusable = false
        champDate.isClickable = true
    }
    
    private fun initialiserDateCalendrier(calendrier: Calendar, dateTexte: String) {
        if (dateTexte.isNotEmpty()) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = format.parse(dateTexte)
                if (date != null) {
                    calendrier.time = date
                }
            } catch (e: Exception) { }
        }
    }
    
    private fun configurerSelecteurHeureInDialogue(champHeure: EditText) {
        val calendrier = Calendar.getInstance()
        
        initialiserHeureCalendrier(calendrier, champHeure.text.toString())
        
        champHeure.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, heure, minute ->
                    calendrier.set(Calendar.HOUR_OF_DAY, heure)
                    calendrier.set(Calendar.MINUTE, minute)
                    val formatHeure = SimpleDateFormat("HH:mm", Locale.getDefault())
                    champHeure.setText(formatHeure.format(calendrier.time))
                },
                calendrier.get(Calendar.HOUR_OF_DAY),
                calendrier.get(Calendar.MINUTE),
                true
            ).show()
        }
        
        champHeure.isFocusable = false
        champHeure.isClickable = true
    }
    
    private fun initialiserHeureCalendrier(calendrier: Calendar, heureTexte: String) {
        if (heureTexte.isNotEmpty()) {
            try {
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val heure = format.parse(heureTexte)
                if (heure != null) {
                    calendrier.time = heure
                }
            } catch (e: Exception) { }
        }
    }
    
    private fun afficherBoiteDialogueEdition(consultation: Consultation) {
        val idConsultation = consultation.getId() ?: return
        
        val vueDialogue = layoutInflater.inflate(R.layout.dialog_edit_consultation, null)
        val champDate = vueDialogue.findViewById<EditText>(R.id.editDateEditText)
        val champHeure = vueDialogue.findViewById<EditText>(R.id.editTimeEditText)
        val champRaison = vueDialogue.findViewById<EditText>(R.id.editReasonEditText)
        val selecteurPatient = vueDialogue.findViewById<Spinner>(R.id.editPatientSpinner)
        
        remplirChampsExistants(consultation, champDate, champHeure, champRaison)
        configurerSelecteurDateInDialogue(champDate)
        configurerSelecteurHeureInDialogue(champHeure)
        configurerSelecteurPatients(selecteurPatient, consultation.getPatient_id())
        
        construireEtAfficherDialogue(vueDialogue, idConsultation, champDate, champHeure, champRaison, selecteurPatient)
    }
    
    private fun remplirChampsExistants(consultation: Consultation, date: EditText, heure: EditText, raison: EditText) {
        consultation.getDate()?.let { date.setText(it) }
        consultation.getHour()?.let { heure.setText(it) }
        consultation.getReason()?.let { raison.setText(it) }
    }
    
    private fun configurerSelecteurPatients(selecteur: Spinner, idPatientActuel: Int?) {
        val listePatients = mutableListOf(ElementPatient(null, getString(R.string.no_patient_spinner)))
        val adaptateurSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listePatients
        )
        adaptateurSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selecteur.adapter = adaptateurSpinner
        
        chargerPatientsPourDialogue(selecteur, listePatients, idPatientActuel)
    }
    
    private fun construireEtAfficherDialogue(
        vue: View, 
        id: Int, 
        date: EditText, 
        heure: EditText, 
        raison: EditText, 
        selecteurPatient: Spinner
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_consultation))
            .setView(vue)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val nouvelleDate = date.text.toString().trim()
                val nouvelleHeure = heure.text.toString().trim()
                val nouvelleRaison = raison.text.toString().trim()
                val patientSelectionne = selecteurPatient.selectedItem as? ElementPatient
                val idPatient = patientSelectionne?.id
                
                mettreAJourConsultation(id, nouvelleDate, nouvelleHeure, idPatient, nouvelleRaison)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
            .show()
    }
    
    private fun chargerPatientsPourDialogue(selecteur: Spinner, listePatients: MutableList<ElementPatient>, idPatientActuel: Int?) {
        lifecycleScope.launch {
            try {
                val requete = CAPRequest.ListPatientsRequest
                val resultatReponse = gestionnaireReseau.sendRequest(requete)
                
                resultatReponse.onSuccess { reponse ->
                    if (reponse.success) {
                        miseAJourListePatients(reponse.data, selecteur, listePatients, idPatientActuel)
                    }
                }
            } catch (e: Exception) { }
        }
    }
    
    private suspend fun miseAJourListePatients(donnees: Any?, selecteur: Spinner, listePatients: MutableList<ElementPatient>, idPatientActuel: Int?) {
        withContext(Dispatchers.Main) {
            val patientsCharges = convertirPatients(donnees)
            listePatients.clear()
            listePatients.add(ElementPatient(null, getString(R.string.no_patient_spinner)))
            listePatients.addAll(patientsCharges)
            
            (selecteur.adapter as? ArrayAdapter<*>)?.notifyDataSetChanged()
            
            if (idPatientActuel != null) {
                val index = listePatients.indexOfFirst { it.id == idPatientActuel }
                if (index >= 0) {
                    selecteur.setSelection(index)
                }
            }
        }
    }
    
    // On extrait les patients de la liste reçue pour remplir le menu déroulant
    private fun convertirPatients(donnees: Any?): List<ElementPatient> {
        val maListe = mutableListOf<ElementPatient>()
        
        if (donnees is List<*>) {
            for (objet in donnees) {
                if (objet is Patient) {
                    val id = objet.getId()
                    val nom = objet.getName()
                    
                    if (id != null && nom.isNotEmpty()) {
                        maListe.add(ElementPatient(id, nom))
                    }
                }
            }
        }
        return maListe
    }
    



    
    private fun mettreAJourConsultation(id: Int, date: String?, heure: String?, idPatient: Int?, raison: String?) {
        lifecycleScope.launch {
            try {
                val requete = CAPRequest.UpdateConsultationRequest(
                    consultationId = id,
                    date = if (date.isNullOrEmpty()) null else date,
                    hour = if (heure.isNullOrEmpty()) null else heure,
                    patientId = idPatient,
                    reason = if (raison.isNullOrEmpty()) null else raison
                )
                
                val resultatReponse = gestionnaireReseau.sendRequest(requete)
                
                resultatReponse.onSuccess { reponse ->
                    withContext(Dispatchers.Main) {
                        afficherMessage(reponse.message)
                        if (reponse.success) {
                            chargerConsultations()
                        }
                    }
                }.onFailure { erreur ->
                    withContext(Dispatchers.Main) {
                        afficherMessage(erreur.message ?: "Erreur")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    afficherMessage(e.message ?: "Erreur")
                }
            }
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
