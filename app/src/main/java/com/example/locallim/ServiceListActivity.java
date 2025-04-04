package com.example.locallim;

public class ServiceListActivity {
    private String service_title;
    private String service_description;
    private String service_author;
    private String service_location;
    private String service_specific_location;
    private String service_telephone;
    private String service_image;
    private String id;
    private String user_id;

    // No-arg constructor needed for Firestore
    public ServiceListActivity() {
    }

    // Getters and setters
    public String getService_title() {
        return service_title;
    }

    public void setService_title(String service_title) {
        this.service_title = service_title;
    }

    public String getService_description() {
        return service_description;
    }

    public void setService_description(String service_description) {
        this.service_description = service_description;
    }

    public String getService_author() {
        return service_author;
    }

    public void setService_author(String service_author) {
        this.service_author = service_author;
    }

    public String getService_location() {
        return service_location;
    }

    public void setService_location(String service_location) {
        this.service_location = service_location;
    }

    public String getService_specific_location() {
        return service_specific_location;
    }

    public void setService_specific_location(String service_specific_location) {
        this.service_specific_location = service_specific_location;
    }

    public String getService_telephone() {
        return service_telephone;
    }

    public void setService_telephone(String service_telephone) {
        this.service_telephone = service_telephone;
    }

    public String getService_image() {
        return service_image;
    }

    public void setService_image(String service_image) {
        this.service_image = service_image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}