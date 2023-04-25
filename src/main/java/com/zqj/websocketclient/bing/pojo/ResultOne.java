package com.zqj.websocketclient.bing.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author Rebecca
 * @since 2023/4/24 21:31
 */

@Data
public class ResultOne {
    private Integer type;
    private String target;
    private List<Arguments> arguments;

    @Data
    public static class Arguments {
        private List<Messages> messages;
        private String requestId;

        @Data
        public static class Messages {
            private String text;
            private String author;
            private String createdAt;
            private String timestamp;
            private String messageId;
            private String offense;
            private List<AdaptiveCards> adaptiveCards;
            private List<SourceAttributions> sourceAttributions;
            private Feedback feedback;
            private String contentOrigin;
            private String privacy;

            @Data
            public static class AdaptiveCards {
                private String type;
                private String version;
                private List<Body> body;

                @Data
                public static class Body {
                    private String type;
                    private String size;
                    private String text;
                    private String wrap;

                }

            }

            @Data
            public static class SourceAttributions {
                private String providerDisplayName;
                private String seeMoreUrl;
                private String imageLink;
                private String imageWidth;
                private String imageHeight;
                private String imageFavicon;
                private String searchQuery;

            }

            @Data
            public static class Feedback {
                private String tag;
                private String updatedOn;
                private String type;

            }

        }

    }

}
