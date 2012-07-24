package me.kylestevenson.lightlevel;

import com.smaxe.uv.media.core.VideoFrame;
import com.smaxe.uv.na.WebcamFactory;
import com.smaxe.uv.na.webcam.IWebcam;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Kyle Stevenson
 * Date: 6/28/12
 * Time: 5:40 PM
 */
public final class Capture {
    public static final long WEBCAM_CAPTURE_INTERVAL = 500;
    public static final double LIGHTING_THRESHOLD = 0.0222;

    public static void main(final String[] args) {
        final List<IWebcam> webcams = WebcamFactory.getWebcams("jitsi");

        if (webcams.size() == 0) {
            System.out.println("No webcams available");
            return;
        }

        // Feel free to mess with the number if you have multiple Webcams or some weird driver like I have
        final IWebcam webcam = webcams.get(0);
        System.out.println(webcam.getName() + " " + webcam.getId() + "/" + webcams.size());

        try {
            webcam.open(new IWebcam.FrameFormat(0, 0), new IWebcam.IListener() {
                @Override
                public void onVideoFrame(final VideoFrame videoFrame) {
                    try {
                        int red = 0;
                        int blue = 0;
                        int green = 0;
                        int whites = 0;

                        for (int i = 0; i < videoFrame.rgb.length; i++) {
                            if (((videoFrame.rgb[i] >> 16) & 0xFF) >= 245 &&
                                    ((videoFrame.rgb[i] >> 8) & 0xFF) >= 245 &&
                                    (videoFrame.rgb[i] & 0xFF) >= 245 && whites < 453) {
                                // We want to skip the 453 pixels of white text that is overlaid onto the image since
                                //      we don't own a license
                                whites++;
                                continue;
                            }

                            red += (videoFrame.rgb[i] >> 16) & 0xFF;
                            green += (videoFrame.rgb[i] >> 8) & 0xFF;
                            blue += videoFrame.rgb[i] & 0xFF;
                        }

                        final double lightLevel = (double) (red + green + blue) / ((videoFrame.rgb.length - 453) * 3 * 255.0);

                        // The threshold for determining if there is in fact light or not.
                        if (lightLevel < LIGHTING_THRESHOLD) {
                            System.out.print("No light; ");
                        }

                        System.out.println("level = " + lightLevel);

                        long timeLeft = WEBCAM_CAPTURE_INTERVAL - (System.currentTimeMillis() % WEBCAM_CAPTURE_INTERVAL);
                        if (timeLeft > 0) {
                            // Sleep until the next interval
                            Thread.sleep(timeLeft);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            webcam.startCapture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
