package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.entreprise.EntrepriseService;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.utilisateur.dto.ChangePasswordRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurResponse;
import cm.kfokam.stock.utilisateur.model.Utilisateur;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class UtilisateurServiceImpl implements UtilisateurService {

    private static final int TEMPORARY_PASSWORD_LENGTH = 10;

    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseService entrepriseService;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Bean
    UserDetailsService userDetailsService() {
        return email -> utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable avec l'email : " + email));
    }

    @Override
    public UtilisateurResponse create(UtilisateurRequest request) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("L'email '%s' est déjà utilisé".formatted(request.email()));
        }
        EntrepriseResponse entreprise = entrepriseService.getById(request.entrepriseId());

        String temporaryPassword = generateTemporaryPassword();

        Utilisateur utilisateur = utilisateurMapper.toEntity(request);
        utilisateur.setMotDePasse(passwordEncoder.encode(temporaryPassword));
        utilisateur.setMustChangePassword(true);
        utilisateur.setEntreprise(entityManager.getReference(Entreprise.class, entreprise.id()));

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        log.info("Mot de passe temporaire pour {} : {}", saved.getEmail(), temporaryPassword);

        return utilisateurMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse getById(Long id) {
        return utilisateurMapper.toResponse(findUtilisateurOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> getAll() {
        return utilisateurMapper.toResponseList(utilisateurRepository.findAll());
    }

    @Override
    public UtilisateurResponse update(Long id, UtilisateurRequest request) {
        Utilisateur utilisateur = findUtilisateurOrThrow(id);

        utilisateurRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException("L'email '%s' est déjà utilisé".formatted(request.email()));
                });

        EntrepriseResponse entreprise = entrepriseService.getById(request.entrepriseId());

        utilisateurMapper.updateEntityFromRequest(request, utilisateur);
        utilisateur.setEntreprise(entityManager.getReference(Entreprise.class, entreprise.id()));

        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    public void delete(Long id) {
        Utilisateur utilisateur = findUtilisateurOrThrow(id);
        utilisateurRepository.delete(utilisateur);
    }

    @Override
    public void changePassword(Long utilisateurId, ChangePasswordRequest request) {
        Utilisateur utilisateur = findUtilisateurOrThrow(utilisateurId);

        if (!passwordEncoder.matches(request.oldPassword(), utilisateur.getMotDePasse())) {
            throw new BadCredentialsException("L'ancien mot de passe est incorrect");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.newPassword()));
        utilisateur.setMustChangePassword(false);
        utilisateurRepository.save(utilisateur);
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, TEMPORARY_PASSWORD_LENGTH).toUpperCase();
    }

    private Utilisateur findUtilisateurOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable avec l'id : " + id));
    }
}