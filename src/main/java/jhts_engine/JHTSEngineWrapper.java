package jhts_engine;

// List
import java.util.ArrayList;

// Library loading part
import cz.adamh.utils.NativeUtils;

// Stream
import java.io.ByteArrayInputStream;
import java.io.IOException;

// Audio
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;


/**
 *  The convenient wrapper class around JNI hts_engine class.
 *
 *  The javadoc indicated function called + adaptation. However, for more information about the
 *  actual function, please read the HTS_ENGINE documentation.
 *
 *  @author <a href="mailto:slemaguer@coli.uni-saarland.de">Sébastien Le Maguer</a>
 */
public class JHTSEngineWrapper
{
    /* Loading library part */
    static {
        String libResourceName;
        String osName = System.getProperty("os.name");
        switch (osName) {
            case ("Mac OS X"):
                libResourceName = "libhts_engine.dylib";
                break;
            case ("Linux"):
                libResourceName = "libhts_engine.so";
                break;
            default:
                throw new RuntimeException("Cannot load library for OS: " + osName);
        }
        try {
            NativeUtils.loadLibraryFromJar("/" + libResourceName);
        } catch (IOException e) {
            e.printStackTrace(); // This is probably not the best way to handle exception :-)
        }
    }

    /** The internal HTS engine */
    private HTS_Engine engine;

    /**
     *  Default constructor which initialize the engine
     *
     */
    public JHTSEngineWrapper() {
        engine = new HTS_Engine();
        HTSEngine.HTS_Engine_initialize(engine);
    }

    /**
     *  Method to clear the engine memory. Needs to be called before shutdown the java application
     *
     */
    public void clear() {
        HTSEngine.HTS_Engine_clear(engine);
    }

    /**********************************************************************
     ***  Configurations/accessors
     **********************************************************************/
    /**
     *  Method to set the voice given by its path. The engine is cleaned so all the other parameters
     *  are reset at the same time
     *
     *  @param voice_path the path of the hts engine voice
     *  @throws HTSEngineException if the loading of voice fails.
     */
    public void setVoice(String voice_path) throws Exception {
        String[] voice = {voice_path};

        // Load the voice
        HTSEngine.HTS_Engine_load(engine, voice, 1);

        // Check that the voice has been loaded
        if (HTSEngine.HTS_Engine_get_nvoices(engine) <= 0) {
            throw new Exception("Loading of the voice failed");
        }
    }

    /**
     *  Method to set the period value
     *
     *  @param period the new period value
     */
    public void setPeriod(int period) {
    }

    /**
     *  Method to set the alpha value
     *
     *  @param alpha the new alpha value
     */
    public void setAlpha(double alpha) {
    }


    /**
     *  Method to set the beta value
     *
     *  @param beta the new beta value
     */
    public void setBeta(double beta) {
    }

    /**
     *  Method to set the speed value
     *
     *  @param speed the new speed value
     */
    public void setSpeed(double speed) {
    }

    /**
     *  Method to set the MSD threshold
     *
     *  @param msd_threshold the MSD threshold
     */
    public void setMSDThreshold(double msd_threshold) {
    }

    /**
     *  Method to set the volume (in dB)
     *
     *  @param speed the new volume (in dB)
     */
    public void setVolume(double volume) {
    }

    /**********************************************************************
     ***  operations
     **********************************************************************/
    /**
     *  Method to get the generated duration per segment
     *
     *  @return the array of labels filled with start and durations
     */
    public ArrayList<FilledLabel> getDurations() {
        ArrayList<FilledLabel> labels_with_dur = new ArrayList<FilledLabel>();

        // Get engine information
        HTS_SStreamSet sss = engine.getSss();
        HTS_Label lab_engine = engine.getLabel();
        HTS_LabelString it = lab_engine.getHead();

        // Get the global duration information
        double rate = engine.getCondition().getFperiod() * 1.0e+07 / engine.getCondition().getSampling_frequency();
        int n_state = (int) engine.getMs().getNum_states();
        SWIGTYPE_p_size_t durations = sss.getDuration();

        // Get the label duration information
        int state = 0;
        int frame = 0;
        for (int i=0; i < lab_engine.getSize(); i++) {
            // Compute segment duration
            int duration = 0;
            for (int j=0; j < n_state; j++) {
                duration += HTSEngine.size_array_getitem(durations, state);
                state++;
            }

            // Add label to the list
            FilledLabel tmp = new FilledLabel(it.getName(),
                                              (long) (frame * rate),
                                              (long) (duration * rate));
            labels_with_dur.add(tmp);

            // Move to next label segment
            it = it.getNext();
            frame += duration;
        }

        return labels_with_dur;
    }

