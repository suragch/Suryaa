package net.studymongolian.suryaa;

class Vocab {

    private long id;
    private long date;
    private long listId;
    private String mongol;
    private String definition;
    private String pronunciation;
    private String audioFileName;

    public Vocab() {
        this.id=0;
        this.date=0;
        this.listId=0;
        this.mongol="";
        this.definition="";
        this.pronunciation="";
        this.audioFileName="";
    }

    // getters
    public long getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public long getList() {
        return listId;
    }

    public String getMongol() {
        return mongol;
    }

    public String getDefinition() {
        return definition;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public String getAudioFileName() {
        return audioFileName;
    }


    // setters
    public void setId(long id) {
        this.id = id;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setList(long listId) {
        this.listId = listId;
    }

    public void setMongol(String mongol) {
        this.mongol = mongol;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public void setAudioFileName(String audioLocation) {
        this.audioFileName = audioLocation;
    }
}
