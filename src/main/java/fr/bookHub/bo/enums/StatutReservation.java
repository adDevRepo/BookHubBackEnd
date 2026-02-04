package fr.bookHub.bo.enums;

public enum StatutReservation {
    EN_ATTENTE,   // Client : "Je le veux" (File d'attente)
    DISPONIBLE,   // Système : "Il est là, venez le chercher" (Compte à rebours)
    TERMINEE,     // Succès : Client venu chercher le livre -> Devient un Emprunt
    ANNULEE       // Échec : Client pas venu ou annulation manuelle
}