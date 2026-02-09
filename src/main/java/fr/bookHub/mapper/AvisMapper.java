package fr.bookHub.mapper;

import fr.bookHub.bo.Avis;
import fr.bookHub.dto.AvisResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvisMapper {
    public static AvisResponseDTO toDto(Avis avis) {
        AvisResponseDTO dto = new AvisResponseDTO();
        dto.setId(avis.getId());
        dto.setLivreId(avis.getLivre().getId());
        dto.setUtilisateurId(avis.getUtilisateur().getId());
        dto.setCommentaire(avis.getCommentaire());
        dto.setNote(avis.getNote());
        dto.setDatePublication(avis.getDatePublication());
        return dto;
    }
}
