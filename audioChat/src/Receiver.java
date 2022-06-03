package src;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import javax.sound.sampled.TargetDataLine;

public class Receiver {
    boolean stopCapture = false;

    ByteArrayOutputStream byteArrayOutputStream;

    AudioFormat audioFormat;

    TargetDataLine targetDataLine;

    AudioInputStream audioInputStream;

    BufferedOutputStream out = null;

    BufferedInputStream in = null;

    Socket sock = null;

    SourceDataLine sourceDataLine;

    public static void main(String[] args) {
        Receiver tx = new Receiver();
        tx.captureAudio();

    }

    private void captureAudio() {
        try {

            sock = new Socket("localhost", 11000);

            out = new BufferedOutputStream(sock.getOutputStream());
            in = new BufferedInputStream(sock.getInputStream());

            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                System.out.println(mixerInfo[cnt].getName());
            }
            audioFormat = getAudioFormat();

            DataLine.Info dataLineInfo = new DataLine.Info(
                    TargetDataLine.class, audioFormat);

            Mixer mixer = AudioSystem.getMixer(mixerInfo[1]);

            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);

            targetDataLine.open(audioFormat);
            targetDataLine.start();

            Thread captureThread = new CaptureThread();
            captureThread.start();

            DataLine.Info dataLineInfo1 = new DataLine.Info(
                    SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem
                    .getLine(dataLineInfo1);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            Thread playThread = new PlayThread();
            playThread.start();

        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    class CaptureThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        public void run() {
            byteArrayOutputStream = new ByteArrayOutputStream();
            stopCapture = false;
            try {
                while (!stopCapture) {

                    int cnt = targetDataLine.read(tempBuffer, 0,
                            tempBuffer.length);

                    out.write(tempBuffer);

                    if (cnt > 0) {

                        byteArrayOutputStream.write(tempBuffer, 0, cnt);

                    }
                }
                byteArrayOutputStream.close();
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Connection to client has been closed!");
                System.exit(0);
            }
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 44100.0F;

        int sampleSizeInBits = 16;

        int channels = 2;

        boolean signed = true;

        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
                bigEndian);
    }

    class PlayThread extends Thread {
        byte tempBuffer[] = new byte[10000];

        public void run() {

            while (true) {
                try {
                    if (!(in.read(tempBuffer) != -1)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                sourceDataLine.write(tempBuffer, 0, 10000);

            }
        }

    }
}

