import javafx.scene.control.ProgressBar;

import java.util.logging.Logger;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    public final static String IMG_FORMAT = "png";
    public final static String FRAMES_DIR = "./frames/";
    public final static String BUCKET_NAME  = "";

    private final static DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private final static ProgressBar sharedProgressBar = new ProgressBar(0.0);
    private final static String timePattern = "HH:mm:ss.SSS";
    private final static Logger logger = Logger.getLogger(Utils.class.getName());
    private static boolean cleanupAfterYourself = false;

    public static long getMillis(String time) {
        long timeMillis;

        if ("".equals(time) || "99:59:59.999".equals(time)) return -1;
        else {
            try {
                SimpleDateFormat timeformatter = new SimpleDateFormat(timePattern);
                timeformatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date date = timeformatter.parse(time);
                timeMillis = date.getTime();
            } catch (ParseException pe) {
                logger.severe("parse exception just happened");
                timeMillis = -1;
            }
        }

        return timeMillis;
    }

    public static boolean deleteFilesIn(File dir) {
        boolean success = true;

        try {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        } catch (NullPointerException npe) {
            success = false;
        }

        dir.delete();

        return success;
    }

    public static ProgressBar getSharedProgressBar() {
        return sharedProgressBar;
    }
    public static Double round(Double value) {
        return Double.valueOf(decimalFormat.format(value));
    }
    public static void setCleanUp(boolean value) { cleanupAfterYourself = value; }
    public static boolean getCleanUp() { return cleanupAfterYourself; }
}
