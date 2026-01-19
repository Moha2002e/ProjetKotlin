package hepl.fead.model.entity

import java.io.Serializable


class Doctor : Serializable {
    private var id: Int? = null
    private var specialite_id: Int? = null
    private var last_name: String? = null
    private var first_name: String? = null
    private var password: String? = null
    
    fun getId(): Int? = id
    fun setId(id: Int?) { this.id = id }
    fun getSpecialite_id(): Int? = specialite_id
    fun setSpecialite_id(specialite_id: Int?) { this.specialite_id = specialite_id }
    fun getLast_name(): String? = last_name
    fun setLast_name(last_name: String?) { this.last_name = last_name }
    fun getFirst_name(): String? = first_name
    fun setFirst_name(first_name: String?) { this.first_name = first_name }
    fun getPassword(): String? = password
    fun setPassword(password: String?) { this.password = password }

    override fun toString(): String {
        val ln = last_name ?: ""
        val fn = first_name ?: ""
        return (ln + (if (fn.trim().isEmpty()) "" else " $fn")).trim()
    }

    fun getName(): String {
        val ln = last_name ?: ""
        val fn = first_name ?: ""
        return (ln + (if (fn.trim().isEmpty()) "" else " $fn")).trim()
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

