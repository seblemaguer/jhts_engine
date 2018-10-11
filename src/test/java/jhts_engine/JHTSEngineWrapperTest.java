package jhts_engine;

import java.util.List;
import java.util.ArrayList;

// IO
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import com.google.common.io.ByteStreams;

// Audio
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

// Testing
import org.testng.Assert;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import org.testng.annotations.*;


public class JHTSEngineWrapperTest {

    private static final File tmp_file = new File("/tmp/htsengine_test_default_arctic_slt.htsvoice");
    JHTSEngineWrapper ew;

    /**
     * Saves the double array as an audio file (using .wav or .au format).
     *
     * @param  filename the name of the audio file
     * @param  samples the array of samples
     * @throws IllegalArgumentException if unable to save {@code filename}
     * @throws IllegalArgumentException if {@code samples} is {@code null}
     */
    public static void save(String filename, AudioInputStream ais) {

        // now save the file
        try {
            if (filename.endsWith(".wav") || filename.endsWith(".WAV")) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filename));
            }
            else if (filename.endsWith(".au") || filename.endsWith(".AU")) {
                AudioSystem.write(ais, AudioFileFormat.Type.AU, new File(filename));
            }
            else {
                throw new IllegalArgumentException("unsupported audio format: '" + filename + "'");
            }
        }
        catch (IOException ioe) {
            throw new IllegalArgumentException("unable to save file '" + filename + "'", ioe);
        }
    }


    @BeforeClass
    public void startEngine() throws Exception {
        ew = new JHTSEngineWrapper();

        byte[] bytes = ByteStreams.toByteArray(JHTSEngineWrapper.class.getResourceAsStream("arctic_slt.htsvoice"));
        try (FileOutputStream fos = new FileOutputStream(tmp_file)) {
            fos.write(bytes);
        }

        // Load voice
        ew.setVoice(tmp_file.toString());
    }

    @AfterClass
    public void clearEngine() {
        ew.clear();
        tmp_file.delete();
    }

    @Test
    public void testSynthesis() throws Exception {
        // Load label
        URL url_lab = JHTSEngineWrapperTest.class.getResource("test.lab");
        File lab_f = new File(url_lab.toURI());
        List<String> lines = Files.readAllLines(lab_f.toPath());
        String labels[] = lines.toArray(new String[0]);

        // Synthesis
        AudioInputStream ais = ew.synthesize(labels);

        // Load reference
        URL url = JHTSEngineWrapperTest.class.getResource("test.wav");
        AudioInputStream ref_ais = AudioSystem.getAudioInputStream(url);

        // Assert equality
        AudioFormat format = ais.getFormat();
        byte[] rend_bytes = new byte[(int) (ais.getFrameLength() * format.getFrameSize())];
        ais.read(rend_bytes);
        ByteBuffer buf = ByteBuffer.wrap(rend_bytes);
        short[] rend_short = new short[buf.asShortBuffer().remaining()];
        buf.asShortBuffer().get(rend_short);

        format = ref_ais.getFormat();
        byte[] ref_bytes = new byte[(int) (ref_ais.getFrameLength() * format.getFrameSize())];
        ref_ais.read(ref_bytes);
        buf = ByteBuffer.wrap(ref_bytes);
        short[] ref_short = new short[buf.asShortBuffer().remaining()];
        buf.asShortBuffer().get(ref_short);

        Assert.assertEquals(ref_short.length, rend_short.length);
        for (int s=0; s<ref_short.length; s++) {
            Assert.assertEquals(ref_short[s], rend_short[s], 0);
        }
    }


    @Test
    public void testLF0() throws Exception {

        // Load label
        URL url_lab = JHTSEngineWrapperTest.class.getResource("test.lab");
        File lab_f = new File(url_lab.toURI());
        List<String> lines = Files.readAllLines(lab_f.toPath());
        String labels[] = lines.toArray(new String[0]);

        // Generate
        ew.generateAcousticParameters(labels);
        double[][] f0 = ew.getGeneratedParameterSequence(1);

        // Load reference F0
        byte[] bytes = ByteStreams.toByteArray(JHTSEngineWrapperTest.class.getResourceAsStream("test.lf0"));
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(bytes);
        byteBuffer.rewind();

        // Define as FLOT
        float[] f0_ref = new float[byteBuffer.asFloatBuffer().remaining()];
        byteBuffer.asFloatBuffer().get(f0_ref);

        // Assert !
        Assert.assertEquals(f0.length, f0_ref.length);
        for (int t=0; t<f0.length; t++) {
            Assert.assertEquals((float) f0[t][0], (float) f0_ref[t], 0.00001);
        }
    }


    @Test
    public void testDurations() throws Exception {

        // Load label
        URL url_lab = JHTSEngineWrapperTest.class.getResource("test.lab");
        File lab_f = new File(url_lab.toURI());
        List<String> lines = Files.readAllLines(lab_f.toPath());
        String labels[] = lines.toArray(new String[0]);

        // Generate
        ew.generateAcousticParameters(labels);
        List<FilledLabel> labels_with_dur = ew.getDurations();
        List<String> gen_lines = new ArrayList<String>();
        for (FilledLabel l: labels_with_dur) {
            gen_lines.add(l.toString());
        }

        // Load reference
        URL url_ref = JHTSEngineWrapperTest.class.getResource("test_lab_with_dur.lab");
        File ref_f = new File(url_ref.toURI());
        List<String> ref_lines = Files.readAllLines(ref_f.toPath());


        // Assert !
        assertThat(gen_lines).hasSameSizeAs(ref_lines);
        assertThat(gen_lines).containsExactlyElementsOf(ref_lines);

    }
}
