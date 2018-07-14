package net.studymongolian.suryaa;

public class Vocab {

    static final int DEFAULT_NEXT_DUE_DATE = 0;
    static final int DEFAULT_CONSECUTIVE_CORRECT = 0;
    static final float DEFAULT_EASINESS_FACTOR = 2.5f;
    static final int DEFAULT_QUALITY_ASSESSMENT = 0;

    private long id;
    private long listId;
    private String mongol;
    private String definition;
    private String pronunciation;
    private String audioFilename;
    private long nextDueDate;
    private int consecutiveCorrect;
    private float easinessFactor;
    private StudyMode studyMode;
    private int qualityAssessment;
    private boolean isFirstViewToday;
    private String exampleSentence;

    public Vocab(StudyMode studyMode) {
        this.id = -1;
        this.listId = -1;
        this.mongol = "";
        this.definition = "";
        this.pronunciation = "";
        this.audioFilename = "";
        this.exampleSentence = "";
        this.nextDueDate = DEFAULT_NEXT_DUE_DATE;
        this.consecutiveCorrect = DEFAULT_CONSECUTIVE_CORRECT;
        this.easinessFactor = DEFAULT_EASINESS_FACTOR;
        this.studyMode = studyMode;
        this.qualityAssessment = DEFAULT_QUALITY_ASSESSMENT;
        this.isFirstViewToday = true;
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

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setAudioFilename(String audioFilename) {
        this.audioFilename = audioFilename;
    }

    public long getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(long nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public int getConsecutiveCorrect() {
        return consecutiveCorrect;
    }

    public void setConsecutiveCorrect(int consecutiveCorrect) {
        this.consecutiveCorrect = consecutiveCorrect;
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

    public int getQualityAssessment() {
        return qualityAssessment;
    }

    public void setQualityAssessment(int value) {
        if (value < 0 || value > 5)
            throw new IllegalArgumentException("Quality assessment value must be in the range 0-5");
        this.qualityAssessment = qualityAssessment;
    }

    public boolean isFirstViewToday() {
        return isFirstViewToday;
    }

    public void setFirstViewToday(boolean firstViewToday) {
        isFirstViewToday = firstViewToday;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }
}
