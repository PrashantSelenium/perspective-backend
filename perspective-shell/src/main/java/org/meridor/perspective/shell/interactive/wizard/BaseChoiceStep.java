package org.meridor.perspective.shell.interactive.wizard;

import org.meridor.perspective.shell.common.misc.Logger;
import org.meridor.perspective.shell.common.misc.Pager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.isExitKey;
import static org.meridor.perspective.shell.common.repository.impl.TextUtils.isSkipKey;

@Component
public abstract class BaseChoiceStep extends AbstractStep {

    @Autowired
    private Logger logger;
    
    @Autowired
    private Pager pager;

    private String answer;


    @Override
    public boolean run() {
        answer = null;
        printMessageWithDefaultAnswer();
        Map<Integer, String> choicesMap = getPossibleChoicesMap();
        Optional<Boolean> returnValue = processZeroOrOneChoice(choicesMap);
        if (returnValue.isPresent()) {
            return returnValue.get();
        }
        printPossibleChoices(choicesMap);
        logger.ok(getPrompt());
        Optional<String> answerCandidate = processAnswer();
        if (!answerCandidate.isPresent()) {
            return false;
        }
        if (isSkipKey(answerCandidate.get())) {
            if (answerRequired()) {
                logger.warn("You can not skip this step.");
            } else {
                return true;
            }
        }
        while (!validateAnswer(choicesMap, answerCandidate.get())) {
            logger.warn(getIncorrectChoiceMessage(choicesMap));
            answerCandidate = processAnswer();
            if (!answerCandidate.isPresent()) {
                return false;
            }
        }
        this.answer = getValueToSave(choicesMap, answerCandidate.get());
        return true;
    }

    private Optional<String> processAnswer() {
        String answer = waitForAnswer();
        if (isExitKey(answer)) {
            return Optional.empty();
        }
        return Optional.of(answer);
    }

    protected abstract List<String> getPossibleChoices();
    
    protected abstract String getPrompt();
    
    protected abstract String getValueToSave(Map<Integer, String> choicesMap, String answer);
    
    protected abstract String getIncorrectChoiceMessage(Map<Integer, String> choicesMap);
    
    protected abstract boolean validateAnswer(Map<Integer, String> choicesMap, String answer);

    protected Map<Integer, String> getPossibleChoicesMap() {
        Map<Integer, String> choicesMap = new HashMap<>();
        List<String> possibleChoices = getPossibleChoices();
        for (int i = 1; i <= possibleChoices.size(); i++) {
            choicesMap.put(i, possibleChoices.get(i - 1));
        }
        return choicesMap;
    }

    protected void printPossibleChoices(Map<Integer, String> possibleChoices) {
        List<String[]> choicesRows = possibleChoices.keySet().stream()
                .map(k -> new String[]{k.toString(), possibleChoices.get(k)})
                .collect(Collectors.toList());
        pager.page(new String[]{"Number", "Name"}, choicesRows);
    }
    
    protected Optional<Boolean> processZeroOrOneChoice(Map<Integer, String> choicesMap) {
        if (choicesMap.size() == 0) {
            if (answerRequired()) {
                logger.error("We're sorry but no possible answers exist. Exiting.");
                return Optional.of(false);
            } else {
                logger.warn("Skipping this step because no possible answers exist.");
                return Optional.of(true);
            }
        }
        if (answerRequired() && choicesMap.size() == 1) {
            Integer singleKey = choicesMap.keySet().toArray(new Integer[choicesMap.keySet().size()])[0];
            String singleAnswer = choicesMap.get(singleKey);
            logger.ok(String.format("Automatically selecting the only possible answer: %s", singleAnswer));
            this.answer = singleAnswer;
            return Optional.of(true);
        }
        return Optional.empty();
    }

    @Override
    public String getAnswer() {
        return answer;
    }

}
