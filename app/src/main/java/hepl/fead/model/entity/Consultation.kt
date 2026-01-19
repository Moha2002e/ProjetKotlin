package hepl.fead.model.entity

import java.io.Serializable


class Consultation : Serializable {
    private var id: Int? = null
    private var doctor_id: Int? = null
    private var patient_id: Int? = null
    private var date: String? = null
    private var hour: String? = null
    private var reason: String? = null
    private var patient_first_name: String? = null
    private var patient_last_name: String? = null
    private var patient_birth_date: String? = null
    private var duree: Int? = null
    
    fun getId(): Int? = id
    fun setId(id: Int?) { this.id = id }
    fun getDoctor_id(): Int? = doctor_id
    fun setDoctor_id(doctor_id: Int?) { this.doctor_id = doctor_id }
    fun getPatient_id(): Int? = patient_id
    fun setPatient_id(patient_id: Int?) { this.patient_id = patient_id }
    fun getDate(): String? = date
    fun setDate(date: String?) { this.date = date }
    fun getHour(): String? = hour
    fun setHour(hour: String?) { this.hour = hour }
    fun getReason(): String? = reason
    fun setReason(reason: String?) { this.reason = reason }
    fun getPatient_first_name(): String? = patient_first_name
    fun setPatient_first_name(patient_first_name: String?) { this.patient_first_name = patient_first_name }
    fun getPatient_last_name(): String? = patient_last_name
    fun setPatient_last_name(patient_last_name: String?) { this.patient_last_name = patient_last_name }
    fun getPatient_birth_date(): String? = patient_birth_date
    fun setPatient_birth_date(patient_birth_date: String?) { this.patient_birth_date = patient_birth_date }
    fun getDuree(): Int? = duree
    fun setDuree(duree: Int?) { this.duree = duree }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

