/*
 * Copyright (c) 2013 Nu Echo Inc. All rights reserved.
 */

package com.nuecho.rivr.cookbook.dialogue;

import static com.nuecho.rivr.voicexml.turn.output.OutputTurns.*;

import org.slf4j.*;

import com.nuecho.rivr.core.dialogue.*;
import com.nuecho.rivr.voicexml.dialogue.*;
import com.nuecho.rivr.voicexml.turn.first.*;
import com.nuecho.rivr.voicexml.turn.input.*;
import com.nuecho.rivr.voicexml.turn.last.*;
import com.nuecho.rivr.voicexml.turn.output.*;
import com.nuecho.rivr.voicexml.turn.output.audio.*;
import com.nuecho.rivr.voicexml.turn.output.grammar.*;

/**
 * The goal of the <code>Interaction</code> is to collect information from the
 * caller (via DTMF recognition, speech recognition or recording) by playing
 * <i>prompts</i> to the user.
 * <p>
 * Contrary to other VoiceXML OutputTurn classes, the <code>Interaction</code>
 * does not necessary map to a simple VoiceXML construct. As a result,
 * <code>Interaction</code> can be cumbersome to work with. Thus, it is
 * recommended to use the <code>Interaction.Builder</code> class to create
 * instances of <code>Interaction</code>.
 * <p>
 * Building an interaction boils down to:
 * <ol>
 * <li>add prompts, i.e. messages that will be played to the caller.
 * <p>
 * NOTE: Every of those prompts can be configured to do DTMF or speech
 * recognition (this is how to implement a <i>barge-in</i> behavior)</li>
 * <li>specify what to do after the prompts are played:
 * <ul>
 * <li>do nothing</li>
 * <li>perform recognition (DTMF, speech or both)</li>
 * <li>perform recording</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * @see com.nuecho.rivr.voicexml.turn.output.Interaction.Builder
 * @see com.nuecho.rivr.voicexml.turn.output.Interaction
 * @author Nu Echo Inc.
 */
public class Dialogue implements VoiceXmlDialogue {

    @Override
    public VoiceXmlLastTurn run(VoiceXmlFirstTurn firstTurn, VoiceXmlDialogueContext context)
            throws Exception {

        GrammarItem dtmfGrammar = new GrammarReference("builtin:dtmf/digits?length=1");
        DtmfRecognition dtmfRecognition = new DtmfRecognition(dtmfGrammar);
        dtmfRecognition.setTermChar("");

        String text = "You can interrupt this message at any time by pressing any key. "
                      + "Here goes a very long message...";

        Interaction interaction = interaction("interruptible-message")
                .addPrompt(dtmfRecognition, new SpeechSynthesis(text))
                .build();

        VoiceXmlInputTurn inputTurn = DialogueUtils.doTurn(interaction, context);

        Logger logger = context.getLogger();
        if (inputTurn.getRecognitionInfo() != null
            || VoiceXmlEvent.hasEvent(VoiceXmlEvent.NO_MATCH, inputTurn.getEvents())) {
            logger.info("Message has been interrupted");
        } else if (VoiceXmlEvent.hasEvent(VoiceXmlEvent.CONNECTION_DISCONNECT_HANGUP,
                                          inputTurn.getEvents())) {
            logger.info("User hung-up during message.");
        } else {
            logger.info("Message has been played completely");
        }

        //end of dialogue
        return new Exit("exit");
    }
}
