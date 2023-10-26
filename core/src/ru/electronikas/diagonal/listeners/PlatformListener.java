package ru.electronikas.diagonal.listeners;

import ru.electronikas.diagonal.model.Product;

/**
 * Created by nikas on 7/1/16.
 */
public interface PlatformListener {

    void share();
    void rate();

    void showBanner();
    void hideBanner();
    void showFullScr();

    void removeAds(Product product);

    void trackEvent(String eventId);
}
