package se.smhi.sharkadm.sql;

public class TranslateHeaderDto {
    private String internal_key;

    public String getInternal_key() {
        return internal_key;
    }

    public void setInternal_key(String internal_key) {
        this.internal_key = internal_key;
    }

    public String getShort_text() {
        return short_text;
    }

    public void setShort_text(String short_text) {
        this.short_text = short_text;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getSwedish() {
        return Swedish;
    }

    public void setSwedish(String swedish) {
        Swedish = swedish;
    }

    public String getDarwin_core() {
        return darwin_core;
    }

    public void setDarwin_core(String darwin_core) {
        this.darwin_core = darwin_core;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    private String short_text;
    private String english;
    private String Swedish;
    private String darwin_core;
    private String comments;

}
