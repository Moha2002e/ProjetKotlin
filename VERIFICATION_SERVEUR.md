# Vérification du fichier RequeteAddConsultation.java côté serveur

## ⚠️ IMPORTANT : Ce fichier DOIT être identique sur le serveur et le client

### Ordre de sérialisation (writeObject) - Lignes 65-74

```java
private void writeObject(ObjectOutputStream out) throws IOException {
    String dateStr = dateString != null ? dateString : (date != null ? date.toString() : null);
    String timeStr = timeString != null ? timeString : (time != null ? time.toString() : null);

    out.writeInt(doctorId);    // ⚠️ Ligne 69
    out.writeInt(duree);       // ⚠️ Ligne 70 - NOUVEAU: int (pas String!)
    out.writeInt(count);       // ⚠️ Ligne 71
    out.writeObject(dateStr);  // ⚠️ Ligne 72
    out.writeObject(timeStr);  // ⚠️ Ligne 73
}
```

### Ordre de désérialisation (readObject) - Lignes 76-109

```java
private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.doctorId = in.readInt();                    // ⚠️ Ligne 77
    this.duree = in.readInt();                       // ⚠️ Ligne 78 - NOUVEAU: int (pas String!)
    this.count = in.readInt();                       // ⚠️ Ligne 79
    String dateStr = (String) in.readObject();       // ⚠️ Ligne 80
    String timeStr = (String) in.readObject();       // ⚠️ Ligne 81
    // ... reste du code
}
```

### ⚠️ Vérifications à faire côté SERVEUR :

1. **Le champ `duree` doit être `int` (pas `String` ou `LocalTime`)**
   ```java
   private transient int duree;  // ✅ Correct
   ```

2. **Dans `writeObject`, ligne 70 doit être :**
   ```java
   out.writeInt(duree);  // ✅ Correct (pas writeObject!)
   ```

3. **Dans `readObject`, ligne 78 doit être :**
   ```java
   this.duree = in.readInt();  // ✅ Correct (pas readObject!)
   ```

4. **Le constructeur doit accepter `int duree` :**
   ```java
   public RequeteAddConsultation(int doctorId, String dateString, String timeString, int count, int duree)
   ```

### 🔧 Si l'erreur persiste :

1. **Supprimer tous les fichiers .class compilés** du serveur
2. **Recompiler complètement** le projet serveur
3. **Redémarrer** le serveur
4. **Vérifier** que le fichier .class utilisé correspond bien au nouveau code source

### 📍 Chemin du fichier côté serveur :

`C:\Users\moha4\Documents\Cours\Archi_ClientServeur_Cryptographie\Projets\eval_2\src\main\java\consultation\server\protocol\RequeteAddConsultation.java`
