# Number Book – Synchronisation des contacts Android avec serveur

Application Android qui lit les contacts du téléphone, les envoie vers un serveur distant via une API REST (Retrofit) et permet de rechercher dans la base distante.

## Fonctionnalités

- Lecture des contacts (nom, numéro) via `ContentResolver`
- Affichage dans un `RecyclerView` (CardView)
- Synchronisation vers serveur PHP/MySQL avec gestion des doublons
- Recherche distante par nom ou numéro
- Gestion des permissions Android (READ_CONTACTS)
- Feedback utilisateur (ProgressBar, toasts détaillés)
- Nettoyage automatique des numéros (espaces, tirets)

## Prérequis

- Android Studio
- Serveur local (XAMPP/WAMP) avec PHP 7.4+ et MySQL
- Téléphone ou émulateur (API 24+)

## Installation

### Backend

1. Copier le dossier `numberbook-api/` dans `htdocs` (XAMPP)
2. Importer le script SQL (création base + table `contact`)
3. Modifier l’IP dans `RetrofitClient.java` (côté Android)

### Android

1. Ouvrir le projet dans Android Studio
2. Modifier `BASE_URL` dans `RetrofitClient.java` (IP de votre PC)
3. Exécuter l’application

## Améliorations par rapport au lab de base

- Layout personnalisé avec `CardView`
- Gestion des doublons côté serveur (UNIQUE KEY)
- Normalisation des numéros avant insertion
- `ProgressBar` pendant les opérations réseau
- Compteur de réussite/échec lors de la synchronisation
- Meilleure gestion des erreurs (codes HTTP, messages explicites)
- Utilisation des dernières versions des bibliothèques

