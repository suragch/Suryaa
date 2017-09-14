package net.studymongolian.suryaa;


public class VocabList {



    private long listId;
    private String name;
    private long dateAccessed;

    public VocabList() {
        this.listId=0;
        this.name="";
        this.dateAccessed=0;
    }

    public String getName() {
        return name;
    }

    public long getListId() {
        return listId;
    }

    public long getDateAccessed() {
        return dateAccessed;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDateAccessed(long dateAccessed) {
        this.dateAccessed = dateAccessed;
    }
}
