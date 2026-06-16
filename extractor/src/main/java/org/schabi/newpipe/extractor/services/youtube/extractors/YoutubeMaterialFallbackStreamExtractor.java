package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.services.youtube.WatchDataCache;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * App-facing YouTube stream extractor fallbacks used by NewPipe Material.
 *
 * <p>The base StreamExtractor default for sub-channel avatars can expose the sub-channel page URL
 * as if it were an image URL. The Material detail page passes getSubChannelAvatars() directly to
 * Coil, so return a real avatar image from the channel page instead.</p>
 */
public class YoutubeMaterialFallbackStreamExtractor extends YoutubeRelatedFallbackStreamExtractor {
    public YoutubeMaterialFallbackStreamExtractor(final StreamingService service,
                                                  final LinkHandler linkHandler,
                                                  final WatchDataCache watchDataCache) {
        super(service, linkHandler, watchDataCache);
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() throws ParsingException {
        final String avatarUrl = fetchChannelAvatarUrl(getSubChannelUrl());
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            return avatarUrl;
        }

        return "";
    }

    @Nonnull
    @Override
    public List<Image> getSubChannelAvatars() throws ParsingException {
        final String avatarUrl = getSubChannelAvatarUrl();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new Image(
                avatarUrl,
                Image.HEIGHT_UNKNOWN,
                Image.WIDTH_UNKNOWN,
                Image.ResolutionLevel.UNKNOWN));
    }

    @Nullable
    private String fetchChannelAvatarUrl(@Nullable final String channelUrl) {
        if (channelUrl == null || channelUrl.isEmpty()) {
            return null;
        }

        try {
            final YoutubeChannelExtractor channelExtractor = new YoutubeChannelExtractor(
                    getService(), getService().getChannelLHFactory().fromUrl(channelUrl));
            channelExtractor.fetchPage();

            final List<Image> avatars = channelExtractor.getAvatars();
            if (avatars == null || avatars.isEmpty()) {
                return null;
            }

            for (int i = avatars.size() - 1; i >= 0; i--) {
                final Image avatar = avatars.get(i);
                if (avatar != null && avatar.getUrl() != null && !avatar.getUrl().isEmpty()) {
                    return avatar.getUrl();
                }
            }
        } catch (final Exception ignored) {
            // Keep stream extraction usable even if the channel avatar fallback cannot be loaded.
        }
        return null;
    }
}
