package com.darkbladedev.exceptions;

import javax.annotation.Nullable;

public class ExceptionBuilder {
    /**
     * Builds a custom exception with the provided information
     * @param exceptionType The type of exception to be created
     * @param source The object or method that caused the error
     * @param extraInfo Additional information about the error (will be converted to MiniMessage Component)
     * @return A custom exception with the provided information
     */
    public static CustomException build(Class<? extends Exception> exceptionType, Object source, @Nullable String... extraInfo) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Exception of type: ")
                     .append(exceptionType.getSimpleName())
                     .append("\nSource: ")
                     .append(source.getClass().getSimpleName());

        if (extraInfo != null && extraInfo.length > 0) {
            messageBuilder.append("\nAdditional Information:\n");
            for (String info : extraInfo) {
                messageBuilder.append("- ").append(info).append("\n");
            }
        }

        return new CustomException(messageBuilder.toString());
    }

}
