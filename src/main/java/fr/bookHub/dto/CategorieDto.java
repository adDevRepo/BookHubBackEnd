package fr.bookHub.dto;

import fr.bookHub.bo.Categorie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CategorieDto {

    // Response (GET)
    public record Response(
            Integer id,
            String nom,
            String code
    ) {
        public static Response fromEntity(Categorie c) {
            if (c == null) return null;
            return new Response(c.getId(), c.getNom(), c.getCode());
        }

        public static List<Response> fromEntityList(List<Categorie> list) {
            return list.stream().map(Response::fromEntity).toList();
        }
    }

    // Request (POST)
    public record Request(
            @NotBlank @Size(min=2, max=50) String nom,
            @NotBlank @Size(min=1, max=10) String code
    ) {
        public static Categorie toEntity(Request dto) {
            Categorie c = new Categorie();
            c.setNom(dto.nom());
            c.setCode(dto.code());
            return c;
        }
    }

    // Update (PUT)
    public record Update(
            @Size(min=2, max=50) String nom,
            @Size(min=1, max=10) String code
    ) {
        public static void applyToEntity(Update dto, Categorie c) {
            if (dto.nom() != null) c.setNom(dto.nom());
            if (dto.code() != null) c.setCode(dto.code());
        }
    }
}
