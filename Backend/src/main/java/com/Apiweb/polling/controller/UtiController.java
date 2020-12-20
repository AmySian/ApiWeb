package com.Apiweb.polling.controller;

import com.Apiweb.polling.exception.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.Apiweb.polling.model.*;
import com.Apiweb.polling.payload.*;
import com.Apiweb.polling.depot.SondageDepot;
import com.Apiweb.polling.depot.UtiDepot;
import com.Apiweb.polling.depot.VoteDepot;
import com.Apiweb.polling.security.CurrentUser;
import com.Apiweb.polling.security.UtilisateurPrincipale;
import com.Apiweb.polling.service.SondageService;
import com.Apiweb.polling.util.AppConstants;
@RestController
@RequestMapping("/api")
public class UtiController {
    @Autowired
    private UtiDepot utiDepot;

    @Autowired
    private SondageDepot sondageDepot;

    @Autowired
    private VoteDepot voteDepot;

    @Autowired
    private SondageService sondageService;

    private static final Logger logger = LoggerFactory.getLogger(UtiController.class);

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public Utilisateur getCurrentUser(@CurrentUser UtilisateurPrincipale currentUser) {
        Utilisateur utilisateur = new Utilisateur(currentUser.getId(), currentUser.getUsername(), currentUser.getNom(), currentUser.getPrenom());
        return utilisateur;
    }

    @GetMapping("/user/verifUsernameDispo")
    public UtilisateurEnligne verifUsernameDispo(@RequestParam(value = "username") String username) {
        Boolean isDispo = !utiDepot.existsByUsername(username);
        return new UtilisateurEnligne(isDispo);
    }

    @GetMapping("/user/verifEmailDispo")
    public UtilisateurEnligne verifEmailDispo(@RequestParam(value = "email") String email) {
        Boolean isDispo = !utiDepot.existsByUsername(email);
        return new UtilisateurEnligne(isDispo);
    }

    @GetMapping("/users/{username}")
    public ProfilUti getProfilUti(@PathVariable(value = "username") String username) {
        User user = utiDepot.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        long sondageCount = sondageDepot.countByCreatedBy(user.getId());
        long voteCount = voteDepot.countByUserId(user.getId());

        ProfilUti profilUti = new ProfilUti(user.getId(), user.getUsername(), user.getNom(),user.getPrenom(), user.getCreatedAt(), sondageCount, voteCount);

        return profilUti;
    }

    @GetMapping("/users/{username}/sondage")
    public PageResponse<SondageResponse> getSondageCreatedBy(@PathVariable(value = "username") String username,
                                                         @CurrentUser UtilisateurPrincipale currentUser,
                                                         @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                         @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return sondageService.getPollsCreatedBy(username, currentUser, page, size);
    }


    @GetMapping("/users/{username}/votes")
    public PageResponse<SondageResponse> getSondageVotedBy(@PathVariable(value = "username") String username,
                                                       @CurrentUser UtilisateurPrincipale currentUser,
                                                       @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                       @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return sondageService.getSondageVotedBy(username, currentUser, page, size);
    }

}
