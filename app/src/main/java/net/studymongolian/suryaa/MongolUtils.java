package net.studymongolian.suryaa;


import net.studymongolian.mongollibrary.MongolCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class MongolUtils {

    private static final Map<Character, String> mongolToRoman;
    static {
        Map<Character, String> aMap = new HashMap<>();
        aMap.put(MongolCode.Uni.A, "a");
        aMap.put(MongolCode.Uni.E, "e");
        aMap.put(MongolCode.Uni.I, "i");
        aMap.put(MongolCode.Uni.O, "o");
        aMap.put(MongolCode.Uni.U, "u");
        aMap.put(MongolCode.Uni.OE, "o");
        aMap.put(MongolCode.Uni.UE, "u");
        aMap.put(MongolCode.Uni.EE, "e");
        aMap.put(MongolCode.Uni.NA, "n");
        aMap.put(MongolCode.Uni.ANG, "ng");
        aMap.put(MongolCode.Uni.BA, "b");
        aMap.put(MongolCode.Uni.PA, "p");
        aMap.put(MongolCode.Uni.QA, "x");
        aMap.put(MongolCode.Uni.GA, "g");
        aMap.put(MongolCode.Uni.MA, "m");
        aMap.put(MongolCode.Uni.LA, "l");
        aMap.put(MongolCode.Uni.SA, "s");
        aMap.put(MongolCode.Uni.SHA, "sh");
        aMap.put(MongolCode.Uni.TA, "t");
        aMap.put(MongolCode.Uni.DA, "d");
        aMap.put(MongolCode.Uni.CHA, "ch");
        aMap.put(MongolCode.Uni.JA, "j");
        aMap.put(MongolCode.Uni.YA, "y");
        aMap.put(MongolCode.Uni.RA, "r");
        aMap.put(MongolCode.Uni.WA, "w");
        aMap.put(MongolCode.Uni.FA, "f");
        aMap.put(MongolCode.Uni.KA, "k");
        aMap.put(MongolCode.Uni.KHA, "k");
        aMap.put(MongolCode.Uni.TSA, "ts");
        aMap.put(MongolCode.Uni.ZA, "z");
        aMap.put(MongolCode.Uni.HAA, "h");
        aMap.put(MongolCode.Uni.ZRA, "r");
        aMap.put(MongolCode.Uni.LHA, "l");
        aMap.put(MongolCode.Uni.ZHI, "zh");
        aMap.put(MongolCode.Uni.CHI, "ch");
        aMap.put(MongolCode.Uni.NNBS, " ");
        aMap.put(MongolCode.Uni.MVS, "-");
        aMap.put(MongolCode.Uni.ZWJ, "");
        aMap.put(MongolCode.Uni.ZWNJ, "");
        aMap.put(MongolCode.Uni.FVS1, "");
        aMap.put(MongolCode.Uni.FVS2, "");
        aMap.put(MongolCode.Uni.FVS3, "");
        aMap.put(MongolCode.Uni.MONGOLIAN_FULL_STOP, " ");
        aMap.put(MongolCode.Uni.MONGOLIAN_COMMA, " ");
        mongolToRoman = Collections.unmodifiableMap(aMap);
    }

    static String romanize(String mongolUnicode) {
        StringBuilder builder = new StringBuilder();
        for (char character : mongolUnicode.toCharArray()) {
            if (mongolToRoman.containsKey(character)) {
                builder.append(mongolToRoman.get(character));
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
    }
}
