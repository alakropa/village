package src;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class ASender {

    ServerSocket MyService;
    Socket clientSocket = null;
    BufferedInputStream input;
    TargetDataLine targetDataLine;

    BufferedOutputStream out;
    ByteArrayOutputStream byteArrayOutputStream;
    AudioFormat audioFormat;

    SourceDataLine sourceDataLine;
    byte tempBuffer[] = new byte[10000];

    ASender() throws LineUnavailableException {
        try {
            audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
            MyService = new ServerSocket(11000);
            clientSocket = MyService.accept();
            captureAudio();
            input = new BufferedInputStream(clientSocket.getInputStream());
            out = new BufferedOutputStream(clientSocket.getOutputStream());

            while (input.read(tempBuffer) != -1) {
                sourceDataLine.write(tempBuffer, 0, 10000);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 44100.0F;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);


    }

    public static void main(String s[]) throws LineUnavailableException {
        ASender s2 = new ASender();
        System.out.println("This is S2 starting!");
    }

    private void captureAudio() {
        try {

            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                System.out.println(mixerInfo[cnt].getName());
            }
            audioFormat = getAudioFormat();

            DataLine.Info dataLineInfo = new DataLine.Info(
                    TargetDataLine.class, audioFormat);

            Mixer mixer = AudioSystem.getMixer(mixerInfo[3]);

            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);

            targetDataLine.open(audioFormat);
            targetDataLine.start();

            Thread captureThread = new CaptureThread();
            captureThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error: at CaptureAudio");
            System.exit(0);
        }
    }

    class CaptureThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        public void run() {
            try {
                while (true) {
                    int cnt = targetDataLine.read(tempBuffer, 0,
                            tempBuffer.length);
                    out.write(tempBuffer);
                }

            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Error: at CaptureThread");
                System.exit(0);
            }
        }
    }

}