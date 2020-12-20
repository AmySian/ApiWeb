package com.Apiweb.polling.service;

import com.Apiweb.polling.depot.SondageDepot;
import com.Apiweb.polling.depot.UtiDepot;
import com.Apiweb.polling.depot.VoteDepot;
import com.Apiweb.polling.exception.BadRequestException;
import com.Apiweb.polling.exception.ResourceNotFoundException;
import com.Apiweb.polling.model.*;
import com.Apiweb.polling.payload.PageResponse;
import com.Apiweb.polling.payload.SondageRequest;
import com.Apiweb.polling.payload.SondageResponse;
import com.Apiweb.polling.payload.VoteRequest;
import com.Apiweb.polling.security.UtilisateurPrincipale;
import com.Apiweb.polling.util.AppConstants;
import com.Apiweb.polling.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SondageService {
    @Autowired
    private SondageDepot sondageDepot;

    @Autowired
    private VoteDepot voteDepot;

    @Autowired
    private UtiDepot utiDepot;

    private static final Logger logger = LoggerFactory.getLogger(SondageService.class);

    public PageResponse<SondageResponse> getAllSondages(UtilisateurPrincipale currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve Polls
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Sondage> sondages = sondageDepot.findAll(pageable);

        if(sondages.getNumberOfElements() == 0) {
            return new PageResponse<>(Collections.emptyList(), sondages.getNumber(),
                    sondages.getSize(), sondages.getTotalElements(), sondages.getTotalPages(), sondages.isLast());
        }

        // Map Polls to PollResponses containing vote counts and poll creator details
        List<Long> sondageIds = sondages.map(Sondage::getId).getContent();
        Map<Long, Long> choixVoteCountMap = getChoixVoteCountMap(sondageIds);
        Map<Long, Long> sondageUserVoteMap = getSondageUserVoteMap(currentUser, sondageIds);
        Map<Long, User> creatorMap = getPollCreatorMap(sondages.getContent());

        List<SondageResponse> sondageResponses = sondages.map(sondage -> {
            return ModelMapper.mapSondageToSondageResponse(sondage,
                    choixVoteCountMap,
                    creatorMap.get(sondage.getCreatedBy()),
                    sondageUserVoteMap == null ? null : sondageUserVoteMap.getOrDefault(sondage.getId(), null));
        }).getContent();

        return new PageResponse<>(sondageResponses, sondages.getNumber(),
                sondages.getSize(), sondages.getTotalElements(), sondages.getTotalPages(), sondages.isLast());
    }

    public PageResponse<SondageResponse> getPollsCreatedBy(String username, UtilisateurPrincipale currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = utiDepot.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Retrieve all polls created by the given username
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Sondage> sondages = sondageDepot.findByCreatedBy(user.getId(), pageable);

        if (sondages.getNumberOfElements() == 0) {
            return new PageResponse<>(Collections.emptyList(), sondages.getNumber(),
                    sondages.getSize(), sondages.getTotalElements(), sondages.getTotalPages(), sondages.isLast());
        }

        // Map Polls to PollResponses containing vote counts and poll creator details
        List<Long> sondageIds = sondages.map(Sondage::getId).getContent();
        Map<Long, Long> choixVoteCountMap = getChoixVoteCountMap(sondageIds);
        Map<Long, Long> sondageUserVoteMap = getSondageUserVoteMap(currentUser, sondageIds);

        List<SondageResponse> sondageResponses = sondages.map(sondage -> {
            return ModelMapper.mapSondageToSondageResponse(sondage,
                    choixVoteCountMap,
                    user,
                    sondageUserVoteMap == null ? null : sondageUserVoteMap.getOrDefault(sondage.getId(), null));
        }).getContent();

        return new PageResponse<>(sondageResponses, sondages.getNumber(),
                sondages.getSize(), sondages.getTotalElements(), sondages.getTotalPages(), sondages.isLast());
    }

    public PageResponse<SondageResponse> getSondageVotedBy(String username, UtilisateurPrincipale currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = utiDepot.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Retrieve all pollIds in which the given username has voted
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Long> userVotedSondageIds = voteDepot.findVotedSondageIdsByUserId(user.getId(), pageable);

        if (userVotedSondageIds.getNumberOfElements() == 0) {
            return new PageResponse<>(Collections.emptyList(), userVotedSondageIds.getNumber(),
                    userVotedSondageIds.getSize(), userVotedSondageIds.getTotalElements(),
                    userVotedSondageIds.getTotalPages(), userVotedSondageIds.isLast());
        }

        // Retrieve all poll details from the voted pollIds.
        List<Long> sondageIds = userVotedSondageIds.getContent();

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Sondage> sondages = sondageDepot.findByIdIn(sondageIds, sort);

        // Map Polls to PollResponses containing vote counts and poll creator details
        Map<Long, Long> choixVoteCountMap = getChoixVoteCountMap(sondageIds);
        Map<Long, Long> sondageUserVoteMap = getSondageUserVoteMap(currentUser, sondageIds);
        Map<Long, User> creatorMap = getPollCreatorMap(sondages);

        List<SondageResponse> sondageResponses = sondages.stream().map(sondage -> {
            return ModelMapper.mapSondageToSondageResponse(sondage,
                    choixVoteCountMap,
                    creatorMap.get(sondage.getCreatedBy()),
                    sondageUserVoteMap == null ? null : sondageUserVoteMap.getOrDefault(sondage.getId(), null));
        }).collect(Collectors.toList());

        return new PageResponse<>(sondageResponses, userVotedSondageIds.getNumber(), userVotedSondageIds.getSize(), userVotedSondageIds.getTotalElements(), userVotedSondageIds.getTotalPages(), userVotedSondageIds.isLast());
    }


    public Sondage createSondage(SondageRequest sondageRequest) {
        Sondage sondage = new Sondage();
        sondage.setQuestion(sondageRequest.getQuestion());

        sondageRequest.getChoix().forEach(choixRequest -> {
            sondage.ajoutChoix(new Choix(choixRequest.getText()));
        });

        Instant now = Instant.now();
        Instant expirationDateTime = now.plus(Duration.ofDays(sondageRequest.getSondageLength().getDays()))
                .plus(Duration.ofHours(sondageRequest.getSondageLength().getHours()));

        sondage.setDateExpiration(expirationDateTime);

        return sondageDepot.save(sondage);
    }

    public SondageResponse getSondageById(Long sondageId, UtilisateurPrincipale currentUser) {
        Sondage sondage = sondageDepot.findById(sondageId).orElseThrow(
                () -> new ResourceNotFoundException("Sondage", "id", sondageId));

        // Retrieve Vote Counts of every choice belonging to the current poll
        List<ChoixVoteCount> votes = voteDepot.countBySondageIdGroupByChoixId(sondageId);

        Map<Long, Long> choixVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoixVoteCount::getChoixId, ChoixVoteCount::getVoteCount));

        // Retrieve poll creator details
        User creator = utiDepot.findById(sondage.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", sondage.getCreatedBy()));

        // Retrieve vote done by logged in user
        Vote userVote = null;
        if(currentUser != null) {
            userVote = voteDepot.findByUserIdAndSondageId(currentUser.getId(), sondageId);
        }

        return ModelMapper.mapSondageToSondageResponse(sondage, choixVotesMap,
                creator, userVote != null ? userVote.getChoix().getId(): null);
    }

    public SondageResponse castVoteAndGetUpdatedSondage(Long sondageId, VoteRequest voteRequest, UtilisateurPrincipale currentUser) {
        Sondage sondage = sondageDepot.findById(sondageId)
                .orElseThrow(() -> new ResourceNotFoundException("Sondage", "id", sondageId));

        if(sondage.getDateExpiration().isBefore(Instant.now())) {
            throw new BadRequestException("Sorry! This Poll has already expired");
        }

        User user = utiDepot.getOne(currentUser.getId());

        Choix selectedChoix = sondage.getChoix().stream()
                .filter(choix -> choix.getId().equals(voteRequest.getChoixId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Choix", "id", voteRequest.getChoixId()));

        Vote vote = new Vote();
        vote.setSondage(sondage);
        vote.setUser(user);
        vote.setChoix(selectedChoix);

        try {
            vote = voteDepot.save(vote);
        } catch (DataIntegrityViolationException ex) {
            logger.info("Utilisateur {} déjà voté au sondage  {}", currentUser.getId(), sondageId);
            throw new BadRequestException("Sorry! You have already cast your vote in this poll");
        }

        //-- Vote Saved, Return the updated Poll Response now --

        // Retrieve Vote Counts of every choice belonging to the current poll
        List<ChoixVoteCount> votes = voteDepot.countBySondageIdGroupByChoixId(sondageId);

        Map<Long, Long> choixVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoixVoteCount::getChoixId, ChoixVoteCount::getVoteCount));

        // Retrieve poll creator details
        User creator = utiDepot.findById(sondage.getCreatedBy())
                .orElseThrow(() ->new ResourceNotFoundException("User", "id", sondage.getCreatedBy()));

        return ModelMapper.mapSondageToSondageResponse(sondage, choixVotesMap, creator, vote.getChoix().getId());
    }


    private void validatePageNumberAndSize(int page, int size) {
        if(page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if(size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    private Map<Long, Long> getChoixVoteCountMap(List<Long> sondageIds) {
        // Retrieve Vote Counts of every Choice belonging to the given sondageIds
        List<ChoixVoteCount> votes = voteDepot.countBySondageIdInGroupByChoixId(sondageIds);

        Map<Long, Long> choixVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoixVoteCount::getChoixId, ChoixVoteCount::getVoteCount));

        return choixVotesMap;
    }

    private Map<Long, Long> getSondageUserVoteMap(UtilisateurPrincipale currentUser, List<Long> sondageIds) {
        // Retrieve Votes done by the logged in user to the given pollIds
        Map<Long, Long> sondageUserVoteMap = null;
        if(currentUser != null) {
            List<Vote> userVotes = voteDepot.findByUserIdAndSondageIdIn(currentUser.getId(), sondageIds);

            sondageUserVoteMap = userVotes.stream()
                    .collect(Collectors.toMap(vote -> vote.getSondage().getId(), vote -> vote.getChoix().getId()));
        }
        return sondageUserVoteMap;
    }

    Map<Long, User> getPollCreatorMap(List<Sondage> polls) {
        // Get Poll Creator details of the given list of polls
        List<Long> creatorIds = polls.stream()
                .map(Sondage::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());

        List<User> creators = utiDepot.findByIdIn(creatorIds);
        Map<Long, User> creatorMap = creators.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return creatorMap;
    }



}
