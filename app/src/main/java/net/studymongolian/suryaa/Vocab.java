package net.studymongolian.suryaa;

class Vocab {

    private long id;
    private long date;
    private long listId;
    private String mongol;
    private String definition;
    private String pronunciation;
    private String audioFileName;

    Vocab() {
        this.id=0;
        this.date=0;
        this.listId=0;
        this.mongol="";
        this.definition="";
        this.pronunciation="";
        this.audioFileName="";
    }

    // getters
    long getId() {
        return id;
    }

    long getDate() {
        return date;
    }

    long getList() {
        return listId;
    }

    String getMongol() {
        return mongol;
    }

    String getDefinition() {
        return definition;
    }

    String getPronunciation() {
        return pronunciation;
    }

    String getAudioFileName() {
        return audioFileName;
    }


    // setters
    void setId(long id) {
        this.id = id;
    }

    void setDate(long date) {
        this.date = date;
    }

    void setList(long listId) {
        this.listId = listId;
    }

    void setMongol(String mongol) {
        this.mongol = (mongol != null) ? mongol : "";
    }

    void setDefinition(String definition) {
        this.definition = (definition != null) ? definition : "";
    }

    void setPronunciation(String pronunciation) {
        this.pronunciation = (pronunciation != null) ? pronunciation : "";
    }

    void setAudioFileName(String audioLocation) {
        this.audioFileName = (audioLocation != null) ? audioLocation : "";
    }

    @Override
    public String toString() {
        return "Vocab{" +
                "id=" + id +
                ", date=" + date +
                ", listId=" + listId +
                ", mongol='" + mongol + '\'' +
                ", definition='" + definition + '\'' +
                ", pronunciation='" + pronunciation + '\'' +
                ", audioFileName='" + audioFileName + '\'' +
                '}';
    }
}
