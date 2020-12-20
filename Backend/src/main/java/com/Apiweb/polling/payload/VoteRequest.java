package com.Apiweb.polling.payload;

import javax.validation.constraints.NotNull;
public class VoteRequest {
    @NotNull
    private Long choixId;

    public Long getChoixId() {
        return choixId;
    }

    public void setChoixId(Long choixId) {
        this.choixId = choixId;
    }
}
