# Utilisation du Serveur CAP existant

Le serveur se trouve dans : `C:\Users\moha4\Documents\Cours\Archi_ClientServeur_Cryptographie\Projets\eval_2`

## 🚀 Démarrage du serveur

### Dans IntelliJ IDEA

1. **Ouvrir le projet eval_2 dans IntelliJ**
   - File → Open → Sélectionner le dossier `eval_2`

2. **Créer une configuration d'exécution**
   - Run → Edit Configurations...
   - + → Application
   - Configurez :
     - **Name**: `CAP Server`
     - **Main class**: `consultation.server.ConsultationServerLauncher`
     - **Working directory**: `$PROJECT_DIR$` (dossier eval_2)
     - **Program arguments**: (vide, ou chemin vers application.properties si différent)

3. **Vérifier le fichier de configuration**
   - Le fichier `src/main/resources/application.properties` doit contenir :
   ```properties
   server.port=9090
   server.threads=4
   
   db.host=192.168.0.15
   db.name=PourStudent
   db.user=Student
   db.password=PassStudent1_
   db.driver=com.mysql.cj.jdbc.Driver
   db.url=jdbc:mysql://192.168.0.15/PourStudent
   ```

4. **Exécuter**
   - Run → Run 'CAP Server' (ou Shift+F10)

### En ligne de commande

```bash
# Depuis le dossier eval_2
cd C:\Users\moha4\Documents\Cours\Archi_ClientServeur_Cryptographie\Projets\eval_2

# Avec Maven
mvn clean compile
mvn exec:java -Dexec.mainClass="consultation.server.ConsultationServerLauncher"

# Ou directement avec Java (après compilation)
java -cp "target/classes;target/dependency/*" consultation.server.ConsultationServerLauncher
```

## 📋 Configuration

Le serveur utilise le fichier `src/main/resources/application.properties` :

```properties
server.port=9090          # Port d'écoute
server.threads=4          # Nombre de threads pour gérer les clients

db.host=192.168.0.15     # Adresse MySQL
db.name=PourStudent       # Nom de la base de données
db.user=Student          # Utilisateur MySQL
db.password=PassStudent1_ # Mot de passe MySQL
db.url=jdbc:mysql://192.168.0.15/PourStudent
```

## 🔌 Différences avec le serveur mono-connexion

Le serveur `eval_2` est **multi-threaded** (gère plusieurs connexions simultanées) :
- ✅ Supporte plusieurs clients en même temps
- ✅ Utilise un thread pool (4 threads par défaut)
- ✅ Plus robuste pour la production

## 📱 Configuration de l'application Android

Dans `NetworkManager.kt`, assurez-vous que le port correspond :

```kotlin
private val serverHost: String = "192.168.0.14"  // IP du serveur
private val serverPort: Int = 9090                // Port du serveur
```

## ✅ Vérification

Quand le serveur démarre, vous devriez voir :
```
Serveur Consultation démarré sur le port 9090 avec 4 threads.
```

Le serveur affichera les messages du protocole dans la console lors des échanges avec l'application mobile.

## 🗑️ Suppression du serveur local

Pour supprimer le dossier `server` du projet actuel (si vous voulez nettoyer) :

**Dans IntelliJ :**
- Clic droit sur le dossier `server` → Delete → Delete

**En ligne de commande :**
```bash
# Depuis le dossier MyApplication
Remove-Item -Path "server" -Recurse -Force
```

