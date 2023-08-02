package com.astarivi.kaizoyu.core.storage.database.io;

import android.content.Context;

import com.astarivi.kaizoyu.utils.Threading;

import java.io.FileInputStream;
import java.io.FileOutputStream;


public class Manager {
    private static final Callback callback = new Callback() {
        @Override
        public void onProgress(int percentage) {
            if (facadeCallback != null) facadeCallback.onProgress(percentage);
            progress = percentage;
        }

        @Override
        public void onFinished() {
            if (facadeCallback != null) facadeCallback.onFinished();
            progress = -1;
            changeState(State.IDLE);
        }

        @Override
        public void onError(Exception e) {
            if (facadeCallback != null) facadeCallback.onError(e);
            progress = -1;
            error = e;
        }
    };
    private static final Export exportDatabase = new Export(callback);
    private static final Import importDatabase = new Import(callback);
    private static FacadeCallback facadeCallback = null;
    private static int progress = -1;
    private static Exception error = null;
    private static State state = State.IDLE;

    public static void subscribe(FacadeCallback callback) {
        facadeCallback = callback;

        facadeCallback.onStatusChange(state);

        if (state == State.IDLE) return;

        if (error != null) {
            facadeCallback.onError(error);
            changeState(State.IDLE);
        }

        if (progress != -1) {
            facadeCallback.onProgress(progress);
        }
    }

    public static void doExportDatabase(Context context, FileOutputStream output) {
        if (state != State.IDLE) return;

        changeState(State.EXPORT);
        Threading.submitTask(Threading.TASK.DATABASE, () -> exportDatabase.exportBackup(context, output));
    }

    public static void doImportDatabase(Context context, FileInputStream zippedFile) {
        if (state != State.IDLE) return;

        changeState(State.IMPORT);
        Threading.submitTask(Threading.TASK.DATABASE, () -> importDatabase.importBackup(context, zippedFile));
    }

    private static void changeState(State s) {
        if (facadeCallback != null) facadeCallback.onStatusChange(s);
        state = s;
    }

    public enum State {
        IDLE,
        EXPORT,
        IMPORT
    }

    public interface Callback {
        void onProgress(int percentage);
        void onFinished();
        void onError(Exception e);
    }

    public interface FacadeCallback {
        void onStatusChange(State currentState);
        void onProgress(int percentage);
        void onFinished();
        void onError(Exception e);
    }
}
