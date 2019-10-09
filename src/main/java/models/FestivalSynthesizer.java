package models;

import constants.Filename;
import constants.Folder;

import java.io.*;

public final class FestivalSynthesizer extends Synthesizer {

    private static final File previewFile = new File(Folder.TEMP.get(), "/preview.scm");
    private static final long serialVersionUID = 1013614822749434394L;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(voice);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        voice = (Voice) in.readObject();
    }

    public enum Voice {
        KAL("kal_diphone"),
        NZ("akl_nz_jdt_diphone");

        private final String name;
        Voice(String voice) {
            this.name = voice;
        }

        public String getName() {
            return name;
        }
    }

    private Voice voice;

    public FestivalSynthesizer() {
        this.voice = Voice.KAL;
    }

    public FestivalSynthesizer(Voice voice) {
        this.voice = voice;
    }

    public Voice getVoice() {
        return voice;
    }

    @Override
    public void preview(String text) {
        try {
            FileWriter writer = new FileWriter(previewFile);
            writer.write(String.format("(voice_%s)\n", voice.getName()));
            writer.write(String.format("(SayText \"%s\")\n", text));
            writer.close();
        } catch (IOException e) {
            // TODO - Handle exception
        }

        ProcessBuilder processBuilder = new ProcessBuilder("festival", "-b", previewFile.toString());
        try {
            Process process = processBuilder.start();
            // TODO - Return status when done
        } catch (IOException e) {
            // TODO - Error checking
        }
    }

    @Override
    public void save(String text, File folder) {

        File textFile = new File(folder, "text.txt");
        try {
            FileWriter writer = new FileWriter(textFile);
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            // TODO - Handle exception
        }

        File synthFile = new File(folder, "synth.scm");
        try {
            FileWriter writer = new FileWriter(synthFile);
            writer.write(String.format("(voice_%s)\n", voice.getName()));
            writer.close();
        } catch (IOException e) {
            // TODO - Handle exception
        }

        File audioFile = new File(folder, Filename.CHUNK_AUDIO.get());
        ProcessBuilder processBuilder = new ProcessBuilder("text2wave", "-o", audioFile.toString(),
                textFile.toString(), "-eval", synthFile.toString(), "-F", "22050");
        try {
            Process process = processBuilder.start();
            // TODO - Return status when done
        } catch (IOException e) {
            // TODO - Error checking
        }
    }
}
