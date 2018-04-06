package net.studymongolian.suryaa;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import net.studymongolian.mongollibrary.ImeContainer;
import net.studymongolian.mongollibrary.KeyboardAeiou;

// ImeContainer should probably be separated into a View and a local Input Method Service
// This class is doing that partially for this application
// TODO move all this functionality to mongol-library
public class MyInputMethodService extends ImeContainer {

    public MyInputMethodService(Context context) {
        super(context);
    }

    public MyInputMethodService(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyInputMethodService(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void onStartInput(EditorInfo attribute, boolean restarting) {
        switch (attribute.inputType) {
            case InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PHONETIC:
                this.onRequestNewKeyboard(getIpaKeyboardDisplayName());
                break;
            default:
                this.onRequestNewKeyboard(getMongolKeyboardDisplayName());
        }
    }

    private String getIpaKeyboardDisplayName() {
        return "ipa";
    }

    private String getMongolKeyboardDisplayName() {
        return "ᠴᠠᠭᠠᠨ ᠲᠣᠯᠤᠭᠠᠢ";
    }
}