    /**
     *  Method to get the generated parameters corresponding to the stream which index is given in
     *  parameter.
     *
     *  @param i_stream the index of the stream
     *  @return the generated parameter array
     */
    public double[][] getGenerateParameterSequence(int i_stream) throws HTSEngineException{
        // Get dimension
        HTS_GStreamSet gss = engine.getGss();
        if (gss == null)
            throw new HTSEngineException("The engine is not initialized properly (run synthesize or generateParameters first !)");

        if (gss.getNstream() == 0)
            throw new HTSEngineException("The engine is not initialized properly, there is no stream in the model set (run synthesize or generateParameters first !)");
       int nb_frames = (int) HTSEngine.HTS_Engine_get_total_frame(engine);
       int dim = (int) HTSEngine.HTS_GStreamSet_get_vector_length(gss, i_stream);

        // Get values
        double[][] out = new double[nb_frames][dim];
        for (int t=0; t<nb_frames; t++)
            for (int d=0; d<dim; d++)
                out[t][d] = (float) HTSEngine.HTS_Engine_get_generated_parameter(engine, i_stream, t, d);

        return out;
    }

    /**
     *  Synthesis method
     *
     *  @param labels the string containing the full context labels (so a multiple line string)
     *  @return the AudioStream containing the result of the synthesis
     *  @throws HTSEngineException if the synthesis fails. A message specify the reason.
     */
    public AudioInputStream synthesize(String labels) throws HTSEngineException {
        String[] label_lines = labels.split("\n");
        return synthesize(label_lines);
    }

    /**
     *  Synthesis method
     *
     *  @param label_lines the string containing the full context labels (so a multiple line string)
     *  @return the AudioStream containing the result of the synthesis
     *  @throws HTSEngineException if the synthesis fails. A message specify the reason.
     */
    public AudioInputStream synthesize(String[] label_lines) throws HTSEngineException {
        // Clear engine generated parameter
        HTSEngine.HTS_Engine_refresh(engine);

        // Achieve synthesis
        HTSEngine.HTS_Engine_synthesize_from_strings(engine, label_lines, label_lines.length);

        // Generate audio inputstream (FIXME:frequency hardcoded)
        HTS_GStreamSet gss = engine.getGss();
        SWIGTYPE_p_double x = gss.getGspeech();
        int nb_samples = (int) gss.getTotal_nsample();

        if (nb_samples <= 0) {
            throw new HTSEngineException("Problem with the synthesis, the produced number of samples should be strictly positive and not: " + nb_samples);
        }

        //  1. Generate header
        int sample_rate = 48000;
        AudioFormat format = new AudioFormat(sample_rate, 16, 1, true, false);   // use 16-bit audio, mono, signed PCM, little Endian


        //  2. fill data
        byte[] data = new byte[2 * nb_samples];
        for (int i = 0; i < nb_samples; i++) {
            double tmp = HTSEngine.double_array_getitem(x, i);
            if (tmp > 32767.0)
                tmp = 32767;
            else if (tmp < -32768.0)
                tmp = -32768;

            int x_i = (int) Math.round(tmp);
            data[2*i + 0] = (byte) x_i;
            data[2*i + 1] = (byte) (x_i >> 8);
        }

        //  3. Get the stream
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        AudioInputStream ais = new AudioInputStream(bais, format, nb_samples);

        // Return the stream
        return ais;
    }

    /**
     *  Method to just generate the acoustic parameter features without going to the vocoder
     *  process.
     *
     *  @param label_lines the label sequence to "synthesize"
     *  @throws HTSEngineException if something goes wrong (see message for more information).
     */
    public void generateAcousticParameters(String[] label_lines) throws HTSEngineException {
        // Clear engine generated parameter
        HTSEngine.HTS_Engine_refresh(engine);

        // Generate state sequence from the labels
        boolean res = HTSEngine.HTS_Engine_generate_state_sequence_from_strings(engine, label_lines, label_lines.length);
        if (! res) {
            HTSEngine.HTS_Engine_refresh(engine);
            throw new HTSEngineException("Generation of the state sequence failed. Check your voice and your labels");
        }

        // Generate acoustic parameters
        res = HTSEngine.HTS_Engine_generate_parameter_sequence(engine);
        if (! res) {
            HTSEngine.HTS_Engine_refresh(engine);
            throw new HTSEngineException("Generation of the parameter sequence failed. Check your voice");
        }

        // Fill the datastructure accurately
        res = HTSEngine.HTS_MinimalGStreamSet_create(engine.getGss(), engine.getPss(),
                                                     engine.getCondition().getFperiod());
        if (! res) {
            HTSEngine.HTS_Engine_refresh(engine);
            throw new HTSEngineException("Generation of the parameter sequence failed. Check your voice");
        }
    }

    /**********************************************************************
     *** JNI Utilities
     **********************************************************************/

    /**
     *  Method to clear the memory of a swig double array
     *
     *  @param ar the swig array to free
     */
    @SuppressWarnings("unused")
	private static void clear(SWIGTYPE_p_double ar) {
        HTSEngine.delete_double_array(ar);
    }
}
