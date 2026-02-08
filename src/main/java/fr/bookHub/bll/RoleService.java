package fr.bookHub.bll;

import fr.bookHub.bo.Role;
import fr.bookHub.bo.enums.NomRole;

import java.util.Optional;

public interface RoleService {

    /**
     * Récupère un rôle à partir de son Enum.
     * Lance une exception si le rôle n'existe pas en base.
     */
    Role consulterParNom(NomRole nomRole);

    /**
     * Vérifie si un rôle existe (utile pour l'initialisation)
     */
    boolean existe(NomRole nomRole);

    /**
     * Crée un rôle (utile pour l'initialisation de l'app)
     */
    Role creer(NomRole nomRole);
}