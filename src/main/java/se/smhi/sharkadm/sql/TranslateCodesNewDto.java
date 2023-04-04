package se.smhi.sharkadm.sql;

public class TranslateCodesNewDto {

    private int Id;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getPublic_value() {
        return public_value;
    }

    public void setPublic_value(String public_value) {
        this.public_value = public_value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSwedish() {
        return swedish;
    }

    public void setSwedish(String swedish) {
        this.swedish = swedish;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    public String getIces_biology() {
        return ices_biology;
    }

    public void setIces_biology(String ices_biology) {
        this.ices_biology = ices_biology;
    }

    public String getIces_physical_and_chemical() {
        return ices_physical_and_chemical;
    }

    public void setIces_physical_and_chemical(String ices_physical_and_chemical) {
        this.ices_physical_and_chemical = ices_physical_and_chemical;
    }

    public String getBodc_nerc() {
        return bodc_nerc;
    }

    public void setBodc_nerc(String bodc_nerc) {
        this.bodc_nerc = bodc_nerc;
    }

    public String getDarwincore() {
        return darwincore;
    }

    public void setDarwincore(String darwincore) {
        this.darwincore = darwincore;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEdmo() {
        return edmo;
    }

    public void setEdmo(String edmo) {
        this.edmo = edmo;
    }

    public String getIces() {
        return ices;
    }

    public void setIces(String ices) {
        this.ices = ices;
    }

    public String getNerc_name() {
        return nerc_name;
    }

    public void setNerc_name(String nerc_name) {
        this.nerc_name = nerc_name;
    }

    public String getNerc_id() {
        return nerc_id;
    }

    public void setNerc_id(String nerc_id) {
        this.nerc_id = nerc_id;
    }

    private String field;
    private String filter;
    private String public_value;
    private String code;
    private String swedish;
    private String english;
    private String synonyms;
    private String ices_biology;
    private String ices_physical_and_chemical;
    private String bodc_nerc;
    private String darwincore;
    private String comments;
    private String source;
    private String edmo;
    private String ices;
    private String nerc_name;
    private String nerc_id;


}
