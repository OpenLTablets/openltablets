/**
 * 
 */
package org.openl.rules.tbasic.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author User
 * 
 */
public class LabelManager {
    public static String LABEL_INSTRUCTION_PREFIX = "gen_label_"; 

    public boolean isLabelInstruction(String labelInstruction) {
        return labelInstruction.startsWith(LABEL_INSTRUCTION_PREFIX);
    }
    
    private Map<LabelType, String> currentLabels;
    private boolean isLoopOperationSet;
    private Stack<Map<LabelType, String>> labelsStack = new Stack<Map<LabelType,String>>();
    
    public void startOperationsSet(boolean isLoopOperationSet) {
        if (currentLabels != null){
            labelsStack.push(currentLabels);
        }
        currentLabels = new HashMap<LabelType, String>();
        this.isLoopOperationSet = isLoopOperationSet;

    }
    
    public void finishOperationsSet() {
        currentLabels = null;
        if (!labelsStack.isEmpty()){
            currentLabels = labelsStack.pop();
        }
    }

    public String getLabelByInstruction(String labelInstruction) {
        if (!isLabelInstruction(labelInstruction)){
            // FIXME
            throw new RuntimeException("Smth wrong.........");
        }
        
        LabelType labelType = getLabelTypeByInstruction(labelInstruction);
        
        String label = getExistingLabel(currentLabels, labelType);
     
        if (label == null) {
            label = generateLabel(labelType.getName());
        }
        
        return label;
    }
    
    private String getExistingLabel(Map<LabelType, String> existingLabels, LabelType labelType) {
        String label = null;
        
        if (existingLabels.containsKey(labelType)){
            label = existingLabels.get(labelType);
        } else if (!isLoopOperationSet && labelType.isLoopLabel()){
            // TODO not very good we use field for recursive action
            label = getLabelFromStack(labelType);
        }
        
        return label;
    }
    
    private String getLabelFromStack(LabelType labelType) {
        Map<LabelType, String> stackedLabels; // get from stack previous piece
        
        // FIXME eliminate pop and push to stack, just iterate
        if (!labelsStack.isEmpty()){
            stackedLabels = labelsStack.pop();
        } else {
            throw new RuntimeException("Smth wrong in labels.....");
        }
        String label =  getExistingLabel(stackedLabels, labelType);
        
        labelsStack.push(stackedLabels);
        
        return label;
    }

    // TODO
    private LabelType getLabelTypeByInstruction(String labelInstruction) {
        String instruction = labelInstruction.substring(LABEL_INSTRUCTION_PREFIX.length());
        
        String loopKeyword = "loop";
        String[] instructionParts = instruction.split("_");
        
        LabelType labelType = new LabelType();
        
        // label should contain 1 or 2 parts, first with label name, second with loop keyword
        if (instructionParts.length < 1 || instructionParts.length > 2 || (instructionParts.length == 2 && !loopKeyword.equals(instructionParts[1]))){
            // FIXME
            throw new RuntimeException("Bad gen label instruction....");
        }
        
        labelType.setLabelType(instructionParts[0]);
        if (instructionParts.length > 1){
            labelType.setLoopLabel(loopKeyword.equals(instructionParts[1]));
        }
        
        return labelType;
    }

    private int nextLabelNumber;
    public String generateLabel(String namePrefix){
        return namePrefix + "Label" + nextLabelNumber++;
    }
    
    private class LabelType{
        private boolean loopLabel;
        private String name;
        
        /**
         * @return the isLoopLabel
         */
        public boolean isLoopLabel() {
            return loopLabel;
        }
        /**
         * @param isLoopLabel the isLoopLabel to set
         */
        public void setLoopLabel(boolean isLoopLabel) {
            this.loopLabel = isLoopLabel;
        }
        /**
         * @return the labelType
         */
        public String getName() {
            return name;
        }
        /**
         * @param labelType the labelType to set
         */
        public void setLabelType(String labelType) {
            this.name = labelType;
        }
        
        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof LabelType)){
                return false;
            }
            
            LabelType otherLabelType = (LabelType)other;
            
            return name.equals(otherLabelType.name) && (loopLabel == otherLabelType.loopLabel);
        }
        
        @Override
        public int hashCode() {
            // FIXME
            return name.hashCode() + (loopLabel ? 11 : 0);
        }
    }

}
