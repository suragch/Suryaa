package net.studymongolian.suryaa;

import java.util.HashMap;
import java.util.Map;

public enum StudyMode {
    MONGOL("M"),
    DEFINITION("D"),
    PRONUNCIATION("P");

    private final String code;
    private static final Map<String,StudyMode> valuesByCode;

    static {
        valuesByCode = new HashMap<>(values().length);
        for (StudyMode value : values()) {
            valuesByCode.put(value.code, value);
        }
    }

    StudyMode(String code) {
        this.code = code;
    }

    public static StudyMode lookupByCode(String code) {
        StudyMode sm = valuesByCode.get(code);
        if (sm == null) return StudyMode.MONGOL;
        return sm;
    }

    public String getCode() {
        return code;
    }
}
