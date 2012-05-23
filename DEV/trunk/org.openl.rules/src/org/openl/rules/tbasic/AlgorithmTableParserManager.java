/**
 *
 */
package org.openl.rules.tbasic;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openl.OpenL;
import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessages;
import org.openl.rules.tbasic.compile.ConversionRuleBean;
import org.openl.runtime.EngineFactory;

/**
 * @author User
 *
 */
public final class AlgorithmTableParserManager implements IAlgorithmTableParserManager {
    // To make class serializable, change synchronization

    private static volatile AlgorithmTableParserManager instance;
    private static Object synchObjectForInstance = new Object();

    private final IAlgorithmTableParserManager rulesWrapperInstance;

    private volatile ConversionRuleBean[] convertionRules;

    private volatile ConversionRuleBean[] fixedConvertionRules;

    private Object synchObjectForConvertionRules = new Object();

    private Object synchObjectForFixedConvertionRules = new Object();

    public static AlgorithmTableParserManager instance() {
        lazyLoadInstance();
        return instance;
    }

    private static void lazyLoadInstance() {
        if (instance == null) {
            synchronized (synchObjectForInstance) {
                if (instance == null) {
                    instance = new AlgorithmTableParserManager();
                }
            }
        }
    }

    private AlgorithmTableParserManager() {
        String sourceType = OpenL.OPENL_JAVA_RULE_NAME;
        URL sourceFile = AlgorithmTableParserManager.class.getResource("AlgorithmTableSpecification.xls");

        EngineFactory<IAlgorithmTableParserManager> engineFactory = new EngineFactory<IAlgorithmTableParserManager>(
                sourceType, sourceFile, IAlgorithmTableParserManager.class);
        engineFactory.setExecutionMode(true);
        
        // get the errors before compiling inside component.
        // As inside they are cleaned in EngineFactory
        //
        List<OpenLMessage> oldMessages = new ArrayList<OpenLMessage>(OpenLMessages.getCurrentInstance().getMessages());        
        rulesWrapperInstance = engineFactory.makeInstance();
        
        // add to the current messages instance old messages
        //
        OpenLMessages.getCurrentInstance().addMessages(oldMessages);
    }

    private ConversionRuleBean[] fixBrokenValues(ConversionRuleBean[] conversionRules) {
        for (ConversionRuleBean conversionRule : conversionRules) {
            fixBrokenValues(conversionRule.getOperationType());
            fixBrokenValues(conversionRule.getOperationParam1());
            fixBrokenValues(conversionRule.getOperationParam2());
            fixBrokenValues(conversionRule.getLabel());
            fixBrokenValues(conversionRule.getNameForDebug());
        }
        return conversionRules;
    }

    private void fixBrokenValues(String[] label) {
        for (int i = 0; i < label.length; i++) {
            if (label[i].equalsIgnoreCase("N/A")) {
                label[i] = null;
            } else if (label[i].equalsIgnoreCase("\"\"")) {
                label[i] = "";
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.rules.tbasic.ITableParserManager#
     *      getStructuredAlgorithmSpecification()
     */
    public TableParserSpecificationBean[] getAlgorithmSpecification() {
        TableParserSpecificationBean[] result = rulesWrapperInstance.getAlgorithmSpecification();

        return result;
    }

    public ConversionRuleBean[] getConversionRules() {
        lazyLoadConversionRules();

        return convertionRules;
    }

    public ConversionRuleBean[] getFixedConversionRules() {
        lazyLoadFixedConvertionRules();

        return fixedConvertionRules;
    }

    /**
     *
     */
    private void lazyLoadConversionRules() {
        if (convertionRules == null) {
            synchronized (synchObjectForConvertionRules) {
                if (convertionRules == null) {
                    convertionRules = rulesWrapperInstance.getConversionRules();
                }
            }
        }
    }

    /**
     *
     */
    private void lazyLoadFixedConvertionRules() {
        if (fixedConvertionRules == null) {
            synchronized (synchObjectForFixedConvertionRules) {
                if (fixedConvertionRules == null) {
                    ConversionRuleBean[] draftConvertionRules = getConversionRules().clone();
                    fixedConvertionRules = fixBrokenValues(draftConvertionRules);
                }
            }
        }
    }

    public String whatIsOperationsGroupName(List<String> groupedOperationNames) {
        return rulesWrapperInstance.whatIsOperationsGroupName(groupedOperationNames);
    }

    public String[] whatOperationsToGroup(String keyword) {
        return rulesWrapperInstance.whatOperationsToGroup(keyword);
    }
}
