package com.Apiweb.polling.util;

import com.Apiweb.polling.model.Sondage;
import com.Apiweb.polling.model.User;
import com.Apiweb.polling.payload.ChoixResponse;
import com.Apiweb.polling.payload.SondageResponse;
import com.Apiweb.polling.payload.Utilisateur;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelMapper {
    public static SondageResponse mapSondageToSondageResponse(Sondage sondage, Map<Long, Long> choixVotesMap, User creator, Long userVote) {
        SondageResponse sondageResponse = new SondageResponse();
        sondageResponse.setId(sondage.getId());
        sondageResponse.setQuestion(sondage.getQuestion());
        sondageResponse.setCreationDateTime(sondage.getCreatedAt());
        sondageResponse.setExpirationDateTime(sondage.getDateExpiration());
        Instant now = Instant.now();
        sondageResponse.setExpired(sondage.getDateExpiration().isBefore(now));

        List<ChoixResponse> choixResponses = sondage.getChoix().stream().map(choice -> {
            ChoixResponse choixResponse = new ChoixResponse();
            choixResponse.setId(choice.getId());
            choixResponse.setText(choice.getText());

            if(choixVotesMap.containsKey(choice.getId())) {
                choixResponse.setVoteCount(choixVotesMap.get(choice.getId()));
            } else {
                choixResponse.setVoteCount(0);
            }
            return choixResponse;
        }).collect(Collectors.toList());

        sondageResponse.setChoix(choixResponses);
        Utilisateur creatorUser = new Utilisateur(creator.getId(), creator.getUsername(), creator.getNom(), creator.getPrenom());
        sondageResponse.setCreatedBy(creatorUser);

        if(userVote != null) {
            sondageResponse.setSelectedChoix(userVote);
        }

        long totalVotes = sondageResponse.getChoix().stream().mapToLong(ChoixResponse::getVoteCount).sum();
        sondageResponse.setTotalVotes(totalVotes);

        return sondageResponse;
    }
}
