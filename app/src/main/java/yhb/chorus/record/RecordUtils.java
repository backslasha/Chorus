package yhb.chorus.record;

import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.media.AudioFormat.CHANNEL_IN_STEREO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;

/**
 * Created by yhb on 18-2-19.
 */

// todo remove echo
class RecordUtils {
    private final List<byte[]> mBuffers;
    private AudioParameter mAudioParameter;
    private AudioRecord mRecorder;
    private AudioTrack mTracker;
    private final int mBufferSize;
    private volatile boolean isRecording = false;
    private volatile boolean isPlaying = false;

    private ExecutorService mExecutorService;

    private static RecordUtils INSTANCE;

    public static RecordUtils getInstance() {
        if (INSTANCE == null) {
            synchronized (RecordUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RecordUtils();
                }
            }
        }
        return INSTANCE;
    }

    private RecordUtils() {

        mAudioParameter = new AudioParameter.Builder()
                .audioSource(MediaRecorder.AudioSource.MIC)
                .sampleRateInHz(44100)
                .chanelConfig(CHANNEL_IN_STEREO)
                .audioFormat(ENCODING_PCM_16BIT)
                .build();


        mBufferSize = mAudioParameter.bufferSizeInBytes;

        mBuffers = new LinkedList<>();

        mExecutorService = Executors.newCachedThreadPool();

        initRecorderAndPlayer(mAudioParameter);
    }

    private void initRecorderAndPlayer(AudioParameter audioParameter) {
        mRecorder = new AudioRecord(audioParameter.audioSource,
                audioParameter.sampleRateInHz,
                audioParameter.chanelConfig,
                audioParameter.audioFormat,
                audioParameter.bufferSizeInBytes
        );


        mTracker = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                audioParameter.sampleRateInHz,
                audioParameter.chanelConfig,
                audioParameter.audioFormat,
                audioParameter.bufferSizeInBytes,
                AudioTrack.MODE_STREAM
        );

    }


    public void start() {

        if (mRecorder == null || mTracker == null) {
            initRecorderAndPlayer(mAudioParameter);
        }

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                startRecord();
            }

            private void startRecord() {
                isRecording = true;

                mRecorder.startRecording();

                while (isRecording) {
                    byte[] audioData = new byte[mBufferSize];
                    mRecorder.read(audioData, 0, audioData.length);
                    mBuffers.add(audioData);
                }

            }
        });

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                startTrack();
            }

            private void startTrack() {
                isPlaying = true;

                mTracker.play();

                while (isPlaying) {
                    byte[] bytes = null;
                    synchronized (mBuffers) {
                        if (!mBuffers.isEmpty()) {
                            bytes = mBuffers.remove(0);
                        }
                    }
                    if (null != bytes) {
                        mTracker.write(bytes, 0, bytes.length);
                    }
                }
            }
        });


    }

    public void stop() {
        isPlaying = false;
        isRecording = false;

        if (null != mTracker) {
            mTracker.stop();
            mTracker.release();
            mTracker = null;
        }

        if (null != mRecorder) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        mBuffers.clear();
    }


    private static class AudioParameter {
        private int audioSource, sampleRateInHz, chanelConfig, audioFormat, bufferSizeInBytes;

        private AudioParameter(Builder builder) {
            this.audioSource = builder.audioSource;
            this.sampleRateInHz = builder.sampleRateInHz;
            this.chanelConfig = builder.chanelConfig;
            this.audioFormat = builder.audioFormat;
            this.bufferSizeInBytes = builder.bufferSizeInBytes;
        }

        static class Builder {

            private int audioSource = MediaRecorder.AudioSource.MIC,
                    sampleRateInHz = 44100,
                    chanelConfig = CHANNEL_IN_STEREO,
                    audioFormat = ENCODING_PCM_16BIT,
                    bufferSizeInBytes = 0;

            AudioParameter build() {
                bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, chanelConfig, audioFormat);
                return new AudioParameter(this);
            }

            Builder audioSource(int audioSource) {
                this.audioSource = audioSource;
                return this;
            }

            Builder sampleRateInHz(int sampleRateInHz) {
                this.sampleRateInHz = sampleRateInHz;
                return this;
            }

            Builder chanelConfig(int chanelConfig) {
                this.chanelConfig = chanelConfig;
                return this;
            }

            Builder audioFormat(int audioFormat) {
                this.audioFormat = audioFormat;
                return this;
            }

        }
    }


}
