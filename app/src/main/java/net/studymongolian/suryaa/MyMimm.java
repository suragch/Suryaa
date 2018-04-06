package net.studymongolian.suryaa;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.text.InputType;
import android.text.method.ArrowKeyMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import net.studymongolian.mongollibrary.MongolEditText;

import java.util.ArrayList;
import java.util.List;

// TODO move all this to mongol-library
public class MyMimm  {

    private static final boolean DEBUG = false;

    private static final String TAG = "MongolImeManager";
    public MyMimm() {}

    private List<RegisteredEditor> mRegisteredEditors;

    private MyInputMethodService mImeContainer;

    private View mCurrentEditor;
    private EditorInfo mCurrentEditorInfo;
    private int mCursorSelStart;
    private int mCursorSelEnd;
    private int mCursorCandidateStart;
    private int mCursorCandidateEnd;

    public void addEditor(View editor, boolean allowSystemKeyboard) {

        // editor must be MongolEditText or EditText
        if (!(editor instanceof EditText) && !(editor instanceof MongolEditText)) {
            throw new RuntimeException("MongolInputMethodManager " +
                    "only supports adding a MongolEditText or EditText " +
                    "at this time. You added: " + editor);
        }

        if (mRegisteredEditors == null) {
            mRegisteredEditors = new ArrayList<>();
        }

        // don't add the same view twice
        for (RegisteredEditor item : mRegisteredEditors) {
            if (item.view == editor) return;
        }

        // give the editor's input connection to the keyboard when editor is focused
        editor.setOnFocusChangeListener(focusListener);
        // TODO if hiding the keyboard on back button then may need to add a touch listener to edit texts too

        // get extra updates from MongolEditText
        // TODO is there any way for us to get these updates from EditText?
        if (editor instanceof MongolEditText) {
            ((MongolEditText) editor).setOnMongolEditTextUpdateListener(mongolEditTextListener);
        }

        // TODO set allow system keyboard to show if hasn't been set
        setAllowSystemKeyboard(editor, allowSystemKeyboard);

        // add editor
        mRegisteredEditors.add(new RegisteredEditor(editor, allowSystemKeyboard));
        mCurrentEditor = editor;
    }

    private void setAllowSystemKeyboard(View editor, boolean allowSystemKeyboard) {
        if (editor instanceof EditText) {
            setAllowSystemKeyboardOnEditText((EditText) editor, allowSystemKeyboard);
        } else if (editor instanceof MongolEditText) {
            ((MongolEditText) editor).setAllowSystemKeyboard(allowSystemKeyboard);
        }
    }

    private void setAllowSystemKeyboardOnEditText(EditText editText, boolean allowSystemKeyboard) {
        // TODO this needs to be tested on lower versions!
        // https://stackoverflow.com/a/45229457

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // api 21+
            editText.setShowSoftInputOnFocus(allowSystemKeyboard);
        } else { // api 11+
            if (allowSystemKeyboard) {
                // re-enable keyboard (see https://stackoverflow.com/a/45228867)
                // FIXME this does not necessarily always work
                editText.setTextIsSelectable(false);
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.setClickable(true);
                editText.setLongClickable(true);
                editText.setMovementMethod(ArrowKeyMovementMethod.getInstance());
                editText.setText(editText.getText(), TextView.BufferType.SPANNABLE);
            } else {
                // disable keyboard
                editText.setTextIsSelectable(true);
            }
        }
    }

    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                setInputConnection(v);
                hideSystemKeyboardIfNeeded(v);
            }
        }

        private void setInputConnection(View v) {
            mCurrentEditor = v;
            EditorInfo tba = getEditorInfo(v);
            InputConnection ic = v.onCreateInputConnection(tba);
            mCurrentEditorInfo = tba;
            if (mImeContainer != null) {
                mImeContainer.setInputConnection(ic);
                mImeContainer.onStartInput(tba, false);
            }
        }

        private void hideSystemKeyboardIfNeeded(View v) {
            for (RegisteredEditor item : mRegisteredEditors) {
                if (item.view == v) {
                    if (!item.allowSystemKeyboard)
                        hideSystemKeyboard(v);
                    break;
                }
            }
        }

        private void hideSystemKeyboard(View v) {
            InputMethodManager imm = (InputMethodManager)
                    v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    };

    private EditorInfo getEditorInfo(View view) {
        EditorInfo editorInfo = new EditorInfo();
        editorInfo.packageName = view.getContext().getPackageName();
        editorInfo.fieldId = view.getId();
        return editorInfo;
    }

    private MongolEditText.OnMongolEditTextInputEventListener mongolEditTextListener =
            new MongolEditText.OnMongolEditTextInputEventListener() {

                // the editor needs to call this every time the selection changes
                // this method is an adaptation of Android source InputMethodManager#updateSelection
                @Override
                public void updateSelection(View view, int selStart, int selEnd,
                                            int candidatesStart, int candidatesEnd) {

                    if ((mCurrentEditor != view && mCurrentEditor == null)
                            || mCurrentEditorInfo == null || mImeContainer == null) {
                        return;
                    }

                    if (mCursorSelStart != selStart || mCursorSelEnd != selEnd
                            || mCursorCandidateStart != candidatesStart
                            || mCursorCandidateEnd != candidatesEnd) {

                        if (DEBUG) Log.v(TAG, "SELECTION CHANGE: " + mImeContainer);
                        final int oldSelStart = mCursorSelStart;
                        final int oldSelEnd = mCursorSelEnd;
                        mCursorSelStart = selStart;
                        mCursorSelEnd = selEnd;
                        mCursorCandidateStart = candidatesStart;
                        mCursorCandidateEnd = candidatesEnd;
                        mImeContainer.onUpdateSelection(oldSelStart, oldSelEnd,
                                selStart, selEnd, candidatesStart, candidatesEnd);
                    }

                }
            };

    public void setIme(MyInputMethodService imeContainer) {
        this.mImeContainer = imeContainer;
    }

    private class RegisteredEditor {
        View view;
        boolean allowSystemKeyboard;

        RegisteredEditor(View editor, boolean allowSystemKeyboard) {
            this.view = editor;
            this.allowSystemKeyboard = allowSystemKeyboard;
        }
    }
}
