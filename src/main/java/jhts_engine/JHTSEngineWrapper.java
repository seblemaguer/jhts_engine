package jhts_engine;

import java.io.IOException;

import cz.adamh.utils.NativeUtils;
import jhts_engine.*;

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

    public JHTSEngineWrapper() {
        HTS_Engine engine = new HTS_Engine();
        HTSEngine.HTS_Engine_initialize(engine);
    }

    /**********************************************************************
     ***  operations
     **********************************************************************/
    public void setVoice(String voice_path) {
    }
    /**********************************************************************
     *** JNI Utilities
     **********************************************************************/
    /**
     *  Util method to convert a swig array to a native double array in java
     *
     *  This method doesn't clean any memory !
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
    private static void clean(SWIGTYPE_p_double ar) {
        HTSEngine.delete_double_array(ar);
    }
}
