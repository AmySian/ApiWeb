package com.Apiweb.polling.payload;

import com.Apiweb.polling.model.Sondage;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
public class SondageRequest {

    @NotBlank
    @Size(max = 140)
    private String question;

    @NotNull
    @Size(min = 2, max = 6)
    @Valid
    private List<ChoixRequest> choix;

    @NotNull
    @Valid
    private SondageLength sondageLength;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<ChoixRequest> getChoix() {
        return choix;
    }

    public void setChoix(List<ChoixRequest> choix) {
        this.choix = choix;
    }

    public SondageLength getSondageLength() {
        return sondageLength;
    }

    public void setSondageLength(SondageLength sondageLength) {
        this.sondageLength = sondageLength;
    }
}
