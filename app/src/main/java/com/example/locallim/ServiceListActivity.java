package com.example.locallim;

public class ServiceListActivity {
    private String service_title, service_image, service_location;

    public ServiceListActivity() {
    }
    public ServiceListActivity(String service_title, String service_location, String service_image) {
        this.service_title = service_title;
        this.service_location = service_location;
        this.service_image = service_image;
    }

    public String getService_title() {
        return service_title;
    }

    public void setService_title(String service_title) {
        this.service_title = service_title;
    }

    public String getService_location() {
        return service_location;
    }

    public void setService_location(String service_location) {
        this.service_location = service_location;
    }

    public String getService_image() {
        return service_image;
    }

    public void setService_image(String service_image) {
        this.service_image = service_image;
    }
}
