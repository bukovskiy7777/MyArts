package com.company.art_and_culture.myarts.pojo;

public class FilterObject {
    private String text;
    private boolean chosen;

    public FilterObject(String text, boolean chosen) {
        this.text = text;
        this.chosen = chosen;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }
}
