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
    public Avis saveOreUpdateAvis(Integer livreId, Integer utilisateurId, int note) {
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(()-> new RuntimeException("Le livre n'existe pas"));
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
       .orElseThrow(()-> new RuntimeException("L'utilisateur n'existe pas"));
        if (avisRepository.findByLivreIdAndUtilisateurId(livreId, utilisateurId).isPresent()) {
            throw new RuntimeException("Un utilisateur ne peut poster qu’un seul avis par livre");
        }
        return avisRepository
                .findByLivreIdAndUtilisateurId(livreId, utilisateurId)
                .map(existingAvis-> {
                    existingAvis.setNote(note);
                    existingAvis.setDatePublication(LocalDateTime.now());
                    return avisRepository.save(existingAvis);
                })
                .orElseGet(()-> avisRepository.save(
                        Avis.builder()
                                .livre(livre)
                                .utilisateur(utilisateur)
                                .note(note)
                                .datePublication(LocalDateTime.now())
                                .build()
                ));
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
