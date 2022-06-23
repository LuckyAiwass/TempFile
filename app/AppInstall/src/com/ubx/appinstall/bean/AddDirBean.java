package com.ubx.appinstall.bean;

public class AddDirBean {
    private String name;
    private String path;
    private String img;
    private boolean enabled;

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {

        return name;
    }

    public String getPath() {
        return path;
    }

    public String getImg() {
        return img;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
