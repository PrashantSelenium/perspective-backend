package org.meridor.perspective.worker.misc.impl;

public final class ValueUtils {
    
    public static String formatQuota(Integer currentValue, Integer maxValue) {
        if (isMeaningless(currentValue, maxValue)) {
            return null;
        }
        return String.format("%s/%s", formatValue(currentValue), formatValue(maxValue));
    }

    private static boolean isMeaningless(Integer currentValue, Integer maxValue) {
        return 
                currentValue == null ||
                currentValue == -1 ||
                (
                        maxValue != null && 
                        ( 
                            maxValue == -1 ||
                            (currentValue == 0 && maxValue == 0)
                        )
                );
    }

    private static String formatValue(Integer value){
        if (value == null) {
            return "?";
        }
        return String.valueOf(value);
    }
    
}
