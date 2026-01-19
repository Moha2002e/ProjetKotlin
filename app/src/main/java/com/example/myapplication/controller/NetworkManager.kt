package com.example.myapplication.controller


import com.example.myapplication.model.CAPRequest
import com.example.myapplication.model.CAPResponse
import consultation.server.protocol.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class NetworkManager(
    private val serverHost: String = "192.168.92.1",
    private val serverPort: Int = 9090
) {
    private var socket: Socket? = null
    private var outputStream: ObjectOutputStream? = null
    private var inputStream: ObjectInputStream? = null
    private var doctorId: Int? = null
    
    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            socket = Socket(serverHost, serverPort)
            outputStream = ObjectOutputStream(socket?.getOutputStream())
            outputStream?.flush()
            inputStream = ObjectInputStream(socket?.getInputStream())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun convertRequest(request: CAPRequest): Any {
        if (request is CAPRequest.LoginRequest) {
            return RequeteLogin(request.login, request.password)
        }
        
        if (request is CAPRequest.AddConsultationRequest) {
            val doctorIdValue = doctorId
            if (doctorIdValue == null) {
                throw IllegalStateException("Médecin non connecté")
            }

            return RequeteAddConsultation(doctorIdValue, request.date, request.hour, request.consecutiveCount, request.duree)
        }
        
        if (request is CAPRequest.AddPatientRequest) {
            return RequeteAddPatient(request.lastName, request.firstName)
        }
        
        if (request is CAPRequest.UpdateConsultationRequest) {

            return RequeteUpdateConsultation(
                request.consultationId, request.date, request.hour, request.patientId, request.reason
            )
        }
        
        if (request is CAPRequest.SearchConsultationsRequest) {

            return RequeteSearchConsultations(doctorId, request.patientId, request.date, request.date)
        }
        
        if (request is CAPRequest.DeleteConsultationRequest) {
            return RequeteDeleteConsultation(request.consultationId)
        }
        
        if (request is CAPRequest.ListPatientsRequest) {
            return RequeteListPatients()
        }
        
        if (request is CAPRequest.LogoutRequest) {
            return RequeteLogout()
        }
        
        throw IllegalArgumentException("Type de requête inconnu")
    }
    
    fun setDoctorId(id: Int) {
        doctorId = id
    }
    



    private fun convertResponse(reponseTraitee: ReponseTraitee): CAPResponse {
        return CAPResponse(
            success = reponseTraitee.isSuccess(),
            message = reponseTraitee.getMessage(),
            data = reponseTraitee.getData()
        )
    }
    
    suspend fun sendRequest(request: CAPRequest): Result<CAPResponse> = withContext(Dispatchers.IO) {
        try {
            if (socket == null) {
                return@withContext Result.failure(Exception("Socket non connecté"))
            }
            if (socket?.isClosed == true) {
                return@withContext Result.failure(Exception("Socket fermé"))
            }
            if (outputStream == null) {
                return@withContext Result.failure(Exception("OutputStream non initialisé"))
            }
            if (inputStream == null) {
                return@withContext Result.failure(Exception("InputStream non initialisé"))
            }
            
            val requeteObj = convertRequest(request)
            





            outputStream?.writeObject(requeteObj)
            outputStream?.flush()// ici on vide le buffer
            

            outputStream?.reset()// ici on vide le buffer pour éviter les doublons



            val responseObj = inputStream?.readObject()
            
            if (responseObj == null) {
                return@withContext Result.failure(Exception("Réponse nulle du serveur"))
            }
            
            if (responseObj is ReponseTraitee) {
                val response = convertResponse(responseObj)
                Result.success(response)
            } else {
                Result.success(CAPResponse(true, "Réponse reçue", responseObj))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun disconnect() {
        try {
            inputStream?.close()
        } catch (e: Exception) {

        }
        try {
            outputStream?.close()
        } catch (e: Exception) {

        }
        try {
            socket?.close()
        } catch (e: Exception) {

        }
        inputStream = null
        outputStream = null
        socket = null
    }
    
    fun isConnected(): Boolean {
        if (socket == null) {
            return false
        }
        if (socket?.isClosed == true) {
            return false
        }
        return socket?.isConnected == true
    }
}
