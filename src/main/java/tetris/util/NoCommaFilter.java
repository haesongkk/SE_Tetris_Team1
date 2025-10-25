package tetris.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public final class NoCommaFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string != null) {
            string = string.replace(",", ""); // 콤마 제거
        }
        if (string == null || string.isEmpty()) return;

            super.insertString(fb, offset, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text != null) {
            text = text.replace(",", "");
        }
        super.replace(fb, offset, length, text, attrs);
    }
    
}