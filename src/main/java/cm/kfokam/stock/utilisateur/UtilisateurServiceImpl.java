package cm.kfokam.stock.utilisateur;

import cm.kfokam.stock.auth.CurrentUserService;
import cm.kfokam.stock.common.entity.Adresse;
import cm.kfokam.stock.email.EmailService;
import cm.kfokam.stock.entreprise.model.Entreprise;
import cm.kfokam.stock.exception.DuplicateEmailException;
import cm.kfokam.stock.exception.EntityNotFoundException;
import cm.kfokam.stock.storage.FileStorageService;
import cm.kfokam.stock.utilisateur.dto.ChangePasswordRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurMeRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurRequest;
import cm.kfokam.stock.utilisateur.dto.UtilisateurResponse;
import cm.kfokam.stock.utilisateur.model.Role;
import cm.kfokam.stock.utilisateur.model.Utilisateur;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class UtilisateurServiceImpl implements UtilisateurService {

    private static final int TEMPORARY_PASSWORD_LENGTH = 10;
    private static final String PHOTO_FOLDER = "utilisateurs";

    private final UtilisateurRepository utilisateurRepository;
    private final CurrentUserService currentUserService;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

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
        Long idEntreprise = currentUserService.getCurrentEntrepriseId();

        String temporaryPassword = generateTemporaryPassword();

        Utilisateur utilisateur = utilisateurMapper.toEntity(request);
        utilisateur.setMotDePasse(passwordEncoder.encode(temporaryPassword));
        utilisateur.setMustChangePassword(true);
        utilisateur.setEntreprise(entityManager.getReference(Entreprise.class, idEntreprise));

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        log.info("Utilisateur créé ({}), email de bienvenue envoyé avec le mot de passe temporaire", saved.getEmail());
        emailService.envoyerMotDePasseTemporaire(saved.getEmail(), saved.getPrenom(), temporaryPassword);

        return utilisateurMapper.toResponse(saved);
    }

    @Override
    public UtilisateurResponse createInitialAdmin(Long entrepriseId, String nom, String prenom, String email,
                                                    String rawPassword, LocalDate dateDeNaissance,
                                                    String rue, String ville, String codePostal, String pays) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("L'email '%s' est déjà utilisé".formatted(email));
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .motDePasse(passwordEncoder.encode(rawPassword))
                .dateDeNaissance(dateDeNaissance)
                .adresse(Adresse.builder().adresse1(rue).ville(ville).codePostal(codePostal).pays(pays).build())
                .entreprise(entityManager.getReference(Entreprise.class, entrepriseId))
                .roles(Set.of(Role.ROLE_ADMIN))
                .mustChangePassword(false)
                .build();

        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse getById(Long id) {
        return utilisateurMapper.toResponse(findUtilisateurOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UtilisateurResponse> getAll(Pageable pageable) {
        return utilisateurRepository.findAllByEntrepriseId(currentUserService.getCurrentEntrepriseId(), pageable)
                .map(utilisateurMapper::toResponse);
    }

    @Override
    public UtilisateurResponse update(Long id, UtilisateurRequest request) {
        Utilisateur utilisateur = findUtilisateurOrThrow(id);

        utilisateurRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException("L'email '%s' est déjà utilisé".formatted(request.email()));
                });

        // entreprise is fixed at creation and never reassigned via update — a user belongs to exactly one tenant.
        utilisateurMapper.updateEntityFromRequest(request, utilisateur);

        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    public UtilisateurResponse updateMine(Long id, UtilisateurMeRequest request) {
        Utilisateur utilisateur = findUtilisateurOrThrow(id);

        utilisateurMapper.updateEntityFromMeRequest(request, utilisateur);

        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    public UtilisateurResponse uploadPhoto(Long id, MultipartFile file) {
        // Self-service only: even an ADMIN cannot upload another user's photo through this endpoint.
        if (!currentUserService.getCurrentUtilisateur().getId().equals(id)) {
            throw new AccessDeniedException("Vous ne pouvez modifier que votre propre photo");
        }

        Utilisateur utilisateur = findUtilisateurOrThrow(id);
        String previousPhoto = utilisateur.getPhoto();

        String objectName = fileStorageService.uploadFile(file, PHOTO_FOLDER);
        utilisateur.setPhoto(objectName);
        Utilisateur saved = utilisateurRepository.save(utilisateur);

        if (previousPhoto != null && !previousPhoto.isBlank()) {
            fileStorageService.deleteFile(previousPhoto);
        }

        return utilisateurMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Utilisateur utilisateur = findUtilisateurOrThrow(id);
        String photo = utilisateur.getPhoto();
        utilisateurRepository.delete(utilisateur);
        if (photo != null && !photo.isBlank()) {
            fileStorageService.deleteFile(photo);
        }
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

    @Override
    public void resetPassword(Long utilisateurId, String newRawPassword) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable avec l'id : " + utilisateurId));

        utilisateur.setMotDePasse(passwordEncoder.encode(newRawPassword));
        utilisateur.setMustChangePassword(false);
        utilisateurRepository.save(utilisateur);
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, TEMPORARY_PASSWORD_LENGTH).toUpperCase();
    }

    private Utilisateur findUtilisateurOrThrow(Long id) {
        return utilisateurRepository.findByIdAndEntrepriseId(id, currentUserService.getCurrentEntrepriseId())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable avec l'id : " + id));
    }
}