package com.example.myapplication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.controller.NetworkManager
import com.example.myapplication.databinding.FragmentAddPatientBinding
import com.example.myapplication.model.CAPRequest
import com.example.myapplication.model.CAPResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPatientFragment : Fragment() {
    private var _liage: FragmentAddPatientBinding? = null
    private val liage get() = _liage!!
    private lateinit var gestionnaireReseau: NetworkManager
    
    override fun onCreateView(
        gonfleur: LayoutInflater,
        conteneur: ViewGroup?,
        etatSauvegarde: Bundle?
    ): View {
        _liage = FragmentAddPatientBinding.inflate(gonfleur, conteneur, false)
        return liage.root
    }
    
    override fun onViewCreated(vue: View, etatSauvegarde: Bundle?) {
        super.onViewCreated(vue, etatSauvegarde)
        
        if (!initialiserDependances()) return
        
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
    
    private fun configurerBoutonAjout() {
        liage.addButton.setOnClickListener {
            ajouterPatient()
        }
    }
    
    private fun ajouterPatient() {
        val prenom = liage.firstNameEditText.text.toString().trim()
        val nom = liage.lastNameEditText.text.toString().trim()
        
        if (prenom.isEmpty() || nom.isEmpty()) {
            afficherMessage(getString(R.string.error_empty_fields))
            return
        }
        
        envoyerRequeteAjout(prenom, nom)
    }
    

    
    private fun envoyerRequeteAjout(prenom: String, nom: String) {
        liage.addButton.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val requete = CAPRequest.AddPatientRequest(
                    firstName = prenom,
                    lastName = nom
                )
                
                val resultatReponse = gestionnaireReseau.sendRequest(requete)
                
                resultatReponse.onSuccess { reponse ->
                    traiterReponseAjout(reponse)
                }.onFailure { erreur ->
                    gererErreur(erreur.message ?: getString(R.string.error))
                }
            } catch (e: Exception) {
                gererErreur(e.message ?: getString(R.string.error))
            }
        }
    }
    
    private suspend fun traiterReponseAjout(reponse: CAPResponse) {
        withContext(Dispatchers.Main) {
            liage.addButton.isEnabled = true
            
            if (reponse.success) {
                val idPatient = reponse.data as? Int
                val message = getString(R.string.success_patient_added, idPatient ?: 0)
                afficherMessage(message)
                
                liage.firstNameEditText.text?.clear()
                liage.lastNameEditText.text?.clear()
            } else {
                afficherMessage(reponse.message)
            }
        }
    }
    

    
    private suspend fun gererErreur(message: String) {
        withContext(Dispatchers.Main) {
            liage.addButton.isEnabled = true
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
