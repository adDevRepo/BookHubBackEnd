package fr.bookHub.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter

public class AvisResponseDTO {

    private Integer id;
    private Integer livreId;
    private String commentaire;
    private Integer utilisateurId;
    private int note;
    private LocalDateTime datePublication;

}
