package com.bookstore.palvindersingh;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class bookMetaData {

    //initialise attributes
    private final String isbn;
    private book book;

    //constructor
    bookMetaData(String isbn) {
        this.isbn = isbn;
        this.book = getData();
    }

    //method to get book meta data
    private book getData() {
        //get data
        JSONObject JSONData = getJSONData();
        JSONObject volumeInfo = getVolumeInfo(JSONData);
        //check if data exists
        if (volumeInfo == null) {
            //return null to indicate a result does not exist
            return null;
        } else {
            //extract meta data
            String title = getBookTitle(volumeInfo);
            String image = getImageURl(volumeInfo);
            ArrayList<String> authors = getAuthor(volumeInfo);
            ArrayList<String> genres = getGenre(volumeInfo);
            //return a book object from data
            book book = new book(authors, genres, image, this.isbn, title, null, false, false, null);
            return book;
        }
    }

    //method to pull main data from google books api
    private JSONObject getJSONData() {
        try {
            //buffer read the url
            URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=" + this.isbn);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String data = "";
            String line = "";
            while (line != null) {
                line = bufferedReader.readLine();
                data += line;
            }
            return new JSONObject(data);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //method to parse the first layer of the overall data
    private JSONObject getVolumeInfo(JSONObject JSONData) {
        try {
            JSONArray items = (JSONArray) JSONData.get("items");
            JSONObject firstItem = (JSONObject) items.get(0);
            return (JSONObject) firstItem.get("volumeInfo");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //method to extract book title
    private String getBookTitle(JSONObject volumeInfo) {
        try {
            return (String) volumeInfo.get("title");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //method to extract image url
    private String getImageURl(JSONObject volumeInfo) {
        try {
            JSONObject urls = (JSONObject) volumeInfo.get("imageLinks");
            return (String) urls.get("thumbnail");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //method to extract author array
    private ArrayList<String> getAuthor(JSONObject volumeInfo) {
        try {
            JSONArray authors = (JSONArray) volumeInfo.get("authors");
            return convertToArrayListFromJSON(authors);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //method to extract genre array
    private ArrayList<String> getGenre(JSONObject volumeInfo) {
        try {
            JSONArray categories = (JSONArray) volumeInfo.get("categories");
            return convertToArrayListFromJSON(categories);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //method to convert a json array to array list for generics
    private ArrayList<String> convertToArrayListFromJSON(JSONArray jsonArray) throws JSONException {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String item = jsonArray.get(i).toString();
            data.add(item);
        }
        return data;
    }

    //getter
    public book getBook() {
        return book;
    }
}