package Client;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioEffect {

    private final Clip clip;

    AudioEffect(String path) {
        File file = new File(path);
        AudioInputStream audioInputStream;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopSound() {
        clip.stop();
    }



}
