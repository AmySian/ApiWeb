package com.Apiweb.polling.payload;
import java.time.Instant;
public class ProfilUti {
    private Long id;
    private String username;
    private String nom;
    private String prenom;
    private Instant joinedAt;
    private Long sondageCount;
    private Long voteCount;

    public ProfilUti(Long id, String username, String nom, String prenom, Instant joinedAt, Long sondageCount, Long voteCount) {
        this.id = id;
        this.username = username;
        this.nom = nom;
        this.prenom = prenom;
        this.joinedAt = joinedAt;
        this.sondageCount = sondageCount;
        this.voteCount = voteCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Long getSondageCount() {
        return sondageCount;
    }

    public void setSondageCount(Long sondageCount) {
        this.sondageCount = sondageCount;
    }

    public Long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Long voteCount) {
        this.voteCount = voteCount;
    }
}
