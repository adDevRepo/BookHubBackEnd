package fr.bookHub.dto;

import fr.bookHub.bo.Avis;
import fr.bookHub.bo.Livre;
import fr.bookHub.bo.Utilisateur;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class AvisDto {

    // Response (GET)
    public record Response(
            Integer id,
            Integer livreId,
            String commentaire,
            Integer utilisateurId,
            int note,
            LocalDateTime datePublication
    ) {
        public static Response fromEntity(Avis avis) {
            if (avis == null) return null;
            return new Response(
                    avis.getId(),
                    avis.getLivre().getId(),
                    avis.getCommentaire(),
                    avis.getUtilisateur().getId(),
                    avis.getNote(),
                    avis.getDatePublication()
            );
        }

        public static List<Response> fromEntityList(List<Avis> list) {
            return list.stream().map(Response::fromEntity).toList();
        }
    }

    // Request (POST/PUT)
    public record Request(
            @NotNull Integer utilisateurId,
            @Size(max = 2000, message = "Le commentaire est trop long (max 2000 caractères)")
            String commentaire,
            @NotNull(message = "La note est obligatoire")
            @Min(value = 1, message = "La note minimum est 1")
            @Max(value = 5, message = "La note maximum est 5")
            int note
    ) {
    }
}
