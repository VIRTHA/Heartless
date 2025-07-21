package com.darkbladedev.exceptions;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;

import com.darkbladedev.utils.MM;

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
        String originalExceptionMessage = null;
        messageBuilder.append("<red><b>¡Ha ocurrido un error en los sistemas de Heartless!</b></red>");
        messageBuilder.append("\n<gray><u>Exception type</u>:</gray> <aqua>")
                     .append(exceptionType.getSimpleName())
                     .append("\n<gray><u>Source</u>:</gray> <aqua>");
        
        // Verificar si source es null para evitar NullPointerException
        if (source != null) {
            if (source.getClass().isAssignableFrom(Exception.class)) {
                originalExceptionMessage = ((Exception) source).getMessage();
            } else {
                messageBuilder.append(source.getClass().getName());
            }
        } else {
            messageBuilder.append("null");
        }

        if (extraInfo != null && extraInfo.length > 0) {
            messageBuilder.append("\n<gray><u>Additional Information</u>:</gray>\n");
            for (String info : extraInfo) {
                messageBuilder.append("    - ").append("<red><b>" + info + "</b></red>").append("\n");
            }
        }

        if (originalExceptionMessage != null) {
            messageBuilder.append("<gray>StackTrace</gray>").append("\n");
            messageBuilder.append("<red>").append(originalExceptionMessage).append("</red>");
        }

        return new CustomException(messageBuilder.toString());
    }


    public static void sendToConsole(CustomException exception) {
        Bukkit.getConsoleSender().sendMessage(MM.toComponent(exception.getMessage()));
    }
}
