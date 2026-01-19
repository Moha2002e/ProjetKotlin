package hepl.fead.model.entity

import java.io.Serializable


class Patient : Serializable {
    private var id: Int? = null
    private var last_name: String? = null
    private var first_name: String? = null
    private var birth_date: String? = null
    
    fun getId(): Int? = id
    fun setId(id: Int?) { this.id = id }
    fun getLast_name(): String? = last_name
    fun setLast_name(last_name: String?) { this.last_name = last_name }
    fun getFirst_name(): String? = first_name
    fun setFirst_name(first_name: String?) { this.first_name = first_name }
    fun getBirth_date(): String? = birth_date
    fun setBirth_date(birth_date: String?) { this.birth_date = birth_date }

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
        @JvmStatic
        private val serialVersionUID: Long = 1L
    }
}
