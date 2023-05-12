package com.zqj.websocketclient.bing.pojo;

import com.zqj.websocketclient.bing.BingUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rebecca
 * @since 2023/4/19 15:02
 */
@Data
@Accessors(chain = true)
public class SendEntity {

    public SendEntity() {
        ArrayList<Arguments> argumentsList = new ArrayList<>();
        argumentsList.add(new Arguments());
        this.arguments = argumentsList;
        this.invocationId = "0";
        this.target = "chat";
        this.type = 4;
    }

    private List<Arguments> arguments;
    private String invocationId;
    private String target;
    private int type;

    @Data
    @Accessors(chain = true)
    public static class Arguments {

        public Arguments() {
            this.source = "cib";
            ArrayList<String> optionsSets = new ArrayList<>();
            optionsSets.add("nlu_direct_response_filter");
            optionsSets.add("deepleo");
            optionsSets.add("disable_emoji_spoken_text");
            optionsSets.add("responsible_ai_policy_235");
            optionsSets.add("enablemm");
            optionsSets.add("h3imaginative");
            optionsSets.add("dtappid");
            optionsSets.add("cricinfo");
            optionsSets.add("cricinfov2");
            optionsSets.add("dv3sugg");
            this.optionsSets = optionsSets;
            ArrayList<String> sliceIds = new ArrayList<>();
            sliceIds.add("222dtappid");
            sliceIds.add("225cricinfo");
            sliceIds.add("224locals0");
            this.sliceIds = sliceIds;
            this.traceId = BingUtil.genRanHex(32);
            this.isStartOfSession = true;
            this.message = new Message();
            ArrayList<PreviousMessages> previousMessagesList = new ArrayList<>();
            previousMessagesList.add(new PreviousMessages());
            this.participant = new Participant();
            this.previousMessages = previousMessagesList;

        }
        private String source;
        private List<String> optionsSets;
        private List<String> sliceIds;
        private String traceId;
        private boolean isStartOfSession;
        private Message message;
        private String conversationSignature;
        private Participant participant;
        private String conversationId;
        private List<PreviousMessages> previousMessages;

        @Data
        @Accessors(chain = true)
        public static class Message {

            public Message() {
                this.author = "user";
                this.text = "";
                this.messageType = "SearchQuery";
            }

            private String author;
            private String text;
            private String messageType;


        }

        @Data
        @Accessors(chain = true)
        public static class Participant {

            private String id;

        }

        @Data
        @Accessors(chain = true)
        public static class PreviousMessages {

            public PreviousMessages() {
                this.author = "user";
                this.contextType = "WebPage";
                this.messageType = "Context";
                this.messageId = "discover-web--page-ping-mriduna-----";
            }

            private String author;
            private String description;
            private String contextType;
            private String messageType;
            private String messageId;


        }

    }


}

