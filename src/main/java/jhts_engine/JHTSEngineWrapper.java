package jhts_engine;

// Library loading part
import cz.adamh.utils.NativeUtils;

// Stream
import java.io.ByteArrayInputStream;
import java.io.IOException;

// Audio
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;


/**
 *  The convinient wrapper class around JNI hts_engine class.
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

    private HTS_Engine engine;

    public JHTSEngineWrapper() {
        engine = new HTS_Engine();
        HTSEngine.HTS_Engine_initialize(engine);
    }

    public void clear() {
        HTSEngine.HTS_Engine_clear(engine);
    }

    /**********************************************************************
     ***  Configurations/accessors
     **********************************************************************/
    public void setVoice(String voice_path) throws Exception {
        String[] voice = {voice_path};

        // Load the voice
        HTSEngine.HTS_Engine_load(engine, voice, 1);

        // Check that the voice has been loaded
        if (HTSEngine.HTS_Engine_get_nvoices(engine) <= 0) {
            throw new Exception("Loading of the voice failed");
        }
    }

    public void setPeriod(int period) {
    }

    public void setAlpha(double alpha) {
    }

    public void setBeta(double beta) {
    }

    public void setSpeed(double speed) {
    }

    public void setMSDThreshold(double msd_threshold) {
    }

    public void setVolume(double volume) {
    }

    /**********************************************************************
     ***  operations
     **********************************************************************/
    public void getDurations() {
        /*

   size_t i, j;
   size_t frame, state, duration;

   HTS_Label *label = &engine->label;
   HTS_SStreamSet *sss = &engine->sss;
   size_t nstate = HTS_ModelSet_get_nstate(&engine->ms);
   double rate = engine->condition.fperiod * 1.0e+07 / engine->condition.sampling_frequency;

   for (i = 0, state = 0, frame = 0; i < HTS_Label_get_size(label); i++) {
      for (j = 0, duration = 0; j < nstate; j++)
         duration += HTS_SStreamSet_get_duration(sss, state++);
      fprintf(fp, "%lu %lu %s\n", (unsigned long) (frame * rate), (unsigned long) ((frame + duration) * rate), HTS_Label_get_string(label, i));
      frame += duration;
   }
         */
    }

    public void getWAV() {
        /*

         */
    }

    public void getGenerateParameterSequence(int i_stream) {
        /*
   size_t i, j;
   float temp;
   HTS_GStreamSet *gss = &engine->gss;

   for (i = 0; i < HTS_GStreamSet_get_total_frame(gss); i++)
      for (j = 0; j < HTS_GStreamSet_get_vector_length(gss, stream_index); j++) {
         temp = (float) HTS_GStreamSet_get_parameter(gss, stream_index, i, j);
         fwrite(&temp, sizeof(float), 1, fp);
      }
         */
    }


    public AudioInputStream synthesize(String labels) throws Exception {
        String[] label_lines = labels.split("\n");
        return synthesize(label_lines);
    }

    public AudioInputStream synthesize(String[] label_lines) throws Exception {
        // Clear engine generated parameter
        HTSEngine.HTS_Engine_refresh(engine);

        // Achieve synthesis
        HTSEngine.HTS_Engine_synthesize_from_strings(engine, label_lines, label_lines.length);

        // Generate audio inputstream (FIXME:frequency hardcoded)
        HTS_GStreamSet gss = engine.getGss();
        SWIGTYPE_p_double x = gss.getGspeech();
        int nb_samples = (int) gss.getTotal_nsample();

        if (nb_samples <= 0) {
            throw new Exception("Problem with the synthesis, the produced number of samples should be strictly positive and not: " + nb_samples);
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

    /**********************************************************************
     *** JNI Utilities
     **********************************************************************/


    /**
     *  Util method to convert a swig array to a native double array in java
     *
     *  This method doesn't clear any memory !
     *
     *  @param ar the swig array
     *  @param length the length of the array
     *  @return the java native double array containing the values from the swig array
     */
    private static double[] swig2java(SWIGTYPE_p_double ar, int length) {
        double[] res = new double[length];

        for (int i=0; i<length; i++)
            res[i] = HTSEngine.double_array_getitem(ar, i);

        return res;
    }

    /**
     *  Utilitary method to generate a swig array from a java native double array
     *
     *  @param ar the double array
     *  @return the swig array containing the values from the java native double array
     */
    private static SWIGTYPE_p_double java2swig(double[] ar) {
        SWIGTYPE_p_double res = HTSEngine.new_double_array(ar.length);

        for (int i=0; i<ar.length; i++)
            HTSEngine.double_array_setitem(res, i, ar[i]);

        return res;
    }

    /**
     *  Method to copy the containing of the java native array into a preallocated swig array
     *
     *  @param src the java native array
     *  @param dest the preallocated swig array
     */
    private static void copy(double[] src, SWIGTYPE_p_double dest) {
        for (int i=0; i<src.length; i++)
            HTSEngine.double_array_setitem(dest, i, src[i]);
    }

    /**
     *  Method to clear the memory of a swig double array
     *
     *  @param ar the swig array to free
     */
    private static void clear(SWIGTYPE_p_double ar) {
        HTSEngine.delete_double_array(ar);
    }
}
