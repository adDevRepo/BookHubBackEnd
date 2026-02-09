package fr.bookHub.bll;

import fr.bookHub.bo.Avis;
import fr.bookHub.bo.Livre;
import fr.bookHub.bo.Utilisateur;
import fr.bookHub.dal.AvisRepository;
import fr.bookHub.dal.LivreRepository;
import fr.bookHub.dal.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvisServiceImpl implements AvisService {


    private final AvisRepository avisRepository;

    private final UtilisateurRepository utilisateurRepository;

    private final LivreRepository livreRepository;

    public AvisServiceImpl(AvisRepository avisRepository,
                           UtilisateurRepository utilisateurRepository,
                           LivreRepository livreRepository) {
        this.avisRepository = avisRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.livreRepository = livreRepository;
    }


    @Override
    @Transactional
    public Avis saveAvis(Integer livreId, Integer utilisateurId, int note, String commentaire) {

        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new IllegalArgumentException("Le livre n'existe pas"));

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("L'utilisateur n'existe pas"));

        if (avisRepository.findByLivreIdAndUtilisateurId(livreId, utilisateurId).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur ne peut poster qu’un seul avis par livre");
        }

        return avisRepository.save(
                Avis.builder()
                        .livre(livre)
                        .utilisateur(utilisateur)
                        .note(note)
                        .commentaire(commentaire)
                        .datePublication(LocalDateTime.now())
                        .build()
        );
    }



    @Override
    @Transactional
    public Avis updateAvis(Integer avisId, int note, String commentaire) {
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new IllegalArgumentException("Avis introuvable"));

        avis.setNote(note);
        avis.setCommentaire(commentaire);
        avis.setDatePublication(LocalDateTime.now());
        return avisRepository.save(avis);
    }

    @Override
    @Transactional
    public void deleteAvisById(Integer avisId) {
        if (!avisRepository.existsById(avisId)) {
            throw new IllegalArgumentException("Avis introuvable");
        }
        avisRepository.deleteById(avisId);
    }

    @Override
    public Double getAverageNoteByLivre(Integer livreId) {
        return avisRepository.findAverageNoteByLivreId(livreId);
    }

    @Override
    public long countAvisByLivre(Integer livreId) {
        return avisRepository.countByLivreId(livreId);
    }

    @Override
    public List<Avis> getAvisByUtilisateur(Integer utilisateurId) {
        return avisRepository.findByUtilisateurIdOrderByDatePublicationDesc(utilisateurId);
    }

    @Override
    public List<Avis> getAvisByLivre(Integer livreId) {
        return avisRepository.findByLivreIdOrderByDatePublicationDesc(livreId);
    }

}
