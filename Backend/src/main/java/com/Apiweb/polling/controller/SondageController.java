package com.Apiweb.polling.controller;

import com.Apiweb.polling.model.*;
import com.Apiweb.polling.payload.*;
import com.Apiweb.polling.depot.SondageDepot;
import com.Apiweb.polling.depot.UtiDepot;
import com.Apiweb.polling.depot.VoteDepot;
import com.Apiweb.polling.security.CurrentUser;
import com.Apiweb.polling.security.UtilisateurPrincipale;
import com.Apiweb.polling.service.SondageService;
import com.Apiweb.polling.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/polls")

public class SondageController {
    @Autowired
    private SondageDepot sondageDepot;

    @Autowired
    private VoteDepot voteDepot;

    @Autowired
    private UtiDepot utiDepot;

    @Autowired
    private SondageService sondageService;

    private static final Logger logger = LoggerFactory.getLogger(SondageController.class);

    @GetMapping
    public PageResponse<SondageResponse> getSondages(@CurrentUser UtilisateurPrincipale currentUser,
                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return sondageService.getAllSondages(currentUser, page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createSondage(@Valid @RequestBody SondageRequest sondageRequest) {
        Sondage sondage = sondageService.createSondage(sondageRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{sondageId}")
                .buildAndExpand(sondage.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Poll Created Successfully"));
    }

    @GetMapping("/{sondageId}")
    public SondageResponse getSondageById(@CurrentUser UtilisateurPrincipale currentUser,
                                    @PathVariable Long sondageId) {
        return sondageService.getSondageById(sondageId, currentUser);
    }

    @PostMapping("/{sondageId}/votes")
    @PreAuthorize("hasRole('USER')")
    public SondageResponse castVoteAndGetUpdatedSondage(@CurrentUser UtilisateurPrincipale currentUser,
                                 @PathVariable Long sondageId,
                                 @Valid @RequestBody VoteRequest voteRequest) {
        return sondageService.castVoteAndGetUpdatedSondage(sondageId, voteRequest, currentUser);
    }


}
