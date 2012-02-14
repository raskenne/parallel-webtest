package com.dynacrongroup.webtest.jtidy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.TidyMessage;

import java.util.*;

import static java.lang.String.format;

/**
 * A TidyMessageListener that uses a map to record failures, and a separate "verify" method to report on the
 * failures.
 */
public class MapTMListener extends AbstractTMListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTMListener.class);

    private TreeMap<TidyMessage.Level, List<TidyMessage>> messages;

    /**
     * Constructs a tidy message listener that reports messages of the given level or higher to a map of levels to
     * messages.
     *
     * @param messages  A map of message levels to lists of messages with that level.
     * @param thresholdLevel             The minimum severity level that will be displayed.
     */
    public MapTMListener(TreeMap<TidyMessage.Level, List<TidyMessage>> messages, TidyMessage.Level thresholdLevel) {
        super(thresholdLevel);
        this.messages = messages;
    }

    /**
     * Constructs a tidy message listener that reports messages of the given level or higher to a map of levels to
     * messages.  Uses default AbstractTMListener threshold.
     *
     * @param messages  A map of message levels to lists of messages with that level.
     */
    public MapTMListener(TreeMap<TidyMessage.Level, List<TidyMessage>> messages) {
        super();
        this.messages = messages;
    }

    @Override
    protected void reportMessage(TidyMessage tidyMessage) {
        TidyMessage.Level messageLevel = tidyMessage.getLevel();
        if (!messages.containsKey(messageLevel)) {
            messages.put(messageLevel, new ArrayList<TidyMessage>());
        }
        messages.get(messageLevel).add(tidyMessage);
    }

    public Map<TidyMessage.Level, List<TidyMessage>> getMessages() {
        return messages;
    }

    /**
     * Verify that no messages were collected.  If any are collected, throw a well-formatted list of the messages.
     * @throws AssertionError A well-formatted list of error messages from jtidy.
     */
    public void verify() throws AssertionError {
        if (!messages.isEmpty()) {
            
            //Error Header
            String description = format("JTidy has reported messages with level [%s] or greater\n", threshold);
            for(TidyMessage.Level currentLevel : messages.descendingKeySet()) {
                
                //Error Subheader: level
                description += (format("\n%s %s messages:\n", messages.get(currentLevel).size(), currentLevel));
                for (TidyMessage tidyMessage : messages.get(currentLevel)) {
                    
                    //Error Entry
                    description += (format("  %s\n", formatTidyMessage(tidyMessage)));
                }
            }
            throw new AssertionError(description);
        }
    }

    /**
     * Messages are sorted by level, so the message format does not include level.
     * @param tidyMessage
     * @return
     */
    @Override
    protected String formatTidyMessage(TidyMessage tidyMessage) {
        return String.format("line %s column %s: %s %s",
                tidyMessage.getLine(),
                tidyMessage.getColumn(),
                tidyMessage.getMessage(),
                formatErrorCode(tidyMessage));
    }
}