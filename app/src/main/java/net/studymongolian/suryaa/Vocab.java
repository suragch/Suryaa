package net.studymongolian.suryaa;

public class Vocab {

    static final int DEFAULT_NEXT_PRACTICE_DATE = 0;
    static final int DEFAULT_NTH_TRY = 1;
    static final int DEFAULT_INTERVAL_IN_DAYS = 1;
    static final float DEFAULT_EASINESS_FACTOR = 2.5f;
    static final StudyMode DEFAULT_STUDY_MODE = StudyMode.MONGOL;

    private long id;
    private long listId;
    private String mongol;
    private String definition;
    private String pronunciation;
    private String audioFilename;
    private long nextPracticeDate;
    private int nthTry;
    private int interval;
    private float easinessFactor;
    private StudyMode studyMode;

    private Vocab() {}

    public Vocab(StudyMode studyMode) {
        this.id = -1;
        this.listId = -1;
        this.mongol = "";
        this.definition = "";
        this.pronunciation = "";
        this.audioFilename = "";
        this.nextPracticeDate = DEFAULT_NEXT_PRACTICE_DATE;
        this.nthTry = DEFAULT_NTH_TRY;
        this.interval = DEFAULT_INTERVAL_IN_DAYS;
        this.easinessFactor = DEFAULT_EASINESS_FACTOR;
        this.studyMode = studyMode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getListId() {
        return listId;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }

    public String getMongol() {
        return mongol;
    }

    public void setMongol(String mongol) {
        this.mongol = mongol;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getAudioFilename() {
        return audioFilename;
    }

    public void setAudioFilename(String audioFilename) {
        this.audioFilename = audioFilename;
    }

    public long getNextPracticeDate() {
        return nextPracticeDate;
    }

    public void setNextPracticeDate(long nextPracticeDate) {
        this.nextPracticeDate = nextPracticeDate;
    }

    public int getNthTry() {
        return nthTry;
    }

    public void setNthTry(int nthTry) {
        this.nthTry = nthTry;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public float getEasinessFactor() {
        return easinessFactor;
    }

    public void setEasinessFactor(float easinessFactor) {
        this.easinessFactor = easinessFactor;
    }

    public StudyMode getStudyMode() {
        return studyMode;
    }

//    public void setStudyMode(StudyMode studyMode) {
//        this.studyMode = studyMode;
//    }
}
