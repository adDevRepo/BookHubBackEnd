package fr.bookHub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter

public class AvisRequestDTO {

    @NotNull
    private Integer utilisateurId;

    @Size(max = 2000, message = "Le commentaire est trop long (max 2000 caractères)")
    private String commentaire;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimum est 1")
    @Max(value = 5, message = "La note maximum est 5")
    private int note;


}
