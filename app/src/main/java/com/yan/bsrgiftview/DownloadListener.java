package com.yan.bsrgiftview;

/**
 * Created by zjl on 17/4/18.
 */

public interface DownloadListener {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
