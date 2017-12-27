package com.github.runningforlife.photosniffer.ui.activity;

/**
 * all user action like : save/share/deleteSync/Favor
 */

public enum UserAction {
    SAVE("Save"),
    SHARE("Share"),
    WALLPAPER("Wallpaper"),
    DELETE("Delete"),
    FAVOR("Favor");

    private String action;

    UserAction(String action){
        this.action = action;
    }

    public String action(){
        return action;
    }

    public void setAction(String action){
        this.action = action;
    }
}
