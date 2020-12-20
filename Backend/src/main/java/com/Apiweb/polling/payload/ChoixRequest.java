package com.Apiweb.polling.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
public class ChoixRequest {
    @NotBlank
    @Size(max = 40)
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
