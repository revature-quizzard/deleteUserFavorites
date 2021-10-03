package com.revature.delete_user_favorites.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

/**
 * SetDocument is necessary because the User returned from DynamoDB has a
 * list of SetDocuments attached to them describing the user's favorite cards
 * and their created list of cards. This is what we are altering.
 */
@Data
@AllArgsConstructor
@Builder
@DynamoDbBean
public class SetDocument {

    private String id;
    private String setName;
    private List<Tag> tags;
    private boolean isPublic;
    private String author;
    private int views;
    private int plays;
    private int studies;
    private int favorites;

    public SetDocument() {
        super();
    }

    public SetDocument(Set subject) {
        this.id = subject.getId();
        this.setName = subject.getSetName();
        this.tags = subject.getTags();
        this.isPublic = subject.isPublic();
        this.author = subject.getAuthor();
        this.views = subject.getViews();
        this.plays = subject.getPlays();
        this.studies = subject.getStudies();
        this.favorites = subject.getFavorites();
    }
}

