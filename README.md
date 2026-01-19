# Application Android - Gestion des Consultations Médicales

Application mobile Android développée en Kotlin permettant aux médecins de gérer leurs consultations via le protocole CAP (Consultation Administration Protocol).

## 📋 Fonctionnalités

- ✅ Authentification médecin (LOGIN)
- ✅ Liste des consultations avec pull-to-refresh
- ✅ Ajout de consultations multiples
- ✅ Ajout de patients
- ✅ Recherche de consultations par date
- ✅ Suppression de consultations libres
- ✅ Navigation avec Navigation Component
- ✅ Internationalisation (Français/Anglais)
- ✅ Architecture MVC

## 🏗️ Architecture

L'application suit le pattern **MVC (Model-View-Controller)** :

- **Model** : `Doctor`, `Patient`, `Consultation`, `CAPRequest`
- **View** : `LoginActivity`, `MainActivity`, Fragments
- **Controller** : `NetworkManager` (communication réseau)

## 📱 Structure de l'application

```
app/src/main/java/com/example/myapplication/
├── model/
│   ├── Doctor.kt
│   ├── Patient.kt
│   ├── Consultation.kt
│   └── CAPRequest.kt
├── controller/
│   └── NetworkManager.kt
└── view/
    ├── LoginActivity.kt
    ├── MainActivity.kt
    ├── ConsultationListFragment.kt
    ├── AddConsultationFragment.kt
    ├── AddPatientFragment.kt
    ├── SearchConsultationsFragment.kt
    └── ConsultationAdapter.kt
```

## 🔌 Protocole CAP

L'application communique avec un serveur CAP via TCP/IP (port 5000 par défaut).

### Commandes implémentées

| Commande | Description |
|----------|-------------|
| `LOGIN` | Authentification médecin |
| `ADD_CONSULTATION` | Créer des consultations |
| `ADD_PATIENT` | Ajouter un patient |
| `SEARCH_CONSULTATIONS` | Rechercher des consultations |
| `DELETE_CONSULTATION` | Supprimer une consultation |
| `LOGOUT` | Déconnexion |

## 🚀 Configuration

### Serveur

**Le serveur CAP se trouve dans un projet séparé :**
- **Chemin** : `C:\Users\moha4\Documents\Cours\Archi_ClientServeur_Cryptographie\Projets\eval_2`
- **Classe principale** : `consultation.server.ConsultationServerLauncher`
- **Port** : `9090` (configuré dans `application.properties`)

Voir `SERVEUR_UTILISATION.md` pour les instructions de démarrage.

Par défaut, l'application Android se connecte à :
- **Host** : `192.168.0.14` (modifiable dans `NetworkManager.kt`)
- **Port** : `5000`

Pour modifier ces paramètres, éditez `NetworkManager.kt` :

```kotlin
class NetworkManager(
    private val serverHost: String = "VOTRE_HOST",
    private val serverPort: Int = VOTRE_PORT
)
```

### Permissions

L'application nécessite les permissions suivantes (déjà configurées dans `AndroidManifest.xml`) :
- `INTERNET`
- `ACCESS_NETWORK_STATE`

## 🌍 Internationalisation

L'application supporte deux langues :
- **Français** (par défaut) : `values/strings.xml`
- **Anglais** : `values-en/strings.xml`

La langue est automatiquement détectée selon les paramètres système Android.

## 📦 Dépendances

- **AndroidX Core KTX** : Extensions Kotlin
- **Material Components** : Interface utilisateur moderne
- **Navigation Component** : Gestion de la navigation
- **Lifecycle** : ViewModel et LiveData
- **Coroutines** : Programmation asynchrone
- **Gson** : Sérialisation JSON

## 🔨 Compilation

1. Ouvrir le projet dans Android Studio
2. Synchroniser Gradle
3. Compiler et exécuter sur un appareil/émulateur Android (API 24+)

## 📖 Utilisation

1. **Connexion** : Entrer les identifiants médecin (login/password)
2. **Navigation** : Utiliser le menu latéral pour accéder aux différentes fonctionnalités
3. **Consultations** : Voir, ajouter, rechercher et supprimer des consultations
4. **Patients** : Ajouter de nouveaux patients

## 📄 Documentation

Voir `SCHEMA_APPLICATION.md` pour le schéma détaillé de l'application avec :
- Diagrammes de navigation
- Flux des événements
- Structure des composants
- Protocole CAP

## ⚠️ Notes importantes

- L'application nécessite une connexion réseau active
- Le serveur CAP doit être accessible sur le réseau configuré
- Les identifiants sont stockés localement dans SharedPreferences (non chiffrés)

## 🔐 Sécurité (Optionnel)

Pour l'option (b) de sécurisation des échanges :
- Utiliser `SSLSocket` au lieu de `Socket`
- Implémenter le chiffrement TLS/SSL
- Ajouter l'authentification par tokens JWT

## 📝 Auteur

Application développée dans le cadre du cours de développement mobile, sécurité et services.

---

**Version** : 1.0  
**Date** : 2025

