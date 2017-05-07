package com.github.runningforlife.photofans.model;

/**
 * all user action like : save/share/delete/Favor
 */

public enum UserAction {
    SAVE("Save"),
    SHARE("Share"),
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
