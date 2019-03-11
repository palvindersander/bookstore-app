package com.bookstore.palvindersingh;

import java.io.Serializable;
import java.util.ArrayList;

//implement serializable in order to be passed between activities
public class book implements Serializable {

    //initialise attributes
    private ArrayList<String> authors;
    private ArrayList<String> genres;
    private String image;
    private String isbn;
    private String title;
    private String ownerReference;
    private Boolean market;
    private String firestoreID;
    private Boolean renting;
    private String rentingID;


    //constructor
    public book() {
    }

    //constructor
    public book(ArrayList<String> authors, ArrayList<String> genres, String image, String isbn, String title, String ownerReference, Boolean market, Boolean renting, String rentingID) {
        this.authors = authors;
        this.genres = genres;
        this.image = image;
        this.isbn = isbn;
        this.title = title;
        this.ownerReference = ownerReference;
        this.market = market;
        this.renting = renting;
        this.rentingID = rentingID;
    }

    //getter and setters
    public String getFirestoreID() {
        return firestoreID;
    }

    public void setFirestoreID(String firestoreID) {
        this.firestoreID = firestoreID;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public String getImage() {
        return image;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getOwnerReference() {
        return ownerReference;
    }

    public void setOwnerReference(String ownerReference) {
        this.ownerReference = ownerReference;
    }

    public Boolean getMarket() {
        return market;
    }

    public void setMarket(Boolean market) {
        this.market = market;
    }

    public Boolean getRenting() {
        return renting;
    }

    public void setRenting(Boolean renting) {
        this.renting = renting;
    }

    public String getRentingID() {
        return rentingID;
    }

    public void setRentingID(String rentingID) {
        this.rentingID = rentingID;
    }
}
