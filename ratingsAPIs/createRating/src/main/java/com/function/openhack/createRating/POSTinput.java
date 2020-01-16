package com.function.openhack.createRating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class POSTinput {

    public String userId;
    public String productId;
    public String locationName;
    public int rating;
    public String userNotes;

    // Used in response
    public String id;
    public String timeStamp;
}