package com.example.myapplication.view

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemConsultationBinding
import hepl.fead.model.entity.Consultation

class ConsultationAdapter(
    var surClicSupprimer: (Consultation) -> Unit,
    var surClicEditer: (Consultation) -> Unit = {},
    private val afficherBoutonSupprimer: Boolean = true
) : ListAdapter<Consultation, ConsultationAdapter.PorteurDeVue>(ComparateurDiffConsultation()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, typeVue: Int): PorteurDeVue {
        val liage = ItemConsultationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PorteurDeVue(liage)
    }
    
    override fun onBindViewHolder(porteur: PorteurDeVue, position: Int) {
        porteur.lier(getItem(position))
    }
    
    inner class PorteurDeVue(
        private val liage: ItemConsultationBinding
    ) : RecyclerView.ViewHolder(liage.root) {
        
        fun lier(consultation: Consultation) {
            afficherDate(consultation)
            afficherHeure(consultation)
            afficherPatient(consultation)
            afficherRaison(consultation)
            afficherDuree(consultation)
            configurerBoutons(consultation)
        }
        
        private fun afficherDate(consultation: Consultation) {
            val texteDate = consultation.getDate() ?: ""
            liage.consultationDateTextView.text = texteDate
        }
        
        private fun afficherHeure(consultation: Consultation) {
            val texteHeure = consultation.getHour() ?: ""
            liage.consultationTimeTextView.text = texteHeure
        }
        
        private fun afficherPatient(consultation: Consultation) {
            val idPatient = consultation.getPatient_id()
            
            if (idPatient != null) {
                configurerPatientAssigne(consultation, idPatient)
            } else {
                configurerPatientNonAssigne()
            }
        }
        
        private fun configurerPatientAssigne(consultation: Consultation, idPatient: Int) {
            val nomPatient = construireNomPatient(consultation, idPatient)
            
            liage.consultationPatientTextView.text = liage.root.context.getString(
                R.string.consultation_patient,
                nomPatient
            )
            liage.consultationPatientTextView.setTextColor(
                liage.root.context.getColor(android.R.color.black)
            )
            liage.consultationPatientTextView.setTypeface(null, Typeface.NORMAL)
        }
        
        private fun construireNomPatient(consultation: Consultation, idPatient: Int): String {
            val prenom = consultation.getPatient_first_name()
            val nom = consultation.getPatient_last_name()
            
            return if (!prenom.isNullOrEmpty() || !nom.isNullOrEmpty()) {
                "${prenom ?: ""} ${nom ?: ""}".trim()
            } else {
                liage.root.context.getString(R.string.patient_id, idPatient)
            }
        }
        
        private fun configurerPatientNonAssigne() {
            liage.consultationPatientTextView.text = liage.root.context.getString(
                R.string.consultation_patient,
                liage.root.context.getString(R.string.consultation_free)
            )
            liage.consultationPatientTextView.setTextColor(
                liage.root.context.getColor(android.R.color.holo_orange_dark)
            )
            liage.consultationPatientTextView.setTypeface(null, Typeface.BOLD)
        }
        
        private fun afficherRaison(consultation: Consultation) {
            val raison = consultation.getReason()
            if (!raison.isNullOrEmpty()) {
                liage.consultationReasonTextView.text = liage.root.context.getString(
                    R.string.consultation_reason,
                    raison
                )
                liage.consultationReasonTextView.visibility = View.VISIBLE
            } else {
                liage.consultationReasonTextView.visibility = View.GONE
            }
        }
        
        private fun afficherDuree(consultation: Consultation) {
            val duree = consultation.getDuree()
            if (duree != null && duree > 0) {
                liage.consultationDurationTextView.text = liage.root.context.getString(
                    R.string.consultation_duration,
                    duree.toString()
                )
                liage.consultationDurationTextView.visibility = View.VISIBLE
            } else {
                liage.consultationDurationTextView.visibility = View.GONE
            }
        }
        
        private fun configurerBoutons(consultation: Consultation) {
            if (afficherBoutonSupprimer) {
                configurerBoutonsEditions(consultation)
            } else {
                masquerBoutonsEditions()
            }
        }
        
        private fun configurerBoutonsEditions(consultation: Consultation) {
            liage.deleteButton.visibility = View.VISIBLE
            liage.deleteButton.setOnClickListener {
                surClicSupprimer(consultation)
            }
            
            liage.editButton.visibility = View.VISIBLE
            liage.editButton.setOnClickListener {
                surClicEditer(consultation)
            }
        }
        
        private fun masquerBoutonsEditions() {
            liage.deleteButton.visibility = View.GONE
            liage.editButton.visibility = View.GONE
        }
    }
    
    class ComparateurDiffConsultation : DiffUtil.ItemCallback<Consultation>() {
        override fun areItemsTheSame(ancienItem: Consultation, nouvelItem: Consultation): Boolean {
            return ancienItem.getId() == nouvelItem.getId()
        }
        
        override fun areContentsTheSame(ancienItem: Consultation, nouvelItem: Consultation): Boolean {
            return ancienItem.getDate() == nouvelItem.getDate() &&
                   ancienItem.getHour() == nouvelItem.getHour() &&
                   ancienItem.getPatient_id() == nouvelItem.getPatient_id() &&
                   ancienItem.getReason() == nouvelItem.getReason() &&
                   ancienItem.getDuree() == nouvelItem.getDuree()
        }
    }
}
