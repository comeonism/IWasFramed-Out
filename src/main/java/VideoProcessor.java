import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IContainer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class VideoProcessor extends MediaListenerAdapter implements Runnable {
    private long lastPtsWrite = Global.NO_PTS;
    private int videoStreamIndex = -1;

    private String filePrefix;
    private String filename;

    private long startMillis;
    private long endMillis;
    private long stepMillis;

    private boolean done;

    private final static Logger logger = Logger.getLogger(VideoProcessor.class.getName());

    public VideoProcessor(String filename, long stepMillis, Optional<Long> startMillis, Optional<Long> endMillis) {
        this.filename = filename;
        this.stepMillis = stepMillis;
        this.filePrefix = "";
        this.done = false;

        if (startMillis.isPresent()) this.startMillis = startMillis.get();
        else this.startMillis = 0;

        if (endMillis.isPresent() && endMillis.get() != -1) this.endMillis = endMillis.get();
        else this.endMillis = getVideoDuration();
    }

    private long getVideoDuration() {
        IContainer container = IContainer.make();
        container.open(filename, IContainer.Type.READ, null);

        return container.getDuration() / 1000;
    }

    @Override
    public void run() {
        IMediaReader reader = ToolFactory.makeReader(filename);

        // we want BufferedImages created in BGR 24bit color space
        reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        reader.addListener(this);

        logger.info("video processor is about to run");

        do {
            if (Thread.interrupted()) {
                logger.warning("thread was interrupted");
                return;
            }
        } while (!done && reader.readPacket() == null);

        logger.info("done creating frames");

        reader.removeListener(this);

        uploadToAWS(getFramesDir());
        deleteLocal(getFramesDir());

        logger.info("all video stuff done");

        return;
    }

    private boolean uploadToAWS(File dir) {
        AWSUploader uploader = new AWSUploader();
        boolean success = uploadThatStuff(uploader, dir);

        logger.info("done uploading");

        if (success) logger.info("successfully uploaded");
        else logger.info("failed to upload miserably");

        return success;
    }

    private boolean uploadThatStuff(AWSUploader uploader, File directory) {
        boolean uploaded = true;
        int noOfFiles = directory.listFiles().length;
        int filesUploaded = 0;

        try {
            if (directory.exists()) {
                for (File anImageFile : directory.listFiles()) {
                    if (!uploader.uploadImage(directory.getName(), anImageFile)) {
                        uploaded = false;
                        break;
                    } else {
                        filesUploaded++;
                        updatePercentage(Double.valueOf(filesUploaded), Double.valueOf(noOfFiles), false);
                    }
                }
            }
        } catch (IOException ioe) {
            logger.severe(ioe.getMessage());
            uploaded = false;
        }

        return uploaded;
    }

    private boolean deleteLocal(File dir) {
        boolean success = true;

        if (Utils.getCleanUp()) {
            success = Utils.deleteFilesIn(dir);

            if (success) logger.info("successfully deleted");
            else logger.info("failed to delete miserably");
        }

        return success;
    }

    public void onVideoPicture(IVideoPictureEvent event)
    {
        try {
            if (earlyReturn(event)) return;

            if (event.getTimeStamp(TimeUnit.MILLISECONDS) >= startMillis - stepMillis) {
                if (lastPtsWrite == Global.NO_PTS) lastPtsWrite = event.getTimeStamp(TimeUnit.MILLISECONDS);

                // if it's time to write the next frame
                if (event.getTimeStamp(TimeUnit.MILLISECONDS) - lastPtsWrite >= stepMillis) {
                    File file = File.createTempFile(getFilenamePrefix(), "." + Utils.IMG_FORMAT, getFramesDir());
                    ImageIO.write(event.getImage(), Utils.IMG_FORMAT, file);
                    lastPtsWrite += stepMillis;

                    logger.info("created a frame");

                    if (event.getTimeStamp(TimeUnit.MILLISECONDS) >= endMillis) {
                        logger.info("done with frames");
                        done = true;
                    }
                }
            }

            updatePercentage(Double.valueOf(event.getTimeStamp(TimeUnit.MILLISECONDS)), Double.valueOf(endMillis), true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private boolean earlyReturn(IVideoPictureEvent event) {
        if (event.getStreamIndex() != videoStreamIndex) {
            if (videoStreamIndex == -1) videoStreamIndex = event.getStreamIndex();
            else return true;
        }

        return false;
    }

    private File getFramesDir() {
        String framesDirName = Utils.FRAMES_DIR + getFilenamePrefix() + "/";
        File framesDirectory = new File(framesDirName);

        if (!framesDirectory.exists()) framesDirectory.mkdirs();

        return framesDirectory;
    }

    private String getFilenamePrefix() {
        if ("".equals(filePrefix)) filePrefix = makeFilePrefix(filename);

        return filePrefix;
    }

    private String makeFilePrefix(String filename) {
        return filename.substring(filename.lastIndexOf("\\") + 1, filename.lastIndexOf("."));
    }

    private void updatePercentage(Double current, Double total, boolean firstHalf) {
        if (firstHalf) Utils.getSharedProgressBar().setProgress(Utils.round(current / total) / 2);
        else Utils.getSharedProgressBar().setProgress(0.5 + Utils.round(current / total));
    }
}