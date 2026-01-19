package com.example.myapplication.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.controller.NetworkManager
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.CAPRequest
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var liage: ActivityMainBinding
    private var configurationBarreApps: AppBarConfiguration? = null
    private lateinit var gestionnaireReseau: NetworkManager
    private lateinit var preferencesPartagees: SharedPreferences
    
    companion object {
        @Volatile
        private var instance: MainActivity? = null

    }
    
    override fun onCreate(etatSauvegarde: Bundle?) {
        super.onCreate(etatSauvegarde)
        instance = this
        
        liage = ActivityMainBinding.inflate(layoutInflater)
        setContentView(liage.root)
        
        setSupportActionBar(liage.appBarMain.toolbar)
        
        initialiserComposants()
        configurerNavigation()
        verifierConnexionServeur()
    }
    
    private fun initialiserComposants() {
        gestionnaireReseau = NetworkManager()
        preferencesPartagees = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        restaurerSession()
    }
    
    private fun restaurerSession() {
        val idDocteur = preferencesPartagees.getInt("doctor_id", -1)
        if (idDocteur != -1) {
            gestionnaireReseau.setDoctorId(idDocteur)
        }
    }
    
    private fun configurerNavigation() {
        val tiroirLayout: DrawerLayout = liage.drawerLayout
        val vueNavigation: NavigationView = liage.navView
        
        // Utilisation de 'post' pour s'assurer que le NavController est prêt
        liage.root.post {
            try {
                val controleurNavigation = findNavController(R.id.nav_host_fragment_content_main)
                configurerBarreDAction(controleurNavigation, tiroirLayout)
                configurerMenuNavigation(vueNavigation, controleurNavigation, tiroirLayout)
            } catch (e: Exception) {
                // NavController peut ne pas être prêt dans certains cas rares
            }
        }
    }
    
    private fun configurerBarreDAction(controleurNavigation: NavController, tiroirLayout: DrawerLayout) {
        configurationBarreApps = AppBarConfiguration(
            setOf(
                R.id.nav_consultations,
                R.id.nav_add_consultation,
                R.id.nav_add_patient,
                R.id.nav_search
            ),
            tiroirLayout
        )
        
        configurationBarreApps?.let { config ->
            setupActionBarWithNavController(controleurNavigation, config)
        }
    }
    
    private fun configurerMenuNavigation(vueNavigation: NavigationView, controleurNavigation: NavController, tiroirLayout: DrawerLayout) {
        vueNavigation.setupWithNavController(controleurNavigation)
        
        vueNavigation.setNavigationItemSelectedListener { elementMenu ->
            if (elementMenu.itemId == R.id.nav_logout) {
                deconnecter()
                true
            } else {
                elementMenu.isChecked = true
                tiroirLayout.closeDrawers()
                controleurNavigation.navigate(elementMenu.itemId)
                true
            }
        }
    }
    
    private fun verifierConnexionServeur() {
        lifecycleScope.launch {
            if (!gestionnaireReseau.isConnected()) {
                val resultatConnexion = gestionnaireReseau.connect()
                if (resultatConnexion.isFailure) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, getString(R.string.error_connection_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return try {
            val controleurNavigation = findNavController(R.id.nav_host_fragment_content_main)
            if (configurationBarreApps != null) {
                controleurNavigation.navigateUp(configurationBarreApps!!) || super.onSupportNavigateUp()
            } else {
                super.onSupportNavigateUp()
            }
        } catch (e: Exception) {
            super.onSupportNavigateUp()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
    
    private fun deconnecter() {
        lifecycleScope.launch {
            try {
                val requete = CAPRequest.LogoutRequest
                gestionnaireReseau.sendRequest(requete)
            } catch (e: Exception) {
                // Ignorer les erreurs réseau lors de la déconnexion
            } finally {
                nettoyerEtRediriger()
            }
        }
    }
    
    private suspend fun nettoyerEtRediriger() {
        withContext(Dispatchers.Main) {
            gestionnaireReseau.disconnect()
            preferencesPartagees.edit().clear().apply()
            
            val intention = Intent(this@MainActivity, LoginActivity::class.java)
            intention.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intention)
            finish()
        }
    }
    
    fun obtenirGestionnaireReseau(): NetworkManager {
        return gestionnaireReseau
    }
}
