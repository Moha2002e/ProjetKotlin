# Schéma de l'Application Android - Gestion des Consultations Médicales

## 📱 Vue d'ensemble

Application mobile Android développée en Kotlin permettant aux médecins de gérer leurs consultations via le protocole CAP (Consultation Administration Protocol).

---

## 🏗️ Architecture MVC

### Structure des packages

```
com.example.myapplication/
├── model/          # Modèles de données
│   ├── Doctor.kt
│   ├── Patient.kt
│   ├── Consultation.kt
│   └── CAPRequest.kt
├── controller/     # Logique métier et communication réseau
│   └── NetworkManager.kt
└── view/           # Interface utilisateur
    ├── LoginActivity.kt
    ├── MainActivity.kt
    ├── ConsultationListFragment.kt
    ├── AddConsultationFragment.kt
    ├── AddPatientFragment.kt
    ├── SearchConsultationsFragment.kt
    └── ConsultationAdapter.kt
```

---

## 🔄 Flux de navigation

```
┌─────────────────────────────────────────────────────────┐
│                    LoginActivity                         │
│  ┌──────────────┐  ┌──────────────┐                    │
│  │ Login Field  │  │ Password     │                    │
│  └──────────────┘  └──────────────┘                    │
│           │                                              │
│           ▼                                              │
│     [LOGIN Button]                                       │
│           │                                              │
│           ▼                                              │
│     CAP Request: LOGIN                                   │
│           │                                              │
│           ▼                                              │
│     Success? ──YES──► MainActivity                      │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                    MainActivity                          │
│  ┌──────────────────────────────────────────────┐      │
│  │         Navigation Drawer                     │      │
│  │  • Mes Consultations                         │      │
│  │  • Ajouter Consultation                      │      │
│  │  • Ajouter Patient                           │      │
│  │  • Rechercher                                │      │
│  │  • Déconnexion                               │      │
│  └──────────────────────────────────────────────┘      │
│                        │                                 │
│        ┌───────────────┼───────────────┐                │
│        │               │               │                │
│        ▼               ▼               ▼                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  │Consultation│  │  Add     │  │  Search  │            │
│  │   List    │  │Consultation│ │          │            │
│  │ Fragment  │  │ Fragment │  │ Fragment │            │
│  └──────────┘  └──────────┘  └──────────┘            │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 Diagramme des activités et fragments

```
┌─────────────────────────────────────────────────────────────┐
│                        ACTIVITÉS                             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  LoginActivity (Point d'entrée)                            │
│  ├─ Intent: ACTION_MAIN, CATEGORY_LAUNCHER                 │
│  ├─ Événements:                                            │
│  │   • onClick(loginButton) → performLogin()                │
│  └─ Actions:                                               │
│      • Connexion au serveur CAP                             │
│      • Authentification médecin                             │
│      • Navigation vers MainActivity si succès               │
│                                                              │
│  MainActivity (Activité principale)                        │
│  ├─ Navigation Drawer                                      │
│  ├─ Toolbar avec menu                                       │
│  ├─ NavHostFragment (conteneur des fragments)             │
│  └─ Fragments:                                             │
│      ├─ ConsultationListFragment                            │
│      ├─ AddConsultationFragment                             │
│      ├─ AddPatientFragment                                  │
│      └─ SearchConsultationsFragment                        │
│                                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                        FRAGMENTS                             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ConsultationListFragment                                   │
│  ├─ RecyclerView avec SwipeRefreshLayout                    │
│  ├─ Événements:                                            │
│  │   • onRefresh() → loadConsultations()                    │
│  │   • onClick(deleteButton) → deleteConsultation()         │
│  └─ Actions CAP:                                           │
│      • SEARCH_CONSULTATIONS (chargement initial)            │
│      • DELETE_CONSULTATION                                  │
│                                                              │
│  AddConsultationFragment                                    │
│  ├─ Formulaires: date, heure, durée, nombre               │
│  ├─ Événements:                                            │
│  │   • onClick(addButton) → addConsultation()               │
│  └─ Actions CAP:                                           │
│      • ADD_CONSULTATION                                     │
│                                                              │
│  AddPatientFragment                                         │
│  ├─ Formulaires: prénom, nom                               │
│  ├─ Événements:                                            │
│  │   • onClick(addButton) → addPatient()                   │
│  └─ Actions CAP:                                           │
│      • ADD_PATIENT                                          │
│                                                              │
│  SearchConsultationsFragment                                │
│  ├─ Formulaire: date                                       │
│  ├─ RecyclerView pour résultats                            │
│  ├─ Événements:                                            │
│  │   • onClick(searchButton) → searchConsultations()         │
│  └─ Actions CAP:                                           │
│      • SEARCH_CONSULTATIONS (avec filtre date)              │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔌 Protocole CAP - Commandes implémentées

| Commande | Requête | Réponse | Fragment/Activity |
|----------|---------|---------|-------------------|
| **LOGIN** | LoginRequest(login, password) | CAPResponse(success, message, doctor) | LoginActivity |
| **ADD_CONSULTATION** | AddConsultationRequest(date, hour, duration, count) | CAPResponse(success, message) | AddConsultationFragment |
| **ADD_PATIENT** | AddPatientRequest(firstName, lastName) | CAPResponse(success, message, patientId) | AddPatientFragment |
| **UPDATE_CONSULTATION** | UpdateConsultationRequest(id, date?, hour?, patientId?, reason?) | CAPResponse(success, message) | ConsultationListFragment |
| **SEARCH_CONSULTATIONS** | SearchConsultationsRequest(patientId?, date?) | CAPResponse(success, message, List<Consultation>) | ConsultationListFragment, SearchConsultationsFragment |
| **DELETE_CONSULTATION** | DeleteConsultationRequest(consultationId) | CAPResponse(success, message) | ConsultationListFragment |
| **LOGOUT** | LogoutRequest | CAPResponse(success, message) | MainActivity |

---

## 🌐 Communication réseau

```
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION ANDROID                       │
│                                                              │
│  NetworkManager                                             │
│  ├─ Socket TCP/IP                                           │
│  ├─ ObjectOutputStream (JSON via Gson)                       │
│  ├─ BufferedReader (réception)                               │
│  └─ Coroutines (Dispatchers.IO)                             │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                        │
                        │ TCP/IP Socket
                        │ (192.168.0.15:5000)
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVEUR CAP                               │
│                                                              │
│  ConsultationServer (Java)                                   │
│  ├─ Port: 5000                                              │
│  ├─ Protocole: CAP (Consultation Administration Protocol) │
│  └─ Format: JSON sérialisé                                  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📱 Intents et navigation

### Intents explicites

```kotlin
// LoginActivity → MainActivity
Intent(this@LoginActivity, MainActivity::class.java)
    .apply {
        // Pas de données supplémentaires nécessaires
        // Les données sont stockées dans SharedPreferences
    }

// MainActivity → LoginActivity (logout)
Intent(this@MainActivity, LoginActivity::class.java)
    .apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
```

### Navigation Component

```xml
<!-- nav_graph.xml -->
<navigation>
    <fragment id="consultationListFragment" />
    <fragment id="addConsultationFragment" />
    <fragment id="addPatientFragment" />
    <fragment id="searchConsultationsFragment" />
</navigation>
```

Navigation via :
- **Navigation Drawer** : Menu latéral avec sélection de destination
- **NavController** : Gestion programmatique de la navigation

---

## 🎯 Événements (Events)

### Événements utilisateur

| Élément UI | Événement | Action |
|------------|-----------|--------|
| `loginButton` | `onClick` | `performLogin()` → CAP LOGIN |
| `addButton` (Consultation) | `onClick` | `addConsultation()` → CAP ADD_CONSULTATION |
| `addButton` (Patient) | `onClick` | `addPatient()` → CAP ADD_PATIENT |
| `searchButton` | `onClick` | `searchConsultations()` → CAP SEARCH_CONSULTATIONS |
| `deleteButton` (RecyclerView) | `onClick` | `deleteConsultation()` → CAP DELETE_CONSULTATION |
| `swipeRefresh` | `onRefresh` | `loadConsultations()` → CAP SEARCH_CONSULTATIONS |
| Menu Navigation | `onNavigationItemSelected` | Navigation vers fragment correspondant |
| `menu_logout` | `onOptionsItemSelected` | `performLogout()` → CAP LOGOUT |

### Événements réseau (Coroutines)

```kotlin
lifecycleScope.launch {
    // Connexion
    networkManager.connect()
        .onSuccess { /* Connexion réussie */ }
        .onFailure { /* Erreur de connexion */ }
    
    // Requête
    networkManager.sendRequest(request)
        .onSuccess { response -> /* Traitement réponse */ }
        .onFailure { error -> /* Gestion erreur */ }
}
```

---

## 🌍 Internationalisation (i18n)

### Langues supportées

- **Français (FR)** : `values/strings.xml` (par défaut)
- **Anglais (EN)** : `values-en/strings.xml`

### Changement de langue

L'application détecte automatiquement la langue du système Android et charge les ressources correspondantes.

---

## 💾 Persistance des données

### SharedPreferences

```kotlin
// Stockage après login
sharedPreferences.edit()
    .putString("doctor_login", login)
    .putString("doctor_password", password)
    .putString("doctor_data", Gson().toJson(doctor))
    .apply()

// Récupération au démarrage
val savedLogin = sharedPreferences.getString("doctor_login", null)
```

### Données stockées

- Identifiants médecin (login/password)
- Données médecin (objet Doctor sérialisé)
- Session active

---

## 🔐 Sécurité (Optionnel - Non implémenté)

### Recommandations pour l'option (b)

Pour sécuriser les échanges :

1. **Chiffrement TLS/SSL**
   - Utiliser `SSLSocket` au lieu de `Socket`
   - Certificat serveur validé

2. **Authentification renforcée**
   - Hashage des mots de passe (SHA-256, bcrypt)
   - Tokens JWT pour les sessions

3. **Chiffrement des données sensibles**
   - Chiffrement AES pour les données stockées localement
   - KeyStore Android pour les clés

---

## 📋 Résumé des composants

| Composant | Type | Responsabilité |
|-----------|------|----------------|
| `LoginActivity` | Activity | Authentification médecin |
| `MainActivity` | Activity | Conteneur principal avec Navigation Drawer |
| `ConsultationListFragment` | Fragment | Liste et gestion des consultations |
| `AddConsultationFragment` | Fragment | Création de consultations |
| `AddPatientFragment` | Fragment | Ajout de patients |
| `SearchConsultationsFragment` | Fragment | Recherche de consultations |
| `NetworkManager` | Controller | Communication réseau avec serveur CAP |
| `ConsultationAdapter` | Adapter | Affichage RecyclerView des consultations |

---

## 🚀 Points d'entrée

1. **Application lancée** → `LoginActivity` (LAUNCHER)
2. **Login réussi** → `MainActivity` (démarre avec `ConsultationListFragment`)
3. **Navigation** → Fragments via Navigation Component

---

**Date de création** : 2025-01-XX  
**Version** : 1.0  
**Auteur** : Application Android - Gestion Consultations Médicales

