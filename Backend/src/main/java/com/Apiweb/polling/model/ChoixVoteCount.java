package com.Apiweb.polling.model;


public class ChoixVoteCount {

    private Long choixId;
    private Long voteCount;

    public ChoixVoteCount(Long choixId, Long voteCount) {
        this.choixId = choixId;
        this.voteCount = voteCount;
    }

    public Long getChoixId() {
        return choixId;
    }

    public void setChoixId(Long choiceId) {
        this.choixId = choiceId;
    }

    public Long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Long voteCount) {
        this.voteCount = voteCount;
    }
}
