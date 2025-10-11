package tetris.scene.game.overlay;

import tetris.GameSettings;
import tetris.util.Animation;
import tetris.util.Theme;
import tetris.util.Theme.ColorType;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.DocumentFilter;
import javax.swing.text.BadLocationException;

public class GOFooter extends Animation {

    GOFooter(boolean isHighScore) {
        super(
            null, null, 
            Theme.BG(), Theme.BG(), Theme.BG(), 
            0, 0, 
            SwingConstants.CENTER, SwingConstants.CENTER
        );

        this.isHighScore = isHighScore;

        int[] screenSize = GameSettings.getInstance().getResolutionSize();
        int width = (int)(screenSize[0] * 0.06f);
        int height = (int)(screenSize[1] * 0.04f);
        setLayout(new GridLayout(2, 1));
        setBorder(BorderFactory.createEmptyBorder(0, width, height, width));

        if (isHighScore) {
            label = new JLabel("ENTER YOUR NAME:", SwingConstants.LEFT);
            label.setFont(Theme.getFont(Theme.GIANTS_REGULAR, 0.012f));
            label.setForeground(Theme.GRAY);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            label.setHorizontalAlignment(JLabel.LEFT);

            nameField = new JTextField();
            nameField.setFont(Theme.getFont(Theme.GIANTS_REGULAR, 0.016f)); 
            nameField.setHorizontalAlignment(JTextField.CENTER);

            nameFieldAl = e -> {
                String name = nameField.getText().strip();
                if (!name.isEmpty()) {
                    onEnter(name);
                }
            };
            nameField.addActionListener(nameFieldAl);
            ((AbstractDocument) nameField.getDocument())
            .setDocumentFilter(new NoCommaFilter());

           

            add(label);
            add(nameField);

        } else {
            label = new JLabel("", SwingConstants.CENTER);
            label.setFont(Theme.getFont(Theme.GIANTS_REGULAR, 0.016f));
            label.setForeground(Theme.Block(ColorType.YELLOW));
            
            retryButton = new JButton("RETRY?");
            
            retryButton.setFont(Theme.getFont(Theme.GIANTS_BOLD, 0.014f));
            retryButton.setForeground(Theme.GRAY);

            retryButton.setFocusPainted(false);
            retryButton.setContentAreaFilled(false);
            //retryButton.setOpaque(true);

            retryButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    retryButton.setForeground(Theme.LIGHT_GRAY);
                    //retryButton.repaint();
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    retryButton.setForeground(Theme.GRAY);
                    //retryButton.repaint();
                }
            });

            add(label);
            add(retryButton);
            
            retryButtonAl = e -> {
                onRetry();
                countdownTimer.stop();
            };
            retryButton.addActionListener(retryButtonAl);
            
        }

        
    }

    void startAnimations() {
        move(0, 100, 0, 0, 1.5f, 0.3f, false);
        if(!isHighScore) {
            Animation.runLater(0.3f, () -> countdown());
        }
    }

    void countdown() {
        label.setText(String.valueOf(countdown));
        countdownTimer = new Timer(1000, e -> {
            countdown--;
            if (countdown > 0) {
                label.setText(String.valueOf(countdown));
            } else {
                countdownTimer.stop();
                onEnter("unranked");
            }
        });
        countdownTimer.start();
    }


    GameOver getGameOverInstance() {
        Container parent = this.getParent();

        if(parent == null || !(parent instanceof GOPanel)) 
            throw new RuntimeException("GOFooter is not in GOCanvas");

        parent = parent.getParent();

        if(parent == null || !(parent instanceof GameOver)) 
            throw new RuntimeException("GOFooter is not in GameOver");

        return (GameOver) parent;
    }

    void onRetry() {
        getGameOverInstance().onRetry();
        release();
    }

    void onEnter(String name) {
        getGameOverInstance().onNext(name);
        release();
    }

    JLabel label;
    JTextField nameField;
    JButton retryButton;
    Timer countdownTimer;

    ActionListener nameFieldAl;
    ActionListener retryButtonAl;

    int countdown = 5;
    boolean isHighScore;


    @Override
    public void release() {
        super.release();

        if(countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }

        if(nameField != null) {
            nameField.removeActionListener(nameFieldAl);
            nameFieldAl = null;
        }

        if(retryButton != null) {
            retryButton.removeActionListener(retryButtonAl);
            retryButtonAl = null;;
        }


        label = null;
        nameField = null;
        retryButton = null;
        isHighScore = false;

    }

    // 콤마(,)를 제거하는 DocumentFilter 
    private static final class NoCommaFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string != null) {
                string = string.replace(",", ""); // 콤마 제거
            }
            if (string == null || string.isEmpty()) return;
            super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text != null) {
                text = text.replace(",", "");
            }
            super.replace(fb, offset, length, text, attrs);
        }
    }
}



