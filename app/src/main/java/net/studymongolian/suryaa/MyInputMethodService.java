package net.studymongolian.suryaa;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import net.studymongolian.mongollibrary.ImeContainer;

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

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        switch (attribute.inputType) {
            case InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PHONETIC:
                this.requestNewKeyboard(1);
                break;
            default:
                this.requestNewKeyboard(0);
        }
    }

}
